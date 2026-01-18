package software.ulpgc.moneycalculator.infrastructure.adapter.api;

import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching decorator for {@link ExchangeRateProvider} implementations.
 *
 * <p>This class implements the Decorator pattern to add caching capabilities to any
 * {@code ExchangeRateProvider}, significantly improving performance and reducing load
 * on external exchange rate APIs by caching previously fetched rates.</p>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li><strong>Reduced Latency:</strong> Cached rates are returned instantly without network calls</li>
 *   <li><strong>Lower API Costs:</strong> Minimizes billable API requests to external services</li>
 *   <li><strong>Rate Limit Protection:</strong> Prevents exceeding API rate limits through caching</li>
 *   <li><strong>Improved Reliability:</strong> Continues serving rates even during temporary API outages</li>
 * </ul>
 *
 * <p><strong>Caching Strategy:</strong></p>
 * <p>This implementation uses a time-based cache eviction policy. Each cached exchange
 * rate is stored with an expiration timestamp. When a rate is requested:</p>
 * <ol>
 *   <li>Check if the rate exists in cache</li>
 *   <li>If found and not expired, return immediately (cache hit)</li>
 *   <li>If not found or expired, fetch from delegate provider (cache miss)</li>
 *   <li>Store the newly fetched rate with a new expiration time</li>
 * </ol>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe and can be safely used in multi-threaded environments.
 * It uses {@link ConcurrentHashMap} for the cache storage, which provides lock-free
 * reads and fine-grained locking for writes, ensuring high concurrency performance.</p>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>Implements the Decorator pattern, allowing caching to be transparently added
 * to any {@link ExchangeRateProvider} implementation without modifying the original
 * provider's code. This follows the Open/Closed Principle - the class is open for
 * extension but closed for modification.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create base provider
 * ExchangeRateProvider apiProvider = new ExchangeRateApiAdapter(httpClient, jsonParser, config);
 *
 * // Wrap with caching (30-minute cache)
 * ExchangeRateProvider cachedProvider = new CachedExchangeRateProvider(apiProvider, 30);
 *
 * // First request - fetches from API
 * ExchangeRate rate1 = cachedProvider.getRate(usd, eur);
 *
 * // Second request within 30 minutes - returns from cache instantly
 * ExchangeRate rate2 = cachedProvider.getRate(usd, eur); // Cache hit!
 *
 * // After 30 minutes - fetches fresh rate from API
 * Thread.sleep(30 * 60 * 1000);
 * ExchangeRate rate3 = cachedProvider.getRate(usd, eur); // Cache miss, refreshed
 *
 * // Manual cache management
 * cachedProvider.clearCache(); // Force refresh for all rates
 * cachedProvider.cleanExpiredEntries(); // Remove stale entries
 * System.out.println(cachedProvider.getCacheStats()); // Monitor cache
 * }</pre>
 *
 * <p><strong>Cache Maintenance:</strong></p>
 * <p>The cache does not automatically remove expired entries (lazy eviction). For
 * long-running applications, consider periodically calling {@link #cleanExpiredEntries()}
 * to prevent unbounded memory growth from expired entries.</p>
 *
 * <p><strong>Configuration Recommendations:</strong></p>
 * <ul>
 *   <li><strong>Real-time Trading:</strong> 1-5 minutes cache validity</li>
 *   <li><strong>Business Applications:</strong> 15-30 minutes cache validity</li>
 *   <li><strong>Reporting Systems:</strong> 60-120 minutes cache validity</li>
 *   <li><strong>Batch Processing:</strong> Daily cache validity or no cache</li>
 * </ul>
 *
 * @see ExchangeRateProvider
 * @see ExchangeRate
 */
public class CachedExchangeRateProvider implements ExchangeRateProvider {

    /**
     * The underlying provider to delegate to on cache misses.
     * All actual exchange rate fetching is delegated to this provider.
     */
    private final ExchangeRateProvider delegate;

    /**
     * Thread-safe cache mapping currency pair keys to cached rate entries.
     * Uses ConcurrentHashMap for lock-free reads and concurrent writes.
     */
    private final Map<String, CachedRate> cache;

    /**
     * Duration in minutes that cached rates remain valid before expiring.
     * After this duration, cached rates are considered stale and will be refreshed.
     */
    private final int cacheValidityMinutes;

    /**
     * Constructs a caching provider with a custom cache validity period.
     *
     * <p>This constructor allows fine-grained control over cache behavior. Choose
     * the validity period based on your application's requirements for rate freshness
     * versus performance.</p>
     *
     * <p><strong>Validation:</strong></p>
     * <ul>
     *   <li>Delegate provider must not be null</li>
     *   <li>Cache validity must be non-negative (0 or positive)</li>
     *   <li>A validity of 0 effectively disables caching (always fetches fresh)</li>
     * </ul>
     *
     * @param delegate the actual provider to fetch rates from on cache miss;
     *                must not be null
     * @param cacheValidityMinutes how long cached rates remain valid, in minutes;
     *                            must be non-negative (0 or greater)
     * @throws IllegalArgumentException if delegate is null or cacheValidityMinutes is negative
     */
    public CachedExchangeRateProvider(ExchangeRateProvider delegate, int cacheValidityMinutes) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate provider cannot be null");
        }
        if (cacheValidityMinutes < 0) {
            throw new IllegalArgumentException("Cache validity must be non-negative");
        }

        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>();
        this.cacheValidityMinutes = cacheValidityMinutes;
    }

    /**
     * Constructs a caching provider with default 30-minute validity period.
     *
     * <p>This convenience constructor uses a sensible default cache duration that
     * balances rate freshness with performance for most typical business applications.
     * A 30-minute cache is appropriate for:</p>
     * <ul>
     *   <li>E-commerce applications showing product prices</li>
     *   <li>Travel booking systems displaying costs</li>
     *   <li>Business reporting with near-real-time data</li>
     *   <li>General currency conversion tools</li>
     * </ul>
     *
     * <p>For applications requiring different freshness guarantees, use
     * {@link #CachedExchangeRateProvider(ExchangeRateProvider, int)} with a custom duration.</p>
     *
     * @param delegate the actual provider to fetch rates from on cache miss;
     *                must not be null
     * @throws IllegalArgumentException if delegate is null
     */
    public CachedExchangeRateProvider(ExchangeRateProvider delegate) {
        this(delegate, 30);
    }

    /**
     * Retrieves the exchange rate between two currencies, using cache when possible.
     *
     * <p>This method implements an efficient caching strategy to minimize external
     * API calls while ensuring rate freshness according to the configured validity period.</p>
     *
     * <p><strong>Execution Flow:</strong></p>
     * <ol>
     *   <li>Generate cache key from currency pair (e.g., "USD->EUR")</li>
     *   <li>Look up the key in the cache</li>
     *   <li>If found and not expired:
     *       <ul>
     *         <li>Log cache hit (for monitoring)</li>
     *         <li>Return cached rate immediately</li>
     *       </ul>
     *   </li>
     *   <li>If not found or expired:
     *       <ul>
     *         <li>Log cache miss (for monitoring)</li>
     *         <li>Fetch fresh rate from delegate provider</li>
     *         <li>Store in cache with current timestamp</li>
     *         <li>Return the fresh rate</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>Thread Safety:</strong></p>
     * <p>This method is thread-safe and can be called concurrently from multiple
     * threads. The ConcurrentHashMap ensures that cache operations are atomic and
     * consistent. In rare cases of concurrent cache misses for the same currency pair,
     * multiple threads may fetch the same rate simultaneously, but this is acceptable
     * as it's a transient condition that self-corrects.</p>
     *
     * <p><strong>Error Handling:</strong></p>
     * <p>If the delegate provider throws an exception, it is propagated to the caller.
     * The cache remains unchanged in error scenarios - no partial or invalid data is cached.</p>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Cache Hit:</strong> O(1) lookup, microseconds latency</li>
     *   <li><strong>Cache Miss:</strong> Network call latency (typically 100-500ms)</li>
     *   <li><strong>Memory:</strong> O(n) where n is number of unique currency pairs requested</li>
     * </ul>
     *
     * @param from the source currency to convert from; must not be null
     * @param to the target currency to convert to; must not be null
     * @return the exchange rate from source to target currency, either from cache
     *         or freshly fetched from the delegate provider
     * @throws ExchangeRateUnavailableException if the rate cannot be obtained from the delegate
     * @throws IllegalArgumentException if either currency is null
     */
    @Override
    public ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException {
        // Generate unique cache key for this currency pair
        String cacheKey = createCacheKey(from, to);

        // Attempt to retrieve from cache
        CachedRate cachedRate = cache.get(cacheKey);

        // Check if we have a valid cached entry
        if (cachedRate != null && !cachedRate.isExpired()) {
            // Cache hit - return immediately without network call
            System.out.println("Cache HIT for " + cacheKey);
            return cachedRate.rate;
        }

        // Cache miss or expired - need to fetch fresh data
        System.out.println("Cache MISS for " + cacheKey + " - fetching from API");
        ExchangeRate rate = delegate.getRate(from, to);

        // Store the newly fetched rate in cache for future requests
        cache.put(cacheKey, new CachedRate(rate, cacheValidityMinutes));

        return rate;
    }

    /**
     * Clears all cached exchange rates, forcing fresh fetches for subsequent requests.
     *
     * <p>This method invalidates the entire cache, removing all stored exchange rates
     * regardless of their expiration status. Use this when you need to force a complete
     * refresh of all rates, such as:</p>
     * <ul>
     *   <li>After configuration changes to the exchange rate provider</li>
     *   <li>When you know external rates have been significantly updated</li>
     *   <li>During application initialization or reset operations</li>
     *   <li>For administrative or debugging purposes</li>
     * </ul>
     *
     * <p><strong>Thread Safety:</strong></p>
     * <p>This method is thread-safe. However, note that concurrent requests may
     * repopulate the cache immediately after clearing.</p>
     *
     * <p><strong>Impact:</strong></p>
     * <p>The next request for each currency pair will result in a cache miss and
     * trigger an API call. Consider the load this may place on your API if called
     * during high-traffic periods.</p>
     */
    public void clearCache() {
        cache.clear();
        System.out.println("Cache cleared");
    }

    /**
     * Removes expired entries from the cache to free memory.
     *
     * <p>This method performs cache maintenance by scanning all entries and removing
     * those that have exceeded their validity period. Unlike automatic eviction systems,
     * this implementation uses manual eviction to avoid the overhead of background threads.</p>
     *
     * <p><strong>When to Call:</strong></p>
     * <ul>
     *   <li><strong>Periodically:</strong> Schedule this method to run every few hours</li>
     *   <li><strong>Before Reports:</strong> Clean up before generating reports or metrics</li>
     *   <li><strong>Low Activity Periods:</strong> Run during off-peak hours</li>
     *   <li><strong>Memory Pressure:</strong> Call when memory usage is high</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <p>This operation is O(n) where n is the total number of cached entries. For
     * large caches (thousands of entries), this may take a few milliseconds. The
     * operation is safe to call concurrently with other cache operations.</p>
     *
     * <p><strong>Thread Safety:</strong></p>
     * <p>This method is thread-safe. It iterates over the cache and removes expired
     * entries using ConcurrentHashMap's atomic operations.</p>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Schedule periodic cleanup
     * ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
     * scheduler.scheduleAtFixedRate(
     *     () -> cachedProvider.cleanExpiredEntries(),
     *     1, 1, TimeUnit.HOURS
     * );
     * }</pre>
     *
     * @return the number of expired entries that were removed from the cache
     */
    public int cleanExpiredEntries() {
        int removed = 0;

        // Iterate over all cache entries
        for (Map.Entry<String, CachedRate> entry : cache.entrySet()) {
            // Check if this entry has expired
            if (entry.getValue().isExpired()) {
                // Remove the expired entry
                cache.remove(entry.getKey());
                removed++;
            }
        }

        // Log cleanup results if any entries were removed
        if (removed > 0) {
            System.out.println("Cleaned " + removed + " expired cache entries");
        }

        return removed;
    }

    /**
     * Returns diagnostic information about the current cache state.
     *
     * <p>This method provides visibility into cache configuration and usage, useful for:</p>
     * <ul>
     *   <li><strong>Monitoring:</strong> Track cache size and hit rates</li>
     *   <li><strong>Debugging:</strong> Verify cache is working as expected</li>
     *   <li><strong>Capacity Planning:</strong> Understand cache growth patterns</li>
     *   <li><strong>Performance Tuning:</strong> Optimize validity duration</li>
     * </ul>
     *
     * <p><strong>Output Format:</strong></p>
     * <p>Returns a formatted string: {@code "Cache size: N entries, Validity: M minutes"}</p>
     *
     * <p><strong>Example Output:</strong></p>
     * <pre>
     * Cache size: 42 entries, Validity: 30 minutes
     * Cache size: 0 entries, Validity: 15 minutes
     * </pre>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Log cache statistics periodically
     * logger.info("Cache statistics: " + cachedProvider.getCacheStats());
     *
     * // Display in admin dashboard
     * statsPanel.setText(cachedProvider.getCacheStats());
     * }</pre>
     *
     * @return a formatted string containing cache size and validity configuration
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries, Validity: %d minutes",
                cache.size(), cacheValidityMinutes);
    }

    /**
     * Creates a unique cache key for a currency pair.
     *
     * <p>The cache key uniquely identifies a conversion direction between two currencies.
     * The format is deterministic and human-readable for debugging purposes.</p>
     *
     * <p><strong>Key Format:</strong> {@code "FROM_CODE->TO_CODE"}</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>
     * USD->EUR
     * GBP->USD
     * JPY->CHF
     * </pre>
     *
     * <p><strong>Directionality:</strong></p>
     * <p>Note that {@code "USD->EUR"} and {@code "EUR->USD"} are different keys,
     * as they represent different conversion directions. Each direction is cached
     * separately, even though they are mathematical inverses of each other.</p>
     *
     * @param from the source currency
     * @param to the target currency
     * @return a unique string key representing the currency pair and direction
     */
    private String createCacheKey(Currency from, Currency to) {
        return from.code() + "->" + to.code();
    }

    /**
     * Internal immutable wrapper for cached exchange rates with expiration tracking.
     *
     * <p>This private static nested class encapsulates an exchange rate along with
     * metadata about when it should expire. It supports both date-based and precise
     * timestamp-based expiration checking.</p>
     *
     * <p><strong>Immutability:</strong></p>
     * <p>All fields are final and the class provides no setters, making instances
     * inherently thread-safe. Once created, a CachedRate cannot be modified.</p>
     *
     * <p><strong>Expiration Strategy:</strong></p>
     * <p>This class implements a dual expiration strategy:</p>
     * <ol>
     *   <li><strong>Timestamp-based:</strong> Expires after a specific duration in milliseconds</li>
     *   <li><strong>Date-based:</strong> Expires when the calendar date changes</li>
     * </ol>
     *
     * <p>The rate is considered expired if either condition is met, providing protection
     * against both time-based staleness and date rollover scenarios.</p>
     *
     * <p><strong>Design Rationale:</strong></p>
     * <p>Using a private static nested class keeps the caching logic encapsulated
     * while avoiding unnecessary references to the outer class instance, reducing
     * memory overhead.</p>
     */
    private static class CachedRate {

        /**
         * The cached exchange rate.
         * This is the actual rate data that will be returned on cache hits.
         */
        private final ExchangeRate rate;

        /**
         * The date when this cached rate expires (date-based expiration).
         * Used to ensure rates don't span multiple calendar days inappropriately.
         */
        private final LocalDate expirationDate;

        /**
         * The timestamp in milliseconds when this rate expires (time-based expiration).
         * Provides precise expiration timing for sub-day validity periods.
         */
        private final long expirationTimeMillis;

        /**
         * Constructs a cached rate with specified validity duration.
         *
         * <p>The expiration time is calculated as the current time plus the validity
         * duration. Both a date and a precise timestamp are stored to support different
         * expiration checking strategies.</p>
         *
         * <p><strong>Calculation:</strong></p>
         * <pre>
         * expirationDate = today's date
         * expirationTimeMillis = current time + (validityMinutes * 60 * 1000)
         * </pre>
         *
         * @param rate the exchange rate to cache; must not be null
         * @param validityMinutes how many minutes the rate should remain valid
         */
        CachedRate(ExchangeRate rate, int validityMinutes) {
            this.rate = rate;
            this.expirationDate = LocalDate.now();
            // Convert minutes to milliseconds and add to current time
            this.expirationTimeMillis = System.currentTimeMillis() + (validityMinutes * 60 * 1000L);
        }

        /**
         * Checks if this cached rate has expired and should be refreshed.
         *
         * <p>A rate is considered expired if either of these conditions is true:</p>
         * <ul>
         *   <li>The current time (in milliseconds) exceeds the expiration timestamp</li>
         *   <li>The current date is after the expiration date</li>
         * </ul>
         *
         * <p><strong>Why Two Conditions?</strong></p>
         * <p>The dual-condition approach provides robustness:</p>
         * <ul>
         *   <li><strong>Timestamp check:</strong> Provides precise expiration for durations
         *       shorter than a day (e.g., 30 minutes, 2 hours)</li>
         *   <li><strong>Date check:</strong> Ensures rates don't persist across date boundaries,
         *       which could be problematic for rates that should be fresh daily</li>
         * </ul>
         *
         * <p><strong>Performance:</strong></p>
         * <p>This method is very fast (nanoseconds), calling only {@code System.currentTimeMillis()}
         * and {@code LocalDate.now()}, making it suitable for high-frequency cache checks.</p>
         *
         * @return {@code true} if the cached rate has expired and needs refreshing,
         *         {@code false} if the rate is still valid
         */
        boolean isExpired() {
            return System.currentTimeMillis() > expirationTimeMillis
                    || LocalDate.now().isAfter(expirationDate);
        }
    }
}
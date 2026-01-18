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
 * {@code ExchangeRateProvider}, significantly improving performance by reducing
 * redundant API calls.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li><strong>Performance Optimization:</strong> Caches exchange rates to avoid repeated API calls</li>
 *   <li><strong>Time-based Expiration:</strong> Cached rates expire after a configurable duration</li>
 *   <li><strong>Thread Safety:</strong> Uses {@link ConcurrentHashMap} for concurrent access</li>
 *   <li><strong>Transparent Fallback:</strong> Automatically fetches fresh data on cache miss or expiration</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Wrap an existing provider with caching (30-minute cache)
 * ExchangeRateProvider baseProvider = new ExchangeRateApiAdapter(...);
 * ExchangeRateProvider cachedProvider = new CachedExchangeRateProvider(baseProvider, 30);
 *
 * // First call hits the API
 * ExchangeRate rate1 = cachedProvider.getRate(usd, eur);
 *
 * // Second call within 30 minutes uses cache
 * ExchangeRate rate2 = cachedProvider.getRate(usd, eur); // Cache hit!
 * }</pre>
 *
 * <p><strong>Design Pattern:</strong> Decorator Pattern</p>
 * <p><strong>Thread Safety:</strong> Thread-safe for concurrent access</p>
 *
 * @author Money Calculator Team
 * @version 1.0
 * @since 2.0
 * @see ExchangeRateProvider
 */
public class CachedExchangeRateProvider implements ExchangeRateProvider {

    /** The underlying provider to delegate to on cache miss */
    private final ExchangeRateProvider delegate;

    /** Thread-safe cache mapping currency pairs to cached rates */
    private final Map<String, CachedRate> cache;

    /** Duration in minutes that cached rates remain valid */
    private final int cacheValidityMinutes;

    /**
     * Constructs a caching provider with custom validity period.
     *
     * @param delegate the actual provider to fetch rates from on cache miss
     * @param cacheValidityMinutes how long cached rates remain fresh (in minutes)
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
     * <p>This is a convenience constructor that uses a sensible default cache
     * duration for most currency exchange rate use cases.</p>
     *
     * @param delegate the actual provider to fetch rates from on cache miss
     */
    public CachedExchangeRateProvider(ExchangeRateProvider delegate) {
        this(delegate, 30);
    }

    /**
     * Retrieves the exchange rate between two currencies, using cache when possible.
     *
     * <p><strong>Cache Strategy:</strong></p>
     * <ol>
     *   <li>Check if rate exists in cache and is still valid</li>
     *   <li>If cache hit and valid, return cached rate immediately</li>
     *   <li>If cache miss or expired, fetch from delegate provider</li>
     *   <li>Store newly fetched rate in cache for future requests</li>
     * </ol>
     *
     * <p><strong>Thread Safety:</strong> This method is thread-safe and can be called
     * concurrently from multiple threads.</p>
     *
     * @param from the source currency to convert from
     * @param to the target currency to convert to
     * @return the exchange rate from source to target currency
     * @throws ExchangeRateUnavailableException if the rate cannot be obtained from the delegate
     * @throws IllegalArgumentException if either currency is null
     */
    @Override
    public ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException {
        String cacheKey = createCacheKey(from, to);

        // Attempt to retrieve from cache
        CachedRate cachedRate = cache.get(cacheKey);

        // Return cached rate if present and not expired
        if (cachedRate != null && !cachedRate.isExpired()) {
            System.out.println("Cache HIT for " + cacheKey);
            return cachedRate.rate;
        }

        // Cache miss or expired - fetch fresh data from delegate
        System.out.println("Cache MISS for " + cacheKey + " - fetching from API");
        ExchangeRate rate = delegate.getRate(from, to);

        // Store in cache for future requests
        cache.put(cacheKey, new CachedRate(rate, cacheValidityMinutes));

        return rate;
    }

    /**
     * Clears all cached exchange rates.
     *
     * <p>This method forces all subsequent rate requests to fetch fresh data
     * from the delegate provider. Useful for manual cache invalidation or
     * when you know rates have been updated.</p>
     */
    public void clearCache() {
        cache.clear();
        System.out.println("Cache cleared");
    }

    /**
     * Removes expired entries from the cache.
     *
     * <p>This method performs cache maintenance by removing entries that have
     * exceeded their validity period. It can be called periodically to prevent
     * unbounded cache growth.</p>
     *
     * @return the number of expired entries removed
     */
    public int cleanExpiredEntries() {
        int removed = 0;
        for (Map.Entry<String, CachedRate> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            System.out.println("Cleaned " + removed + " expired cache entries");
        }
        return removed;
    }

    /**
     * Returns diagnostic information about the cache state.
     *
     * <p>This method provides visibility into cache configuration and current size,
     * useful for monitoring and debugging cache behavior.</p>
     *
     * @return a formatted string containing cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries, Validity: %d minutes",
                cache.size(), cacheValidityMinutes);
    }

    /**
     * Creates a unique cache key for a currency pair.
     *
     * <p>The key format is: {@code "FROM_CODE->TO_CODE"} (e.g., "USD->EUR")</p>
     *
     * @param from the source currency
     * @param to the target currency
     * @return a unique string key representing the currency pair
     */
    private String createCacheKey(Currency from, Currency to) {
        return from.code() + "->" + to.code();
    }

    /**
     * Internal immutable wrapper for cached exchange rates with expiration tracking.
     *
     * <p>This class encapsulates an exchange rate along with its expiration time,
     * supporting both date-based and time-based expiration checking.</p>
     *
     * <p><strong>Immutability:</strong> All fields are final and the class has no setters,
     * making instances thread-safe.</p>
     */
    private static class CachedRate {

        /** The cached exchange rate */
        private final ExchangeRate rate;

        /** The date when this rate expires (for date-based expiration) */
        private final LocalDate expirationDate;

        /** The timestamp in milliseconds when this rate expires (for precise expiration) */
        private final long expirationTimeMillis;

        /**
         * Constructs a cached rate with specified validity duration.
         *
         * @param rate the exchange rate to cache
         * @param validityMinutes how many minutes the rate remains valid
         */
        CachedRate(ExchangeRate rate, int validityMinutes) {
            this.rate = rate;
            this.expirationDate = LocalDate.now();
            this.expirationTimeMillis = System.currentTimeMillis() + (validityMinutes * 60 * 1000L);
        }

        /**
         * Checks if this cached rate has expired.
         *
         * <p>A rate is considered expired if either:</p>
         * <ul>
         *   <li>The current time exceeds the expiration timestamp, OR</li>
         *   <li>The current date is after the expiration date</li>
         * </ul>
         *
         * @return {@code true} if the rate has expired, {@code false} otherwise
         */
        boolean isExpired() {
            return System.currentTimeMillis() > expirationTimeMillis
                    || LocalDate.now().isAfter(expirationDate);
        }
    }
}
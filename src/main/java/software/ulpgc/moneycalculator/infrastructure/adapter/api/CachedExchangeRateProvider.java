package software.ulpgc.moneycalculator.infrastructure.adapter.api;

import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator that adds caching to an ExchangeRateProvider.
 * This improves performance and reduces API calls.
 *
 * FEATURES:
 * - Caches exchange rates to avoid repeated API calls
 * - Respects rate expiration (default 30 minutes)
 * - Thread-safe using ConcurrentHashMap
 * - Falls through to delegate when cache miss or expired
 *
 * USAGE:
 * ExchangeRateProvider cached = new CachedExchangeRateProvider(
 *     new ExchangeRateApiAdapter(...),
 *     30 // cache for 30 minutes
 * );
 */
public class CachedExchangeRateProvider implements ExchangeRateProvider {
    private final ExchangeRateProvider delegate;
    private final Map<String, CachedRate> cache;
    private final int cacheValidityMinutes;

    /**
     * Create a caching provider with custom validity period
     *
     * @param delegate The actual provider to fetch rates from
     * @param cacheValidityMinutes How long rates are considered fresh (in minutes)
     */
    public CachedExchangeRateProvider(ExchangeRateProvider delegate, int cacheValidityMinutes) {
        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>();
        this.cacheValidityMinutes = cacheValidityMinutes;
    }

    /**
     * Create a caching provider with default 30-minute validity
     */
    public CachedExchangeRateProvider(ExchangeRateProvider delegate) {
        this(delegate, 30);
    }

    @Override
    public ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException {
        String cacheKey = createCacheKey(from, to);

        // Check cache first
        CachedRate cachedRate = cache.get(cacheKey);

        // If cached and still valid, return it
        if (cachedRate != null && !cachedRate.isExpired()) {
            System.out.println("Cache HIT for " + cacheKey);
            return cachedRate.rate;
        }

        // Cache miss or expired - fetch from delegate
        System.out.println("Cache MISS for " + cacheKey + " - fetching from API");
        ExchangeRate rate = delegate.getRate(from, to);

        // Store in cache
        cache.put(cacheKey, new CachedRate(rate, cacheValidityMinutes));

        return rate;
    }

    /**
     * Clear all cached rates
     */
    public void clearCache() {
        cache.clear();
        System.out.println("Cache cleared");
    }

    /**
     * Clear expired entries from cache
     * @return number of entries removed
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
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries, Validity: %d minutes",
                cache.size(), cacheValidityMinutes);
    }

    private String createCacheKey(Currency from, Currency to) {
        return from.code() + "->" + to.code();
    }

    /**
     * Internal class to hold a cached rate with its expiration time
     */
    private static class CachedRate {
        private final ExchangeRate rate;
        private final LocalDate expirationDate;
        private final long expirationTimeMillis;

        CachedRate(ExchangeRate rate, int validityMinutes) {
            this.rate = rate;
            this.expirationDate = LocalDate.now();
            this.expirationTimeMillis = System.currentTimeMillis() + (validityMinutes * 60 * 1000L);
        }

        boolean isExpired() {
            // Check both date and time for more precise expiration
            return System.currentTimeMillis() > expirationTimeMillis
                    || LocalDate.now().isAfter(expirationDate);
        }
    }
}
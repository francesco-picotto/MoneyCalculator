package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;

/**
 * Output port interface for fetching current exchange rates between currencies.
 *
 * <p>This interface defines the contract for accessing exchange rate data from
 * external sources. Following the Hexagonal Architecture pattern, it serves as
 * an output port that is implemented by infrastructure adapters (e.g., REST API
 * clients, database repositories, cached rate providers).</p>
 *
 * <p>The application core depends on this abstraction rather than concrete
 * exchange rate services, ensuring independence from specific data sources and
 * allowing for easy substitution of different rate providers.</p>
 *
 * <p><strong>Common Implementation Strategies:</strong></p>
 * <ul>
 *   <li><strong>API Client:</strong> Fetch real-time rates from financial APIs
 *       (e.g., ECB, Open Exchange Rates, Currency Layer)</li>
 *   <li><strong>Database Repository:</strong> Retrieve cached or historical rates
 *       from a local database</li>
 *   <li><strong>Composite Provider:</strong> Combine multiple sources with
 *       fallback mechanisms</li>
 *   <li><strong>Mock Provider:</strong> Return fixed rates for testing purposes</li>
 * </ul>
 */
public interface ExchangeRateProvider {

    /**
     * Retrieves the current exchange rate for converting from one currency to another.
     *
     * <p>This method fetches the exchange rate needed to convert an amount from
     * the source currency to the target currency. The returned rate should be
     * current and accurate, though the exact freshness depends on the implementation
     * (real-time API, cached rates, etc.).</p>
     *
     * <p>The exchange rate object contains not only the conversion rate value but
     * also metadata such as the source and target currencies, timestamp, and
     * potentially the data source.</p>
     *
     * <p><strong>Implementation Considerations:</strong></p>
     * <ul>
     *   <li><strong>Rate Freshness:</strong> Determine if rates should be fetched
     *       in real-time or if cached rates are acceptable</li>
     *   <li><strong>Error Handling:</strong> Handle network failures, API limits,
     *       or unavailable currency pairs gracefully</li>
     *   <li><strong>Rate Direction:</strong> Ensure the rate is in the correct
     *       direction (from → to) or apply appropriate inversion</li>
     *   <li><strong>Precision:</strong> Maintain sufficient decimal precision
     *       for accurate conversions</li>
     * </ul>
     *
     * @param from the source {@link Currency} to convert from; must not be null
     * @param to the target {@link Currency} to convert to; must not be null
     * @return an {@link ExchangeRate} object containing the conversion rate and
     *         associated metadata
     * @throws ExchangeRateUnavailableException if the exchange rate cannot be
     *         retrieved due to various reasons such as:
     *         <ul>
     *           <li>Network connectivity issues</li>
     *           <li>External API errors or timeouts</li>
     *           <li>Unsupported currency pair</li>
     *           <li>Rate data not available for the requested currencies</li>
     *           <li>API rate limits exceeded</li>
     *         </ul>
     * @throws IllegalArgumentException if either currency parameter is null
     */
    ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException;
}
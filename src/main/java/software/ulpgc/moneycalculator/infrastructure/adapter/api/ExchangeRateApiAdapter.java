package software.ulpgc.moneycalculator.infrastructure.adapter.api;

import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClient;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClientException;
import software.ulpgc.moneycalculator.infrastructure.http.HttpResponse;
import software.ulpgc.moneycalculator.infrastructure.json.JsonParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Infrastructure adapter for fetching exchange rates from the ExchangeRate-API service.
 *
 * <p>This class implements the {@link ExchangeRateProvider} output port, serving as a bridge
 * between the application core and the external ExchangeRate-API web service. It follows the
 * Hexagonal Architecture pattern by adapting the external API's format and protocol to match
 * the domain model expected by the application.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Construct properly formatted API request URLs</li>
 *   <li>Make HTTP GET requests to fetch exchange rate data</li>
 *   <li>Parse JSON responses from the API</li>
 *   <li>Convert API response format to domain {@link ExchangeRate} objects</li>
 *   <li>Handle HTTP errors and API failures gracefully</li>
 *   <li>Translate infrastructure exceptions to domain exceptions</li>
 * </ul>
 *
 * <p><strong>API Endpoint Used:</strong></p>
 * <pre>
 * GET {baseUrl}/v6/{apiKey}/pair/{fromCode}/{toCode}
 * </pre>
 *
 * <p><strong>Expected Response Format:</strong></p>
 * <pre>{@code
 * {
 *   "result": "success",
 *   "documentation": "https://www.exchangerate-api.com/docs",
 *   "terms_of_use": "https://www.exchangerate-api.com/terms",
 *   "time_last_update_unix": 1704067200,
 *   "time_last_update_utc": "Mon, 01 Jan 2025 00:00:00 +0000",
 *   "time_next_update_unix": 1704153600,
 *   "time_next_update_utc": "Tue, 02 Jan 2025 00:00:00 +0000",
 *   "base_code": "USD",
 *   "target_code": "EUR",
 *   "conversion_rate": 0.85
 * }
 * }</pre>
 *
 * <p><strong>Error Response Example:</strong></p>
 * <pre>{@code
 * {
 *   "result": "error",
 *   "error-type": "unsupported-code",
 *   "documentation": "https://www.exchangerate-api.com/docs"
 * }
 * }</pre>
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li><strong>Network Errors:</strong> HttpClientException → ExchangeRateUnavailableException</li>
 *   <li><strong>HTTP Errors:</strong> Non-2xx status → ExchangeRateUnavailableException</li>
 *   <li><strong>API Errors:</strong> result != "success" → ExchangeRateUnavailableException</li>
 *   <li><strong>Parse Errors:</strong> Missing fields or invalid JSON → ExchangeRateUnavailableException</li>
 *   <li><strong>Unexpected Errors:</strong> Any other exception → ExchangeRateUnavailableException</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe and can be safely used from multiple threads. All
 * dependencies are either immutable or thread-safe themselves (HttpClient, JsonParser).</p>
 *
 * <p><strong>Caching Recommendation:</strong></p>
 * <p>This adapter makes a fresh API call for every request. To reduce API usage and
 * improve performance, consider wrapping this adapter with {@link CachedExchangeRateProvider}:</p>
 * <pre>{@code
 * // Create base adapter
 * ExchangeRateProvider baseProvider = new ExchangeRateApiAdapter(
 *     httpClient, jsonParser, config
 * );
 *
 * // Wrap with caching (30-minute cache)
 * ExchangeRateProvider cachedProvider = new CachedExchangeRateProvider(
 *     baseProvider, 30
 * );
 * }</pre>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create dependencies
 * HttpClient httpClient = new HttpClientImpl(Duration.ofSeconds(10));
 * JsonParser jsonParser = new GsonJsonParser();
 * ExchangeRateApiConfig config = new ExchangeRateApiConfig(
 *     "https://v6.exchangerate-api.com",
 *     "your-api-key"
 * );
 *
 * // Create adapter
 * ExchangeRateProvider provider = new ExchangeRateApiAdapter(
 *     httpClient, jsonParser, config
 * );
 *
 * // Fetch exchange rate
 * Currency usd = Currency.of("USD", "United States Dollar");
 * Currency eur = Currency.of("EUR", "Euro");
 *
 * try {
 *     ExchangeRate rate = provider.getRate(usd, eur);
 *     System.out.println("Rate: " + rate.rate());
 * } catch (ExchangeRateUnavailableException e) {
 *     System.err.println("Failed to fetch rate: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see ExchangeRateProvider
 * @see ExchangeRate
 * @see ExchangeRateApiConfig
 * @see CachedExchangeRateProvider
 */
public class ExchangeRateApiAdapter implements ExchangeRateProvider {

    /**
     * HTTP client for making requests to the ExchangeRate-API service.
     */
    private final HttpClient httpClient;

    /**
     * JSON parser for deserializing API responses into Java objects.
     */
    private final JsonParser jsonParser;

    /**
     * Configuration containing the API base URL and authentication key.
     */
    private final ExchangeRateApiConfig config;

    /**
     * Constructs a new ExchangeRateApiAdapter with required dependencies.
     *
     * <p>All dependencies are injected through the constructor following the
     * Dependency Injection pattern. This design enables easy testing with mock
     * implementations and keeps the adapter decoupled from specific infrastructure choices.</p>
     *
     * @param httpClient the HTTP client for making API requests; must not be null
     * @param jsonParser the JSON parser for processing responses; must not be null
     * @param config the API configuration with base URL and key; must not be null
     */
    public ExchangeRateApiAdapter(
            HttpClient httpClient,
            JsonParser jsonParser,
            ExchangeRateApiConfig config
    ) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.config = config;
    }

    /**
     * Retrieves the exchange rate between two currencies from the API.
     *
     * <p>This method orchestrates the complete workflow for fetching an exchange rate:</p>
     * <ol>
     *   <li>Construct the API endpoint URL with currency codes</li>
     *   <li>Make an HTTP GET request to the API</li>
     *   <li>Validate the HTTP response status code</li>
     *   <li>Parse the JSON response body</li>
     *   <li>Extract the conversion rate and metadata</li>
     *   <li>Create and return a domain ExchangeRate object</li>
     * </ol>
     *
     * <p><strong>API Call Example:</strong></p>
     * <pre>
     * GET https://v6.exchangerate-api.com/v6/abc123/pair/USD/EUR
     *
     * Response:
     * {
     *   "result": "success",
     *   "base_code": "USD",
     *   "target_code": "EUR",
     *   "conversion_rate": 0.85
     * }
     * </pre>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Network Failure:</strong> If the HTTP request fails due to network issues,
     *       connection timeout, or DNS resolution failure, wraps HttpClientException
     *       in ExchangeRateUnavailableException</li>
     *   <li><strong>HTTP Error:</strong> If the API returns a non-2xx status code (404, 500, etc.),
     *       throws ExchangeRateUnavailableException with status code</li>
     *   <li><strong>API Error:</strong> If the API returns {"result": "error"}, throws
     *       ExchangeRateUnavailableException with error details</li>
     *   <li><strong>Parse Error:</strong> If the response JSON is malformed or missing required
     *       fields, throws ExchangeRateUnavailableException with parse error</li>
     *   <li><strong>Unexpected Error:</strong> Any other exception is caught and wrapped in
     *       ExchangeRateUnavailableException for consistent error handling</li>
     * </ul>
     *
     * <p><strong>Rate Freshness:</strong></p>
     * <p>Each call to this method makes a fresh API request. For better performance and
     * reduced API usage, consider wrapping this adapter with {@link CachedExchangeRateProvider}.</p>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Network Latency:</strong> Typically 100-500ms depending on location</li>
     *   <li><strong>Timeout:</strong> Controlled by HttpClient configuration</li>
     *   <li><strong>Rate Limits:</strong> Subject to ExchangeRate-API rate limits (depends on plan)</li>
     * </ul>
     *
     * @param from the source currency to convert from; must not be null
     * @param to the target currency to convert to; must not be null
     * @return an ExchangeRate object containing the conversion rate and metadata
     * @throws ExchangeRateUnavailableException if the rate cannot be fetched due to:
     *         network errors, API failures, HTTP errors, parse errors, or any other issue
     */
    @Override
    public ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException {
        try {
            // Step 1: Build the API endpoint URL
            String url = buildUrl(from, to);

            // Step 2: Make HTTP GET request to the API
            HttpResponse response = httpClient.get(url);

            // Step 3: Validate HTTP response status
            if (!response.isSuccessful()) {
                throw new ExchangeRateUnavailableException(
                        "API request failed with status: " + response.statusCode()
                );
            }

            // Step 4-6: Parse response and create domain object
            return parseExchangeRate(response.body(), from, to);

        } catch (HttpClientException e) {
            // Network errors, connection failures, timeouts
            throw new ExchangeRateUnavailableException(
                    "Failed to fetch exchange rate: " + e.getMessage(), e
            );
        } catch (Exception e) {
            // Catch-all for unexpected errors (parse errors, null pointers, etc.)
            throw new ExchangeRateUnavailableException(
                    "Unexpected error fetching exchange rate: " + e.getMessage(), e
            );
        }
    }

    /**
     * Constructs the API endpoint URL for fetching an exchange rate between two currencies.
     *
     * <p>Builds a URL following the ExchangeRate-API format:</p>
     * <pre>
     * {baseUrl}/v6/{apiKey}/pair/{fromCode}/{toCode}
     * </pre>
     *
     * <p><strong>Example URLs:</strong></p>
     * <pre>
     * https://v6.exchangerate-api.com/v6/abc123def456/pair/USD/EUR
     * https://v6.exchangerate-api.com/v6/abc123def456/pair/GBP/JPY
     * https://v6.exchangerate-api.com/v6/abc123def456/pair/CHF/CAD
     * </pre>
     *
     * <p><strong>URL Components:</strong></p>
     * <ul>
     *   <li><strong>baseUrl:</strong> From config (e.g., https://v6.exchangerate-api.com)</li>
     *   <li><strong>v6:</strong> API version</li>
     *   <li><strong>apiKey:</strong> From config for authentication</li>
     *   <li><strong>pair:</strong> Endpoint identifier</li>
     *   <li><strong>fromCode:</strong> ISO 4217 code of source currency</li>
     *   <li><strong>toCode:</strong> ISO 4217 code of target currency</li>
     * </ul>
     *
     * @param from the source currency
     * @param to the target currency
     * @return the complete API endpoint URL as a string
     */
    private String buildUrl(Currency from, Currency to) {
        return String.format("%s/v6/%s/pair/%s/%s",
                config.getBaseUrl(),
                config.getApiKey(),
                from.code(),
                to.code()
        );
    }

    /**
     * Parses the JSON response from the API and converts it to a domain ExchangeRate object.
     *
     * <p>This method expects a JSON response in the following format:</p>
     * <pre>{@code
     * {
     *   "result": "success",
     *   "base_code": "USD",
     *   "target_code": "EUR",
     *   "conversion_rate": 0.85,
     *   ...other fields...
     * }
     * }</pre>
     *
     * <p><strong>Parsing Steps:</strong></p>
     * <ol>
     *   <li>Parse JSON string into a Map using JsonParser</li>
     *   <li>Extract and validate the "result" field</li>
     *   <li>Extract the "conversion_rate" field</li>
     *   <li>Convert the rate to BigDecimal for precision</li>
     *   <li>Create ExchangeRate with current date and provided currencies</li>
     * </ol>
     *
     * <p><strong>Validation:</strong></p>
     * <ul>
     *   <li>Checks that the "result" field equals "success"</li>
     *   <li>Verifies the "conversion_rate" field exists and is not null</li>
     *   <li>Throws ExchangeRateUnavailableException for any validation failures</li>
     * </ul>
     *
     * <p><strong>Date Assignment:</strong></p>
     * <p>The exchange rate is assigned the current date ({@link LocalDate#now()}) as
     * its validity date. The API provides update timestamps, but for simplicity this
     * implementation uses the current date. For more precise tracking, you could parse
     * the API's timestamp fields.</p>
     *
     * <p><strong>Number Handling:</strong></p>
     * <p>The conversion rate from JSON can be either a Double or an Integer (for rates
     * like 1.0). The code handles both by casting to {@link Number} first, then calling
     * {@code doubleValue()} which works for any numeric type.</p>
     *
     * <p><strong>Error Cases:</strong></p>
     * <pre>{@code
     * // Error response
     * {
     *   "result": "error",
     *   "error-type": "unsupported-code"
     * }
     * → Throws: ExchangeRateUnavailableException("API returned unsuccessful result: error")
     *
     * // Missing rate field
     * {
     *   "result": "success"
     * }
     * → Throws: ExchangeRateUnavailableException("API response missing conversion_rate field")
     * }</pre>
     *
     * @param json the JSON response body from the API
     * @param from the source currency (used to create the ExchangeRate object)
     * @param to the target currency (used to create the ExchangeRate object)
     * @return an ExchangeRate object created from the parsed data
     * @throws ExchangeRateUnavailableException if the API returned an error or
     *         the response format is invalid
     */
    private ExchangeRate parseExchangeRate(String json, Currency from, Currency to) {
        // Parse JSON string into a Map structure
        Map<String, Object> data = jsonParser.parseObject(json);

        // Validate that the API call was successful
        String result = (String) data.get("result");
        if (!"success".equals(result)) {
            throw new ExchangeRateUnavailableException(
                    "API returned unsuccessful result: " + result
            );
        }

        // Extract the conversion rate value
        Object rateObj = data.get("conversion_rate");
        if (rateObj == null) {
            throw new ExchangeRateUnavailableException(
                    "API response missing conversion_rate field"
            );
        }

        // Convert to double (handles both Integer and Double from JSON)
        double rate = ((Number) rateObj).doubleValue();

        // Create and return the domain ExchangeRate object
        // Using current date as the rate's validity date
        return ExchangeRate.of(LocalDate.now(), from, to, BigDecimal.valueOf(rate));
    }
}
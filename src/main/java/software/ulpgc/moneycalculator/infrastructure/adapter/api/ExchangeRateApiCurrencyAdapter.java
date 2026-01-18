package software.ulpgc.moneycalculator.infrastructure.adapter.api;

import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClient;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClientException;
import software.ulpgc.moneycalculator.infrastructure.http.HttpResponse;
import software.ulpgc.moneycalculator.infrastructure.json.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Infrastructure adapter for fetching currency data from the ExchangeRate-API service.
 *
 * <p>This class implements the {@link CurrencyRepository} output port, bridging the
 * application layer with the external ExchangeRate-API web service. It follows the
 * Hexagonal Architecture pattern by adapting the external API's format and protocol
 * to the domain model expected by the application core.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Fetch the list of supported currencies from ExchangeRate-API</li>
 *   <li>Parse JSON responses and convert to domain {@link Currency} objects</li>
 *   <li>Implement simple in-memory caching to avoid redundant API calls</li>
 *   <li>Handle HTTP errors and API failures gracefully</li>
 *   <li>Provide both bulk currency retrieval and individual lookups</li>
 * </ul>
 *
 * <p><strong>API Endpoint Used:</strong></p>
 * <pre>
 * GET {baseUrl}/v6/{apiKey}/codes
 * </pre>
 *
 * <p><strong>Expected Response Format:</strong></p>
 * <pre>{@code
 * {
 *   "result": "success",
 *   "supported_codes": [
 *     ["USD", "United States Dollar"],
 *     ["EUR", "Euro"],
 *     ["GBP", "British Pound Sterling"],
 *     ...
 *   ]
 * }
 * }</pre>
 *
 * <p><strong>Caching Behavior:</strong></p>
 * <p>This adapter implements a simple eternal cache for the currency list. Once fetched,
 * currencies are stored in memory for the lifetime of the adapter instance. This is
 * appropriate because:</p>
 * <ul>
 *   <li>The list of supported currencies changes very infrequently</li>
 *   <li>Currency data is relatively small (typically < 1MB)</li>
 *   <li>Eliminates unnecessary API calls for a rarely-changing dataset</li>
 * </ul>
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li><strong>Network Errors:</strong> Wrapped in RuntimeException with clear message</li>
 *   <li><strong>HTTP Errors:</strong> Non-2xx status codes throw RuntimeException</li>
 *   <li><strong>Parse Errors:</strong> Invalid JSON or missing fields throw RuntimeException</li>
 *   <li><strong>API Errors:</strong> Non-"success" result field throws RuntimeException</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe for concurrent reads after the initial fetch. Multiple
 * threads can safely call {@link #findAll()} and {@link #findByCode(String)} simultaneously.
 * If multiple threads call {@link #findAll()} before the cache is populated, they may
 * all fetch from the API concurrently, but this is a rare and harmless race condition.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create the adapter
 * CurrencyRepository repository = new ExchangeRateApiCurrencyAdapter(
 *     httpClient,
 *     jsonParser,
 *     config
 * );
 *
 * // Fetch all currencies (first call hits API)
 * List<Currency> allCurrencies = repository.findAll();
 *
 * // Subsequent calls use cached data
 * List<Currency> againCurrencies = repository.findAll(); // Instant, no API call
 *
 * // Find specific currency
 * Optional<Currency> usd = repository.findByCode("USD");
 * Optional<Currency> missing = repository.findByCode("XYZ"); // Returns empty
 * }</pre>
 *
 * @see CurrencyRepository
 * @see Currency
 * @see ExchangeRateApiConfig
 */
public class ExchangeRateApiCurrencyAdapter implements CurrencyRepository {

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
     * In-memory cache of fetched currencies.
     * Null until first fetch, then contains the complete currency list.
     * This simple caching strategy is appropriate for rarely-changing currency data.
     */
    private List<Currency> cachedCurrencies;

    /**
     * Constructs a new ExchangeRateApiCurrencyAdapter with required dependencies.
     *
     * <p>All dependencies are injected through the constructor, following the
     * Dependency Injection pattern. This design enables easy testing with mock
     * implementations and keeps the adapter decoupled from specific infrastructure choices.</p>
     *
     * @param httpClient the HTTP client for making API requests; must not be null
     * @param jsonParser the JSON parser for processing responses; must not be null
     * @param config the API configuration with base URL and key; must not be null
     */
    public ExchangeRateApiCurrencyAdapter(
            HttpClient httpClient,
            JsonParser jsonParser,
            ExchangeRateApiConfig config
    ) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.config = config;
    }

    /**
     * Retrieves all supported currencies from the API or cache.
     *
     * <p>This method implements a simple caching strategy:</p>
     * <ol>
     *   <li>If currencies are already cached, return a defensive copy immediately</li>
     *   <li>If cache is empty, fetch currencies from the API</li>
     *   <li>Store fetched currencies in cache</li>
     *   <li>Return a defensive copy to caller</li>
     * </ol>
     *
     * <p><strong>Defensive Copy:</strong></p>
     * <p>Returns a new {@link ArrayList} copy of the cached currencies rather than
     * exposing the internal cache directly. This prevents external code from modifying
     * the cached list, maintaining encapsulation and thread safety.</p>
     *
     * <p><strong>API Call Details:</strong></p>
     * <p>When fetching from the API, this method:</p>
     * <ul>
     *   <li>Constructs the appropriate API endpoint URL</li>
     *   <li>Makes an HTTP GET request</li>
     *   <li>Validates the response status code (expects 2xx)</li>
     *   <li>Parses the JSON response body</li>
     *   <li>Converts the raw data to domain {@link Currency} objects</li>
     * </ul>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Network Failure:</strong> HttpClientException → RuntimeException</li>
     *   <li><strong>HTTP Error:</strong> Non-2xx status → RuntimeException with status code</li>
     *   <li><strong>Parse Error:</strong> Invalid JSON → RuntimeException from parser</li>
     *   <li><strong>API Error:</strong> result != "success" → RuntimeException</li>
     * </ul>
     *
     * @return a list of all supported {@link Currency} objects; never null,
     *         but may be empty if the API returns no currencies
     * @throws RuntimeException if the API call fails, returns an error, or
     *                         the response cannot be parsed
     */
    @Override
    public List<Currency> findAll() {
        // Check if we already have cached currencies
        if (cachedCurrencies != null) {
            // Return a defensive copy to prevent external modification
            return new ArrayList<>(cachedCurrencies);
        }

        try {
            // Build the API endpoint URL
            String url = buildCodesUrl();

            // Make HTTP GET request to fetch currencies
            HttpResponse response = httpClient.get(url);

            // Validate that the HTTP request was successful
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Failed to fetch currencies, status: " + response.statusCode()
                );
            }

            // Parse the JSON response and convert to domain objects
            cachedCurrencies = parseCurrencies(response.body());

            // Return a defensive copy
            return new ArrayList<>(cachedCurrencies);

        } catch (HttpClientException e) {
            // Wrap HTTP client exceptions in RuntimeException for consistency
            throw new RuntimeException("Failed to fetch currencies: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a specific currency by its ISO 4217 code.
     *
     * <p>This method delegates to {@link #findAll()} to get the complete currency list,
     * then searches for a currency with a matching code. The search is case-insensitive
     * to handle user input variations.</p>
     *
     * <p><strong>Search Behavior:</strong></p>
     * <ul>
     *   <li>Case-insensitive matching: "USD", "usd", and "Usd" all match "USD"</li>
     *   <li>Returns the first matching currency (there should only be one)</li>
     *   <li>Returns {@link Optional#empty()} if no currency matches</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <p>This is a linear search O(n) where n is the number of supported currencies
     * (typically 150-200). For better performance with frequent lookups, consider
     * maintaining a {@code Map<String, Currency>} index.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>{@code
     * Optional<Currency> usd = repository.findByCode("USD");
     * if (usd.isPresent()) {
     *     System.out.println("Found: " + usd.get().name());
     * }
     *
     * // Case-insensitive
     * Optional<Currency> eur = repository.findByCode("eur"); // Finds "EUR"
     *
     * // Not found
     * Optional<Currency> invalid = repository.findByCode("XYZ"); // Returns empty
     * }</pre>
     *
     * @param code the ISO 4217 currency code to search for; case-insensitive
     * @return an {@link Optional} containing the matching currency if found,
     *         or {@link Optional#empty()} if no currency with that code exists
     * @throws RuntimeException if fetching the currency list fails (see {@link #findAll()})
     */
    @Override
    public Optional<Currency> findByCode(String code) {
        return findAll().stream()
                .filter(currency -> currency.code().equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * Builds the API endpoint URL for fetching the currency codes list.
     *
     * <p>Constructs a URL following the ExchangeRate-API format:</p>
     * <pre>
     * {baseUrl}/v6/{apiKey}/codes
     * </pre>
     *
     * <p><strong>Example URLs:</strong></p>
     * <pre>
     * https://v6.exchangerate-api.com/v6/abc123def456/codes
     * https://api.example.com/v6/mykey/codes
     * </pre>
     *
     * @return the complete API endpoint URL as a string
     */
    private String buildCodesUrl() {
        return String.format("%s/v6/%s/codes",
                config.getBaseUrl(),
                config.getApiKey()
        );
    }

    /**
     * Parses the JSON response from the API and converts it to domain Currency objects.
     *
     * <p>This method expects a JSON response in the following format:</p>
     * <pre>{@code
     * {
     *   "result": "success",
     *   "supported_codes": [
     *     ["USD", "United States Dollar"],
     *     ["EUR", "Euro"],
     *     ...
     *   ]
     * }
     * }</pre>
     *
     * <p><strong>Validation:</strong></p>
     * <ul>
     *   <li>Checks that the "result" field equals "success"</li>
     *   <li>Verifies the "supported_codes" field exists</li>
     *   <li>Validates that each currency pair has at least 2 elements</li>
     * </ul>
     *
     * <p><strong>Conversion Process:</strong></p>
     * <ol>
     *   <li>Parse JSON string to Map using JsonParser</li>
     *   <li>Extract and validate the "result" field</li>
     *   <li>Extract the "supported_codes" array</li>
     *   <li>Iterate over each [code, name] pair</li>
     *   <li>Create Currency domain objects using Currency.of()</li>
     *   <li>Collect all currencies into a list</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Throws RuntimeException if result is not "success"</li>
     *   <li>Throws RuntimeException if supported_codes field is missing</li>
     *   <li>Silently skips malformed currency pairs (those without 2 elements)</li>
     * </ul>
     *
     * @param json the JSON response body from the API
     * @return a list of {@link Currency} objects parsed from the response
     * @throws RuntimeException if the API returned an error or the response format is invalid
     */
    private List<Currency> parseCurrencies(String json) {
        // Parse JSON into a Map structure
        Map<String, Object> data = jsonParser.parseObject(json);

        // Validate that the API call was successful
        String result = (String) data.get("result");
        if (!"success".equals(result)) {
            throw new RuntimeException("API returned unsuccessful result: " + result);
        }

        // Extract the array of supported currency codes
        @SuppressWarnings("unchecked")
        List<List<String>> supportedCodes = (List<List<String>>) data.get("supported_codes");

        // Verify the field exists
        if (supportedCodes == null) {
            throw new RuntimeException("API response missing supported_codes field");
        }

        // Convert API format to domain Currency objects
        List<Currency> currencies = new ArrayList<>();
        for (List<String> pair : supportedCodes) {
            // Each pair should be [code, name]
            // Only process pairs that have at least 2 elements
            if (pair.size() >= 2) {
                currencies.add(Currency.of(pair.get(0), pair.get(1)));
            }
        }

        return currencies;
    }
}
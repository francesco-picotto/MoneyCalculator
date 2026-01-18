package software.ulpgc.moneycalculator.infrastructure.adapter.api;

/**
 * Configuration container for the ExchangeRate-API service settings.
 *
 * <p>This immutable configuration class encapsulates all necessary settings required
 * to communicate with the ExchangeRate-API external service. It ensures that configuration
 * values are validated at construction time, following the fail-fast principle.</p>
 *
 * <p><strong>Immutability:</strong></p>
 * <p>All fields are final and set through the constructor. This design ensures thread
 * safety and prevents accidental modification of configuration after initialization,
 * which could lead to inconsistent behavior in multi-threaded environments.</p>
 *
 * <p><strong>Validation Strategy:</strong></p>
 * <p>The constructor validates both the base URL and API key during instantiation,
 * ensuring that invalid configurations cannot be created. This prevents downstream
 * errors that would be harder to diagnose if invalid configuration were allowed.</p>
 *
 * <p><strong>Configuration Values:</strong></p>
 * <ul>
 *   <li><strong>Base URL:</strong> The root endpoint of the API (e.g., https://v6.exchangerate-api.com)</li>
 *   <li><strong>API Key:</strong> The authentication key provided by ExchangeRate-API</li>
 * </ul>
 *
 * <p><strong>ExchangeRate-API Service:</strong></p>
 * <p>ExchangeRate-API (https://www.exchangerate-api.com) is a service providing
 * real-time and historical currency exchange rates. Features include:</p>
 * <ul>
 *   <li>Support for 160+ currencies</li>
 *   <li>Real-time exchange rate updates</li>
 *   <li>Free tier available for development</li>
 *   <li>RESTful JSON API</li>
 *   <li>ISO 4217 currency code support</li>
 * </ul>
 *
 * <p><strong>API Endpoints:</strong></p>
 * <p>This configuration is used to construct URLs for various API operations:</p>
 * <ul>
 *   <li>{@code /v6/{apiKey}/pair/{from}/{to}} - Get exchange rate between currencies</li>
 *   <li>{@code /v6/{apiKey}/codes} - Get list of supported currencies</li>
 *   <li>{@code /v6/{apiKey}/latest/{base}} - Get all rates for a base currency</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create configuration
 * ExchangeRateApiConfig config = new ExchangeRateApiConfig(
 *     "https://v6.exchangerate-api.com",
 *     "abc123def456"
 * );
 *
 * // Use in adapter
 * ExchangeRateProvider provider = new ExchangeRateApiAdapter(
 *     httpClient,
 *     jsonParser,
 *     config
 * );
 *
 * // Construct specific endpoint URL
 * String pairUrl = config.getBaseUrl() + "/v6/" + config.getApiKey() + "/pair/USD/EUR";
 *
 * // Or use helper method
 * String codesUrl = config.getFullUrl("v6/" + config.getApiKey() + "/codes");
 * }</pre>
 *
 * <p><strong>Configuration Sources:</strong></p>
 * <p>Configuration values are typically loaded from:</p>
 * <ul>
 *   <li>Application properties files (application.properties)</li>
 *   <li>Environment variables</li>
 *   <li>Configuration management systems</li>
 *   <li>Command-line arguments</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li>Never hard-code API keys in source code</li>
 *   <li>Store API keys in secure configuration stores</li>
 *   <li>Use environment variables in production</li>
 *   <li>Rotate API keys periodically</li>
 *   <li>Use different keys for different environments</li>
 * </ul>
 *
 * @see ExchangeRateApiAdapter
 * @see ExchangeRateApiCurrencyAdapter
 */
public class ExchangeRateApiConfig {

    /**
     * The base URL of the ExchangeRate-API service.
     *
     * <p>This should be the root endpoint without trailing slash. Example:
     * {@code "https://v6.exchangerate-api.com"}</p>
     *
     * <p>For testing or alternative deployments, this can be pointed to a mock server
     * or local instance.</p>
     */
    private final String baseUrl;

    /**
     * The API key for authenticating with ExchangeRate-API.
     *
     * <p>This key is issued by ExchangeRate-API upon account registration. Each
     * request to the API must include this key in the URL path for authentication.</p>
     *
     * <p>Example format: {@code "abc123def456ghi789jkl012"}</p>
     */
    private final String apiKey;

    /**
     * Constructs a new ExchangeRateApiConfig with the specified base URL and API key.
     *
     * <p>Both parameters are validated during construction. The constructor will throw
     * {@link IllegalArgumentException} if either parameter is null or blank (empty or
     * only whitespace).</p>
     *
     * <p><strong>Parameter Requirements:</strong></p>
     * <ul>
     *   <li><strong>baseUrl:</strong> Must not be null, empty, or only whitespace</li>
     *   <li><strong>apiKey:</strong> Must not be null, empty, or only whitespace</li>
     * </ul>
     *
     * <p><strong>Normalization:</strong></p>
     * <p>Both parameters are trimmed of leading and trailing whitespace to handle
     * accidental spaces in configuration files.</p>
     *
     * <p><strong>Validation Examples:</strong></p>
     * <pre>{@code
     * // Valid configurations
     * new ExchangeRateApiConfig("https://v6.exchangerate-api.com", "mykey123");
     * new ExchangeRateApiConfig(" https://api.example.com ", " key "); // Trimmed
     *
     * // Invalid configurations (throw IllegalArgumentException)
     * new ExchangeRateApiConfig(null, "mykey");        // Null base URL
     * new ExchangeRateApiConfig("", "mykey");          // Empty base URL
     * new ExchangeRateApiConfig("   ", "mykey");       // Blank base URL
     * new ExchangeRateApiConfig("https://api.com", null);     // Null API key
     * new ExchangeRateApiConfig("https://api.com", "");       // Empty API key
     * new ExchangeRateApiConfig("https://api.com", "   ");    // Blank API key
     * }</pre>
     *
     * @param baseUrl the base URL of the API service; must not be null or blank
     * @param apiKey the API authentication key; must not be null or blank
     * @throws IllegalArgumentException if baseUrl is null or blank
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    public ExchangeRateApiConfig(String baseUrl, String apiKey) {
        this.baseUrl = validateBaseUrl(baseUrl);
        this.apiKey = validateApiKey(apiKey);
    }

    /**
     * Validates and normalizes the base URL.
     *
     * <p>This method ensures the base URL is not null or blank, and trims any
     * leading or trailing whitespace. The validation occurs during construction
     * to fail fast if the configuration is invalid.</p>
     *
     * @param baseUrl the base URL to validate
     * @return the validated and trimmed base URL
     * @throws IllegalArgumentException if baseUrl is null or blank
     */
    private String validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        return baseUrl.trim();
    }

    /**
     * Validates and normalizes the API key.
     *
     * <p>This method ensures the API key is not null or blank, and trims any
     * leading or trailing whitespace. The validation occurs during construction
     * to fail fast if the configuration is invalid.</p>
     *
     * @param apiKey the API key to validate
     * @return the validated and trimmed API key
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    private String validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        return apiKey.trim();
    }

    /**
     * Returns the base URL of the API service.
     *
     * <p>The base URL is the root endpoint without any path components. It should
     * be used as the starting point for constructing specific API endpoint URLs.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * String baseUrl = config.getBaseUrl();
     * // Returns: "https://v6.exchangerate-api.com"
     *
     * // Use to construct specific endpoint
     * String endpoint = baseUrl + "/v6/" + config.getApiKey() + "/pair/USD/EUR";
     * }</pre>
     *
     * @return the validated and trimmed base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the API authentication key.
     *
     * <p>This key must be included in API requests for authentication. The exact
     * placement depends on the API endpoint - for ExchangeRate-API, it's typically
     * part of the URL path.</p>
     *
     * <p><strong>Security Note:</strong></p>
     * <p>Be careful when logging or displaying API keys. Consider redacting them
     * in logs to prevent accidental exposure.</p>
     *
     * @return the validated and trimmed API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Constructs a complete URL by appending a path to the base URL.
     *
     * <p>This helper method combines the base URL with a specific path to create
     * a full API endpoint URL. A forward slash separator is automatically added
     * between the base URL and path.</p>
     *
     * <p><strong>Path Format:</strong></p>
     * <p>The path should not start with a forward slash, as one is added automatically.
     * However, the method handles paths that start with a slash gracefully.</p>
     *
     * <p><strong>Usage Examples:</strong></p>
     * <pre>{@code
     * ExchangeRateApiConfig config = new ExchangeRateApiConfig(
     *     "https://v6.exchangerate-api.com",
     *     "mykey"
     * );
     *
     * // Without leading slash
     * String url1 = config.getFullUrl("v6/mykey/codes");
     * // Returns: "https://v6.exchangerate-api.com/v6/mykey/codes"
     *
     * // With leading slash (also works)
     * String url2 = config.getFullUrl("/v6/mykey/codes");
     * // Returns: "https://v6.exchangerate-api.com//v6/mykey/codes"
     * // Note: Double slash, so prefer without leading slash
     *
     * // Building dynamic path
     * String path = String.format("v6/%s/pair/%s/%s",
     *     config.getApiKey(), "USD", "EUR");
     * String url3 = config.getFullUrl(path);
     * // Returns: "https://v6.exchangerate-api.com/v6/mykey/pair/USD/EUR"
     * }</pre>
     *
     * @param path the path component to append to the base URL; should not start with /
     * @return the complete URL combining base URL and path
     */
    public String getFullUrl(String path) {
        return baseUrl + "/" + path;
    }
}
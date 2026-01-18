package software.ulpgc.moneycalculator.config;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.application.port.input.GetCurrenciesQuery;
import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.application.port.output.MoneyPresenter;
import software.ulpgc.moneycalculator.application.usecase.ExchangeMoneyUseCase;
import software.ulpgc.moneycalculator.application.usecase.LoadCurrenciesUseCase;
import software.ulpgc.moneycalculator.infrastructure.adapter.api.ExchangeRateApiAdapter;
import software.ulpgc.moneycalculator.infrastructure.adapter.api.ExchangeRateApiConfig;
import software.ulpgc.moneycalculator.infrastructure.adapter.api.ExchangeRateApiCurrencyAdapter;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClient;
import software.ulpgc.moneycalculator.infrastructure.http.HttpClientImpl;
import software.ulpgc.moneycalculator.infrastructure.json.GsonJsonParser;
import software.ulpgc.moneycalculator.infrastructure.json.JsonParser;

import java.time.Duration;

/**
 * Manual dependency injection configuration for the Money Calculator application.
 *
 * <p>This class serves as the Composition Root for the application, responsible for
 * constructing and wiring together all components of the system. It implements manual
 * dependency injection following the Dependency Inversion Principle and ensures that
 * all dependencies flow inward toward the application core.</p>
 *
 * <p><strong>Architecture Pattern:</strong></p>
 * <p>This configuration supports the Hexagonal Architecture by:</p>
 * <ul>
 *   <li>Creating use case instances (application core)</li>
 *   <li>Providing infrastructure adapters (external dependencies)</li>
 *   <li>Wiring ports to adapters at runtime</li>
 *   <li>Managing component lifecycle (singletons vs. transient instances)</li>
 * </ul>
 *
 * <p><strong>Dependency Injection Strategy:</strong></p>
 * <p>This class uses constructor injection to wire dependencies. Shared infrastructure
 * components (HTTP client, JSON parser, API adapters) are created as singletons using
 * lazy initialization, while use cases may be created as new instances for each request
 * depending on their statefulness.</p>
 *
 * <p><strong>Framework Alternative:</strong></p>
 * <p>In larger applications, consider using a mature DI framework such as:</p>
 * <ul>
 *   <li><strong>Spring Framework:</strong> Full-featured with extensive ecosystem</li>
 *   <li><strong>Google Guice:</strong> Lightweight with compile-time validation</li>
 *   <li><strong>Dagger:</strong> Code-generation based for maximum performance</li>
 * </ul>
 *
 * <p><strong>Configuration Requirements:</strong></p>
 * <p>This class expects the following properties to be configured in the
 * {@link ApplicationConfig}:</p>
 * <ul>
 *   <li>{@code api.exchangerate.baseurl} - Base URL for the exchange rate API (optional, has default)</li>
 *   <li>{@code api.exchangerate.key} - API key for authentication (required)</li>
 *   <li>{@code http.timeout.seconds} - HTTP request timeout in seconds (optional, default: 10)</li>
 * </ul>
 */
public class DependencyInjectionConfig {

    /**
     * Application configuration containing all property values.
     * Used to configure infrastructure components with appropriate settings.
     */
    private final ApplicationConfig appConfig;

    // Singleton instances - lazily initialized on first access

    /**
     * Shared HTTP client instance for making external API calls.
     * Singleton to reuse connection pools and reduce resource overhead.
     */
    private HttpClient httpClient;

    /**
     * Shared JSON parser instance for serializing/deserializing JSON data.
     * Singleton as JSON parsers are typically stateless and thread-safe.
     */
    private JsonParser jsonParser;

    /**
     * Shared exchange rate provider adapter.
     * Singleton to enable potential caching of exchange rates.
     */
    private ExchangeRateProvider exchangeRateProvider;

    /**
     * Shared currency repository adapter.
     * Singleton to enable caching of available currencies list.
     */
    private CurrencyRepository currencyRepository;

    /**
     * Constructs a new DependencyInjectionConfig with the specified application configuration.
     *
     * <p>This constructor initializes the configuration manager but does not create any
     * components immediately. Components are created lazily when first requested, allowing
     * for faster application startup and reduced memory footprint.</p>
     *
     * @param appConfig the application configuration containing all necessary settings;
     *                 must not be null
     * @throws NullPointerException if appConfig is null
     */
    public DependencyInjectionConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Provides a singleton HTTP client for making external HTTP requests.
     *
     * <p>The HTTP client is configured with a timeout from the application properties.
     * If no timeout is specified, a default of 10 seconds is used. Using a singleton
     * HTTP client allows for connection pooling and resource reuse across multiple requests.</p>
     *
     * <p><strong>Configuration:</strong></p>
     * <ul>
     *   <li>Property: {@code http.timeout.seconds}</li>
     *   <li>Default: 10 seconds</li>
     * </ul>
     *
     * @return the shared {@link HttpClient} instance
     */
    public HttpClient httpClient() {
        if (httpClient == null) {
            // Read timeout from configuration, default to 10 seconds if not specified
            int timeoutSeconds = appConfig.getInt("http.timeout.seconds", 10);
            httpClient = new HttpClientImpl(Duration.ofSeconds(timeoutSeconds));
        }
        return httpClient;
    }

    /**
     * Provides a singleton JSON parser for serialization and deserialization.
     *
     * <p>Currently configured to use Gson as the JSON parsing library. The parser
     * is stateless and thread-safe, making it safe to share across the application.</p>
     *
     * @return the shared {@link JsonParser} instance
     */
    public JsonParser jsonParser() {
        if (jsonParser == null) {
            jsonParser = new GsonJsonParser();
        }
        return jsonParser;
    }

    /**
     * Provides the configuration for the Exchange Rate API.
     *
     * <p>This method reads the API base URL and API key from the application
     * configuration and creates an immutable configuration object. The API key
     * is required and the application will fail fast with a clear error message
     * if it's not configured.</p>
     *
     * <p><strong>Configuration Properties:</strong></p>
     * <ul>
     *   <li>{@code api.exchangerate.baseurl} - Optional, defaults to "https://v6.exchangerate-api.com"</li>
     *   <li>{@code api.exchangerate.key} - Required, must not be blank</li>
     * </ul>
     *
     * @return a new {@link ExchangeRateApiConfig} instance with API settings
     * @throws IllegalStateException if the API key is not configured or is blank
     */
    public ExchangeRateApiConfig exchangeRateApiConfig() {
        // Read base URL with default fallback
        String baseUrl = appConfig.get(
                "api.exchangerate.baseurl",
                "https://v6.exchangerate-api.com"
        );

        // Read API key - this is required
        String apiKey = appConfig.get("api.exchangerate.key");

        // Validate that API key is present and non-blank
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "API key not configured. Please set 'api.exchangerate.key' in application.properties"
            );
        }

        return new ExchangeRateApiConfig(baseUrl, apiKey);
    }

    /**
     * Provides a singleton exchange rate provider adapter.
     *
     * <p>This adapter is responsible for fetching current exchange rates from the
     * external Exchange Rate API. It's implemented as a singleton to enable potential
     * caching strategies and reduce the number of external API calls.</p>
     *
     * <p><strong>Dependencies:</strong></p>
     * <ul>
     *   <li>{@link HttpClient} - For making HTTP requests to the API</li>
     *   <li>{@link JsonParser} - For parsing JSON responses</li>
     *   <li>{@link ExchangeRateApiConfig} - For API endpoint and authentication</li>
     * </ul>
     *
     * @return the shared {@link ExchangeRateProvider} instance
     * @throws IllegalStateException if API configuration is invalid
     */
    public ExchangeRateProvider exchangeRateProvider() {
        if (exchangeRateProvider == null) {
            exchangeRateProvider = new ExchangeRateApiAdapter(
                    httpClient(),
                    jsonParser(),
                    exchangeRateApiConfig()
            );
        }
        return exchangeRateProvider;
    }

    /**
     * Provides a singleton currency repository adapter.
     *
     * <p>This adapter is responsible for fetching the list of available currencies
     * from the external Exchange Rate API. It's implemented as a singleton to enable
     * caching of the currency list, which typically doesn't change frequently.</p>
     *
     * <p><strong>Dependencies:</strong></p>
     * <ul>
     *   <li>{@link HttpClient} - For making HTTP requests to the API</li>
     *   <li>{@link JsonParser} - For parsing JSON responses</li>
     *   <li>{@link ExchangeRateApiConfig} - For API endpoint and authentication</li>
     * </ul>
     *
     * @return the shared {@link CurrencyRepository} instance
     * @throws IllegalStateException if API configuration is invalid
     */
    public CurrencyRepository currencyRepository() {
        if (currencyRepository == null) {
            currencyRepository = new ExchangeRateApiCurrencyAdapter(
                    httpClient(),
                    jsonParser(),
                    exchangeRateApiConfig()
            );
        }
        return currencyRepository;
    }

    /**
     * Creates a new exchange money command use case instance.
     *
     * <p>This method creates a new instance of the {@link ExchangeMoneyUseCase} for
     * each invocation. While the use case itself could be stateless, creating new
     * instances allows for presenter-specific configurations and ensures clean
     * separation between different UI contexts.</p>
     *
     * <p><strong>Use Case Responsibilities:</strong></p>
     * <ul>
     *   <li>Validate input parameters (source money, target currency)</li>
     *   <li>Fetch exchange rates from the provider</li>
     *   <li>Perform currency conversion using domain logic</li>
     *   <li>Present results or errors through the provided presenter</li>
     * </ul>
     *
     * <p><strong>Usage Pattern:</strong></p>
     * <pre>{@code
     * // Create presenter for your specific UI context
     * MoneyPresenter presenter = new SwingMoneyPresenter(resultPanel);
     *
     * // Get use case instance configured with your presenter
     * ExchangeMoneyCommand command = config.exchangeMoneyCommand(presenter);
     *
     * // Execute the conversion
     * command.execute(sourceMoney, targetCurrency);
     * }</pre>
     *
     * @param presenter the presenter for displaying results to the user; must not be null
     * @return a new {@link ExchangeMoneyCommand} instance
     * @throws NullPointerException if presenter is null
     */
    public ExchangeMoneyCommand exchangeMoneyCommand(MoneyPresenter presenter) {
        return new ExchangeMoneyUseCase(
                exchangeRateProvider(),
                presenter
        );
    }

    /**
     * Creates a new get currencies query use case instance.
     *
     * <p>This method creates a new instance of the {@link LoadCurrenciesUseCase} for
     * querying available currencies. The use case provides read-only access to the
     * currency catalog.</p>
     *
     * <p><strong>Use Case Responsibilities:</strong></p>
     * <ul>
     *   <li>Retrieve all available currencies</li>
     *   <li>Find specific currencies by code</li>
     *   <li>Validate currency codes</li>
     *   <li>Handle currency not found scenarios</li>
     * </ul>
     *
     * <p><strong>Usage Pattern:</strong></p>
     * <pre>{@code
     * GetCurrenciesQuery query = config.getCurrenciesQuery();
     *
     * // Get all currencies for dropdown population
     * List<Currency> currencies = query.getAllCurrencies();
     *
     * // Find specific currency
     * Currency usd = query.findByCode("USD");
     * }</pre>
     *
     * @return a new {@link GetCurrenciesQuery} instance
     */
    public GetCurrenciesQuery getCurrenciesQuery() {
        return new LoadCurrenciesUseCase(currencyRepository());
    }
}
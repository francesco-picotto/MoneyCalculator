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
 * Manual dependency injection configuration.
 * In a larger application, consider using a DI framework like Spring or Guice.
 */
public class DependencyInjectionConfig {
    private final ApplicationConfig appConfig;
    
    // Singletons
    private HttpClient httpClient;
    private JsonParser jsonParser;
    private ExchangeRateProvider exchangeRateProvider;
    private CurrencyRepository currencyRepository;

    public DependencyInjectionConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    public HttpClient httpClient() {
        if (httpClient == null) {
            int timeoutSeconds = appConfig.getInt("http.timeout.seconds", 10);
            httpClient = new HttpClientImpl(Duration.ofSeconds(timeoutSeconds));
        }
        return httpClient;
    }

    public JsonParser jsonParser() {
        if (jsonParser == null) {
            jsonParser = new GsonJsonParser();
        }
        return jsonParser;
    }

    public ExchangeRateApiConfig exchangeRateApiConfig() {
        String baseUrl = appConfig.get("api.exchangerate.baseurl", "https://v6.exchangerate-api.com");
        String apiKey = appConfig.get("api.exchangerate.key");
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "API key not configured. Please set 'api.exchangerate.key' in application.properties"
            );
        }
        
        return new ExchangeRateApiConfig(baseUrl, apiKey);
    }

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

    public ExchangeMoneyCommand exchangeMoneyCommand(MoneyPresenter presenter) {
        return new ExchangeMoneyUseCase(
            exchangeRateProvider(),
            presenter
        );
    }

    public GetCurrenciesQuery getCurrenciesQuery() {
        return new LoadCurrenciesUseCase(currencyRepository());
    }
}

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
 * Adapter for fetching currencies from ExchangeRate-API.
 * Implements the CurrencyRepository port.
 */
public class ExchangeRateApiCurrencyAdapter implements CurrencyRepository {
    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final ExchangeRateApiConfig config;
    private List<Currency> cachedCurrencies;

    public ExchangeRateApiCurrencyAdapter(
        HttpClient httpClient,
        JsonParser jsonParser,
        ExchangeRateApiConfig config
    ) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.config = config;
    }

    @Override
    public List<Currency> findAll() {
        if (cachedCurrencies != null) {
            return new ArrayList<>(cachedCurrencies);
        }

        try {
            String url = buildCodesUrl();
            HttpResponse response = httpClient.get(url);
            
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    "Failed to fetch currencies, status: " + response.statusCode()
                );
            }
            
            cachedCurrencies = parseCurrencies(response.body());
            return new ArrayList<>(cachedCurrencies);
            
        } catch (HttpClientException e) {
            throw new RuntimeException("Failed to fetch currencies: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        return findAll().stream()
            .filter(currency -> currency.code().equalsIgnoreCase(code))
            .findFirst();
    }

    private String buildCodesUrl() {
        return String.format("%s/v6/%s/codes",
            config.getBaseUrl(),
            config.getApiKey()
        );
    }

    private List<Currency> parseCurrencies(String json) {
        Map<String, Object> data = jsonParser.parseObject(json);
        
        String result = (String) data.get("result");
        if (!"success".equals(result)) {
            throw new RuntimeException("API returned unsuccessful result: " + result);
        }
        
        @SuppressWarnings("unchecked")
        List<List<String>> supportedCodes = (List<List<String>>) data.get("supported_codes");
        
        if (supportedCodes == null) {
            throw new RuntimeException("API response missing supported_codes field");
        }
        
        List<Currency> currencies = new ArrayList<>();
        for (List<String> pair : supportedCodes) {
            if (pair.size() >= 2) {
                currencies.add(Currency.of(pair.get(0), pair.get(1)));
            }
        }
        
        return currencies;
    }
}

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
 * Adapter for fetching exchange rates from ExchangeRate-API.
 * Implements the ExchangeRateProvider port.
 */
public class ExchangeRateApiAdapter implements ExchangeRateProvider {
    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final ExchangeRateApiConfig config;

    public ExchangeRateApiAdapter(
        HttpClient httpClient,
        JsonParser jsonParser,
        ExchangeRateApiConfig config
    ) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.config = config;
    }

    @Override
    public ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException {
        try {
            String url = buildUrl(from, to);
            HttpResponse response = httpClient.get(url);
            
            if (!response.isSuccessful()) {
                throw new ExchangeRateUnavailableException(
                    "API request failed with status: " + response.statusCode()
                );
            }
            
            return parseExchangeRate(response.body(), from, to);
            
        } catch (HttpClientException e) {
            throw new ExchangeRateUnavailableException(
                "Failed to fetch exchange rate: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new ExchangeRateUnavailableException(
                "Unexpected error fetching exchange rate: " + e.getMessage(), e
            );
        }
    }

    private String buildUrl(Currency from, Currency to) {
        return String.format("%s/v6/%s/pair/%s/%s",
            config.getBaseUrl(),
            config.getApiKey(),
            from.code(),
            to.code()
        );
    }

    private ExchangeRate parseExchangeRate(String json, Currency from, Currency to) {
        Map<String, Object> data = jsonParser.parseObject(json);
        
        String result = (String) data.get("result");
        if (!"success".equals(result)) {
            throw new ExchangeRateUnavailableException(
                "API returned unsuccessful result: " + result
            );
        }
        
        Object rateObj = data.get("conversion_rate");
        if (rateObj == null) {
            throw new ExchangeRateUnavailableException(
                "API response missing conversion_rate field"
            );
        }
        
        double rate = ((Number) rateObj).doubleValue();
        
        return ExchangeRate.of(LocalDate.now(), from, to, BigDecimal.valueOf(rate));
    }
}

package software.ulpgc.moneycalculator.infrastructure.adapter.api;

/**
 * Configuration for the ExchangeRate-API service.
 */
public class ExchangeRateApiConfig {
    private final String baseUrl;
    private final String apiKey;

    public ExchangeRateApiConfig(String baseUrl, String apiKey) {
        this.baseUrl = validateBaseUrl(baseUrl);
        this.apiKey = validateApiKey(apiKey);
    }

    private String validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        return baseUrl.trim();
    }

    private String validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        return apiKey.trim();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getFullUrl(String path) {
        return baseUrl + "/" + path;
    }
}

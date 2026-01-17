package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Abstraction for HTTP client operations.
 * Allows swapping implementations without affecting business logic.
 */
public interface HttpClient {
    HttpResponse get(String url) throws HttpClientException;
    HttpResponse post(String url, String body) throws HttpClientException;
}

package software.ulpgc.moneycalculator.infrastructure.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

/**
 * Implementation of HttpClient using Java's built-in HttpClient (Java 11+).
 */
public class HttpClientImpl implements HttpClient {
    private final java.net.http.HttpClient client;
    private final Duration timeout;

    public HttpClientImpl(Duration timeout) {
        this.timeout = timeout;
        this.client = java.net.http.HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
    }

    public HttpClientImpl() {
        this(Duration.ofSeconds(10));
    }

    @Override
    public HttpResponse get(String url) throws HttpClientException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();

            java.net.http.HttpResponse<String> response = client.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            return new HttpResponse(
                response.statusCode(),
                response.body()
            );
        } catch (IOException e) {
            throw new HttpClientException("HTTP request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpClientException("HTTP request was interrupted", e);
        }
    }

    @Override
    public HttpResponse post(String url, String body) throws HttpClientException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            java.net.http.HttpResponse<String> response = client.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            return new HttpResponse(
                response.statusCode(),
                response.body()
            );
        } catch (IOException e) {
            throw new HttpClientException("HTTP request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpClientException("HTTP request was interrupted", e);
        }
    }
}

package software.ulpgc.moneycalculator.infrastructure.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

/**
 * Implementation of {@link HttpClient} using Java's built-in HttpClient (Java 11+).
 *
 * <p>This class provides a concrete implementation of the HttpClient interface using
 * the modern {@code java.net.http.HttpClient} API introduced in Java 11. This implementation
 * offers good performance, native HTTP/2 support, and requires no external dependencies.</p>
 *
 * <p><strong>Advantages of Java 11+ HttpClient:</strong></p>
 * <ul>
 *   <li><strong>Zero Dependencies:</strong> Part of the Java standard library</li>
 *   <li><strong>HTTP/2 Support:</strong> Automatic protocol negotiation with fallback to HTTP/1.1</li>
 *   <li><strong>Modern Design:</strong> Fluent API with builder pattern</li>
 *   <li><strong>Async Support:</strong> Built-in support for asynchronous requests (not used here)</li>
 *   <li><strong>Connection Pooling:</strong> Automatic connection reuse and pooling</li>
 *   <li><strong>Compression:</strong> Automatic gzip/deflate decompression</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong></p>
 * <p>This implementation supports configurable timeout duration for both connection
 * establishment and request completion. Timeouts help prevent indefinite blocking
 * when servers are slow or unresponsive.</p>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe and can be safely used from multiple threads concurrently.
 * The underlying {@code java.net.http.HttpClient} uses connection pooling and is designed
 * for concurrent use. A single instance can be shared across an application.</p>
 *
 * <p><strong>Resource Management:</strong></p>
 * <p>The Java HttpClient manages its own resources and connection pool. No explicit
 * cleanup is required - resources are automatically released when the JVM shuts down.
 * However, if you need to shut down the client explicitly, you can call
 * {@code client.shutdown()} or {@code client.shutdownNow()} on the underlying client.</p>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>This implementation converts all checked exceptions from the underlying HTTP
 * client into {@link HttpClientException}, providing a consistent exception interface
 * regardless of the specific failure type.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create with custom timeout
 * HttpClient client = new HttpClientImpl(Duration.ofSeconds(30));
 *
 * // Create with default 10-second timeout
 * HttpClient defaultClient = new HttpClientImpl();
 *
 * // Make requests (thread-safe, can be called concurrently)
 * HttpResponse response1 = client.get("https://api.example.com/data");
 * HttpResponse response2 = client.post("https://api.example.com/submit", jsonData);
 *
 * // Share single instance across application
 * public class AppConfig {
 *     private static final HttpClient HTTP_CLIENT = new HttpClientImpl(Duration.ofSeconds(15));
 *
 *     public static HttpClient getHttpClient() {
 *         return HTTP_CLIENT;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Comparison with Alternatives:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Library</th>
 *     <th>Pros</th>
 *     <th>Cons</th>
 *   </tr>
 *   <tr>
 *     <td>Java HttpClient</td>
 *     <td>No dependencies, HTTP/2, modern API</td>
 *     <td>Requires Java 11+</td>
 *   </tr>
 *   <tr>
 *     <td>Apache HttpClient</td>
 *     <td>Feature-rich, very mature</td>
 *     <td>External dependency, verbose API</td>
 *   </tr>
 *   <tr>
 *     <td>OkHttp</td>
 *     <td>Simple, efficient, HTTP/2</td>
 *     <td>External dependency</td>
 *   </tr>
 * </table>
 *
 * @see HttpClient
 * @see HttpResponse
 * @see HttpClientException
 */
public class HttpClientImpl implements HttpClient {

    /**
     * The underlying Java HTTP client that performs the actual HTTP communication.
     * Configured with connection timeout and other settings.
     */
    private final java.net.http.HttpClient client;

    /**
     * The timeout duration for both connection establishment and request completion.
     * Applied to all HTTP requests made by this client.
     */
    private final Duration timeout;

    /**
     * Constructs a new HttpClientImpl with the specified timeout duration.
     *
     * <p>This constructor creates a new HTTP client instance with a custom timeout.
     * The timeout applies to both the connection phase (establishing the TCP connection)
     * and the request phase (waiting for the server's response).</p>
     *
     * <p><strong>Timeout Behavior:</strong></p>
     * <ul>
     *   <li>If connection cannot be established within the timeout, throws HttpClientException</li>
     *   <li>If server doesn't respond within the timeout, throws HttpClientException</li>
     *   <li>Does not apply to reading the response body (which is typically fast)</li>
     * </ul>
     *
     * <p><strong>Choosing a Timeout:</strong></p>
     * <ul>
     *   <li><strong>Fast APIs (5-10 seconds):</strong> For quick, reliable services</li>
     *   <li><strong>Standard APIs (10-30 seconds):</strong> For most external APIs</li>
     *   <li><strong>Slow APIs (30-60 seconds):</strong> For processing-intensive operations</li>
     *   <li><strong>Batch Operations (60+ seconds):</strong> For large data transfers</li>
     * </ul>
     *
     * @param timeout the timeout duration for connection and request; must not be null
     * @throws NullPointerException if timeout is null
     */
    public HttpClientImpl(Duration timeout) {
        this.timeout = timeout;

        // Build the HTTP client with specified timeout configuration
        this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(timeout)  // Timeout for establishing connection
                .build();
    }

    /**
     * Constructs a new HttpClientImpl with default 10-second timeout.
     *
     * <p>This convenience constructor is suitable for most use cases where external
     * APIs are reasonably fast and reliable. A 10-second timeout provides a good
     * balance between allowing sufficient time for response and preventing long waits
     * when services are unavailable.</p>
     *
     * <p><strong>When 10 Seconds Is Appropriate:</strong></p>
     * <ul>
     *   <li>Well-maintained external APIs with good SLAs</li>
     *   <li>Internal microservices on the same network</li>
     *   <li>Simple GET requests for small data payloads</li>
     *   <li>Standard CRUD operations</li>
     * </ul>
     *
     * <p><strong>When to Use Custom Timeout:</strong></p>
     * <ul>
     *   <li>Slow or unreliable external services</li>
     *   <li>Large file uploads/downloads</li>
     *   <li>Complex server-side processing</li>
     *   <li>Real-time applications needing fast failures</li>
     * </ul>
     */
    public HttpClientImpl() {
        this(Duration.ofSeconds(10));
    }

    /**
     * Performs an HTTP GET request using Java's HttpClient.
     *
     * <p>This method constructs an HTTP GET request, sends it to the specified URL,
     * and returns the response. The request includes the configured timeout and uses
     * HTTP/2 when supported by the server, falling back to HTTP/1.1 if necessary.</p>
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Create HTTP request with GET method and timeout</li>
     *   <li>Send request using the configured HTTP client</li>
     *   <li>Receive response (status code and body)</li>
     *   <li>Wrap response in our HttpResponse object</li>
     *   <li>Handle errors by wrapping in HttpClientException</li>
     * </ol>
     *
     * <p><strong>HTTP/2 Protocol:</strong></p>
     * <p>The Java HttpClient automatically uses HTTP/2 when available, which provides:</p>
     * <ul>
     *   <li>Request multiplexing (multiple requests over one connection)</li>
     *   <li>Header compression for reduced bandwidth</li>
     *   <li>Server push capability (though rarely used)</li>
     *   <li>Binary protocol for better performance</li>
     * </ul>
     *
     * <p><strong>Error Translation:</strong></p>
     * <p>The following Java exceptions are caught and wrapped:</p>
     * <ul>
     *   <li><strong>IOException:</strong> Network errors, connection failures → HttpClientException</li>
     *   <li><strong>InterruptedException:</strong> Thread interruption → HttpClientException
     *       (interrupt flag is restored before throwing)</li>
     * </ul>
     *
     * @param url the URL to send the GET request to
     * @return an HttpResponse containing status code and response body
     * @throws HttpClientException if an I/O error occurs or the thread is interrupted
     */
    @Override
    public HttpResponse get(String url) throws HttpClientException {
        try {
            // Build the HTTP GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))      // Parse URL into URI
                    .timeout(timeout)           // Apply configured timeout
                    .GET()                      // Set HTTP method to GET
                    .build();

            // Send the request and receive the response
            java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            // Wrap in our HttpResponse abstraction
            return new HttpResponse(
                    response.statusCode(),
                    response.body()
            );

        } catch (IOException e) {
            // Network errors, connection failures, timeout, etc.
            throw new HttpClientException("HTTP request failed: " + e.getMessage(), e);

        } catch (InterruptedException e) {
            // Thread was interrupted during the HTTP request
            // Restore the interrupt flag before throwing
            Thread.currentThread().interrupt();
            throw new HttpClientException("HTTP request was interrupted", e);
        }
    }

    /**
     * Performs an HTTP POST request using Java's HttpClient.
     *
     * <p>This method constructs an HTTP POST request with the specified body content,
     * sends it to the specified URL, and returns the response. The Content-Type header
     * is automatically set to {@code application/json}, which is appropriate for most
     * modern REST APIs.</p>
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Create HTTP request with POST method and JSON content type</li>
     *   <li>Attach the request body</li>
     *   <li>Send request using the configured HTTP client</li>
     *   <li>Receive response (status code and body)</li>
     *   <li>Wrap response in our HttpResponse object</li>
     *   <li>Handle errors by wrapping in HttpClientException</li>
     * </ol>
     *
     * <p><strong>Content-Type Header:</strong></p>
     * <p>The request includes {@code Content-Type: application/json} header, indicating
     * the body contains JSON data. If you need to POST other content types (XML, form data),
     * consider extending this method or creating specialized POST methods.</p>
     *
     * <p><strong>Request Body:</strong></p>
     * <p>The body is sent as UTF-8 encoded string. For large payloads or binary data,
     * consider using streaming body publishers available in the Java HttpClient API.</p>
     *
     * <p><strong>Example Request:</strong></p>
     * <pre>
     * POST https://api.example.com/users HTTP/2
     * Content-Type: application/json
     *
     * {"username": "john", "email": "john@example.com"}
     * </pre>
     *
     * @param url the URL to send the POST request to
     * @param body the request body content (typically JSON); may be empty but not null
     * @return an HttpResponse containing status code and response body
     * @throws HttpClientException if an I/O error occurs or the thread is interrupted
     */
    @Override
    public HttpResponse post(String url, String body) throws HttpClientException {
        try {
            // Build the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))                           // Parse URL into URI
                    .timeout(timeout)                                // Apply configured timeout
                    .header("Content-Type", "application/json")     // Set JSON content type
                    .POST(HttpRequest.BodyPublishers.ofString(body)) // Set request body
                    .build();

            // Send the request and receive the response
            java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            // Wrap in our HttpResponse abstraction
            return new HttpResponse(
                    response.statusCode(),
                    response.body()
            );

        } catch (IOException e) {
            // Network errors, connection failures, timeout, etc.
            throw new HttpClientException("HTTP request failed: " + e.getMessage(), e);

        } catch (InterruptedException e) {
            // Thread was interrupted during the HTTP request
            // Restore the interrupt flag before throwing
            Thread.currentThread().interrupt();
            throw new HttpClientException("HTTP request was interrupted", e);
        }
    }
}
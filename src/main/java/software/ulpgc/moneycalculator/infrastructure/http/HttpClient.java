package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Abstraction for HTTP client operations.
 *
 * <p>This interface defines a technology-agnostic API for making HTTP requests,
 * allowing the application to swap HTTP client implementations without affecting
 * business logic or infrastructure code. This follows the Dependency Inversion
 * Principle by depending on an abstraction rather than concrete HTTP libraries.</p>
 *
 * <p><strong>Design Benefits:</strong></p>
 * <ul>
 *   <li><strong>Library Independence:</strong> Switch between Java's HttpClient, Apache HttpClient,
 *       OkHttp, or other libraries without changing calling code</li>
 *   <li><strong>Testability:</strong> Easy to mock for unit testing without actual network calls</li>
 *   <li><strong>Simplicity:</strong> Provides only essential HTTP operations, keeping API minimal</li>
 *   <li><strong>Consistency:</strong> Uniform error handling across different HTTP implementations</li>
 * </ul>
 *
 * <p><strong>Supported HTTP Methods:</strong></p>
 * <p>This interface currently supports the two most commonly used HTTP methods:</p>
 * <ul>
 *   <li><strong>GET:</strong> Retrieve data from servers (idempotent, safe)</li>
 *   <li><strong>POST:</strong> Submit data to servers (non-idempotent)</li>
 * </ul>
 *
 * <p>Additional methods (PUT, DELETE, PATCH, HEAD) can be added as needed following
 * the same pattern.</p>
 *
 * <p><strong>Available Implementations:</strong></p>
 * <ul>
 *   <li><strong>HttpClientImpl:</strong> Uses Java 11+ built-in HttpClient (current default)</li>
 *   <li><strong>ApacheHttpClientAdapter:</strong> Uses Apache HttpComponents (future option)</li>
 *   <li><strong>OkHttpClientAdapter:</strong> Uses OkHttp library (future option)</li>
 *   <li><strong>MockHttpClient:</strong> Returns predefined responses for testing</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create HTTP client
 * HttpClient client = new HttpClientImpl(Duration.ofSeconds(10));
 *
 * // Make GET request
 * try {
 *     HttpResponse response = client.get("https://api.example.com/data");
 *     if (response.isSuccessful()) {
 *         String data = response.body();
 *         processData(data);
 *     } else {
 *         System.err.println("Request failed: " + response.statusCode());
 *     }
 * } catch (HttpClientException e) {
 *     System.err.println("Network error: " + e.getMessage());
 * }
 *
 * // Make POST request
 * String requestBody = "{\"key\": \"value\"}";
 * HttpResponse postResponse = client.post("https://api.example.com/submit", requestBody);
 * }</pre>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All methods throw {@link HttpClientException} for network-level failures.
 * Note that HTTP error status codes (4xx, 5xx) are returned as successful
 * {@link HttpResponse} objects - it's the caller's responsibility to check
 * {@link HttpResponse#isSuccessful()} or {@link HttpResponse#statusCode()}.</p>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>Implementations should be thread-safe, allowing concurrent HTTP requests
 * from multiple threads. The Java 11 HttpClient and most modern HTTP libraries
 * are thread-safe by default.</p>
 *
 * <p><strong>Resource Management:</strong></p>
 * <p>Implementations that maintain connection pools or other resources should
 * implement {@link AutoCloseable} for proper resource cleanup, though this is
 * optional as many HTTP clients don't require explicit closing.</p>
 *
 * @see HttpResponse
 * @see HttpClientException
 * @see HttpClientImpl
 */
public interface HttpClient {

    /**
     * Performs an HTTP GET request to the specified URL.
     *
     * <p>GET requests are used to retrieve data from servers. They should be
     * idempotent (multiple identical requests have the same effect as a single request)
     * and safe (they don't modify server state).</p>
     *
     * <p><strong>When to Use GET:</strong></p>
     * <ul>
     *   <li>Fetching data from REST APIs</li>
     *   <li>Retrieving web pages or resources</li>
     *   <li>Reading configuration or status information</li>
     *   <li>Querying databases through API endpoints</li>
     * </ul>
     *
     * <p><strong>URL Requirements:</strong></p>
     * <ul>
     *   <li>Must be a valid, absolute URL (including protocol)</li>
     *   <li>Should be properly URL-encoded (spaces as %20, etc.)</li>
     *   <li>Can include query parameters (e.g., ?param1=value1&param2=value2)</li>
     *   <li>Supports both HTTP and HTTPS protocols</li>
     * </ul>
     *
     * <p><strong>Response Handling:</strong></p>
     * <p>The method returns an {@link HttpResponse} for any response received from
     * the server, regardless of status code. Always check the status:</p>
     * <pre>{@code
     * HttpResponse response = client.get(url);
     *
     * if (response.isSuccessful()) {
     *     // 2xx status - process response body
     *     String data = response.body();
     * } else {
     *     // Non-2xx status - handle error
     *     switch (response.statusCode()) {
     *         case 404:
     *             throw new ResourceNotFoundException();
     *         case 429:
     *             throw new RateLimitExceededException();
     *         case 500:
     *             throw new ServerErrorException();
     *         default:
     *             throw new ApiException("HTTP " + response.statusCode());
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <p>This method throws {@link HttpClientException} for network-level failures:</p>
     * <ul>
     *   <li><strong>Connection Failure:</strong> Cannot connect to host</li>
     *   <li><strong>Timeout:</strong> Request exceeds configured timeout duration</li>
     *   <li><strong>DNS Failure:</strong> Cannot resolve hostname</li>
     *   <li><strong>SSL/TLS Error:</strong> Certificate validation or handshake failure</li>
     *   <li><strong>Interruption:</strong> Thread interrupted during request</li>
     *   <li><strong>Malformed URL:</strong> Invalid URL syntax</li>
     * </ul>
     *
     * <p><strong>Timeout Behavior:</strong></p>
     * <p>The timeout duration is configured when creating the HttpClient implementation.
     * If a request exceeds this timeout, an HttpClientException is thrown. Consider
     * implementing retry logic for timeout failures:</p>
     * <pre>{@code
     * int retries = 3;
     * for (int i = 0; i < retries; i++) {
     *     try {
     *         return client.get(url);
     *     } catch (HttpClientException e) {
     *         if (i == retries - 1) throw e;
     *         Thread.sleep(1000 * (i + 1)); // Exponential backoff
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Simple GET request
     * HttpResponse response = client.get("https://api.example.com/users/123");
     *
     * // GET with query parameters
     * String url = "https://api.example.com/search?q=java&limit=10";
     * HttpResponse searchResults = client.get(url);
     *
     * // GET with error handling
     * try {
     *     HttpResponse response = client.get(apiUrl);
     *     if (response.isSuccessful()) {
     *         return parseJson(response.body());
     *     } else {
     *         throw new ApiException("Status: " + response.statusCode());
     *     }
     * } catch (HttpClientException e) {
     *     logger.error("Network error: " + e.getMessage(), e);
     *     return getDefaultData(); // Fallback
     * }
     * }</pre>
     *
     * @param url the URL to send the GET request to; must be a valid absolute URL
     * @return an {@link HttpResponse} containing the status code and response body
     * @throws HttpClientException if a network error occurs, the request times out,
     *         the connection fails, or the thread is interrupted
     */
    HttpResponse get(String url) throws HttpClientException;

    /**
     * Performs an HTTP POST request to the specified URL with the given body content.
     *
     * <p>POST requests are used to submit data to servers. Unlike GET requests, POST
     * is not required to be idempotent or safe - it can modify server state and repeated
     * identical requests may have different effects.</p>
     *
     * <p><strong>When to Use POST:</strong></p>
     * <ul>
     *   <li>Creating new resources (e.g., create user, add item)</li>
     *   <li>Submitting forms or data</li>
     *   <li>Triggering server-side actions</li>
     *   <li>Uploading files or large data payloads</li>
     *   <li>Authentication requests</li>
     * </ul>
     *
     * <p><strong>Content Type:</strong></p>
     * <p>The implementation should set an appropriate Content-Type header based on
     * the body content. For JSON (most common with REST APIs), the Content-Type
     * should be {@code "application/json"}. Implementations may support other
     * content types as needed.</p>
     *
     * <p><strong>URL and Body Requirements:</strong></p>
     * <ul>
     *   <li>URL must be a valid, absolute URL (including protocol)</li>
     *   <li>Body can be any valid string content (JSON, XML, form data, etc.)</li>
     *   <li>Body may be null or empty for POST requests without a payload</li>
     *   <li>For JSON, ensure proper escaping and encoding</li>
     * </ul>
     *
     * <p><strong>Response Handling:</strong></p>
     * <p>Similar to GET requests, always check the response status:</p>
     * <pre>{@code
     * String jsonBody = "{\"name\": \"John\", \"age\": 30}";
     * HttpResponse response = client.post(url, jsonBody);
     *
     * if (response.isSuccessful()) {
     *     // Success - often 200, 201 (Created), or 202 (Accepted)
     *     if (response.statusCode() == 201) {
     *         System.out.println("Resource created successfully");
     *     }
     * } else if (response.statusCode() == 400) {
     *     // Bad request - check error message in response body
     *     String error = response.body();
     *     throw new ValidationException(error);
     * } else {
     *     throw new ApiException("POST failed: " + response.statusCode());
     * }
     * }</pre>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <p>Throws {@link HttpClientException} for the same network-level failures as
     * {@link #get(String)}, including connection failures, timeouts, DNS errors, etc.</p>
     *
     * <p><strong>Idempotency Considerations:</strong></p>
     * <p>POST requests are not idempotent by default. If you need idempotent behavior,
     * consider:</p>
     * <ul>
     *   <li>Using PUT instead of POST when appropriate</li>
     *   <li>Implementing idempotency tokens in your API</li>
     *   <li>Being cautious with retry logic (may create duplicates)</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // POST JSON data
     * String json = """
     *     {
     *       "username": "john_doe",
     *       "email": "john@example.com"
     *     }
     *     """;
     * HttpResponse response = client.post("https://api.example.com/users", json);
     *
     * // POST with error handling
     * try {
     *     HttpResponse response = client.post(apiUrl, requestBody);
     *     if (response.isSuccessful()) {
     *         String result = response.body();
     *         return parseResponse(result);
     *     } else if (response.statusCode() == 409) {
     *         throw new ConflictException("Resource already exists");
     *     } else {
     *         throw new ApiException("POST failed: " + response.statusCode());
     *     }
     * } catch (HttpClientException e) {
     *     logger.error("Network error during POST: " + e.getMessage(), e);
     *     throw new ServiceUnavailableException("Cannot reach server", e);
     * }
     *
     * // POST without body
     * HttpResponse response = client.post("https://api.example.com/trigger", "");
     * }</pre>
     *
     * @param url the URL to send the POST request to; must be a valid absolute URL
     * @param body the request body content to send; typically JSON or form data;
     *            may be null or empty for POST requests without payload
     * @return an {@link HttpResponse} containing the status code and response body
     * @throws HttpClientException if a network error occurs, the request times out,
     *         the connection fails, or the thread is interrupted
     */
    HttpResponse post(String url, String body) throws HttpClientException;
}
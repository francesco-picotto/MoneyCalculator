package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Exception thrown when HTTP client operations fail.
 *
 * <p>This checked exception represents any failure that occurs during HTTP communication,
 * including network errors, timeouts, connection issues, and protocol violations. It
 * serves as a unified exception type for all HTTP-related failures, simplifying error
 * handling for code that uses the {@link HttpClient}.</p>
 *
 * <p><strong>Why Checked Exception?</strong></p>
 * <p>This is a checked exception (extends {@link Exception}) because:</p>
 * <ul>
 *   <li>HTTP failures are expected and recoverable scenarios</li>
 *   <li>Forces callers to explicitly handle network failures</li>
 *   <li>Makes error handling visible in method signatures</li>
 *   <li>Enables proper exception handling strategies (retry, fallback, etc.)</li>
 * </ul>
 *
 * <p><strong>Common Failure Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Network Issues:</strong> No internet connection, DNS resolution failure</li>
 *   <li><strong>Timeout:</strong> Request or connection timeout exceeded</li>
 *   <li><strong>Connection Refused:</strong> Target server not accepting connections</li>
 *   <li><strong>Protocol Errors:</strong> Invalid HTTP response format</li>
 *   <li><strong>SSL/TLS Errors:</strong> Certificate validation failures</li>
 *   <li><strong>Interruption:</strong> Thread interrupted during HTTP operation</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * public ExchangeRate fetchRate(Currency from, Currency to) {
 *     try {
 *         HttpResponse response = httpClient.get(buildUrl(from, to));
 *         return parseResponse(response);
 *     } catch (HttpClientException e) {
 *         // Log the error with full context
 *         logger.error("Failed to fetch exchange rate: " + e.getMessage(), e);
 *
 *         // Throw domain-specific exception
 *         throw new ExchangeRateUnavailableException(
 *             "Cannot retrieve exchange rate due to network error",
 *             e
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Error Handling Strategies:</strong></p>
 * <pre>{@code
 * // Strategy 1: Retry with exponential backoff
 * int maxRetries = 3;
 * for (int i = 0; i < maxRetries; i++) {
 *     try {
 *         return httpClient.get(url);
 *     } catch (HttpClientException e) {
 *         if (i == maxRetries - 1) throw e;
 *         Thread.sleep((long) Math.pow(2, i) * 1000);
 *     }
 * }
 *
 * // Strategy 2: Fallback to cached data
 * try {
 *     return httpClient.get(url);
 * } catch (HttpClientException e) {
 *     logger.warn("Using cached data due to: " + e.getMessage());
 *     return getCachedResponse();
 * }
 *
 * // Strategy 3: Fail fast and notify user
 * try {
 *     return httpClient.get(url);
 * } catch (HttpClientException e) {
 *     showErrorDialog("Network connection failed. Please check your internet.");
 *     return null;
 * }
 * }</pre>
 *
 * <p><strong>Preserving Cause:</strong></p>
 * <p>Always use the constructor with cause when wrapping lower-level exceptions.
 * This preserves the complete exception chain for debugging:</p>
 * <pre>{@code
 * try {
 *     // Attempt HTTP operation
 * } catch (IOException e) {
 *     throw new HttpClientException("Request failed", e);
 * } catch (InterruptedException e) {
 *     Thread.currentThread().interrupt();
 *     throw new HttpClientException("Request interrupted", e);
 * }
 * }</pre>
 *
 * @see HttpClient
 * @see HttpResponse
 */
public class HttpClientException extends Exception {

    /**
     * Constructs a new HttpClientException with the specified detail message.
     *
     * <p>Use this constructor when the exception is not caused by another exception,
     * such as when validating input parameters or detecting logical errors in
     * HTTP request construction.</p>
     *
     * <p><strong>Message Guidelines:</strong></p>
     * <ul>
     *   <li>Be specific about what failed (don't just say "HTTP error")</li>
     *   <li>Include relevant context (URL, HTTP method, etc.)</li>
     *   <li>Use present tense (e.g., "HTTP request fails" not "HTTP request failed")</li>
     *   <li>Avoid exposing sensitive information (API keys, auth tokens)</li>
     * </ul>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>{@code
     * throw new HttpClientException("Invalid URL format: " + url);
     * throw new HttpClientException("HTTP GET request to " + endpoint + " failed");
     * throw new HttpClientException("Connection timeout after 30 seconds");
     * throw new HttpClientException("Cannot connect to host: " + hostname);
     * }</pre>
     *
     * @param message the detail message explaining what went wrong; should not be null
     */
    public HttpClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new HttpClientException with the specified detail message and cause.
     *
     * <p>This is the preferred constructor when wrapping lower-level exceptions from
     * the underlying HTTP library or Java I/O layer. Preserving the original cause
     * provides the complete exception chain for debugging and monitoring.</p>
     *
     * <p><strong>Common Causes to Wrap:</strong></p>
     * <ul>
     *   <li><strong>IOException:</strong> Network errors, connection failures</li>
     *   <li><strong>InterruptedException:</strong> Thread interruption during HTTP call</li>
     *   <li><strong>SocketTimeoutException:</strong> Request timeout</li>
     *   <li><strong>UnknownHostException:</strong> DNS resolution failure</li>
     *   <li><strong>SSLException:</strong> SSL/TLS handshake failure</li>
     * </ul>
     *
     * <p><strong>Interruption Handling:</strong></p>
     * <p>When catching {@code InterruptedException}, always restore the interrupt flag:</p>
     * <pre>{@code
     * try {
     *     // HTTP operation
     * } catch (InterruptedException e) {
     *     Thread.currentThread().interrupt(); // Restore interrupt flag
     *     throw new HttpClientException("HTTP request was interrupted", e);
     * }
     * }</pre>
     *
     * <p><strong>Usage Examples:</strong></p>
     * <pre>{@code
     * // Wrapping I/O exception
     * try {
     *     return client.send(request);
     * } catch (IOException e) {
     *     throw new HttpClientException("HTTP request failed: " + e.getMessage(), e);
     * }
     *
     * // Wrapping timeout exception
     * try {
     *     return performRequest();
     * } catch (SocketTimeoutException e) {
     *     throw new HttpClientException("Request timeout after 10 seconds", e);
     * }
     *
     * // Wrapping SSL exception
     * try {
     *     return secureClient.send(request);
     * } catch (SSLException e) {
     *     throw new HttpClientException("SSL handshake failed", e);
     * }
     * }</pre>
     *
     * <p><strong>Benefits of Preserving Cause:</strong></p>
     * <ul>
     *   <li>Complete stack traces for debugging</li>
     *   <li>Root cause analysis in logging systems</li>
     *   <li>Detailed error reporting in monitoring tools</li>
     *   <li>Better understanding of failure patterns</li>
     * </ul>
     *
     * @param message the detail message explaining what went wrong; should not be null
     * @param cause the underlying exception that caused this exception; may be null
     *             but typically should be provided for wrapped exceptions
     */
    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
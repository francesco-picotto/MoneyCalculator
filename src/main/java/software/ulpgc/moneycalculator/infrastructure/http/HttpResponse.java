package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Represents an HTTP response with status code and body content.
 *
 * <p>This class is a simple immutable value object that encapsulates the essential
 * components of an HTTP response. It provides a clean abstraction over the underlying
 * HTTP client library, allowing the application code to work with HTTP responses
 * without depending on specific HTTP client implementations.</p>
 *
 * <p><strong>Immutability:</strong></p>
 * <p>All fields are final and set through the constructor. Once created, an HttpResponse
 * instance cannot be modified, making it thread-safe and suitable for caching or sharing
 * across threads.</p>
 *
 * <p><strong>Design Rationale:</strong></p>
 * <p>This simplified response model focuses on the most commonly needed HTTP response
 * components. For applications needing additional features (headers, cookies, trailers),
 * this class can be extended or wrapped without affecting existing code.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // HTTP client creates response
 * HttpResponse response = new HttpResponse(200, "{\"result\": \"success\"}");
 *
 * // Check if request was successful
 * if (response.isSuccessful()) {
 *     String body = response.body();
 *     // Process response body
 * } else {
 *     int status = response.statusCode();
 *     System.err.println("Request failed with status: " + status);
 * }
 *
 * // Use in logging
 * System.out.println(response); // HttpResponse{statusCode=200, bodyLength=24}
 * }</pre>
 *
 * <p><strong>Status Code Interpretation:</strong></p>
 * <p>HTTP status codes follow the standard ranges:</p>
 * <ul>
 *   <li><strong>1xx (100-199):</strong> Informational responses</li>
 *   <li><strong>2xx (200-299):</strong> Successful responses</li>
 *   <li><strong>3xx (300-399):</strong> Redirection messages</li>
 *   <li><strong>4xx (400-499):</strong> Client error responses</li>
 *   <li><strong>5xx (500-599):</strong> Server error responses</li>
 * </ul>
 *
 * @see HttpClient
 * @see HttpClientException
 */
public class HttpResponse {

    /**
     * The HTTP status code returned by the server.
     *
     * <p>Common status codes include:</p>
     * <ul>
     *   <li>200 - OK (request succeeded)</li>
     *   <li>201 - Created (resource created successfully)</li>
     *   <li>400 - Bad Request (client error)</li>
     *   <li>401 - Unauthorized (authentication required)</li>
     *   <li>404 - Not Found (resource doesn't exist)</li>
     *   <li>429 - Too Many Requests (rate limit exceeded)</li>
     *   <li>500 - Internal Server Error (server error)</li>
     *   <li>503 - Service Unavailable (temporary unavailability)</li>
     * </ul>
     */
    private final int statusCode;

    /**
     * The response body as a string.
     *
     * <p>This contains the actual content returned by the server, typically:</p>
     * <ul>
     *   <li>JSON for REST APIs</li>
     *   <li>XML for SOAP services</li>
     *   <li>HTML for web pages</li>
     *   <li>Plain text for simple responses</li>
     * </ul>
     *
     * <p>May be null or empty for responses with no body (e.g., 204 No Content).</p>
     */
    private final String body;

    /**
     * Constructs a new HttpResponse with the specified status code and body.
     *
     * <p>This constructor creates an immutable response object. No validation is
     * performed on the status code or body - it's the caller's responsibility to
     * provide valid values.</p>
     *
     * <p><strong>Parameter Guidelines:</strong></p>
     * <ul>
     *   <li><strong>statusCode:</strong> Should be a valid HTTP status code (typically 100-599)</li>
     *   <li><strong>body:</strong> Can be null for responses without content</li>
     * </ul>
     *
     * @param statusCode the HTTP status code (e.g., 200, 404, 500)
     * @param body the response body content as a string; may be null
     */
    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * Returns the HTTP status code of this response.
     *
     * <p>The status code indicates the result of the HTTP request. Use this to
     * determine if the request succeeded or what type of error occurred.</p>
     *
     * <p><strong>Common Usage:</strong></p>
     * <pre>{@code
     * int status = response.statusCode();
     *
     * if (status == 200) {
     *     // Success - process response
     * } else if (status == 404) {
     *     // Not found
     * } else if (status >= 500) {
     *     // Server error - maybe retry
     * }
     * }</pre>
     *
     * @return the HTTP status code as an integer
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the response body content as a string.
     *
     * <p>The body contains the actual data returned by the server. For successful
     * API calls, this typically contains JSON or XML data that needs to be parsed.</p>
     *
     * <p><strong>Null Handling:</strong></p>
     * <p>The body may be null for certain response types (e.g., 204 No Content,
     * HEAD requests). Always check for null before processing:</p>
     * <pre>{@code
     * String body = response.body();
     * if (body != null && !body.isEmpty()) {
     *     // Process body
     * }
     * }</pre>
     *
     * <p><strong>Large Bodies:</strong></p>
     * <p>For very large responses, consider streaming the body rather than loading
     * it entirely into memory as a string.</p>
     *
     * @return the response body as a string; may be null
     */
    public String body() {
        return body;
    }

    /**
     * Checks if this response represents a successful HTTP request.
     *
     * <p>A response is considered successful if its status code is in the 2xx range
     * (200-299). This includes:</p>
     * <ul>
     *   <li>200 OK - Standard success response</li>
     *   <li>201 Created - Resource successfully created</li>
     *   <li>202 Accepted - Request accepted for processing</li>
     *   <li>204 No Content - Success with no response body</li>
     * </ul>
     *
     * <p><strong>Usage Pattern:</strong></p>
     * <pre>{@code
     * if (response.isSuccessful()) {
     *     // Parse and process response body
     *     String data = response.body();
     *     processData(data);
     * } else {
     *     // Handle error
     *     throw new ApiException("Request failed: " + response.statusCode());
     * }
     * }</pre>
     *
     * <p><strong>Note on Redirects:</strong></p>
     * <p>3xx redirect status codes are not considered "successful" by this method.
     * Most HTTP clients handle redirects automatically, so you typically won't see
     * 3xx codes in responses. If you do, you may need to handle them explicitly.</p>
     *
     * @return {@code true} if status code is in range 200-299, {@code false} otherwise
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Returns a string representation of this HTTP response for debugging and logging.
     *
     * <p>The format includes the status code and body length (not the full body) to
     * provide useful debugging information without overwhelming logs with potentially
     * large response bodies.</p>
     *
     * <p><strong>Format:</strong> {@code "HttpResponse{statusCode=XXX, bodyLength=YYY}"}</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>
     * HttpResponse{statusCode=200, bodyLength=1523}
     * HttpResponse{statusCode=404, bodyLength=45}
     * HttpResponse{statusCode=500, bodyLength=0}
     * </pre>
     *
     * <p><strong>Null Body Handling:</strong></p>
     * <p>If the body is null, the length is shown as 0 rather than displaying "null".</p>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * logger.debug("Received response: " + response);
     * // Output: Received response: HttpResponse{statusCode=200, bodyLength=342}
     *
     * System.out.println("API call result: " + response);
     * // Output: API call result: HttpResponse{statusCode=201, bodyLength=87}
     * }</pre>
     *
     * @return a formatted string suitable for logging and debugging
     */
    @Override
    public String toString() {
        return String.format("HttpResponse{statusCode=%d, bodyLength=%d}",
                statusCode, body != null ? body.length() : 0);
    }
}
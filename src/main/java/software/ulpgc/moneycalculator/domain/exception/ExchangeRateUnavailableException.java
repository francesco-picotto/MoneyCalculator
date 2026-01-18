package software.ulpgc.moneycalculator.domain.exception;

/**
 * Exception thrown when exchange rate data cannot be retrieved from the provider.
 *
 * <p>This domain-specific exception represents a failure to obtain exchange rate
 * information needed for currency conversions. Unlike {@link CurrencyNotFoundException}
 * which indicates invalid currency references, this exception signals infrastructure
 * or data availability issues.</p>
 *
 * <p><strong>Common Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Network Issues:</strong> Loss of connectivity to external exchange rate APIs</li>
 *   <li><strong>API Errors:</strong> External service returns errors (500, 503, rate limits)</li>
 *   <li><strong>Authentication Failures:</strong> Invalid or expired API keys</li>
 *   <li><strong>Unsupported Currency Pairs:</strong> Exchange rate provider doesn't support
 *       the requested currency pair</li>
 *   <li><strong>Timeout Errors:</strong> API requests exceed configured timeout duration</li>
 *   <li><strong>Data Unavailability:</strong> Historical rates not available for requested dates</li>
 * </ul>
 *
 * <p><strong>Exception Type:</strong></p>
 * <p>This is a {@link RuntimeException} (unchecked exception) because:</p>
 * <ul>
 *   <li>Infrastructure failures are often transient and recoverable</li>
 *   <li>Forcing checked exceptions throughout the codebase would reduce flexibility</li>
 *   <li>Allows for centralized error handling at appropriate boundaries</li>
 *   <li>Enables retry mechanisms without cluttering business logic</li>
 * </ul>
 *
 * <p><strong>Difference from CurrencyNotFoundException:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Aspect</th>
 *     <th>CurrencyNotFoundException</th>
 *     <th>ExchangeRateUnavailableException</th>
 *   </tr>
 *   <tr>
 *     <td>Cause</td>
 *     <td>Invalid currency reference</td>
 *     <td>Infrastructure/data failure</td>
 *   </tr>
 *   <tr>
 *     <td>Recovery</td>
 *     <td>User must provide valid currency</td>
 *     <td>Retry may succeed</td>
 *   </tr>
 *   <tr>
 *     <td>Frequency</td>
 *     <td>Relatively rare (user errors)</td>
 *     <td>More common (network issues)</td>
 *   </tr>
 * </table>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * public ExchangeRate getRate(Currency from, Currency to)
 *         throws ExchangeRateUnavailableException {
 *     try {
 *         return apiClient.fetchExchangeRate(from.code(), to.code());
 *     } catch (IOException e) {
 *         throw new ExchangeRateUnavailableException(
 *             "Unable to fetch exchange rate from API", e
 *         );
 *     } catch (ApiRateLimitException e) {
 *         throw new ExchangeRateUnavailableException(
 *             "API rate limit exceeded. Please try again later.", e
 *         );
 *     }
 * }
 *
 * // Handling in use case layer with retry logic
 * try {
 *     ExchangeRate rate = provider.getRate(from, to);
 *     Money result = money.convert(rate);
 *     presenter.presentSuccess(result, rate);
 * } catch (ExchangeRateUnavailableException e) {
 *     presenter.presentError("Exchange rate unavailable: " + e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Error Handling Strategies:</strong></p>
 * <ul>
 *   <li><strong>User Notification:</strong> Display friendly error message to user</li>
 *   <li><strong>Retry Logic:</strong> Implement exponential backoff for transient failures</li>
 *   <li><strong>Fallback Sources:</strong> Try alternative exchange rate providers</li>
 *   <li><strong>Cached Rates:</strong> Use slightly stale rates as fallback</li>
 *   <li><strong>Logging:</strong> Record failures for monitoring and debugging</li>
 * </ul>
 */
public class ExchangeRateUnavailableException extends RuntimeException {

    /**
     * Constructs a new ExchangeRateUnavailableException with the specified detail message.
     *
     * <p>The detail message should clearly explain why the exchange rate could not be
     * retrieved, helping users understand the issue and potentially take corrective action.</p>
     *
     * <p><strong>Message Guidelines:</strong></p>
     * <ul>
     *   <li>Explain what went wrong in non-technical terms for user-facing messages</li>
     *   <li>Include relevant details (currency pair, error type) for logging</li>
     *   <li>Suggest recovery actions when appropriate (e.g., "try again later")</li>
     *   <li>Avoid exposing sensitive information (API keys, internal endpoints)</li>
     * </ul>
     *
     * <p><strong>Example Messages:</strong></p>
     * <pre>{@code
     * // User-friendly messages
     * "Unable to fetch exchange rate. Please check your internet connection."
     * "Exchange rate service is temporarily unavailable. Please try again later."
     * "The requested currency pair is not supported by the exchange rate provider."
     *
     * // Detailed messages for logging
     * "API request timeout while fetching USD to EUR rate"
     * "HTTP 429 - Rate limit exceeded on ExchangeRate-API"
     * "Authentication failed: Invalid API key"
     * }</pre>
     *
     * @param message the detail message explaining why the exchange rate is unavailable;
     *               should not be null
     */
    public ExchangeRateUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructs a new ExchangeRateUnavailableException with the specified detail
     * message and cause.
     *
     * <p>This constructor is the preferred way to create this exception when wrapping
     * lower-level infrastructure exceptions. Preserving the cause provides the complete
     * exception chain for debugging and allows monitoring systems to track root causes.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Network Errors:</strong> Wrapping {@code IOException}, {@code SocketTimeoutException}</li>
     *   <li><strong>HTTP Errors:</strong> Wrapping HTTP client exceptions</li>
     *   <li><strong>Parsing Errors:</strong> Wrapping JSON parsing exceptions when API returns malformed data</li>
     *   <li><strong>Authentication Errors:</strong> Wrapping security exceptions</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Wrapping network exception
     * try {
     *     HttpResponse response = httpClient.get(apiUrl);
     *     return parseExchangeRate(response);
     * } catch (IOException e) {
     *     throw new ExchangeRateUnavailableException(
     *         "Network error while fetching exchange rate", e
     *     );
     * }
     *
     * // Wrapping HTTP error with status code
     * if (response.statusCode() == 429) {
     *     throw new ExchangeRateUnavailableException(
     *         "API rate limit exceeded. Please try again in " + retryAfter + " seconds",
     *         new ApiRateLimitException(response)
     *     );
     * }
     *
     * // Wrapping parsing exception
     * try {
     *     return jsonParser.parse(response.body(), ExchangeRateDto.class);
     * } catch (JsonParseException e) {
     *     throw new ExchangeRateUnavailableException(
     *         "Invalid response from exchange rate API", e
     *     );
     * }
     * }</pre>
     *
     * <p><strong>Benefits of Preserving Cause:</strong></p>
     * <ul>
     *   <li>Complete stack traces for debugging</li>
     *   <li>Monitoring systems can track root cause patterns</li>
     *   <li>Distinguishes between different failure types</li>
     *   <li>Enables more sophisticated retry logic based on cause type</li>
     * </ul>
     *
     * @param message the detail message explaining why the exchange rate is unavailable;
     *               should not be null
     * @param cause the underlying exception that caused the exchange rate to be unavailable;
     *             may be null if there is no underlying cause, but typically should be provided
     */
    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
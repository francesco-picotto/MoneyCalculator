package software.ulpgc.moneycalculator.domain.exception;

/**
 * Exception thrown when a requested currency cannot be found in the system.
 *
 * <p>This is a domain-specific exception that represents a business rule violation:
 * attempting to use or reference a currency that doesn't exist in the application's
 * currency catalog. It is a runtime exception to allow for flexible error handling
 * strategies across different layers of the application.</p>
 *
 * <p><strong>Common Scenarios:</strong></p>
 * <ul>
 *   <li>User enters an invalid currency code (e.g., "XYZ")</li>
 *   <li>Application attempts to load a currency that hasn't been configured</li>
 *   <li>Currency data source doesn't include a requested currency</li>
 *   <li>Typo in currency code during lookup operations</li>
 * </ul>
 *
 * <p><strong>Exception Type:</strong></p>
 * <p>This is a {@link RuntimeException} (unchecked exception) because:</p>
 * <ul>
 *   <li>Currency lookups are common operations throughout the application</li>
 *   <li>Forcing checked exception handling would clutter the codebase</li>
 *   <li>The decision to catch and handle this exception can be made at appropriate
 *       boundaries (e.g., use case layer, presentation layer)</li>
 *   <li>It represents a recoverable error that should be communicated to users</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * public Currency findByCode(String code) {
 *     return repository.findByCode(code)
 *         .orElseThrow(() -> new CurrencyNotFoundException(
 *             "Currency not found: " + code
 *         ));
 * }
 *
 * // Handling in use case layer
 * try {
 *     Currency currency = currencyQuery.findByCode("XYZ");
 * } catch (CurrencyNotFoundException e) {
 *     presenter.presentError("Invalid currency code: " + e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>Include the invalid currency code in the exception message</li>
 *   <li>Catch at appropriate boundaries (use cases, controllers)</li>
 *   <li>Convert to user-friendly messages in the presentation layer</li>
 *   <li>Consider logging for debugging purposes</li>
 * </ul>
 */
public class CurrencyNotFoundException extends RuntimeException {

    /**
     * Constructs a new CurrencyNotFoundException with the specified detail message.
     *
     * <p>The detail message should clearly indicate which currency was not found,
     * typically including the currency code that was searched for.</p>
     *
     * <p><strong>Message Guidelines:</strong></p>
     * <ul>
     *   <li>Include the specific currency code that wasn't found</li>
     *   <li>Keep the message concise but informative</li>
     *   <li>Use present tense (e.g., "Currency not found: USD")</li>
     * </ul>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * throw new CurrencyNotFoundException("Currency not found: " + code);
     * throw new CurrencyNotFoundException("Invalid currency code: XYZ");
     * throw new CurrencyNotFoundException("Currency EUR is not supported");
     * }</pre>
     *
     * @param message the detail message explaining which currency was not found;
     *               should not be null
     */
    public CurrencyNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new CurrencyNotFoundException with the specified detail message
     * and cause.
     *
     * <p>This constructor is useful when wrapping lower-level exceptions that occurred
     * during currency lookup operations, such as database errors or API failures.
     * Preserving the cause helps with debugging and allows higher layers to access
     * the complete exception chain.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Wrapping database exceptions during repository queries</li>
     *   <li>Wrapping API client exceptions during external service calls</li>
     *   <li>Preserving the original error while providing domain context</li>
     * </ul>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * try {
     *     return apiClient.fetchCurrency(code);
     * } catch (ApiException e) {
     *     throw new CurrencyNotFoundException(
     *         "Currency not found: " + code + " (API error)",
     *         e
     *     );
     * }
     * }</pre>
     *
     * @param message the detail message explaining which currency was not found;
     *               should not be null
     * @param cause the underlying exception that caused this exception to be thrown;
     *             may be null if there is no underlying cause
     */
    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
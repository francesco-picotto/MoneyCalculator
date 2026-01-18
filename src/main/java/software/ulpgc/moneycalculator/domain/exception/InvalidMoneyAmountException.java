package software.ulpgc.moneycalculator.domain.exception;

/**
 * Exception thrown when attempting to create or manipulate money with an invalid amount.
 *
 * <p>This domain-specific exception enforces business rules related to monetary amounts,
 * preventing the creation of {@link software.ulpgc.moneycalculator.domain.model.Money}
 * objects in invalid states. It represents violations of fundamental monetary constraints.</p>
 *
 * <p><strong>Common Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Negative Amounts:</strong> Attempting to create money with a negative value</li>
 *   <li><strong>Null Amounts:</strong> Providing null as the amount parameter</li>
 *   <li><strong>Invalid Operations:</strong> Subtraction resulting in negative balance</li>
 *   <li><strong>Negative Multiplication:</strong> Multiplying money by a negative factor</li>
 *   <li><strong>Arithmetic Overflow:</strong> Calculations exceeding maximum representable values</li>
 *   <li><strong>Invalid Precision:</strong> Amounts that cannot be properly represented</li>
 * </ul>
 *
 * <p><strong>Design Rationale:</strong></p>
 * <p>This exception is part of the Value Object pattern implementation for the Money class.
 * By throwing this exception during construction or operations, the Money class ensures it
 * cannot exist in an invalid state, adhering to the "always valid" principle of domain-driven
 * design.</p>
 *
 * <p><strong>Exception Type:</strong></p>
 * <p>This is a {@link RuntimeException} (unchecked exception) because:</p>
 * <ul>
 *   <li>Invalid money amounts typically indicate programming errors</li>
 *   <li>These errors should be caught during development and testing</li>
 *   <li>Forcing checked exceptions would clutter arithmetic operations</li>
 *   <li>The exception can be handled at appropriate boundaries when needed</li>
 * </ul>
 *
 * <p><strong>Prevention over Recovery:</strong></p>
 * <p>While this exception can be caught and handled, the preferred approach is to prevent
 * invalid amounts through:</p>
 * <ul>
 *   <li>Input validation at the UI layer</li>
 *   <li>Type-safe APIs that make invalid states impossible</li>
 *   <li>Clear documentation of preconditions</li>
 *   <li>Comprehensive unit tests</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // In Money class constructor
 * private BigDecimal validateAndNormalize(BigDecimal amount) {
 *     if (amount == null) {
 *         throw new InvalidMoneyAmountException("Amount cannot be null");
 *     }
 *     if (amount.compareTo(BigDecimal.ZERO) < 0) {
 *         throw new InvalidMoneyAmountException(
 *             "Amount cannot be negative: " + amount
 *         );
 *     }
 *     return amount.setScale(2, RoundingMode.HALF_UP);
 * }
 *
 * // In Money subtraction operation
 * public Money subtract(Money other) {
 *     BigDecimal result = this.amount.subtract(other.amount);
 *     if (result.compareTo(BigDecimal.ZERO) < 0) {
 *         throw new InvalidMoneyAmountException(
 *             "Subtraction would result in negative amount: " + result
 *         );
 *     }
 *     return Money.of(result, this.currency);
 * }
 *
 * // In Money multiplication
 * public Money multiply(double factor) {
 *     if (factor < 0) {
 *         throw new InvalidMoneyAmountException(
 *             "Cannot multiply by negative factor: " + factor
 *         );
 *     }
 *     return Money.of(amount.multiply(BigDecimal.valueOf(factor)), currency);
 * }
 *
 * // Handling in application layer
 * try {
 *     Money result = price.subtract(discount);
 *     return result;
 * } catch (InvalidMoneyAmountException e) {
 *     throw new BusinessRuleViolationException(
 *         "Discount cannot exceed price", e
 *     );
 * }
 * }</pre>
 *
 * <p><strong>Relationship to Other Exceptions:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Exception</th>
 *     <th>Domain</th>
 *     <th>Typical Cause</th>
 *   </tr>
 *   <tr>
 *     <td>InvalidMoneyAmountException</td>
 *     <td>Money amounts</td>
 *     <td>Invalid numerical values</td>
 *   </tr>
 *   <tr>
 *     <td>CurrencyNotFoundException</td>
 *     <td>Currency references</td>
 *     <td>Unknown currency codes</td>
 *   </tr>
 *   <tr>
 *     <td>ExchangeRateUnavailableException</td>
 *     <td>Exchange rates</td>
 *     <td>Infrastructure failures</td>
 *   </tr>
 * </table>
 */
public class InvalidMoneyAmountException extends RuntimeException {

    /**
     * Constructs a new InvalidMoneyAmountException with the specified detail message.
     *
     * <p>The detail message should clearly explain what constraint was violated and,
     * when possible, include the invalid value that caused the exception.</p>
     *
     * <p><strong>Message Guidelines:</strong></p>
     * <ul>
     *   <li>Clearly state which validation rule was violated</li>
     *   <li>Include the problematic value for easier debugging</li>
     *   <li>Use consistent phrasing across the application</li>
     *   <li>Keep messages concise but informative</li>
     * </ul>
     *
     * <p><strong>Example Messages:</strong></p>
     * <pre>{@code
     * // Null amount
     * "Amount cannot be null"
     *
     * // Negative amount
     * "Amount cannot be negative: -50.00"
     *
     * // Subtraction resulting in negative
     * "Subtraction would result in negative amount: -25.50"
     *
     * // Invalid multiplication factor
     * "Cannot multiply by negative factor: -2.5"
     *
     * // Overflow scenario
     * "Amount exceeds maximum representable value: 999999999999.99"
     *
     * // Precision loss
     * "Amount precision exceeds allowed decimal places: 123.456789"
     * }</pre>
     *
     * @param message the detail message explaining what constraint was violated;
     *               should not be null and should include the invalid value when available
     */
    public InvalidMoneyAmountException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidMoneyAmountException with the specified detail message
     * and cause.
     *
     * <p>This constructor is useful when an invalid money amount is discovered while
     * handling another exception, such as arithmetic exceptions, parsing errors, or
     * validation failures. Preserving the cause provides the complete error context.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Parsing Errors:</strong> When converting string input to numeric amounts</li>
     *   <li><strong>Arithmetic Overflow:</strong> When BigDecimal operations fail</li>
     *   <li><strong>Validation Chains:</strong> When one validation depends on another</li>
     *   <li><strong>External Data:</strong> When processing amounts from external sources</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Wrapping number format exception
     * try {
     *     BigDecimal amount = new BigDecimal(userInput);
     *     return Money.of(amount, currency);
     * } catch (NumberFormatException e) {
     *     throw new InvalidMoneyAmountException(
     *         "Invalid amount format: " + userInput, e
     *     );
     * }
     *
     * // Wrapping arithmetic exception
     * try {
     *     BigDecimal result = amount1.divide(amount2, 2, RoundingMode.HALF_UP);
     *     return Money.of(result, currency);
     * } catch (ArithmeticException e) {
     *     throw new InvalidMoneyAmountException(
     *         "Division resulted in invalid amount", e
     *     );
     * }
     *
     * // Wrapping validation exception from external system
     * try {
     *     validateAmountAgainstBusinessRules(amount);
     *     return Money.of(amount, currency);
     * } catch (ValidationException e) {
     *     throw new InvalidMoneyAmountException(
     *         "Amount violates business rules: " + amount, e
     *     );
     * }
     * }</pre>
     *
     * <p><strong>Benefits of Preserving Cause:</strong></p>
     * <ul>
     *   <li>Complete exception chain for debugging</li>
     *   <li>Distinguishes between different types of amount validation failures</li>
     *   <li>Enables more specific error recovery strategies</li>
     *   <li>Maintains audit trail for compliance and logging</li>
     * </ul>
     *
     * @param message the detail message explaining what constraint was violated;
     *               should not be null
     * @param cause the underlying exception that led to discovering the invalid amount;
     *             may be null if there is no underlying cause
     */
    public InvalidMoneyAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
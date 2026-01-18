package software.ulpgc.moneycalculator.ui;

import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Interface for UI components that collect monetary input from users.
 *
 * <p>This interface defines the contract for dialogs or input forms that capture
 * a monetary amount along with its associated currency. Implementations typically
 * provide UI elements such as text fields for the amount and dropdown lists for
 * currency selection.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Display input controls for amount and currency selection</li>
 *   <li>Validate user input (numeric format, positive values, etc.)</li>
 *   <li>Convert raw input into domain {@link Money} objects</li>
 *   <li>Provide user-friendly error messages for invalid input</li>
 * </ul>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>This interface is part of the View layer in the Model-View-Controller (MVC)
 * pattern. It abstracts the UI technology, allowing the application to work with
 * different UI frameworks (Swing, JavaFX, web-based) without changing business logic.</p>
 *
 * <p><strong>Typical Input Components:</strong></p>
 * <ul>
 *   <li><strong>Amount Field:</strong> Text input for numeric values (e.g., "100.50")</li>
 *   <li><strong>Currency Selector:</strong> Dropdown/combobox with available currencies</li>
 *   <li><strong>Labels:</strong> Descriptive text to guide the user</li>
 *   <li><strong>Validation:</strong> Real-time or on-submit input validation</li>
 * </ul>
 *
 * <p><strong>Available Implementations:</strong></p>
 * <ul>
 *   <li><strong>SwingMoneyDialog:</strong> Swing-based implementation with JTextField
 *       and JComboBox</li>
 *   <li><strong>JavaFXMoneyDialog:</strong> JavaFX implementation (future)</li>
 *   <li><strong>WebMoneyDialog:</strong> HTML/JavaScript implementation (future)</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // In a controller or main class
 * MoneyDialog dialog = new SwingMoneyDialog();
 *
 * // Load available currencies
 * List<Currency> currencies = currencyRepository.findAll();
 * ((SwingMoneyDialog) dialog).setCurrencies(currencies);
 *
 * // When user submits form
 * Money sourceMoney = dialog.get();
 *
 * // Use the money object
 * if (sourceMoney != null && sourceMoney.amount().doubleValue() > 0) {
 *     // Proceed with conversion
 *     exchangeMoneyCommand.execute(sourceMoney, targetCurrency);
 * }
 * }</pre>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>Implementations should handle validation errors gracefully, displaying error
 * messages to users rather than throwing exceptions. For invalid input, they may
 * return a default Money object (e.g., zero amount) or display an error dialog
 * and return null.</p>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>Implementations are typically not thread-safe as they're UI components that
 * should only be accessed from the UI thread (Event Dispatch Thread in Swing,
 * JavaFX Application Thread, etc.).</p>
 *
 * @see Money
 * @see software.ulpgc.moneycalculator.ui.swing.SwingMoneyDialog
 */
public interface MoneyDialog {

    /**
     * Retrieves the monetary amount and currency entered by the user.
     *
     * <p>This method reads the current values from the UI input components and
     * constructs a {@link Money} domain object. It should perform validation
     * to ensure the input is valid before creating the Money object.</p>
     *
     * <p><strong>Validation Checks:</strong></p>
     * <p>Implementations typically validate:</p>
     * <ul>
     *   <li>Amount is a valid numeric value</li>
     *   <li>Amount is non-negative (or positive, depending on requirements)</li>
     *   <li>Currency has been selected</li>
     *   <li>Input fields are not empty</li>
     * </ul>
     *
     * <p><strong>Error Handling Strategies:</strong></p>
     * <ul>
     *   <li><strong>Dialog Approach:</strong> Show error dialog and return null or zero</li>
     *   <li><strong>Inline Validation:</strong> Show error message in UI and prevent submission</li>
     *   <li><strong>Default Values:</strong> Return Money with zero amount for invalid input</li>
     *   <li><strong>Exception:</strong> Throw validation exception (less common for UI)</li>
     * </ul>
     *
     * <p><strong>Return Value Scenarios:</strong></p>
     * <pre>{@code
     * // Valid input: "100" USD selected
     * Money money = dialog.get();
     * // Returns: Money(100.00, USD)
     *
     * // Empty amount field
     * Money money = dialog.get();
     * // Returns: Money(0.00, selected_currency) or null after showing error
     *
     * // Invalid text: "abc"
     * Money money = dialog.get();
     * // Returns: Money(0.00, selected_currency) or null after showing error
     *
     * // Negative value: "-50"
     * Money money = dialog.get();
     * // Returns: Error message shown, Money(0.00, selected_currency)
     * }</pre>
     *
     * <p><strong>Thread Context:</strong></p>
     * <p>This method must be called from the UI thread (e.g., Swing's Event Dispatch
     * Thread). Calling from other threads may cause UI inconsistencies or exceptions.</p>
     *
     * @return a {@link Money} object containing the user's input amount and selected
     *         currency; may return a zero-amount Money object or null for invalid input,
     *         depending on implementation strategy
     */
    Money get();
}
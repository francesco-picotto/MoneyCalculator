package software.ulpgc.moneycalculator.ui;

import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Interface for UI components that display monetary values to users.
 *
 * <p>This interface defines the contract for display components that present the results
 * of currency conversions or other monetary information. Implementations provide visual
 * representation of {@link Money} objects in a user-friendly format.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Display monetary amounts with proper formatting</li>
 *   <li>Show currency codes or symbols alongside amounts</li>
 *   <li>Provide visual feedback for successful operations</li>
 *   <li>Handle display of error states (typically via additional methods)</li>
 * </ul>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>This interface is part of the View layer in the Model-View-Controller (MVC)
 * pattern and also serves as an output port in the Hexagonal Architecture. It abstracts
 * how monetary results are presented, allowing different UI technologies or formats
 * without changing business logic.</p>
 *
 * <p><strong>Typical Display Formats:</strong></p>
 * <ul>
 *   <li><strong>Simple:</strong> "100.50 USD"</li>
 *   <li><strong>With Symbols:</strong> "$100.50"</li>
 *   <li><strong>With Thousands Separator:</strong> "1,234.56 EUR"</li>
 *   <li><strong>Localized:</strong> "1.234,56 EUR" (European format)</li>
 *   <li><strong>With Description:</strong> "Converted Amount: $100.50"</li>
 * </ul>
 *
 * <p><strong>Display States:</strong></p>
 * <ul>
 *   <li><strong>Initial:</strong> "No conversion yet" or placeholder text</li>
 *   <li><strong>Success:</strong> Formatted money value (e.g., "85.00 EUR")</li>
 *   <li><strong>Error:</strong> Error message (via additional method like showError())</li>
 *   <li><strong>Loading:</strong> "Converting..." or progress indicator</li>
 * </ul>
 *
 * <p><strong>Available Implementations:</strong></p>
 * <ul>
 *   <li><strong>SwingMoneyDisplay:</strong> Swing JLabel-based display with formatting</li>
 *   <li><strong>JavaFXMoneyDisplay:</strong> JavaFX Label implementation (future)</li>
 *   <li><strong>ConsoleMoneyDisplay:</strong> Console/terminal text output</li>
 *   <li><strong>WebMoneyDisplay:</strong> HTML element manipulation (future)</li>
 * </ul>
 *
 * <p><strong>Usage in MVC Pattern:</strong></p>
 * <pre>{@code
 * // Controller executes use case
 * public class MoneyController {
 *     private final MoneyDisplay display;
 *     private final ExchangeMoneyCommand command;
 *
 *     public void convertMoney(Money source, Currency target) {
 *         // Command executes and updates display via presenter
 *         command.execute(source, target);
 *         // Display is updated by the presenter through this interface
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Usage with Presenter Pattern:</strong></p>
 * <pre>{@code
 * // Presenter uses MoneyDisplay to show results
 * public class MoneyPresenterAdapter implements MoneyPresenter {
 *     private final MoneyDisplay display;
 *
 *     public void presentSuccess(Money converted, ExchangeRate rate) {
 *         display.show(converted);
 *         // Optionally show rate info elsewhere
 *     }
 *
 *     public void presentError(String message) {
 *         if (display instanceof SwingMoneyDisplay) {
 *             ((SwingMoneyDisplay) display).showError(message);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Formatting Considerations:</strong></p>
 * <ul>
 *   <li>Use appropriate decimal places (typically 2 for most currencies)</li>
 *   <li>Apply thousands separators for readability</li>
 *   <li>Consider locale-specific formatting rules</li>
 *   <li>Use consistent font sizes and colors for amounts</li>
 *   <li>Highlight positive results (green) vs errors (red)</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>Implementations are typically not thread-safe and must be accessed from the
 * UI thread. Use appropriate thread synchronization when updating from background threads:</p>
 * <pre>{@code
 * // In Swing
 * SwingUtilities.invokeLater(() -> display.show(money));
 *
 * // In JavaFX
 * Platform.runLater(() -> display.show(money));
 * }</pre>
 *
 * @see Money
 * @see software.ulpgc.moneycalculator.application.port.output.MoneyPresenter
 * @see software.ulpgc.moneycalculator.ui.swing.SwingMoneyDisplay
 */
public interface MoneyDisplay {

    /**
     * Displays the given monetary amount in the UI.
     *
     * <p>This method updates the display component to show the provided {@link Money}
     * object. Implementations should format the amount appropriately and present it
     * in a clear, readable manner.</p>
     *
     * <p><strong>Formatting Requirements:</strong></p>
     * <p>Implementations should:</p>
     * <ul>
     *   <li>Format the amount with appropriate decimal places (typically 2)</li>
     *   <li>Include thousands separators for large amounts</li>
     *   <li>Display the currency code (e.g., "USD", "EUR")</li>
     *   <li>Use appropriate colors (often green for successful conversions)</li>
     *   <li>Apply suitable font size and style for readability</li>
     * </ul>
     *
     * <p><strong>Display Format Examples:</strong></p>
     * <pre>
     * Simple format:     "100.50 USD"
     * With separator:    "1,234.56 EUR"
     * Large amounts:     "1,000,000.00 GBP"
     * Decimal only:      "0.85 JPY"
     * </pre>
     *
     * <p><strong>Null Handling:</strong></p>
     * <p>Implementations should handle null input gracefully:</p>
     * <ul>
     *   <li>Display an error message: "Error: Invalid conversion"</li>
     *   <li>Show placeholder text: "No result"</li>
     *   <li>Clear the display</li>
     *   <li>Log a warning and show previous value</li>
     * </ul>
     *
     * <p><strong>Implementation Example:</strong></p>
     * <pre>{@code
     * public void show(Money money) {
     *     if (money == null) {
     *         label.setText("Error: Invalid conversion");
     *         label.setForeground(Color.RED);
     *         return;
     *     }
     *
     *     // Format amount with two decimal places and thousands separator
     *     DecimalFormat formatter = new DecimalFormat("#,##0.00");
     *     String formatted = formatter.format(money.amount());
     *
     *     // Display: "1,234.56 EUR"
     *     label.setText(formatted + " " + money.currency().code());
     *     label.setForeground(new Color(0, 100, 0)); // Dark green
     * }
     * }</pre>
     *
     * <p><strong>Visual Feedback:</strong></p>
     * <p>Consider providing visual feedback to indicate successful display:</p>
     * <ul>
     *   <li>Color coding (green for success, red for error)</li>
     *   <li>Animation (brief highlight or fade-in effect)</li>
     *   <li>Size emphasis (larger font for result amounts)</li>
     *   <li>Icons (checkmark for successful conversion)</li>
     * </ul>
     *
     * <p><strong>Thread Context:</strong></p>
     * <p>This method must be called from the UI thread:</p>
     * <pre>{@code
     * // From background thread in Swing
     * Money result = calculateConversion();
     * SwingUtilities.invokeLater(() -> display.show(result));
     *
     * // From background thread in JavaFX
     * Money result = calculateConversion();
     * Platform.runLater(() -> display.show(result));
     * }</pre>
     *
     * <p><strong>Extended Functionality:</strong></p>
     * <p>Many implementations provide additional methods beyond this interface:</p>
     * <ul>
     *   <li>{@code showError(String message)} - Display error messages</li>
     *   <li>{@code clear()} - Reset display to initial state</li>
     *   <li>{@code showLoading()} - Show loading indicator</li>
     *   <li>{@code setFormatting(DecimalFormat format)} - Customize number format</li>
     * </ul>
     *
     * @param money the monetary amount to display; may be null (handled by implementation)
     */
    void show(Money money);
}
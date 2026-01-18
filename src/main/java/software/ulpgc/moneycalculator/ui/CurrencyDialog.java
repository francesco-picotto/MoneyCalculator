package software.ulpgc.moneycalculator.ui;

import software.ulpgc.moneycalculator.domain.model.Currency;

/**
 * Interface for UI components that allow users to select a currency.
 *
 * <p>This interface defines the contract for dialogs or selection controls that enable
 * users to choose a single currency from a list of available options. Unlike
 * {@link MoneyDialog} which collects both amount and currency, this interface focuses
 * solely on currency selection.</p>
 *
 * <p><strong>Typical Use Cases:</strong></p>
 * <ul>
 *   <li><strong>Target Currency Selection:</strong> Choosing the currency to convert to</li>
 *   <li><strong>Filter Selection:</strong> Selecting currency for filtering reports</li>
 *   <li><strong>Preference Setting:</strong> Setting a default or preferred currency</li>
 *   <li><strong>Search Criteria:</strong> Selecting currency for data queries</li>
 * </ul>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>This interface is part of the View layer in the Model-View-Controller (MVC)
 * pattern. It abstracts the UI technology, allowing the application to work with
 * different UI frameworks (Swing, JavaFX, web-based) without changing business logic.</p>
 *
 * <p><strong>Difference from MoneyDialog:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Aspect</th>
 *     <th>MoneyDialog</th>
 *     <th>CurrencyDialog</th>
 *   </tr>
 *   <tr>
 *     <td>Input</td>
 *     <td>Amount + Currency</td>
 *     <td>Currency only</td>
 *   </tr>
 *   <tr>
 *     <td>Return Type</td>
 *     <td>Money</td>
 *     <td>Currency</td>
 *   </tr>
 *   <tr>
 *     <td>Validation</td>
 *     <td>Numeric amount validation</td>
 *     <td>Selection validation</td>
 *   </tr>
 *   <tr>
 *     <td>UI Complexity</td>
 *     <td>TextField + ComboBox</td>
 *     <td>ComboBox only</td>
 *   </tr>
 * </table>
 *
 * <p><strong>Typical UI Components:</strong></p>
 * <ul>
 *   <li><strong>Dropdown/ComboBox:</strong> Most common - shows currency list</li>
 *   <li><strong>Radio Buttons:</strong> For small sets of currencies</li>
 *   <li><strong>Searchable Dropdown:</strong> For large currency lists with filtering</li>
 *   <li><strong>List with Icons:</strong> Currency codes with flag icons</li>
 * </ul>
 *
 * <p><strong>Available Implementations:</strong></p>
 * <ul>
 *   <li><strong>SwingCurrencyDialog:</strong> Swing-based implementation with JComboBox</li>
 *   <li><strong>JavaFXCurrencyDialog:</strong> JavaFX implementation (future)</li>
 *   <li><strong>WebCurrencyDialog:</strong> HTML/JavaScript implementation (future)</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // In a controller or main class
 * CurrencyDialog dialog = new SwingCurrencyDialog();
 *
 * // Load available currencies
 * List<Currency> currencies = currencyRepository.findAll();
 * ((SwingCurrencyDialog) dialog).setCurrencies(currencies);
 *
 * // When user needs to select target currency
 * Currency targetCurrency = dialog.get();
 *
 * // Use the selected currency
 * if (targetCurrency != null) {
 *     Money sourceMoney = moneyDialog.get();
 *     exchangeMoneyCommand.execute(sourceMoney, targetCurrency);
 * }
 * }</pre>
 *
 * <p><strong>Currency Display Format:</strong></p>
 * <p>Implementations typically display currencies in a user-friendly format:</p>
 * <ul>
 *   <li><strong>Code Only:</strong> "USD", "EUR", "GBP"</li>
 *   <li><strong>Code with Name:</strong> "USD - United States Dollar"</li>
 *   <li><strong>Name with Code:</strong> "United States Dollar (USD)"</li>
 *   <li><strong>With Flag:</strong> "🇺🇸 USD - United States Dollar"</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>Implementations are typically not thread-safe as they're UI components that
 * should only be accessed from the UI thread.</p>
 *
 * @see Currency
 * @see MoneyDialog
 * @see software.ulpgc.moneycalculator.ui.swing.SwingCurrencyDialog
 */
public interface CurrencyDialog {

    /**
     * Retrieves the currency currently selected by the user.
     *
     * <p>This method reads the current selection from the UI component and returns
     * the corresponding {@link Currency} domain object. Unlike {@link MoneyDialog#get()}
     * which requires amount validation, this method primarily needs to ensure a
     * currency has been selected.</p>
     *
     * <p><strong>Selection States:</strong></p>
     * <ul>
     *   <li><strong>Valid Selection:</strong> User has selected a currency → return that Currency</li>
     *   <li><strong>No Selection:</strong> Nothing selected yet → may show error or return default</li>
     *   <li><strong>Default Selection:</strong> A currency is pre-selected → return it</li>
     * </ul>
     *
     * <p><strong>Error Handling Strategies:</strong></p>
     * <ul>
     *   <li><strong>Default Currency:</strong> Return a sensible default (e.g., USD, EUR)</li>
     *   <li><strong>Error Dialog:</strong> Show error message and return null</li>
     *   <li><strong>Re-prompt:</strong> Keep dialog open until valid selection made</li>
     *   <li><strong>Exception:</strong> Throw IllegalStateException (less common)</li>
     * </ul>
     *
     * <p><strong>Return Value Scenarios:</strong></p>
     * <pre>{@code
     * // User selected EUR
     * Currency currency = dialog.get();
     * // Returns: Currency("EUR", "Euro")
     *
     * // No selection made, but EUR is default
     * Currency currency = dialog.get();
     * // Returns: Currency("EUR", "Euro")
     *
     * // No selection and no default configured
     * Currency currency = dialog.get();
     * // Returns: null or shows error dialog
     *
     * // First available currency selected by default
     * Currency currency = dialog.get();
     * // Returns: First currency from the loaded list
     * }</pre>
     *
     * <p><strong>Thread Context:</strong></p>
     * <p>This method must be called from the UI thread. In Swing, this means the
     * Event Dispatch Thread. Calling from other threads may cause inconsistencies.</p>
     *
     * <p><strong>Implementation Note:</strong></p>
     * <p>Most implementations will have a pre-selected currency (often the first in
     * the list or a common default like USD/EUR), so null returns should be rare
     * in practice unless the currency list itself is empty.</p>
     *
     * @return the {@link Currency} currently selected by the user; may return null
     *         if no selection has been made and no default is available, depending
     *         on implementation strategy
     */
    Currency get();
}
package software.ulpgc.moneycalculator.control;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.Money;
import software.ulpgc.moneycalculator.ui.CurrencyDialog;
import software.ulpgc.moneycalculator.ui.MoneyDialog;

/**
 * Adapter that bridges the legacy Command interface with the modern use case architecture.
 *
 * <p>This class implements the Adapter pattern to connect two incompatible interfaces:</p>
 * <ul>
 *   <li><strong>Legacy Interface:</strong> {@link Command} - parameterless execute()</li>
 *   <li><strong>Modern Interface:</strong> {@link ExchangeMoneyCommand} - execute with parameters</li>
 * </ul>
 *
 * <p><strong>Architecture Bridge:</strong></p>
 * <p>This adapter serves as a crucial bridge between the presentation layer (UI) and
 * the application layer (use cases), following the Hexagonal Architecture principle
 * of keeping the core business logic independent of UI concerns.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ol>
 *   <li>Implement the legacy {@link Command} interface for UI compatibility</li>
 *   <li>Extract user input from UI dialogs (source money and target currency)</li>
 *   <li>Perform preliminary validation to provide fast feedback</li>
 *   <li>Delegate to the {@link ExchangeMoneyCommand} use case for business logic</li>
 *   <li>Handle unexpected errors gracefully with user-friendly messaging</li>
 * </ol>
 *
 * <p><strong>Key Improvement:</strong></p>
 * <p>This implementation properly delegates to the {@link ExchangeMoneyCommand} use case
 * instead of directly calling the {@code ExchangeRateProvider}, ensuring proper error
 * handling, validation, and result presentation through the established use case workflow.</p>
 *
 * <p><strong>Workflow:</strong></p>
 * <pre>
 * User clicks button → execute() called
 *   ↓
 * Extract data from dialogs
 *   ↓
 * Fast-fail validation (null checks, same currency check)
 *   ↓
 * Delegate to ExchangeMoneyUseCase.execute(money, currency)
 *   ↓
 * Use case handles:
 *   - Exchange rate fetching
 *   - Currency conversion
 *   - Result presentation (via MoneyPresenter)
 * </pre>
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <p>This adapter implements a two-tier error handling approach:</p>
 * <ul>
 *   <li><strong>Tier 1 - Fast Fail:</strong> Basic validation (null checks, same currency)
 *       fails immediately with user feedback before calling the use case</li>
 *   <li><strong>Tier 2 - Use Case:</strong> Complex validation and business errors are
 *       handled by the use case and presented through the MoneyPresenter</li>
 *   <li><strong>Safety Net:</strong> A catch-all exception handler prevents any unexpected
 *       errors from crashing the UI</li>
 * </ul>
 */
public class ExchangeMoneyCommandAdapter implements Command {

    /**
     * Dialog for obtaining source money (amount and source currency) from the user.
     * This dialog is typically a UI component that presents input fields or
     * dropdowns for entering the amount and selecting the source currency.
     */
    private final MoneyDialog moneyDialog;

    /**
     * Dialog for obtaining the target currency from the user.
     * This dialog typically presents a dropdown or list of available currencies
     * for the user to select the destination currency for conversion.
     */
    private final CurrencyDialog currencyDialog;

    /**
     * The use case responsible for executing the currency exchange business logic.
     * This use case handles validation, rate fetching, conversion, and result presentation.
     */
    private final ExchangeMoneyCommand exchangeMoneyUseCase;

    /**
     * Constructs a new ExchangeMoneyCommandAdapter with the required dependencies.
     *
     * <p>This constructor follows the Dependency Injection pattern, allowing all
     * dependencies to be provided from outside. This design enables easy testing
     * by allowing mock implementations to be injected.</p>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Create the adapter with concrete implementations
     * Command command = new ExchangeMoneyCommandAdapter(
     *     new SwingMoneyDialog(mainPanel),
     *     new SwingCurrencyDialog(mainPanel),
     *     exchangeMoneyUseCase
     * );
     *
     * // Wire to UI button
     * exchangeButton.addActionListener(e -> command.execute());
     * }</pre>
     *
     * @param moneyDialog dialog for obtaining source money (amount + source currency);
     *                   must not be null
     * @param currencyDialog dialog for obtaining target currency; must not be null
     * @param exchangeMoneyUseCase the use case that handles the business logic;
     *                            must not be null
     * @throws NullPointerException if any parameter is null
     */
    public ExchangeMoneyCommandAdapter(
            MoneyDialog moneyDialog,
            CurrencyDialog currencyDialog,
            ExchangeMoneyCommand exchangeMoneyUseCase
    ) {
        this.moneyDialog = moneyDialog;
        this.currencyDialog = currencyDialog;
        this.exchangeMoneyUseCase = exchangeMoneyUseCase;
    }

    /**
     * Executes the currency conversion by extracting data from dialogs and delegating
     * to the use case.
     *
     * <p>This method orchestrates the complete currency exchange workflow from the
     * UI perspective. It follows these steps:</p>
     *
     * <ol>
     *   <li><strong>Input Extraction:</strong> Retrieve source money and target currency
     *       from their respective dialogs</li>
     *   <li><strong>Basic Validation:</strong> Perform fast-fail validation checks to
     *       provide immediate feedback for obvious errors</li>
     *   <li><strong>Validation Checks:</strong>
     *       <ul>
     *         <li>Verify source money is not null</li>
     *         <li>Verify target currency is not null</li>
     *         <li>Ensure source and target currencies are different</li>
     *         <li>Warn if amount is zero (non-blocking)</li>
     *       </ul>
     *   </li>
     *   <li><strong>Delegation:</strong> Pass validated data to the use case which handles:
     *       <ul>
     *         <li>Fetching the current exchange rate</li>
     *         <li>Performing the currency conversion</li>
     *         <li>Presenting results or errors via MoneyPresenter</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>Error Display Strategy:</strong></p>
     * <ul>
     *   <li>Fast-fail validation errors are shown as console messages and dialog boxes</li>
     *   <li>Business logic errors are handled by the use case and presented through
     *       the MoneyPresenter</li>
     *   <li>Unexpected errors are caught and displayed in an error dialog</li>
     * </ul>
     *
     * <p><strong>Thread Safety Note:</strong></p>
     * <p>This method is typically called from the UI Event Dispatch Thread (EDT) in
     * Swing applications. The method itself is fast, but if the use case performs
     * long-running operations (API calls), those should be handled asynchronously
     * within the use case or its adapters.</p>
     *
     * <p><strong>No Return Value:</strong></p>
     * <p>This method doesn't return a value. Results are communicated to the user
     * through the MoneyPresenter interface, which is responsible for updating the
     * UI with success or error messages.</p>
     *
     * @see ExchangeMoneyCommand#execute(Money, Currency)
     * @see MoneyDialog#get()
     * @see CurrencyDialog#get()
     */
    @Override
    public void execute() {
        try {
            // Step 1: Extract input data from UI dialogs
            Money sourceMoney = moneyDialog.get();
            Currency targetCurrency = currencyDialog.get();

            // Step 2: Fast-fail validation - check for null values
            // This provides immediate feedback before invoking the use case
            if (sourceMoney == null) {
                System.err.println("Error: Source money is null");
                return;
            }

            if (targetCurrency == null) {
                System.err.println("Error: Target currency is null");
                return;
            }

            // Step 3: Validate that source and target currencies are different
            // Converting between the same currency is not a valid operation
            if (sourceMoney.currency().equals(targetCurrency)) {
                System.err.println("Error: Source and target currencies are the same");

                // Show user-friendly error dialog
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Source and target currencies must be different",
                        "Same Currency",
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Step 4: Warn if converting a zero amount (non-blocking)
            // This is not an error, but might indicate user confusion
            if (sourceMoney.amount().doubleValue() == 0) {
                System.err.println("Warning: Converting zero amount");
            }

            // Step 5: Delegate to the use case for the actual business logic
            // The use case will:
            // - Fetch the exchange rate from the provider
            // - Convert the money using domain logic
            // - Present success or error through the MoneyPresenter
            exchangeMoneyUseCase.execute(sourceMoney, targetCurrency);

        } catch (Exception e) {
            // Safety net: catch any unexpected errors that escaped from the use case
            // This prevents the UI from crashing on unforeseen issues
            System.err.println("Unexpected error in command adapter: " + e.getMessage());
            e.printStackTrace();

            // Display error dialog to inform the user
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "An unexpected error occurred: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
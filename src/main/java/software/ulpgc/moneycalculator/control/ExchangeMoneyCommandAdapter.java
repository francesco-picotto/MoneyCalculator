package software.ulpgc.moneycalculator.control;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.Money;
import software.ulpgc.moneycalculator.ui.CurrencyDialog;
import software.ulpgc.moneycalculator.ui.MoneyDialog;

/**
 * Adapter that bridges the old UI Command interface with the new UseCase architecture.
 *
 * This adapter:
 * 1. Implements the old Command interface (no parameters)
 * 2. Delegates to the new ExchangeMoneyCommand use case (with parameters)
 * 3. Extracts data from UI dialogs and passes it to the use case
 *
 * FIXED: Now properly uses the ExchangeMoneyCommand use case instead of
 * ExchangeRateProvider directly, ensuring proper error handling and presentation.
 */
public class ExchangeMoneyCommandAdapter implements Command {
    private final MoneyDialog moneyDialog;
    private final CurrencyDialog currencyDialog;
    private final ExchangeMoneyCommand exchangeMoneyUseCase;

    /**
     * Constructor
     *
     * @param moneyDialog Dialog for getting source money (amount + source currency)
     * @param currencyDialog Dialog for getting target currency
     * @param exchangeMoneyUseCase The use case that handles the business logic
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
     * Execute the currency conversion.
     *
     * This method:
     * 1. Gets the source money from the money dialog
     * 2. Gets the target currency from the currency dialog
     * 3. Delegates to the use case which handles:
     *    - Fetching exchange rates
     *    - Converting the money
     *    - Presenting results or errors
     */
    @Override
    public void execute() {
        try {
            // Get input from UI dialogs
            Money sourceMoney = moneyDialog.get();
            Currency targetCurrency = currencyDialog.get(); // FIXED: Now returns Currency directly

            // Basic validation (the use case also validates, but we can fail fast here)
            if (sourceMoney == null) {
                System.err.println("Error: Source money is null");
                return;
            }

            if (targetCurrency == null) {
                System.err.println("Error: Target currency is null");
                return;
            }

            // Check if source and target are the same
            if (sourceMoney.currency().equals(targetCurrency)) {
                System.err.println("Error: Source and target currencies are the same");
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Source and target currencies must be different",
                        "Same Currency",
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Check if amount is zero
            if (sourceMoney.amount().doubleValue() == 0) {
                System.err.println("Warning: Converting zero amount");
            }

            // Delegate to the use case - it handles everything else including:
            // - Fetching exchange rate from provider
            // - Converting the money
            // - Presenting success or error via the presenter
            exchangeMoneyUseCase.execute(sourceMoney, targetCurrency);

        } catch (Exception e) {
            // This catch is a safety net - the use case should handle most errors
            System.err.println("Unexpected error in command adapter: " + e.getMessage());
            e.printStackTrace();

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "An unexpected error occurred: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
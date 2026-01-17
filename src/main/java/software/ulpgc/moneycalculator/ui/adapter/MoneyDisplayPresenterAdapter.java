package software.ulpgc.moneycalculator.ui.adapter;

import software.ulpgc.moneycalculator.application.port.output.MoneyPresenter;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.domain.model.Money;
import software.ulpgc.moneycalculator.ui.MoneyDisplay;
import software.ulpgc.moneycalculator.ui.swing.SwingMoneyDisplay;

import javax.swing.JOptionPane;

/**
 * Adapter that bridges the new MoneyPresenter interface with the old MoneyDisplay interface.
 * This allows you to keep your existing UI code while using the new architecture.
 *
 * FIXED: Now properly displays errors in the UI instead of just printing to console
 */
public class MoneyDisplayPresenterAdapter implements MoneyPresenter {
    private final MoneyDisplay moneyDisplay;

    public MoneyDisplayPresenterAdapter(MoneyDisplay moneyDisplay) {
        this.moneyDisplay = moneyDisplay;
    }

    @Override
    public void presentSuccess(Money convertedMoney, ExchangeRate exchangeRate) {
        // Simply delegate to the old MoneyDisplay interface
        moneyDisplay.show(convertedMoney);

        // Optionally log the exchange rate used
        System.out.println("Conversion successful using rate: " + exchangeRate);
    }

    @Override
    public void presentError(String errorMessage) {
        // FIXED: Now shows error in the UI instead of just console

        // First, log to console for debugging
        System.err.println("Conversion error: " + errorMessage);

        // Then show in UI
        // Check if the display is a SwingMoneyDisplay which has a showError method
        if (moneyDisplay instanceof SwingMoneyDisplay) {
            SwingMoneyDisplay swingDisplay = (SwingMoneyDisplay) moneyDisplay;
            swingDisplay.showError(errorMessage);
        } else {
            // Fallback to showing a dialog
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    "Conversion Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Output port for presenting money conversion results.
 * This interface is implemented by UI presenters.
 */
public interface MoneyPresenter {
    void presentSuccess(Money convertedMoney, ExchangeRate exchangeRate);
    void presentError(String errorMessage);
}

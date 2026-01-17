package software.ulpgc.moneycalculator.application.port.input;

import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Input port for exchanging money from one currency to another.
 * This is the use case interface that the UI layer depends on.
 */
public interface ExchangeMoneyCommand {
    void execute(Money sourceMoney, Currency targetCurrency);
}

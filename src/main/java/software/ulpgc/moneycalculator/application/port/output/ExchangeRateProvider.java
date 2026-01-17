package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;

/**
 * Output port for fetching exchange rates.
 * This interface is implemented by infrastructure adapters.
 */
public interface ExchangeRateProvider {
    ExchangeRate getRate(Currency from, Currency to) throws ExchangeRateUnavailableException;
}

package software.ulpgc.moneycalculator.application.port.input;

import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;

/**
 * Input port for querying available currencies.
 */
public interface GetCurrenciesQuery {
    List<Currency> getAllCurrencies();
    Currency findByCode(String code);
}

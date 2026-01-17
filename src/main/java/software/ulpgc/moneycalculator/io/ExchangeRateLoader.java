package software.ulpgc.moneycalculator.io;

import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;

/**
 * Backward-compatible implementation of the old ExchangeRateLoader interface.
 * Delegates to the new ExchangeRateProvider.
 * 
 * This allows you to keep code that uses the old ExchangeRateLoader interface
 * while migrating to the new architecture.
 */
public interface ExchangeRateLoader {
    ExchangeRate load(Currency from, Currency to);
    
    /**
     * Adapter that wraps the new ExchangeRateProvider to implement the old interface.
     */
    static ExchangeRateLoader fromProvider(ExchangeRateProvider provider) {
        return provider::getRate;
    }
}

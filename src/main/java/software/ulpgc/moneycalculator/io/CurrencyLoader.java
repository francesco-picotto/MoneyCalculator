package software.ulpgc.moneycalculator.io;

import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;

/**
 * Backward-compatible implementation of the old CurrencyLoader interface.
 * Delegates to the new CurrencyRepository.
 * 
 * This allows you to keep code that uses the old CurrencyLoader interface
 * while migrating to the new architecture.
 */
public interface CurrencyLoader {
    List<Currency> loadAll();
    
    /**
     * Adapter that wraps the new CurrencyRepository to implement the old interface.
     */
    static CurrencyLoader fromRepository(CurrencyRepository repository) {
        return repository::findAll;
    }
}

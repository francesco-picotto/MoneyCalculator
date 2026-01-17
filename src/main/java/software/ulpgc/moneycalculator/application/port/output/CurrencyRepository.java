package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Output port for accessing currency data.
 * This interface is implemented by infrastructure adapters.
 */
public interface CurrencyRepository {
    List<Currency> findAll();
    Optional<Currency> findByCode(String code);
}

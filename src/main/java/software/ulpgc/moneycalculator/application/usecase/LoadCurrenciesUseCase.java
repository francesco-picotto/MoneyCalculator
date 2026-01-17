package software.ulpgc.moneycalculator.application.usecase;

import software.ulpgc.moneycalculator.application.port.input.GetCurrenciesQuery;
import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.domain.exception.CurrencyNotFoundException;
import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;

/**
 * Use case for loading available currencies.
 */
public class LoadCurrenciesUseCase implements GetCurrenciesQuery {
    private final CurrencyRepository currencyRepository;

    public LoadCurrenciesUseCase(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public Currency findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        
        return currencyRepository.findByCode(code.trim().toUpperCase())
            .orElseThrow(() -> new CurrencyNotFoundException(
                "Currency not found: " + code
            ));
    }
}

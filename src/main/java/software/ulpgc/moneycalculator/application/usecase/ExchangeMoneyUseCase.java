package software.ulpgc.moneycalculator.application.usecase;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.application.port.output.MoneyPresenter;
import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Use case for exchanging money between currencies.
 * Orchestrates the business flow without containing business logic.
 */
public class ExchangeMoneyUseCase implements ExchangeMoneyCommand {
    private final ExchangeRateProvider exchangeRateProvider;
    private final MoneyPresenter moneyPresenter;

    public ExchangeMoneyUseCase(
        ExchangeRateProvider exchangeRateProvider,
        MoneyPresenter moneyPresenter
    ) {
        this.exchangeRateProvider = exchangeRateProvider;
        this.moneyPresenter = moneyPresenter;
    }

    @Override
    public void execute(Money sourceMoney, Currency targetCurrency) {
        try {
            validateInputs(sourceMoney, targetCurrency);
            
            ExchangeRate exchangeRate = exchangeRateProvider.getRate(
                sourceMoney.currency(), 
                targetCurrency
            );
            
            Money convertedMoney = sourceMoney.convert(exchangeRate);
            
            moneyPresenter.presentSuccess(convertedMoney, exchangeRate);
            
        } catch (ExchangeRateUnavailableException e) {
            moneyPresenter.presentError(
                "Unable to fetch exchange rate: " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            moneyPresenter.presentError(
                "Invalid input: " + e.getMessage()
            );
        } catch (Exception e) {
            moneyPresenter.presentError(
                "An unexpected error occurred: " + e.getMessage()
            );
        }
    }

    private void validateInputs(Money sourceMoney, Currency targetCurrency) {
        if (sourceMoney == null) {
            throw new IllegalArgumentException("Source money cannot be null");
        }
        if (targetCurrency == null) {
            throw new IllegalArgumentException("Target currency cannot be null");
        }
        if (sourceMoney.currency().equals(targetCurrency)) {
            throw new IllegalArgumentException(
                "Source and target currencies must be different"
            );
        }
    }
}

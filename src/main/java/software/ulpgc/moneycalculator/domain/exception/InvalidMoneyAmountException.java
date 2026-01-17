package software.ulpgc.moneycalculator.domain.exception;

public class InvalidMoneyAmountException extends RuntimeException {
    public InvalidMoneyAmountException(String message) {
        super(message);
    }

    public InvalidMoneyAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}

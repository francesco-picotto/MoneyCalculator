package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Exception thrown when HTTP operations fail.
 */
public class HttpClientException extends Exception {
    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

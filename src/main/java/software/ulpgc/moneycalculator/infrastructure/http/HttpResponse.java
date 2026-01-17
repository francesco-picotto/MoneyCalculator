package software.ulpgc.moneycalculator.infrastructure.http;

/**
 * Represents an HTTP response.
 */
public class HttpResponse {
    private final int statusCode;
    private final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return String.format("HttpResponse{statusCode=%d, bodyLength=%d}", 
            statusCode, body != null ? body.length() : 0);
    }
}

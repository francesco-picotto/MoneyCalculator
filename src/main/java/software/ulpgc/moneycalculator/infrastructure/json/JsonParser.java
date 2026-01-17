package software.ulpgc.moneycalculator.infrastructure.json;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for JSON parsing operations.
 * Allows swapping JSON libraries without affecting business logic.
 */
public interface JsonParser {
    Map<String, Object> parseObject(String json);
    List<Object> parseArray(String json);
    <T> T parse(String json, Class<T> type);
}

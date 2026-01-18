package software.ulpgc.moneycalculator.infrastructure.json;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for JSON parsing and serialization operations.
 *
 * <p>This interface provides a technology-agnostic API for working with JSON data,
 * allowing the application to swap JSON parsing libraries without affecting business
 * logic or infrastructure code. This follows the Dependency Inversion Principle by
 * depending on an abstraction rather than concrete JSON library implementations.</p>
 *
 * <p><strong>Design Benefits:</strong></p>
 * <ul>
 *   <li><strong>Library Independence:</strong> Switch between Gson, Jackson, org.json without code changes</li>
 *   <li><strong>Testability:</strong> Easy to mock for unit testing without library dependencies</li>
 *   <li><strong>Consistent API:</strong> Provides uniform interface across different parsing needs</li>
 *   <li><strong>Flexibility:</strong> Can add custom parsing logic or validation in implementations</li>
 * </ul>
 *
 * <p><strong>Supported Parsing Operations:</strong></p>
 * <ul>
 *   <li><strong>Object Parsing:</strong> Convert JSON to Map&lt;String, Object&gt; for dynamic access</li>
 *   <li><strong>Array Parsing:</strong> Convert JSON arrays to List&lt;Object&gt;</li>
 *   <li><strong>Type-Safe Parsing:</strong> Deserialize JSON directly to specific Java classes</li>
 * </ul>
 *
 * <p><strong>Available Implementations:</strong></p>
 * <ul>
 *   <li><strong>GsonJsonParser:</strong> Uses Google's Gson library (current default)</li>
 *   <li><strong>JacksonJsonParser:</strong> Uses FasterXML's Jackson library (future option)</li>
 *   <li><strong>OrgJsonParser:</strong> Uses org.json library (lightweight option)</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * JsonParser parser = new GsonJsonParser();
 *
 * // Parse JSON object
 * String json = "{\"name\": \"John\", \"age\": 30}";
 * Map<String, Object> data = parser.parseObject(json);
 * String name = (String) data.get("name");
 * Double age = (Double) data.get("age");
 *
 * // Parse JSON array
 * String arrayJson = "[\"USD\", \"EUR\", \"GBP\"]";
 * List<Object> currencies = parser.parseArray(arrayJson);
 *
 * // Type-safe parsing
 * String personJson = "{\"name\": \"Alice\", \"age\": 25}";
 * Person person = parser.parse(personJson, Person.class);
 * }</pre>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>Implementations should throw appropriate runtime exceptions (typically
 * {@code JsonParseException} or {@code RuntimeException}) when encountering:</p>
 * <ul>
 *   <li>Malformed JSON syntax</li>
 *   <li>Type mismatches during deserialization</li>
 *   <li>Null or empty input strings</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>Implementations should be thread-safe for concurrent parsing operations.
 * Most JSON libraries (Gson, Jackson) are thread-safe by default when using
 * their default configurations.</p>
 *
 * @see GsonJsonParser
 */
public interface JsonParser {

    /**
     * Parses a JSON object string into a Map structure.
     *
     * <p>This method is ideal for scenarios where you need dynamic access to JSON
     * fields without creating specific Java classes, such as when working with
     * external API responses where the structure may vary or is not fully known.</p>
     *
     * <p><strong>Return Type:</strong></p>
     * <p>Returns a {@code Map<String, Object>} where:</p>
     * <ul>
     *   <li>Keys are JSON property names (strings)</li>
     *   <li>Values are parsed JSON values with appropriate Java types:
     *       <ul>
     *         <li>JSON strings → {@code String}</li>
     *         <li>JSON numbers → {@code Double} (for decimals) or {@code Integer} (for integers)</li>
     *         <li>JSON booleans → {@code Boolean}</li>
     *         <li>JSON objects → {@code Map<String, Object>} (nested)</li>
     *         <li>JSON arrays → {@code List<Object>} (nested)</li>
     *         <li>JSON null → {@code null}</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * String json = """
     *     {
     *       "result": "success",
     *       "conversion_rate": 0.85,
     *       "valid": true,
     *       "metadata": {
     *         "date": "2025-01-18"
     *       }
     *     }
     *     """;
     *
     * Map<String, Object> data = parser.parseObject(json);
     *
     * String result = (String) data.get("result");
     * Double rate = (Double) data.get("conversion_rate");
     * Boolean valid = (Boolean) data.get("valid");
     *
     * @SuppressWarnings("unchecked")
     * Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
     * String date = (String) metadata.get("date");
     * }</pre>
     *
     * <p><strong>Type Casting:</strong></p>
     * <p>Note that you'll need to cast values to their expected types. Consider
     * using proper null checks and type validation to avoid {@code ClassCastException}:</p>
     * <pre>{@code
     * Object rateObj = data.get("conversion_rate");
     * if (rateObj instanceof Number) {
     *     double rate = ((Number) rateObj).doubleValue();
     * }
     * }</pre>
     *
     * @param json the JSON object string to parse; must represent a valid JSON object
     * @return a Map representation of the JSON object with string keys and object values
     * @throws RuntimeException if the JSON is malformed or does not represent an object
     * @throws NullPointerException if json is null (implementation-dependent)
     */
    Map<String, Object> parseObject(String json);

    /**
     * Parses a JSON array string into a List structure.
     *
     * <p>This method converts JSON arrays into Java lists, useful for processing
     * collections of items without needing to define specific collection classes.</p>
     *
     * <p><strong>Return Type:</strong></p>
     * <p>Returns a {@code List<Object>} where each element is a parsed JSON value:</p>
     * <ul>
     *   <li>JSON strings → {@code String}</li>
     *   <li>JSON numbers → {@code Double} or {@code Integer}</li>
     *   <li>JSON booleans → {@code Boolean}</li>
     *   <li>JSON objects → {@code Map<String, Object>}</li>
     *   <li>JSON arrays → {@code List<Object>} (nested)</li>
     *   <li>JSON null → {@code null}</li>
     * </ul>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Simple array
     * String json = "[\"USD\", \"EUR\", \"GBP\"]";
     * List<Object> currencies = parser.parseArray(json);
     * for (Object curr : currencies) {
     *     System.out.println((String) curr);
     * }
     *
     * // Array of objects
     * String complexJson = """
     *     [
     *       {"code": "USD", "name": "US Dollar"},
     *       {"code": "EUR", "name": "Euro"}
     *     ]
     *     """;
     * List<Object> items = parser.parseArray(complexJson);
     * for (Object item : items) {
     *     @SuppressWarnings("unchecked")
     *     Map<String, Object> currency = (Map<String, Object>) item;
     *     System.out.println(currency.get("code"));
     * }
     * }</pre>
     *
     * <p><strong>Homogeneous vs Heterogeneous Arrays:</strong></p>
     * <p>JSON arrays can contain mixed types. Handle this appropriately:</p>
     * <pre>{@code
     * String mixedJson = "[\"text\", 42, true, null]";
     * List<Object> mixed = parser.parseArray(mixedJson);
     * // Need to check types before casting
     * }</pre>
     *
     * @param json the JSON array string to parse; must represent a valid JSON array
     * @return a List representation of the JSON array with object elements
     * @throws RuntimeException if the JSON is malformed or does not represent an array
     * @throws NullPointerException if json is null (implementation-dependent)
     */
    List<Object> parseArray(String json);

    /**
     * Parses a JSON string directly into a specific Java type (type-safe deserialization).
     *
     * <p>This method provides type-safe JSON parsing by deserializing directly into
     * Java objects of the specified class. This is the preferred approach when you
     * have defined Java classes that match your JSON structure, as it eliminates
     * the need for manual casting and provides compile-time type safety.</p>
     *
     * <p><strong>Requirements for Target Class:</strong></p>
     * <ul>
     *   <li>Must have a no-argument constructor (public or package-private)</li>
     *   <li>Fields should match JSON property names (or use annotations for mapping)</li>
     *   <li>Should follow JavaBean conventions for complex objects</li>
     *   <li>Can use primitives, wrappers, Strings, collections, and nested objects</li>
     * </ul>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Define a data class
     * public class ExchangeRateResponse {
     *     private String result;
     *     private double conversion_rate;
     *
     *     // Getters and setters
     * }
     *
     * // Parse JSON into the class
     * String json = "{\"result\": \"success\", \"conversion_rate\": 0.85}";
     * ExchangeRateResponse response = parser.parse(json, ExchangeRateResponse.class);
     *
     * // Access with type safety
     * if ("success".equals(response.getResult())) {
     *     double rate = response.getConversionRate();
     * }
     * }</pre>
     *
     * <p><strong>Collection Types:</strong></p>
     * <p>For parsing into generic collection types (List&lt;T&gt;, Map&lt;K,V&gt;),
     * implementations may require additional methods or use of TypeToken/TypeReference
     * due to Java's type erasure. In such cases, prefer the non-generic
     * {@link #parseArray(String)} and {@link #parseObject(String)} methods.</p>
     *
     * <p><strong>Null Handling:</strong></p>
     * <p>JSON null values are typically mapped to null Java field values. Ensure
     * your target class can handle null fields appropriately.</p>
     *
     * @param <T> the type to deserialize into
     * @param json the JSON string to parse; must be valid JSON
     * @param type the Class object representing the target type
     * @return an instance of type T populated with data from the JSON
     * @throws RuntimeException if JSON is malformed or cannot be mapped to the target type
     * @throws NullPointerException if json or type is null (implementation-dependent)
     */
    <T> T parse(String json, Class<T> type);
}
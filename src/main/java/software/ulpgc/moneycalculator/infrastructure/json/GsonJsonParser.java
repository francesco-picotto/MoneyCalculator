package software.ulpgc.moneycalculator.infrastructure.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * JSON parser implementation using Google's Gson library.
 *
 * <p>This class implements the {@link JsonParser} interface using Gson, a popular
 * and efficient Java library for JSON serialization and deserialization. Gson provides
 * a good balance of performance, ease of use, and feature completeness.</p>
 *
 * <p><strong>Why Gson?</strong></p>
 * <ul>
 *   <li><strong>Simplicity:</strong> Clean API with sensible defaults</li>
 *   <li><strong>No Annotations Required:</strong> Works with POJOs without modification</li>
 *   <li><strong>Performance:</strong> Fast parsing and serialization</li>
 *   <li><strong>Null Handling:</strong> Gracefully handles null values</li>
 *   <li><strong>Flexible:</strong> Supports custom serializers and deserializers</li>
 *   <li><strong>Mature:</strong> Well-tested and widely used in production</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe. The underlying {@link Gson} instance is stateless
 * and can be safely shared across multiple threads. All parsing operations are
 * concurrent-safe without requiring synchronization.</p>
 *
 * <p><strong>Gson Configuration:</strong></p>
 * <p>This implementation uses Gson's default configuration, which:</p>
 * <ul>
 *   <li>Does not serialize null fields</li>
 *   <li>Uses standard Java naming conventions</li>
 *   <li>Handles primitives, strings, collections, and nested objects</li>
 *   <li>Supports circular reference detection</li>
 * </ul>
 *
 * <p>For custom configuration (date formats, naming policies, null handling), consider
 * using {@code GsonBuilder} in the constructor.</p>
 *
 * <p><strong>Type Handling:</strong></p>
 * <p>Gson uses {@link TypeToken} to preserve generic type information at runtime,
 * working around Java's type erasure. This allows proper parsing of generic collections
 * like {@code Map<String, Object>} and {@code List<Object>}.</p>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Parsing Speed:</strong> Very fast, comparable to Jackson</li>
 *   <li><strong>Memory Usage:</strong> Low overhead, no reflection caching by default</li>
 *   <li><strong>Startup Time:</strong> Minimal initialization cost</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * JsonParser parser = new GsonJsonParser();
 *
 * // Parse complex nested JSON
 * String json = """
 *     {
 *       "result": "success",
 *       "rates": {
 *         "EUR": 0.85,
 *         "GBP": 0.73
 *       },
 *       "currencies": ["USD", "EUR", "GBP"]
 *     }
 *     """;
 *
 * Map<String, Object> data = parser.parseObject(json);
 * @SuppressWarnings("unchecked")
 * Map<String, Object> rates = (Map<String, Object>) data.get("rates");
 * Double eurRate = (Double) rates.get("EUR");
 * }</pre>
 *
 * <p><strong>Alternative Libraries:</strong></p>
 * <p>If you need to switch JSON libraries, implement {@link JsonParser} with:</p>
 * <ul>
 *   <li><strong>Jackson:</strong> More features, slightly faster, more complex API</li>
 *   <li><strong>org.json:</strong> Lightweight, no external dependencies, less features</li>
 *   <li><strong>JSON-B:</strong> Jakarta EE standard, good for enterprise applications</li>
 * </ul>
 *
 * @see JsonParser
 * @see Gson
 */
public class GsonJsonParser implements JsonParser {

    /**
     * The Gson instance used for all JSON parsing operations.
     *
     * <p>This instance is created once during construction and reused for all parsing
     * operations. Gson instances are thread-safe and stateless, making them suitable
     * for reuse in multi-threaded environments.</p>
     *
     * <p>Uses default Gson configuration which is appropriate for most use cases.
     * For custom configuration, this could be replaced with a GsonBuilder-constructed
     * instance.</p>
     */
    private final Gson gson;

    /**
     * Constructs a new GsonJsonParser with default Gson configuration.
     *
     * <p>Creates a standard {@link Gson} instance with default settings. This
     * configuration is suitable for most JSON parsing needs without requiring
     * additional customization.</p>
     *
     * <p><strong>Default Behavior:</strong></p>
     * <ul>
     *   <li>Uses standard Java field names (no name translation)</li>
     *   <li>Does not serialize null values</li>
     *   <li>Handles standard Java types automatically</li>
     *   <li>No pretty printing (compact JSON output)</li>
     * </ul>
     *
     * <p><strong>Custom Configuration Example:</strong></p>
     * <p>If you need custom Gson configuration, modify this constructor:</p>
     * <pre>{@code
     * public GsonJsonParser() {
     *     this.gson = new GsonBuilder()
     *         .setDateFormat("yyyy-MM-dd")
     *         .setPrettyPrinting()
     *         .serializeNulls()
     *         .create();
     * }
     * }</pre>
     */
    public GsonJsonParser() {
        this.gson = new Gson();
    }

    /**
     * Parses a JSON object string into a Map structure using Gson.
     *
     * <p>This method uses Gson's {@link TypeToken} to preserve generic type information,
     * allowing proper deserialization into {@code Map<String, Object>}. The TypeToken
     * technique is necessary because Java's type erasure would otherwise prevent Gson
     * from knowing the exact generic types to use.</p>
     *
     * <p><strong>Gson's Type Mapping:</strong></p>
     * <ul>
     *   <li>JSON strings → {@code String}</li>
     *   <li>JSON numbers (with decimals) → {@code Double}</li>
     *   <li>JSON numbers (integers) → {@code Double} (Gson defaults all numbers to Double)</li>
     *   <li>JSON booleans → {@code Boolean}</li>
     *   <li>JSON objects → {@code Map<String, Object>}</li>
     *   <li>JSON arrays → {@code List<Object>}</li>
     *   <li>JSON null → {@code null}</li>
     * </ul>
     *
     * <p><strong>Important Note on Numbers:</strong></p>
     * <p>Gson parses all JSON numbers as {@code Double} by default, even integers.
     * If you need to distinguish between integer and decimal values, you'll need to
     * check the value or use custom deserialization.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * String json = "{\"name\": \"Alice\", \"age\": 30, \"active\": true}";
     * Map<String, Object> data = parser.parseObject(json);
     *
     * String name = (String) data.get("name");
     * Double age = (Double) data.get("age");  // Note: parsed as Double, not Integer
     * Boolean active = (Boolean) data.get("active");
     * }</pre>
     *
     * @param json the JSON object string to parse; must be valid JSON object syntax
     * @return a Map with string keys and object values representing the JSON structure
     * @throws com.google.gson.JsonSyntaxException if the JSON is malformed
     * @throws com.google.gson.JsonIOException if there's an I/O error during parsing
     */
    @Override
    public Map<String, Object> parseObject(String json) {
        // Use TypeToken to preserve generic type information at runtime
        // This allows Gson to properly deserialize into Map<String, Object>
        return gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
    }

    /**
     * Parses a JSON array string into a List structure using Gson.
     *
     * <p>Similar to {@link #parseObject(String)}, this method uses {@link TypeToken}
     * to preserve generic type information for proper deserialization of the array
     * into {@code List<Object>}.</p>
     *
     * <p><strong>Element Type Mapping:</strong></p>
     * <p>Each element in the returned list corresponds to a JSON value with the
     * same type mappings as described in {@link #parseObject(String)}.</p>
     *
     * <p><strong>Handling Heterogeneous Arrays:</strong></p>
     * <p>JSON arrays can contain mixed types. You'll need to check element types
     * at runtime:</p>
     * <pre>{@code
     * String json = "[\"text\", 42.5, true, null, {\"key\": \"value\"}]";
     * List<Object> items = parser.parseArray(json);
     *
     * for (Object item : items) {
     *     if (item instanceof String) {
     *         System.out.println("String: " + item);
     *     } else if (item instanceof Double) {
     *         System.out.println("Number: " + item);
     *     } else if (item instanceof Boolean) {
     *         System.out.println("Boolean: " + item);
     *     } else if (item instanceof Map) {
     *         System.out.println("Object: " + item);
     *     } else if (item == null) {
     *         System.out.println("Null value");
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Nested Arrays:</strong></p>
     * <p>Nested arrays are parsed recursively:</p>
     * <pre>{@code
     * String json = "[[1, 2], [3, 4], [5, 6]]";
     * List<Object> outer = parser.parseArray(json);
     *
     * for (Object item : outer) {
     *     @SuppressWarnings("unchecked")
     *     List<Object> inner = (List<Object>) item;
     *     // Process inner array
     * }
     * }</pre>
     *
     * @param json the JSON array string to parse; must be valid JSON array syntax
     * @return a List with object elements representing the JSON array
     * @throws com.google.gson.JsonSyntaxException if the JSON is malformed
     * @throws com.google.gson.JsonIOException if there's an I/O error during parsing
     */
    @Override
    public List<Object> parseArray(String json) {
        // Use TypeToken to preserve generic type information at runtime
        // This allows Gson to properly deserialize into List<Object>
        return gson.fromJson(json, new TypeToken<List<Object>>(){}.getType());
    }

    /**
     * Parses a JSON string directly into a specific Java type using Gson.
     *
     * <p>This method provides type-safe deserialization by directly converting JSON
     * into instances of the specified class. Gson automatically maps JSON properties
     * to Java fields based on field names.</p>
     *
     * <p><strong>Field Mapping Rules:</strong></p>
     * <ul>
     *   <li>JSON property names must match Java field names exactly (case-sensitive)</li>
     *   <li>Use {@code @SerializedName} annotation for different names</li>
     *   <li>Fields not present in JSON are set to their default values (null, 0, false)</li>
     *   <li>Extra JSON properties that don't match fields are ignored</li>
     * </ul>
     *
     * <p><strong>Requirements for Target Class:</strong></p>
     * <ul>
     *   <li>Must have a no-argument constructor (can be private)</li>
     *   <li>Fields can be private (Gson uses reflection)</li>
     *   <li>Getters/setters are not required</li>
     *   <li>Can use final fields (set via reflection during construction)</li>
     * </ul>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Define a data class
     * public class Person {
     *     private String name;
     *     private int age;
     *     private boolean active;
     *
     *     // No-arg constructor (can be private)
     *     private Person() {}
     *
     *     // Getters (optional for Gson, but useful for access)
     *     public String getName() { return name; }
     *     public int getAge() { return age; }
     *     public boolean isActive() { return active; }
     * }
     *
     * // Parse JSON into Person
     * String json = "{\"name\": \"Alice\", \"age\": 30, \"active\": true}";
     * Person person = parser.parse(json, Person.class);
     *
     * // Access with type safety
     * System.out.println(person.getName()); // "Alice"
     * System.out.println(person.getAge());  // 30
     * }</pre>
     *
     * <p><strong>Customization with Annotations:</strong></p>
     * <pre>{@code
     * public class ExchangeRateResponse {
     *     @SerializedName("conversion_rate")
     *     private double rate;  // Maps JSON "conversion_rate" to field "rate"
     *
     *     private String result;  // Maps to JSON "result" directly
     * }
     * }</pre>
     *
     * <p><strong>Handling Nested Objects:</strong></p>
     * <p>Gson automatically handles nested object structures:</p>
     * <pre>{@code
     * public class Response {
     *     private String result;
     *     private Metadata metadata;  // Nested object
     *
     *     public static class Metadata {
     *         private String date;
     *         private String source;
     *     }
     * }
     *
     * String json = """
     *     {
     *       "result": "success",
     *       "metadata": {
     *         "date": "2025-01-18",
     *         "source": "API"
     *       }
     *     }
     *     """;
     * Response response = parser.parse(json, Response.class);
     * }</pre>
     *
     * @param <T> the type to deserialize into
     * @param json the JSON string to parse; must be valid JSON
     * @param type the Class object representing the target type
     * @return a new instance of type T populated with data from the JSON
     * @throws com.google.gson.JsonSyntaxException if JSON is malformed or incompatible with type
     * @throws com.google.gson.JsonIOException if there's an I/O error during parsing
     */
    @Override
    public <T> T parse(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
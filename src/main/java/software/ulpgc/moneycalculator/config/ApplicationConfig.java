package software.ulpgc.moneycalculator.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for loading and accessing application properties.
 *
 * <p>This class provides a centralized way to manage application configuration
 * settings loaded from properties files. It follows the Single Responsibility
 * Principle by focusing solely on configuration management and provides type-safe
 * accessors for common property types.</p>
 *
 * <p><strong>Typical Usage:</strong></p>
 * <pre>{@code
 * ApplicationConfig config = new ApplicationConfig("application.properties");
 * String apiKey = config.get("api.exchangerate.key");
 * int timeout = config.getInt("http.timeout.seconds", 10);
 * boolean debugMode = config.getBoolean("app.debug", false);
 * }</pre>
 *
 * <p><strong>Configuration File Location:</strong></p>
 * <p>Properties files should be placed in the classpath (typically in
 * {@code src/main/resources}). The class loader will search for files in the
 * standard classpath locations.</p>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>This class uses fail-fast error handling - if the configuration file cannot
 * be loaded or required properties are missing, runtime exceptions are thrown
 * during initialization rather than allowing the application to run with
 * incomplete configuration.</p>
 */
public class ApplicationConfig {

    /**
     * The loaded properties from the configuration file.
     * Contains all key-value pairs read from the properties file.
     */
    private final Properties properties;

    /**
     * Constructs a new ApplicationConfig by loading properties from the specified file.
     *
     * <p>The configuration file is loaded from the classpath using the class loader.
     * This constructor will fail immediately if the file cannot be found or read,
     * ensuring that configuration errors are caught early in the application lifecycle.</p>
     *
     * @param configFile the name of the properties file to load (e.g., "application.properties");
     *                  the file should be in the classpath
     * @throws IllegalArgumentException if the configuration file is not found in the classpath
     * @throws RuntimeException if the file exists but cannot be read due to I/O errors
     */
    public ApplicationConfig(String configFile) {
        this.properties = loadProperties(configFile);
    }

    /**
     * Loads properties from a configuration file in the classpath.
     *
     * <p>This method uses the class loader to locate and read the properties file.
     * The file is automatically closed after reading, even if an error occurs,
     * thanks to the try-with-resources statement.</p>
     *
     * <p><strong>File Format:</strong></p>
     * <p>The file should follow the standard Java properties format:</p>
     * <pre>
     * # Comments start with # or !
     * property.name=value
     * another.property=another value
     * </pre>
     *
     * @param configFile the name of the properties file to load
     * @return a Properties object containing all loaded key-value pairs
     * @throws IllegalArgumentException if the configuration file is not found
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    private Properties loadProperties(String configFile) {
        Properties props = new Properties();

        // Try to load the file from the classpath using try-with-resources
        // for automatic resource management
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(configFile)) {

            // Verify that the file was found in the classpath
            if (input == null) {
                throw new IllegalArgumentException(
                        "Config file not found: " + configFile
                );
            }

            // Load the properties from the input stream
            props.load(input);

        } catch (IOException e) {
            // Wrap I/O exceptions in a runtime exception to simplify error handling
            throw new RuntimeException("Failed to load configuration", e);
        }

        return props;
    }

    /**
     * Retrieves a configuration property value as a string.
     *
     * <p>This is the basic property accessor that all other typed accessors build upon.
     * If the property key does not exist, this method returns {@code null}.</p>
     *
     * @param key the property key to look up
     * @return the property value as a string, or {@code null} if the key doesn't exist
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves a configuration property value with a default fallback.
     *
     * <p>This method is useful when a configuration property is optional and you
     * want to provide a sensible default value if it's not specified.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Use port 8080 if not configured
     * String port = config.get("server.port", "8080");
     * }</pre>
     *
     * @param key the property key to look up
     * @param defaultValue the value to return if the key doesn't exist
     * @return the property value if it exists, otherwise the default value
     */
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Retrieves a configuration property value as an integer.
     *
     * <p>This method parses the property value as an integer. If the property
     * does not exist or cannot be parsed as a valid integer, a runtime exception
     * will be thrown.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * int maxRetries = config.getInt("api.max.retries");
     * }</pre>
     *
     * @param key the property key to look up
     * @return the property value parsed as an integer
     * @throws NumberFormatException if the property value is not a valid integer
     * @throws NullPointerException if the property doesn't exist (attempting to parse null)
     */
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Retrieves a configuration property value as an integer with a default fallback.
     *
     * <p>This method provides safe integer parsing with a default value if the
     * property is not configured. This is the recommended way to read optional
     * integer properties.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Use 30 second timeout if not configured
     * int timeout = config.getInt("http.timeout.seconds", 30);
     * }</pre>
     *
     * @param key the property key to look up
     * @param defaultValue the value to return if the key doesn't exist
     * @return the property value parsed as an integer, or the default value if
     *         the key doesn't exist
     * @throws NumberFormatException if the property exists but is not a valid integer
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Retrieves a configuration property value as a boolean.
     *
     * <p>This method parses the property value as a boolean using standard Java
     * boolean parsing rules (case-insensitive "true" returns {@code true}, all
     * other values return {@code false}).</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * boolean enableCache = config.getBoolean("cache.enabled");
     * }</pre>
     *
     * @param key the property key to look up
     * @return {@code true} if the property value is "true" (case-insensitive),
     *         {@code false} otherwise (including if the property doesn't exist)
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Retrieves a configuration property value as a boolean with a default fallback.
     *
     * <p>This method provides safe boolean parsing with a default value if the
     * property is not configured. This is the recommended way to read optional
     * boolean properties.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Enable debug mode by default in development
     * boolean debug = config.getBoolean("app.debug.mode", true);
     * }</pre>
     *
     * @param key the property key to look up
     * @param defaultValue the value to return if the key doesn't exist
     * @return the property value parsed as a boolean, or the default value if
     *         the key doesn't exist
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}
package software.ulpgc.moneycalculator.infrastructure.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * Implementation of JsonParser using Google's Gson library.
 */
public class GsonJsonParser implements JsonParser {
    private final Gson gson;

    public GsonJsonParser() {
        this.gson = new Gson();
    }

    @Override
    public Map<String, Object> parseObject(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
    }

    @Override
    public List<Object> parseArray(String json) {
        return gson.fromJson(json, new TypeToken<List<Object>>(){}.getType());
    }

    @Override
    public <T> T parse(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}

package cumulus_message_adapter.message_parser;

import java.util.Map;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Utilities for converting JSON to string/map
 */
public class JsonUtilities {

  /**
   * Private no argument constructor for utility class
   */
  private JsonUtilities() {}

  /**
   * Convert json string to Map object
   *
   * @param jsonString The json string
   * @return The converted Map object
   */
  public static Map<String, Object> convertJsonStringToMap(String jsonString) {
    Type mapObjectType = new TypeToken<Map<String, Object>>() {}.getType();
    return new Gson().fromJson(jsonString, mapObjectType);
  }

  /**
   * Convert Map object to json string
   *
   * @param map The Map object
   * @return The converted json string
   */
  public static String convertMapToJsonString(Map<String, Object> map) {
    return new Gson().toJson(map);
  }

}

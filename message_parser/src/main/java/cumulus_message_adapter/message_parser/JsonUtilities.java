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
   * Convert json string to Map object
   *
   * @param jsonString The json string
   * @return The converted Map object
   */
  public static Map<String,Object> convertJsonStringToMap(String jsonString) {
      Gson gson = new Gson();
      Type mapObjectType = new TypeToken<Map<String, Object>>() {}.getType();
      return gson.fromJson(jsonString, mapObjectType);
  }

  /**
   * Convert Map object to json string
   *
   * @param map The Map object
   * @return The converted json string
   */
  public static String convertMapToJsonString(Map<String,Object> map) {
      Gson gson = new Gson();
      return gson.toJson(map);
  }

}

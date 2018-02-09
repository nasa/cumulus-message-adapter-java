package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Utilities for logging adapter messages
 */
public class AdapterLogger
{
    static final Logger _logger = LogManager.getLogger();

    static final String LEVEL_ERROR = "error";

    /**
     * Use the keys to traverse through a JSON object to find a nested object
     * 
     * @param json - the json as a String
     * @param keys - stack of keys
     * @return the found nested obect in string form, null if cannot be found
     */
    private static String GetNestedObject(String json, Stack<String> keys)
    {
        Gson gson = new Gson();
        
        Map map = gson.fromJson(json, Map.class);

        if(map != null && keys.isEmpty() == false)
        {
            String key = keys.pop();
            Object nestedJson = map.get(key);

            if(nestedJson == null)
            {
                return null;
            }

            if(keys.isEmpty())
            {
                return nestedJson.toString();
            }
            
            return GetNestedObject(nestedJson.toString(), keys);
        }

        return null;
    }

    /**
     * Generate the log message by extracting fields from the AWS Lambda context and the event
     * 
     * @param context - AWS Lambda context
     * @param eventString - JSON string of the event 
     * @param level - log level
     * @param message - log message
     * @return message string to log
     */
    private static String GenerateMessage(Context context, String eventString, String level, String message)
    {
        Gson gson = new Gson();

        Date date = new Date();

        Stack<String> executionNameKeys = new Stack<String>();
        executionNameKeys.push("execution_name");
        executionNameKeys.push("cumulus_meta");
        String executionName = GetNestedObject(eventString, executionNameKeys);

        Map<String, String> map = new HashMap<String, String>();
        map.put("executions", executionName);
        map.put("level", level);
        map.put("sender", context != null ? context.getFunctionName() : null);
        map.put("message", message);
        map.put("timestamp", new Timestamp(date.getTime()).toString());

        return gson.toJson(map);
    }

    /**
     * Log an error
     * 
     * @param context - AWS Lambda context
     * @param eventString - JSON string of the event 
     * @param message - log message
     */
    public static void LogError(Context context, String event, String message)
    {
        _logger.error(GenerateMessage(context, event, LEVEL_ERROR, message));
    }

}
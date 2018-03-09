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
    static final String LEVEL_FATAL = "fatal";
    static final String LEVEL_WARNING = "warn";
    static final String LEVEL_INFO = "info";
    static final String LEVEL_DEBUG = "debug";
    static final String LEVEL_TRACE = "trace";

    static String _executions;
    static String _sender;

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
            
            return GetNestedObject(gson.toJson(nestedJson), keys);
        }

        return null;
    }

    /**
     * Generate the log message by extracting fields from the AWS Lambda context and the event
     * 
     * @param level - log level
     * @param message - log message
     * @return message string to log
     */
    private static String GenerateMessage(String level, String message)
    {
        Gson gson = new Gson();

        Date date = new Date();

        Map<String, String> map = new HashMap<String, String>();
        map.put("executions", _executions);
        map.put("level", level);
        map.put("sender", _sender);
        map.put("message", message);
        map.put("timestamp", new Timestamp(date.getTime()).toString());

        return gson.toJson(map);
    }

    /**
     * Get the executions from the original event and set for use in logs
     * 
     * @param event - the original event passed into Lambda
     */
    static void SetExecutions(String event)
    {
        Stack<String> executionNameKeys = new Stack<String>();
        executionNameKeys.push("execution_name");
        executionNameKeys.push("cumulus_meta");
        _executions = GetNestedObject(event, executionNameKeys);
    }

    /**
     * Initialize the logger with info from the context and original event
     * 
     * @param context - AWS Lambda context
     * @param event - the original event passed into Lambda
     */
    static void InitializeLogger(Context context, String event)
    {
        // Initialize executions from event 
        SetExecutions(event);

        // Initialize sender from context
        _sender = (context != null ? context.getFunctionName() : null);
    }

    /**
     * Log an error
     * 
     * @param message - log message
     */
    public static void LogError(String message)
    {
        _logger.error(GenerateMessage(LEVEL_ERROR, message));
    }

    /**
     * Log a fatal error
     * 
     * @param message - log message
     */
    public static void LogFatal(String message)
    {
        _logger.fatal(GenerateMessage(LEVEL_FATAL, message));
    }

    /**
     * Log a warning
     * 
     * @param message - log message
     */
    public static void LogWarning(String message)
    {
        _logger.warn(GenerateMessage(LEVEL_WARNING, message));
    }

    /**
     * Log an info message
     * 
     * @param message - log message
     */
    public static void LogInfo(String message)
    {
        _logger.info(GenerateMessage(LEVEL_INFO, message));
    }

    /**
     * Log a debug message
     * 
     * @param message - log message
     */
    public static void LogDebug(String message)
    {
        _logger.debug(GenerateMessage(LEVEL_DEBUG, message));
    }
    
    /**
     * Log a trace message
     * 
     * @param message - log message
     */
    public static void LogTrace(String message)
    {
        _logger.trace(GenerateMessage(LEVEL_TRACE, message));
    }
}
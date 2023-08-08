package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.time.Instant;

/**
 * Utilities for logging adapter messages
 */
public class AdapterLogger {
    static final Logger _logger = LogManager.getLogger();

    static final String LEVEL_ERROR = "error";
    static final String LEVEL_FATAL = "fatal";
    static final String LEVEL_WARNING = "warn";
    static final String LEVEL_INFO = "info";
    static final String LEVEL_DEBUG = "debug";
    static final String LEVEL_TRACE = "trace";

    static String _asyncOperationId;
    static String _executions;
    static String _granules;
    static String _parentArn;
    static String _sender;
    static String _stackName;
    static String _version;

    /**
     * Use the keys to traverse through a JSON object to find a nested object
     *
     * @param json - the json as a String
     * @param keys - stack of keys
     * @return the found nested obect in string form, null if cannot be found
     */
    private static String GetNestedObject(String json, Stack<String> keys) {
        Gson gson = new Gson();

        Map<String, Object> map = JsonUtils.toMap(json);

        if (map != null && !keys.isEmpty()) {
            String key = keys.pop();
            Object nestedJson = map.get(key);

            if (nestedJson == null) {
                return null;
            }

            if (keys.isEmpty()) {
                return (nestedJson instanceof String) ? nestedJson.toString() : gson.toJson(nestedJson);
            }

            return GetNestedObject(gson.toJson(nestedJson), keys);
        }

        return null;
    }

    /**
     * Convert the json path string to Stack object
     *
     * @param jsonPath the json path e.g. cma.event.payload.granules
     * @return the Stack object with json keys in reverse order
     */
    private static Stack<String> jsonPathToStack(String jsonPath) {
        String[] paths = jsonPath.split("\\.");
        Stack<String> keys = new Stack<String>();
        // add json keys to the stack backwards
        for (int i = paths.length - 1; i >= 0; i--) {
            keys.push(paths[i]);
        }
        return keys;
    }

    /**
     * Generate the log message by extracting fields from the AWS Lambda context and
     * the event
     *
     * @param level   - log level
     * @param message - log message
     * @return message string to log
     */
    private static String GenerateMessage(String level, String message) {
        Gson gson = new Gson();

        Map<String, String> map = new HashMap<String, String>();
        map.put("asyncOperationId", _asyncOperationId);
        map.put("executions", _executions);
        map.put("granules", _granules);
        map.put("parentArn", _parentArn);
        map.put("sender", _sender);
        map.put("stackName", _stackName);
        map.put("version", _version);
        map.put("level", level);
        map.put("message", message);
        map.put("timestamp", Instant.now().toString());

        return gson.toJson(map);
    }

    /**
     * Get the asyncOperationId from execution message and set for use in logs
     *
     * @param event - the original event passed into Lambda
     */
    static void SetAsyncOperationId(String event) {
        _asyncOperationId = null;
        final String[] paths = { "cumulus_meta.asyncOperationId", "cma.event.cumulus_meta.asyncOperationId" };

        for (String path : paths) {
            String value = GetNestedObject(event, jsonPathToStack(path));
            if (value != null) {
                _asyncOperationId = value;
                break;
            }
        }
    }

    /**
     * Get the executions from the original event and set for use in logs
     *
     * @param event - the original event passed into Lambda
     */
    static void SetExecutions(String event) {
        _executions = null;
        final String[] paths = { "cumulus_meta.execution_name", "cma.event.cumulus_meta.execution_name" };

        for (String path : paths) {
            String value = GetNestedObject(event, jsonPathToStack(path));
            if (value != null) {
                _executions = value;
                break;
            }
        }
    }

    /**
     * Get the granules (granuleId) from the original event and set for use in logs
     *
     * @param event - the original event passed into Lambda
     */
    static void SetGranules(String event) {
        _granules = null;
        final String[] paths = { "payload.granules", "meta.input_granules", "cma.event.payload.granules",
                "cma.event.meta.input_granules" };

        Gson gson = new Gson();
        for (String path : paths) {
            String granulesString = GetNestedObject(event, jsonPathToStack(path));

            if (granulesString != null) {
                // get granuleId from each of the granules in the list
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> granules = gson.fromJson(granulesString,
                        listType);

                if (granules != null && !granules.isEmpty()) {
                    ArrayList<String> granuleIds = new ArrayList<String>();
                    for (Map<String, Object> granule : granules) {
                        granuleIds.add(granule.get("granuleId").toString());
                    }
                    _granules = gson.toJson(granuleIds);
                    break;
                }
            }
        }
    }

    /**
     * Get the parent arn from execution message and set for use in logs
     *
     * @param event - the original event passed into Lambda
     */
    static void SetParentArn(String event) {
        _parentArn = null;
        final String[] paths = { "cumulus_meta.parentExecutionArn", "cma.event.cumulus_meta.parentExecutionArn" };

        for (String path : paths) {
            String value = GetNestedObject(event, jsonPathToStack(path));
            if (value != null) {
                _parentArn = value;
                break;
            }
        }
    }

    /**
     * Get the stackname from the meta of the event and set for use in logs
     *
     * @param event - the original event passed into Lambda
     */
    static void SetStackName(String event) {
        _stackName = null;
        final String[] paths = { "meta.stack", "cma.event.meta.stack" };

        for (String path : paths) {
            String value = GetNestedObject(event, jsonPathToStack(path));
            if (value != null) {
                _stackName = value;
                break;
            }
        }
    }

    /**
     * Initialize the logger with info from the context and original event
     *
     * @param context - AWS Lambda context
     * @param event   - the original event passed into Lambda
     */
    public static void InitializeLogger(Context context, String event) {
        // Initialize log parameters from event
        SetAsyncOperationId(event);
        SetExecutions(event);
        SetGranules(event);
        SetParentArn(event);
        SetStackName(event);

        // Initialize sender from context
        _sender = (context != null ? context.getFunctionName() : null);
        _version = (context != null ? context.getFunctionVersion() : null);
    }

    /**
     * Log an error
     *
     * @param message - log message
     */
    public static void LogError(String message) {
        _logger.error(GenerateMessage(LEVEL_ERROR, message));
    }

    /**
     * Log a fatal error
     *
     * @param message - log message
     */
    public static void LogFatal(String message) {
        _logger.fatal(GenerateMessage(LEVEL_FATAL, message));
    }

    /**
     * Log a warning
     *
     * @param message - log message
     */
    public static void LogWarning(String message) {
        _logger.warn(GenerateMessage(LEVEL_WARNING, message));
    }

    /**
     * Log an info message
     *
     * @param message - log message
     */
    public static void LogInfo(String message) {
        _logger.info(GenerateMessage(LEVEL_INFO, message));
    }

    /**
     * Log a debug message
     *
     * @param message - log message
     */
    public static void LogDebug(String message) {
        _logger.debug(GenerateMessage(LEVEL_DEBUG, message));
    }

    /**
     * Log a trace message
     *
     * @param message - log message
     */
    public static void LogTrace(String message) {
        _logger.trace(GenerateMessage(LEVEL_TRACE, message));
    }
}

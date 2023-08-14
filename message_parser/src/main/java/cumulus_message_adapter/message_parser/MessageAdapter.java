package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageAdapter implements IMessageAdapter
{
    private static final int MESSAGE_ADAPTER_TIMEOUT = 60 * 5; // seconds

    public String GetMessageAdapterEnvironmentVariable()
    {
        return System.getenv("CUMULUS_MESSAGE_ADAPTER_DIR");
    }

    private ProcessBuilder buildProcess(String command)
    {
        String messageAdapterPath = "cumulus-message-adapter";
        String messageAdapterDir = GetMessageAdapterEnvironmentVariable();
        if(messageAdapterDir != null) {
            messageAdapterPath = messageAdapterDir;
        }
        String systemPython = "python3";
        boolean pythonExistsInPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(systemPython)));

        if (pythonExistsInPath && System.getenv("USE_CMA_BINARY") != "true") {
          return new ProcessBuilder(systemPython, messageAdapterPath, command);
        }
        // If there is no system python or USE_CMA_BINARY is true, attempt use of pre-packaged CMA binary
        return new ProcessBuilder(messageAdapterPath + "/cma_bin/cma", command);
      }

    /**
     * Call to message adapter zip to execute a message adapter function. Pass args through the process input
     * and read return result from process output.
     * @param messageAdapterFunction - 'loadAndUpdateRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to message adapter function. Json that contains all of the params.
     * @return the return from the message adapter function
     */
    public String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson)
        throws MessageAdapterException
    {
        String messageAdapterOutput = "";

        try
        {
            ProcessBuilder processBuilder = buildProcess(messageAdapterFunction);
            Process process = processBuilder.start();

            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            writer.write(inputJson);
            writer.close();

            //fix to avoid overflowing processBuilder InputStream

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            //If we ever return more than a single (even very large line) from cumulus_message_adapter this won't work.
            while ((line = reader.readLine()) != null){
                AdapterLogger.LogDebug(line);
                break;
            }
            reader.close();
            Boolean processComplete = false;

            try
            {
                processComplete = process.waitFor(MESSAGE_ADAPTER_TIMEOUT, TimeUnit.SECONDS);
            }
            catch(InterruptedException e)
            {
                // Log that there was an error and then it'll go into the error code below where we can
                // get and log the output from stderr
                AdapterLogger.LogError(String.format("Cumulus Message Adapter error: %s: %s", messageAdapterFunction, e.getMessage()));
            }

            int exitValue = 1;
            if(processComplete) {
                exitValue = process.exitValue();
            }

            if(processComplete && exitValue == 0) // Success
            {
                messageAdapterOutput = line;
            }
            else // An error has occurred
            {
                Scanner scanner = new Scanner(process.getErrorStream());
                StringBuilder errorMessageBuilder = new StringBuilder();
                while(scanner.hasNextLine())
                {
                    errorMessageBuilder.append(scanner.nextLine());
                }
                scanner.close();

                String errorString = String.format("%s: %s", messageAdapterFunction, errorMessageBuilder.toString());
                AdapterLogger.LogError("Cumulus Message Adapter error: " + errorString);

                throw new MessageAdapterException("Error executing " + errorString);
            }
        }
        catch(IOException e)
        {
            AdapterLogger.LogError("Unable to find Cumulus Message Adapter: " + e.getMessage());
            throw new MessageAdapterException("Unable to find Cumulus Message Adapter", e.getCause());
        }

        return messageAdapterOutput;
    }

    /**
     * Format the arguments and call the 'loadAndUpdateRemoteEvent' message adapter function
     *
     * @param eventJson - Json passed from lambda
     * @param context - AWS Lambda context
     * @param schemaLocations - locations of JSON schemas
     * @return result of 'loadAndUpdateRemoteEvent'
     */
    public String LoadAndUpdateRemoteEvent(String eventJson, Context context, SchemaLocations schemaLocations)
        throws MessageAdapterException
    {
        Gson gson = new Gson();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(eventJson, Map.class));
        map.put("context", context);
        map.put("schemas", schemaLocations);

        return CallMessageAdapterFunction("loadAndUpdateRemoteEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'loadNestedEvent' message adapter function
     *
     * @param eventJson - Json from loadAndUpdateRemoteEvent
     * @param context - AWS Lambda context
     * @param schemaLocations - locations of JSON schemas
     * @return result of 'loadNestedEvent'
     */
    public String LoadNestedEvent(String eventJson, Context context, SchemaLocations schemaLocations)
        throws MessageAdapterException
    {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(eventJson, Map.class));
        map.put("context", context);
        map.put("schemas", schemaLocations);

        return CallMessageAdapterFunction("loadNestedEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'createNextEvent' message adapter function
     *
     * @param remoteEventJson - Json result from 'loadAndUpdateRemoteEvent'
     * @param nestedEventJson - Json result from 'loadNestedEvent'
     * @param taskJson - result from calling the task
     * @param schemaLocations - locations of JSON schemas
     * @return result of 'createNextEvent'
     */
    public String CreateNextEvent(String remoteEventJson, String nestedEventJson, String taskJson, SchemaLocations schemaLocations)
        throws MessageAdapterException
    {
        // Use GsonBuilder here to output message_config as null in null case
        // instead of dropping the key
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();

        Map<String, Object> nestedEventMap = JsonUtils.toMap(nestedEventJson);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(remoteEventJson, Map.class));
        map.put("message_config", nestedEventMap.get("messageConfig"));
        map.put("handler_response", gson.fromJson(taskJson, Map.class));
        map.put("schemas", schemaLocations);

        return CallMessageAdapterFunction("createNextEvent", gson.toJson(map));
    }
}

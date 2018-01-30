package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageAdapter implements IMessageAdapter
{
    /**
     * Call to message adapter zip to execute a message adapter function. Pass args through the process input
     * and read return result from process output.
     * @param messageAdapterFunction - 'loadRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to message adapter function. Json that contains all of the params.
     * @return the return from the message adapter function
     */
    public String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson)
        throws MessageAdapterException
    {
        String messageAdapterOutput = "";

        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "cumulus-message-adapter.zip", messageAdapterFunction);

            Process process = processBuilder.start();

            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            writer.write(inputJson);
            writer.close();

            // Log the entire error
            // TO DO: Update logging
            Scanner scanner = new Scanner(process.getErrorStream());
            Boolean hasError = false;
            while(scanner.hasNextLine()) 
            {
                hasError = true;
                System.out.println(String.format("Cumulus Message Adapter error: %s: %s", messageAdapterFunction, scanner.nextLine()));  
            }
            scanner.close();

            if(hasError)
            {
                throw new MessageAdapterException("Error executing " + messageAdapterFunction);
            }

            scanner = new Scanner(process.getInputStream());
            if(scanner.hasNextLine()) 
            {
                messageAdapterOutput = scanner.nextLine();
            }
            scanner.close();
        }
        catch(IOException e)
        {
            throw new MessageAdapterException("Unable to find Cumulus Message Adapter", e.getCause());      
        }

        return messageAdapterOutput;        
    }

    /**
     * Format the arguments and call the 'loadRemoteEvent' message adapter function
     * @param eventJson - Json passed from lambda
     * @return result of 'loadRemoteEvent'
     */
    public String LoadRemoteEvent(String eventJson)
        throws MessageAdapterException
    {
        Gson gson = new Gson();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(eventJson, Map.class));

        return CallMessageAdapterFunction("loadRemoteEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'loadNestedEvent' message adapter function
     * @param eventJson - Json from loadRemoteEvent
     * @param context - AWS Lambda context
     * @return result of 'loadNestedEvent'
     */
    public String LoadNestedEvent(String eventJson, Context context)
        throws MessageAdapterException
    {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(eventJson, Map.class));
        map.put("context", context);

        return CallMessageAdapterFunction("loadNestedEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'createNextEvent' message adapter function
     * @param remoteEventJson - Json result from 'loadRemoteEvent'
     * @param nestedEventJson - Json result from 'loadNestedEvent'
     * @param taskJson - result from calling the task
     * @return result of 'createNextEvent'
     */
    public String CreateNextEvent(String remoteEventJson, String nestedEventJson, String taskJson)
        throws MessageAdapterException
    {
        // Use GsonBuilder here to output message_config as null in null case
        // instead of dropping the key
        GsonBuilder gsonBuilder = new GsonBuilder();  
        gsonBuilder.serializeNulls();  
        Gson gson = gsonBuilder.create();

        Map nestedEventMap = gson.fromJson(nestedEventJson, Map.class);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(remoteEventJson, Map.class));
        map.put("message_config", nestedEventMap.get("message_config"));
        map.put("handler_response", gson.fromJson(taskJson, Map.class));

        return CallMessageAdapterFunction("createNextEvent", gson.toJson(map));
    }
}
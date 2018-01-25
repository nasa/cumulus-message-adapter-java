package cumulus_sled.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;

public class Sled implements ISled
{
    /**
     * Call to sled zip to execute a sled function. Pass args through the process input
     * and read return result from process output.
     * @param sledFunction - 'loadRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to sled function. Json that contains all of the params.
     * @return the return from the sled function
     */
    public String CallSledFunction(String sledFunction, String inputJson)
       // throws IOException
    {
        Runtime runtime = Runtime.getRuntime();
        String sledOutput = "";

        // TO DO
        try
        {
            Process process = runtime.exec("python ./cumulus-sled.zip " + sledFunction);

            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            writer.write(inputJson);
            writer.close();

            System.out.println("Input JSON: " + inputJson);

            System.out.println(process.isAlive());

            Scanner scanner = new Scanner(process.getErrorStream());
            while(scanner.hasNextLine()) 
            {
                sledOutput = scanner.nextLine();
                System.out.println("ERROR: " + sledOutput);  
            }
            scanner.close();

            scanner = new Scanner(process.getInputStream());
            if(scanner.hasNextLine()) 
            {
                sledOutput = scanner.nextLine();
                System.out.println("OUTPUT: " + sledOutput);  
            }
            scanner.close();
        }
        catch(IOException e)
        {
            return e.getMessage();       
        }

        return sledOutput;        
    }

    /**
     * Format the arguments and call the 'loadRemoteEvent' sled function
     * @param eventJson - Json passed from lambda
     * @return result of 'loadRemoteEvent'
     */
    public String LoadRemoteEvent(String eventJson)
       // throws IOException
    {
        System.out.println("Load Remote Event");

        Gson gson = new Gson();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("event", gson.fromJson(eventJson, Map.class));

        return CallSledFunction("loadRemoteEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'loadNestedEvent' sled function
     * @param eventJson - Json from loadRemoteEvent
     * @param context - AWS Lambda context
     * @return result of 'loadNestedEvent'
     */
    public String LoadNestedEvent(String eventJson, Context context)
    {
        // TEMP
        return eventJson;

        // Call load nested event with the output
        // Gson gson = new Gson();
        // Map<String, Object> map = new HashMap<String, Object>();
        // map.put("event", gson.fromJson(event, Map.class));
        // map.put("context", context);

        // return CallSledFunction("loadNestedEvent", gson.toJson(map));
    }

    /**
     * Format the arguments and call the 'createNextEvent' sled function
     * @param remoteEventJson - Json result from 'loadRemoteEvent'
     * @param nestedEventJson - Json result from 'loadNestedEvent'
     * @param taskJson - result from calling the task
     * @return result of 'createNextEvent'
     */
    public String CreateNextEvent(String remoteEventJson, String nestedEventJson, String taskJson)
    {
        // TEMP
        return nestedEventJson;

        // Gson gson = new Gson();

        // Map nestedEventMap = gson.fromJson(nestedEventJson, Map.class);

        // Map<String, Object> map = new HashMap<String, Object>();
        // map.put("event", gson.fromJson(remoteEventJson, Map.class));
        // map.put("message_config", nestedEventMap.get("message_config"));
        // map.put("handler_response", gson.fromJson(remoteEventJson, Map.class));

        // return CallSledFunction("createNextEvent", gson.toJson(map));
    }
}
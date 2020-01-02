package cumulus_message_adapter.message_parser;

import com.google.gson.Gson;
import java.util.Map;

/**
 * Stub message adapter functionality for testing the event portion of the input
 */
public class TestEventMessageAdapter extends MessageAdapter
{
    /**
     * Return the event portion of the input JSON for testing purposes
     *
     * @param messageAdapterFunction - 'loadAndUpdateRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to message adapter function. Json that contains all of the params.
     * @return inputJson to test JSON conversions
     */
    public String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson)
        throws MessageAdapterException
    {
        Gson gson = new Gson();

        Map<String, Object> inputMap = JsonUtilities.convertJsonStringToMap(inputJson);

        return gson.toJson(inputMap.get("event"));
    }
}

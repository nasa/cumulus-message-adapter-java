package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

/**
 * Stub message adapter functionality for testing
 */
public class TestMessageAdapter extends MessageAdapter
{
    /**
     * Return input JSON for testing purposes
     * 
     * @param messageAdapterFunction - 'loadAndUpdateRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to message adapter function. Json that contains all of the params.
     * @return inputJson to test JSON conversions
     */
    public String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson)
        throws MessageAdapterException
    {
        return inputJson;
    }
}
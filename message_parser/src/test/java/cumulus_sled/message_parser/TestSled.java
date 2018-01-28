package cumulus_sled.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

import com.google.gson.Gson;

import java.util.Map;
import java.util.HashMap;

/**
 * Stub sled functionality for testing
 */
public class TestSled extends Sled
{
    /**
     * Return input JSON for testing purposes
     * @param sledFunction - 'loadRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to sled function. Json that contains all of the params.
     * @return inputJson to test JSON conversions
     */
    public String CallSledFunction(String sledFunction, String inputJson)
        throws MessageAdapterException
    {
        return inputJson;
    }
}
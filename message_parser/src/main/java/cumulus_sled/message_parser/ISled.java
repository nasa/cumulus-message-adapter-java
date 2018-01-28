package cumulus_sled.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

/**
 * Sled interface
 * TO DO: Update after contract is finalized
 */
public interface ISled
{
    String CallSledFunction(String sledFunction, String inputJson) throws MessageAdapterException;
    String LoadRemoteEvent(String eventJson) throws MessageAdapterException;
    String LoadNestedEvent(String eventJson, Context context) throws MessageAdapterException;
    String CreateNextEvent(String remoteEventJson, String nestedEventJson, String taskJson) throws MessageAdapterException;
}
package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

public interface IMessageAdapter
{
    String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson) throws MessageAdapterException;
    String LoadRemoteEvent(String eventJson, SchemaLocations schemaLocations) throws MessageAdapterException;
    String LoadNestedEvent(String eventJson, Context context, SchemaLocations schemaLocations) throws MessageAdapterException;
    String CreateNextEvent(String remoteEventJson, String nestedEventJson, String taskJson, SchemaLocations schemaLocations) throws MessageAdapterException;
}
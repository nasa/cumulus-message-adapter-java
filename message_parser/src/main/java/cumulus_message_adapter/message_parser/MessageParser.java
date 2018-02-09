package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

/**
 * Hanldes messages by passing the input through the message adapter and using the output as input 
 * for business logic. The output of the business logic is passed back to the message adapter.
 */
public class MessageParser implements IMessageParser
{
    /**
     * Instance of the message adapter to use for message processing
     */
    private IMessageAdapter _messageAdapter;

    /**
     * Default constructor - creates a new message adapter instance
     */
    public MessageParser()
    {
        _messageAdapter = new MessageAdapter();
    }

    /**
     * Constructor, takes an instance of the message adapter. Can be used for testing
     * 
     * @param messageAdapter - instance of the message adapter to use
     */
    public MessageParser(IMessageAdapter messageAdapter)
    {
        _messageAdapter = messageAdapter;
    }

    /**
     * Handles the message by passing the input through the message adapter with the context serialized as Json.
     * Calls task business logic and passes the output back to the message adapter
     * 
     * @param input - input Json
     * @param context - AWS Lambda context
     * @param task - callback to business logic function
     * @param schemaLocation - locations of the JSON schema files, can be null
     * @return output of create next event
     */
    private String HandleMessage(String input, Context context, ITask task, SchemaLocations schemaLocations)
        throws MessageAdapterException
    {
        Boolean messageAdapterDisabled = Boolean.valueOf(System.getenv("CUMULUS_MESSAGE_ADAPTER_DISABLED"));

        try
        {
            // If the message adapter is disabled, call the task with original input
            if(messageAdapterDisabled)
            {
                return task.PerformFunction(input, context);
            }

            String remoteEvent = _messageAdapter.LoadRemoteEvent(context, input, schemaLocations);

            String eventInput = _messageAdapter.LoadNestedEvent(remoteEvent, context, schemaLocations);

            String taskOutput = task.PerformFunction(eventInput, context);

            return _messageAdapter.CreateNextEvent(context, remoteEvent, eventInput, taskOutput, schemaLocations);
        }
        catch(Exception e)
        {
            AdapterLogger.LogError(context, input, e.getMessage());

            if(e.getClass().getSimpleName().contains("WorkflowError") || 
               e.getClass().getSimpleName().contains("WorkflowException"))
            {
                return "{\"payload\":null,\"exception\":\"" + e.getMessage() + "\"}";  
            }
            
            throw new MessageAdapterException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Passes the input through the message adapter and runs the task with the output from the message adapter.
     * Passes the output of the task back to the message adapter and uses the output to create the next event.
     * Schema locations default to the default schema location.
     * 
     * @param input - input Json
     * @param context - AWS Lambda context
     * @param task - callback to business logic function
     * @return output of create next event
     */
    public String RunCumulusTask(String input, Context context, ITask task)
        throws MessageAdapterException
    {
        return HandleMessage(input, context, task, null);
    }

    /**
     * Passes the input through the message adapter and runs the task with the output from the message adapter.
     * Passes the output of the task back to the message adapter and uses the output to create the next event.
     * Message adapter will validate schemas against the JSON schema files found at the given locations or the files
     * found at the default location if schema locations are null.
     * 
     * @param input - input Json
     * @param context - AWS Lambda context
     * @param task - callback to business logic function
     * @param inputSchemaLocation - location of the input JSON schema file, can be null
     * @param outputSchemaLocation - location of the output JSON schema file, can be null
     * @param configSchemaLocation - location of the config JSON schema file, can be null
     * @return output of create next event
     */
    public String RunCumulusTask(String input, Context context, ITask task, String inputSchemaLocation, String outputSchemaLocation, String configSchemaLocation)
        throws MessageAdapterException
    {
        SchemaLocations schemaLocations = null;
        
        if(inputSchemaLocation != null || outputSchemaLocation != null || configSchemaLocation != null)
        {
            schemaLocations = new SchemaLocations(inputSchemaLocation, outputSchemaLocation, configSchemaLocation);
        }

        return HandleMessage(input, context, task, schemaLocations);
    }
}

package cumulus_sled.message_parser;

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
     */
    public String HandleMessage(String input, Context context, ITask task)
    {
        Boolean messageAdapterDisabled = Boolean.valueOf(System.getenv("CUMULUS_MESSAGE_ADAPTER_DISABLED"));

        try
        {
            // If the message adapter is disabled, call the task with original input
            if(messageAdapterDisabled)
            {
                return task.PerformFunction(input);
            }

            String remoteEvent = _messageAdapter.LoadRemoteEvent(input);

            String eventInput = _messageAdapter.LoadNestedEvent(remoteEvent, context);

            String taskOutput = task.PerformFunction(eventInput);

            return _messageAdapter.CreateNextEvent(remoteEvent, eventInput, taskOutput);
        }
        catch(Exception e)
        {
            return "{\"payload\":null,\"exception\":\"" + e.getMessage() + "\"}";  
        }
    }
}

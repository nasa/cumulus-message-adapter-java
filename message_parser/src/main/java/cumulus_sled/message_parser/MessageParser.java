package cumulus_sled.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 


/**
 * Hanldes messages by passing the input through the sled and using the output as input 
 * for business logic. The output of the business logic is passed back to the sled.
 */
public class MessageParser implements IMessageParser
{
    /**
     * Instance of the sled to use for message processing
     */
    private ISled _sled;

    /**
     * Default constructor - creates a new sled instance
     */
    public MessageParser()
    {
        _sled = new Sled();
    }

    /**
     * Constructor, takes an instance of the sled. Can be used for testing
     * 
     * @param sled - instance of the sled to use
     */
    public MessageParser(ISled sled)
    {
        _sled = sled;
    }

    /**
     * Handles the message by passing the input through the sled with the context serialized as Json.
     * Calls task business logic and passes the output back to the sled
     * 
     * @param input - input Json
     * @param context - AWS Lambda context
     * @param task - callback to business logic function
     */
    public String HandleMessage(String input, Context context, ITask task)
        throws MessageAdapterException
    {
        Boolean messageAdapterDisabled = Boolean.valueOf(System.getenv("CUMULUS_MESSAGE_ADAPTER_DISABLED"));

        // If the message adapter is disabled, call the task with original input
        if(messageAdapterDisabled)
        {
            return task.PerformFunction(input);
        }

        String remoteEvent = _sled.LoadRemoteEvent(input);

        String eventInput = _sled.LoadNestedEvent(remoteEvent, context);

        String taskOutput = task.PerformFunction(eventInput);

        return _sled.CreateNextEvent(remoteEvent, eventInput, taskOutput);
    }
}

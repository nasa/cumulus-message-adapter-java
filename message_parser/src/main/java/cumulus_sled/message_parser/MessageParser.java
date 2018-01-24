package cumulus_sled.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

import com.google.gson.Gson;

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
    {
        Gson gson = new Gson();
        String contextAsJson = gson.toJson(context);

        String eventInput = _sled.LoadNestedEvent(input, contextAsJson);

        String taskOutput = task.PerformFunction(eventInput);

        return _sled.CreateNextEvent(taskOutput, input);
    }
}

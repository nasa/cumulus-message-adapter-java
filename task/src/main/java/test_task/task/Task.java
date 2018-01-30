package test_task.task;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;

import cumulus_message_adapter.message_parser.MessageParser;
import cumulus_message_adapter.message_parser.ITask;
import cumulus_message_adapter.message_parser.MessageAdapterException;

/**
 * Lambda request handler for testing Message Parser
 *
 */
public class Task implements RequestHandler<String, String> 
{
    /**
     * Handle request coming from Lambda. Call Message Parser.
     * @param input - String input from lambda
     * @param context - Lambda context
     * @return output from message parser
     */
    public String handleRequest(String input, Context context) {
        MessageParser parser = new MessageParser();

        try
        {
            return parser.HandleMessage(input, context, new TaskLogic());
        }
        catch(MessageAdapterException e)
        {
            return e.getMessage();
        }
    }
}

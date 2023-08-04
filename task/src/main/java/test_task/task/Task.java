package test_task.task;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;

import cumulus_message_adapter.message_parser.AdapterLogger;
import cumulus_message_adapter.message_parser.MessageParser;
import cumulus_message_adapter.message_parser.MessageAdapterException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Lambda request handler for testing Message Parser
 *
 */
public class Task implements RequestHandler<String, String>
{
    String className = this.getClass().getName();

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
            // If AdapterLogger is used before calling 'RunCumulusTask', initialze it first
            AdapterLogger.InitializeLogger(context, input);
            AdapterLogger.LogDebug(this.className + " handleRequest Input: " + input);
            return parser.RunCumulusTask(input, context, new TaskLogic());
        }
        catch(MessageAdapterException e)
        {
            AdapterLogger.LogError(this.className + " handleRequest Error: " + e.getMessage());
            return e.getMessage();
        }
    }

    public void handleRequestStreams(InputStream inputStream, OutputStream outputStream, Context context) throws IOException, MessageAdapterException {
        MessageParser parser = new MessageParser();

        String input = IOUtils.toString(inputStream, "UTF-8");
        // If AdapterLogger is used before calling 'RunCumulusTask', initialze it first
        AdapterLogger.InitializeLogger(context, input);
        AdapterLogger.LogDebug(this.className + " Input: " + input);
        String output = parser.RunCumulusTask(input, context, new TaskLogic());
        AdapterLogger.LogDebug(this.className + " Output: " + output);
        outputStream.write(output.getBytes(Charset.forName("UTF-8")));
    }
}

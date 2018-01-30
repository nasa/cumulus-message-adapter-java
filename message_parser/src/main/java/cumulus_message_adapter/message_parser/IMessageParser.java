package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.ITask; 

/**
 * Message Parser interface
 */
public interface IMessageParser
{
    public String HandleMessage(String input, Context context, ITask task) throws MessageAdapterException;
}
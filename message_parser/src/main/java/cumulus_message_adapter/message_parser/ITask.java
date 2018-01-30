package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context; 

public interface ITask 
{
    String PerformFunction(String input, Context context) throws Exception;
}
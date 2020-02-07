package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context;

public class TestExceptionTask implements ITask {

    /**
     * Mock business logic that throws an error
     * @param input - input string
     * @param context - Lambda context
     * @return Throws a NullPointerException
     */
    public String PerformFunction(String input, Context context)
        throws Exception
    {
      throw new NullPointerException();
    }
}

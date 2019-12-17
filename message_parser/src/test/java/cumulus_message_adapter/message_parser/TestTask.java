package cumulus_message_adapter.message_parser;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.WorkflowException;

public class TestTask implements ITask
{
    private boolean _throwWorkflowException;

    public TestTask(boolean throwWorkflowException)
    {
        _throwWorkflowException = throwWorkflowException;
    }

    /**
     * Mock business logic that returns Json
     * @param input - input string
     * @return Json string used for testing or throws a WorkflowException
     */
    public String PerformFunction(String input, Context context)
        throws Exception
    {
        // throw new NullPointerException();

        if(_throwWorkflowException)
        {
            throw new WorkflowException("workflow exception");
        }

        return "{\"task\":\"complete\"}";
    }
}

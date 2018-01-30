package test_task.task;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.ITask;

public class TaskLogic implements ITask
{
    /**
     * Sample business logic. Return string with task input.
     * @param input - input string
     * @return JSON string
     */
    public String PerformFunction(String input, Context context)
    {
        return "{\"status\":\"complete\"}";
    }
}
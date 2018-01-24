package test_task.task;

import cumulus_sled.message_parser.ITask;

public class TaskLogic implements ITask
{
    /**
     * Sample business logic. Return string with task input.
     * @param input - input string
     * @return string with task input
     */
    public String PerformFunction(String input)
    {
        return "[Performing business logic. Input:  " + input + "]";
    }
}
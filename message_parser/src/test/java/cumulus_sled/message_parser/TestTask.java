package cumulus_sled.message_parser;

public class TestTask implements ITask
{
    /**
     * Mock business logic that appends to a string
     * @param input - input string
     */
    public String PerformFunction(String input)
    {
        return input + " [Function]";
    }
}
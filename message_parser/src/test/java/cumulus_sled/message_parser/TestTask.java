package cumulus_sled.message_parser;

public class TestTask implements ITask
{
    /**
     * Mock business logic that returns Json
     * @param input - input string
     * @return Json string used for testing
     */
    public String PerformFunction(String input)
    {
        return "{\"task\":\"complete\"}";
    }
}
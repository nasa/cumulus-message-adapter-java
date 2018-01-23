package cumulus_sled.message_parser;

import cumulus_sled.message_parser.ISled; 

/**
 * Stub sled functionality for testing
 */
public class TestSled implements ISled
{
    /**
     * Stub load nested event
     */
    public String LoadNestedEvent(String eventJson, String contextJson)
    {
        return eventJson + " [Nested Event]";
    }

    /**
     * Stub create nested event
     */
    public String CreateNextEvent(String nestedEventJson, String eventJson)
    {
        return nestedEventJson + " [Next Event]";
    }
}
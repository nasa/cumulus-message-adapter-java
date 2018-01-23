package cumulus_sled.message_parser;

/* TO DO: Update this with actual sled functionality. Currently using for lambda testing
*/
public class Sled implements ISled
{
    public String LoadNestedEvent(String eventJson, String contextJson)
    {
        return " [Load Event + " + contextJson + "]";
    }

    public String CreateNextEvent(String nestedEventJson, String eventJson)
    {
        return nestedEventJson;
    }
}
package cumulus_sled.message_parser;

/**
 * Sled interface
 * TO DO: Update after contract is finalized
 */
public interface ISled
{
    String LoadNestedEvent(String eventJson, String contextJson);
    String CreateNextEvent(String nestedEventJson, String eventJson);
}
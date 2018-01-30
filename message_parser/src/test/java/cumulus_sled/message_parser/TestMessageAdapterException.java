package cumulus_sled.message_parser;

/**
 * Mock message adapter exception functionality for testing
 */
public class TestMessageAdapterException extends MessageAdapter
{
    /**
     * Throw an exception for testing purposes
     * @param messageAdapterFunction - 'loadRemoteEvent', 'loadNestedEvent', or 'createNextEvent'
     * @param inputJson - argument to message adapter function. Json that contains all of the params.
     */
    public String CallMessageAdapterFunction(String messageAdapterFunction, String inputJson)
        throws MessageAdapterException
    {
        throw new MessageAdapterException("test exception");
    }
}
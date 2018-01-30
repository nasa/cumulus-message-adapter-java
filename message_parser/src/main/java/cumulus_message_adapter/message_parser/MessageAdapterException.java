package cumulus_message_adapter.message_parser;

public class MessageAdapterException extends Exception
{
    private static String FormatMessage(String message)
    {
        return "An error occurred in the Cumulus Message Adapter: " + message;
    }

    public MessageAdapterException(String message)
    {
        super(FormatMessage(message));
    }

    public MessageAdapterException(String message, Throwable cause)
    {
        super(FormatMessage(message), cause);
    }
}
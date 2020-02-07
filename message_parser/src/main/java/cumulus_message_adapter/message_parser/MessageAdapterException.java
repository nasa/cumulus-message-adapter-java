package cumulus_message_adapter.message_parser;

public class MessageAdapterException extends Exception
{
	/**
     *
     */
    private static final long serialVersionUID = 1707026426796540923L;

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

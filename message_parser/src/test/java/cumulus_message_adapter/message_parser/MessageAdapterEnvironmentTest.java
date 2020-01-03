package cumulus_message_adapter.message_parser;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

public class MessageAdapterEnvironmentTest
{
    @BeforeClass
    public static void setup() throws IOException
    {
        AdapterUtilities.deleteCMA("alternate-cumulus-message-adapter");
        AdapterUtilities.deleteCMA("cumulus-message-adapter");
        AdapterUtilities.downloadCMA("alternate-cumulus-message-adapter");
    }

    @AfterClass
    public static void teardown() throws IOException
    {
        AdapterUtilities.deleteCMA("alternate-cumulus-message-adapter");
    }

    /*
     * Test that the message handler is hitting all of the correct functions and converting the params
     * to JSON correctly if an alternate path is specified
     */
    @Test
    public void testMessageAdapterAlternate()
    {
        class MessageAdapterMock extends MessageAdapter {
            public String GetMessageAdapterEnvironmentVariable()
            {
                return "alternate-cumulus-message-adapter";
            }
        }

        MessageParser parser = new MessageParser(new MessageAdapterMock());
        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map<String, Object> expectedOutputJson = AdapterUtilities.getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false));

            Map<String, Object> taskOuputJson = Json.toMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }
}

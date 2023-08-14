package cumulus_message_adapter.message_parser;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

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
     * to JSON correctly
     */
    void testAndVerifyMessageParser()
    {
        MessageParser parser = new MessageParser(new MessageAdapter());
        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map<String, Object> expectedOutputJson = AdapterUtilities.getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false));

            Map<String, Object> taskOuputJson = JsonUtils.toMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /*
     * Test message handler works correctly when an alternate path is specified
     */
    @Test
    public void testMessageAdapterAlternate() {
        withEnvironmentVariable("CUMULUS_MESSAGE_ADAPTER_DIR", "alternate-cumulus-message-adapter")
                .execute(() -> testAndVerifyMessageParser());
    }

    /*
     * Test message handler works correctly when packaged CMA is executed
     */
    @Test
    public void testPackagedCma() throws Exception {
        withEnvironmentVariable("USE_CMA_BINARY", "true")
                .execute(() -> testAndVerifyMessageParser());
    }
}

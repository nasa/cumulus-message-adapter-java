package cumulus_message_adapter.message_parser;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.condition.EnabledOnJre;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MessageAdapterEnvironmentTest
{
    @BeforeAll
    public static void setup() throws IOException
    {
        //AdapterUtilities.deleteCMA("alternate-cumulus-message-adapter");
        //AdapterUtilities.deleteCMA("cumulus-message-adapter");
        //AdapterUtilities.downloadCMA("cumulus-message-adapter");
        //AdapterUtilities.downloadCMA("alternate-cumulus-message-adapter");
    }

    @AfterAll
    public static void teardown() throws IOException
    {
        //AdapterUtilities.deleteCMA("cumulus-message-adapter");
        //AdapterUtilities.deleteCMA("alternate-cumulus-message-adapter");
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
    @EnabledOnJre(JAVA_8)
    public void testMessageAdapterAlternate() throws Exception {
        String currentDirectory = System.getProperty("user.dir");
        String alternativeDirectory = currentDirectory + File.separator + "alternate-cumulus-message-adapter";
        withEnvironmentVariable("CUMULUS_MESSAGE_ADAPTER_DIR", alternativeDirectory)
                .execute(() -> testAndVerifyMessageParser());
    }

    /*
     * Test message handler works correctly when packaged CMA is executed
     */

    @Test
    @EnabledOnJre(JAVA_8)
    public void testPackagedCma() throws Exception {
        withEnvironmentVariable("USE_CMA_BINARY", "true")
                .execute(() -> testAndVerifyMessageParser());
    }

    /*
     * Test message handler works correctly when an alternate CMA path is specified and packaged CMA is executed
     */
    @Test
    @EnabledOnJre(JAVA_8)
    public void testAlternativeCmaPathAndPackagedCma() throws Exception {
        String currentDirectory = System.getProperty("user.dir");
        String alternativeDirectory = currentDirectory + File.separator + "alternate-cumulus-message-adapter";
        withEnvironmentVariable("CUMULUS_MESSAGE_ADAPTER_DIR", alternativeDirectory)
                .and("USE_CMA_BINARY", "true")
                .execute(() -> testAndVerifyMessageParser());
    }
}

package cumulus_message_adapter.message_parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit test for Message Parser test.
 */
@FixMethodOrder
public class MessageParserTest {
    @BeforeClass
    public static void setup() throws IOException {
        AdapterUtilities.deleteCMA("cumulus-message-adapter");
        AdapterUtilities.downloadCMA("cumulus-message-adapter");
    }

    @AfterClass
    public static void teardown() throws IOException {
        AdapterUtilities.deleteCMA("cumulus-message-adapter");
    }

    /*
     * Test that the message handler is hitting all of the correct functions and
     * converting the params to JSON correctly
     */
    @Test
    public void testMessageAdapter() {
        MessageParser parser = new MessageParser(new MessageAdapter());
        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map<String, Object> expectedOutputJson = AdapterUtilities.getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false));

            Map<String, Object> taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that when passing in schema locations they are serialized to JSON
     * correctly
     */
    @Test
    public void testSchemaLocations() {
        MessageParser parser = new MessageParser(new MessageAdapter());

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map expectedOutputJson = AdapterUtilities.getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false), "input.json",
                    "output.json", "config.json");

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that LoadAndUpdateRemoteEvent converts input to JSON correctly
     */
    @Test
    public void testLoadAndUpdateRemoteEvent() {
        MessageAdapter messageAdapter = new MessageAdapter();

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            // the message is not changed
            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            Map expectedOutputJson = AdapterUtilities.convertJsonStringToMap(expectedJsonString);

            String taskOutputString = messageAdapter.LoadAndUpdateRemoteEvent(inputJsonString, null, null);

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, expectedOutputJson);
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that LoadNestedEvent converts input to JSON correctly
     */
    @Test
    public void testLoadNestedEvent() {
        MessageAdapter messageAdapter = new MessageAdapter();

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            String expectedOutput = "{\"input\": {\"anykey\": \"anyvalue\"}, \"config\": {\"bar\": \"baz\"}}";

            assertEquals(expectedOutput, messageAdapter.LoadNestedEvent(inputJsonString, null, null));
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that CreateNextEvent converts input to JSON correctly
     */
    @Test
    public void testCreateNextEvent() {
        MessageAdapter messageAdapter = new MessageAdapter();
        String nestedEventJson = "{\"input\": {\"anykey\": \"anyvalue\"}, \"messageConfig\": {\"bar\": \"baz\"}}";
        String taskOutput = "{\"task\":\"complete\"}";

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.output.json");
            Map expectedOutputJson = AdapterUtilities.getExpectedTestTaskOutputJson();

            String taskOutputString = messageAdapter.CreateNextEvent(inputJsonString, nestedEventJson, taskOutput,
                    null);

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the response when there is a WorkflowException
     */
    @Test
    public void testWorkflowException() {
        MessageParser parser = new MessageParser(new MessageAdapter());

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.output.json");
            Map expectedOutputJson = AdapterUtilities.convertJsonStringToMap(expectedJsonString);
            expectedOutputJson.put("payload", null);
            expectedOutputJson.put("exception", "workflow exception");

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(true));

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that non-workflow exception is thrown
     */
    @Test
    public void testNonWorkflowException() {
        MessageParser parser = new MessageParser(new MessageAdapter());

        try {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            parser.RunCumulusTask(inputJsonString, null, new TestExceptionTask());
        } catch (MessageAdapterException | IOException e) {
            assertTrue(e.getMessage().endsWith("java.lang.NullPointerException"));
            assertEquals(e.getCause().getClass(), NullPointerException.class);
        }
    }

    @Test
    public void testLoggerWithCmaConfiguration() throws MessageAdapterException {
        String inputJsonFile = "parameter.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "someexecutionname");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithContext() throws MessageAdapterException {
        String inputJsonFile = "context.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "16b2cb46ae879f09047dfa677");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithGranulesInPayload() throws MessageAdapterException {
        String inputJsonFile = "execution.granule.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("granules", "[\"MOD09GQ.A2016358.h13v04.006.2016360104606\",\"MOD09GQ.A2016358.h13v04.007.2017\"]");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithCmaGranulesInPayload() throws MessageAdapterException {
        String inputJsonFile = "parameterized.granule.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("granules", "[\"MOD09GQ.A2016358.h13v04.006.2016360104606\",\"MOD09GQ.A2016358.h13v04.007.2017\"]");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithInputGranule() throws MessageAdapterException {
        String inputJsonFile = "execution.input_granule.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("granules", "[\"MOD09GQ.A2016358.h13v04.006.2016360104606\",\"MOD09GQ.A2016358.h13v04.007.2017\"]");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithCmaInputGranule() throws MessageAdapterException {
        String inputJsonFile = "parameterized.input_granule.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("granules", "[\"MOD09GQ.A2016358.h13v04.006.2016360104606\",\"MOD09GQ.A2016358.h13v04.007.2017\"]");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithExecution() throws MessageAdapterException {
        String inputJsonFile = "execution.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("level", "error");
        testLogger(inputJsonFile, expectedObject);
    }

    @Test
    public void testLoggerWithCmaExecution() throws MessageAdapterException {
        String inputJsonFile = "parameterized.input.json";
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("executions", "execution_value");
        expectedObject.addProperty("parentArn", "arn:aws:states:us-east-1:12345:execution:DiscoverGranules:8768aebb");
        expectedObject.addProperty("level", "error");
        expectedObject.addProperty("asyncOperationId", "async-id-123");
        expectedObject.addProperty("stackName", "cumulus-stack");
        testLogger(inputJsonFile, expectedObject);
    }

    private void testLogger(String inputJsonFile, JsonObject expectedObject) throws MessageAdapterException {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        TestAppender appender = (TestAppender) config.getAppenders().get("TestAppender");
        appender.ClearMessages();

        MessageParser parser = new MessageParser(new TestEventMessageAdapter());
        try {
            String inputJsonString = AdapterUtilities.loadResourceToString(inputJsonFile);
            parser.RunCumulusTask(inputJsonString, null, new TestTask(true));

            String logMessage = appender.GetLogMessage(0);
            JsonElement logElement = new JsonParser().parse(logMessage);
            JsonObject logObject = logElement.getAsJsonObject();
            String errorMessage = logObject.get("message").getAsString();

            // Remove message for special comparison and timestamp since it is dynamically generated
            logObject.remove("message");
            logObject.remove("timestamp");

            assertEquals(expectedObject, logObject);
            assertTrue(errorMessage.startsWith("cumulus_message_adapter.message_parser.WorkflowException: workflow exception"));
        } catch (MessageAdapterException | IOException e) {
            e.printStackTrace();
            fail();
        } finally {
            appender.ClearMessages();
        }
    }
}

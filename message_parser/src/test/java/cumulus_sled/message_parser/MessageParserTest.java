package cumulus_message_adapter.message_parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for Message Parser test.
 */
public class MessageParserTest
{
    /**
     * load the example output json message from file and update it with TestTask output
     */
    private Map getExpectedTestTaskOutputJson() throws IOException
    {
        String expectedJsonString = AdapterUtilities.loadResourceToString("basic.output.json");
        Map expectedOutputJson = AdapterUtilities.convertJsonStringToMap(expectedJsonString);
        HashMap<String, String> taskMap = new HashMap<String, String>();
        taskMap.put("task", "complete");
        expectedOutputJson.put("payload", taskMap);
        return expectedOutputJson;
    }

    @BeforeClass
    public static void setup() throws IOException
    {
        AdapterUtilities.deleteCMA();
        AdapterUtilities.downloadCMA();
    }

    @AfterClass
    public static void teardown() throws IOException
    {
        AdapterUtilities.deleteCMA();
    }

    /*
     * Test that the message handler is hitting all of the correct functions and converting the params
     * to JSON correctly
     */
    @Test
    public void testMessageAdapter()
    {
        MessageParser parser = new MessageParser(new MessageAdapter());
        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map expectedOutputJson = getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false));

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
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
            Map expectedOutputJson = getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false));

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }


    /**
     * Test that when passing in schema locations they are serialized to JSON correctly
     */
    @Test
    public void testSchemaLocations()
    {
        MessageParser parser = new MessageParser(new MessageAdapter());

        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            Map expectedOutputJson = getExpectedTestTaskOutputJson();

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(false), "input.json", "output.json", "config.json");

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that LoadAndUpdateRemoteEvent converts input to JSON correctly
     */
    @Test
    public void testLoadAndUpdateRemoteEvent()
    {
        MessageAdapter messageAdapter = new MessageAdapter();

        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            // the message is not changed
            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            Map expectedOutputJson = AdapterUtilities.convertJsonStringToMap(expectedJsonString);

            String taskOutputString = messageAdapter.LoadAndUpdateRemoteEvent(inputJsonString, null, null);

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, expectedOutputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that LoadNestedEvent converts input to JSON correctly
     */
    @Test
    public void testLoadNestedEvent()
    {
        MessageAdapter messageAdapter = new MessageAdapter();

        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");
            String expectedOutput = "{\"input\": {\"anykey\": \"anyvalue\"}, \"config\": {\"bar\": \"baz\"}}";

            assertEquals(expectedOutput, messageAdapter.LoadNestedEvent(inputJsonString, null, null));
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test that CreateNextEvent converts input to JSON correctly
     */
    @Test
    public void testCreateNextEvent()
    {
        MessageAdapter messageAdapter = new MessageAdapter();
        String nestedEventJson = "{\"input\": {\"anykey\": \"anyvalue\"}, \"messageConfig\": {\"bar\": \"baz\"}}";
        String taskOutput = "{\"task\":\"complete\"}";

        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.output.json");
            Map expectedOutputJson = getExpectedTestTaskOutputJson();

            String taskOutputString = messageAdapter.CreateNextEvent(inputJsonString, nestedEventJson, taskOutput, null);

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the response when there is an exception
     */
    @Test
    public void testException()
    {
        MessageParser parser = new MessageParser(new MessageAdapter());

        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("basic.input.json");

            String expectedJsonString = AdapterUtilities.loadResourceToString("basic.output.json");
            Map expectedOutputJson = AdapterUtilities.convertJsonStringToMap(expectedJsonString);
            expectedOutputJson.put("payload", null);
            expectedOutputJson.put("exception", "workflow exception");

            String taskOutputString = parser.RunCumulusTask(inputJsonString, null, new TestTask(true));

            Map taskOuputJson = AdapterUtilities.convertJsonStringToMap(taskOutputString);
            assertEquals(expectedOutputJson, taskOuputJson);
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void testLoggerWithCmaConfiguration() throws MessageAdapterException
    {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        TestAppender appender = (TestAppender) config.getAppenders().get("TestAppender");

        MessageParser parser = new MessageParser(new TestEventMessageAdapter());
        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("parameter.input.json");
            parser.RunCumulusTask(inputJsonString, null, new TestTask(true));

            // Test that the part of the message minus the actual timestamp is correct
            String expectedLog = "{\"executions\":\"someexecutionname\",\"level\":\"error\",\"message\":\"workflow exception\",\"timestamp\"";
            assertEquals(expectedLog, appender.GetLogMessage(1).substring(0, expectedLog.length()));
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testLogger() throws MessageAdapterException
    {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        TestAppender appender = (TestAppender) config.getAppenders().get("TestAppender");

        MessageParser parser = new MessageParser(new TestEventMessageAdapter());
        try
        {
            String inputJsonString = AdapterUtilities.loadResourceToString("context.input.json");
            parser.RunCumulusTask(inputJsonString, null, new TestTask(true));

            // Test that the part of the message minus the actual timestamp is correct
            String expectedLog = "{\"executions\":\"16b2cb46ae879f09047dfa677\",\"level\":\"error\",\"message\":\"workflow exception\",\"timestamp\"";
            assertEquals(expectedLog, appender.GetLogMessage(2).substring(0, expectedLog.length()));
        }
        catch(MessageAdapterException|IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }
}

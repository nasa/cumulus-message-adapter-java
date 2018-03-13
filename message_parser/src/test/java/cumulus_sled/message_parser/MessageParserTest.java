package cumulus_message_adapter.message_parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for Message Parser test.
 */
public class MessageParserTest
{
    /**
     * Test that the message handler is hitting all of the correct functions and converting the params
     * to JSON correctly
     */
    @Test
    public void testMessageAdapter()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"message_config\":null,\"schemas\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}}}";

        try
        {
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(false)));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that when passing in schema locations they are serialized to JSON correctly
     */
    @Test
    public void testSchemaLocations()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"message_config\":null,\"schemas\":{\"input\":\"input.json\",\"output\":\"output.json\",\"config\":\"config.json\"},\"handler_response\":{\"task\":\"complete\"},\"event\":{\"schemas\":{\"input\":\"input.json\",\"output\":\"output.json\",\"config\":\"config.json\"},\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}}}";

        try
        {
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(false), "input.json", "output.json", "config.json"));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadRemoteEvent converts input to JSON correctly
     */
    @Test
    public void testLoadRemoteEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, messageAdapter.LoadRemoteEvent(inputJson, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadNestedEvent converts input to JSON correctly
     */
    @Test
    public void testLoadNestedEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, messageAdapter.LoadNestedEvent(inputJson, null, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that CreateNextEvent converts input to JSON correctly
     */
    @Test
    public void testCreateNextEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String nestedEventJson = "{\"input\": {\"anykey\": \"anyvalue\"}, \"config\": {\"bar\": \"baz\"}}";
        String taskOutput = "{\"task\":\"complete\"}";
    
        String expectedOutput = "{\"message_config\":null,\"schemas\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";

        try
        {
            assertEquals(expectedOutput, messageAdapter.CreateNextEvent(inputJson, nestedEventJson, taskOutput, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test the response when there is an exception
     */
    @Test
    public void testException()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"payload\":null,\"exception\":\"workflow exception\"}";

        try
        {
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(true)));
        }
        catch(MessageAdapterException e)
        {
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
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"state_machine\":\"arn:aws:states:us-west-2:254524764682:stateMachine:podaacDevCumulusIngestGranu-J3EAwfUq70X9\",\"execution_name\":\"16b2cb46ae879f09047dfa677\",\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        parser.RunCumulusTask(inputJson, null, new TestTask(true));

        // Test that the part of the message minus the actual timestamp is correct
        String expectedLog = "{\"executions\":\"16b2cb46ae879f09047dfa677\",\"level\":\"error\",\"message\":\"workflow exception\",\"timestamp\"";
        assertEquals(expectedLog, appender.GetLogMessage(1).substring(0, expectedLog.length()));
    }
}

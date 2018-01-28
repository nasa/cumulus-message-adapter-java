package cumulus_sled.message_parser;

import java.text.MessageFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for Message Parser test.
 */
public class MessageParserTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MessageParserTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MessageParserTest.class );
    }

    /**
     * Test that the message handler is hitting all of the correct functions and converting the params
     * to JSON correctly
     */
    public void testMessageAdapter()
    {
        MessageParser parser = new MessageParser(new TestSled());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"message_config\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}}}";
        
        try
        {
            assertEquals(expectedOutput, parser.HandleMessage(inputJson, null, new TestTask()));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadRemoteEvent converts input to JSON correctly
     */
    public void testLoadRemoteEvent()
    {
        TestSled sled = new TestSled();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, sled.LoadRemoteEvent(inputJson));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadNestedEvent converts input to JSON correctly
     */
    public void testLoadNestedEvent()
    {
        TestSled sled = new TestSled();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, sled.LoadNestedEvent(inputJson, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that CreateNextEvent converts input to JSON correctly
     */
    public void testCreateNextEvent()
    {
        TestSled sled = new TestSled();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String nestedEventJson = "{\"input\": {\"anykey\": \"anyvalue\"}, \"config\": {\"bar\": \"baz\"}}";
        String taskOutput = "{\"task\":\"complete\"}";
    
        String expectedOutput = "{\"message_config\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";

        try
        {
            assertEquals(expectedOutput, sled.CreateNextEvent(inputJson, nestedEventJson, taskOutput));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }
}

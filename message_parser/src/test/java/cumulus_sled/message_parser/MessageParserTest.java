package cumulus_sled.message_parser;

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
     * Test that the message handler is hitting all of the correct functions
     */
    public void testSled()
    {
        MessageParser parser = new MessageParser(new TestSled());
        assertEquals("Input [Nested Event] [Function] [Next Event]", parser.HandleMessage("Input", null, new TestTask()));
    }
}

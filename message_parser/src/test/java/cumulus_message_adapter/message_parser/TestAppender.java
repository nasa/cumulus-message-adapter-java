package cumulus_message_adapter.message_parser;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Filter;;

/**
 * Custom appender for testing the logging. This appender just saves the log message so they
 * can be retrieved later.
 */
@Plugin(name = "TestAppender", category = "Core", elementType = "apender", printObject = true)
public class TestAppender extends AbstractAppender {
    private final ArrayList<String> _log = new ArrayList<String>();

    protected TestAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, false, null);
    }

    public TestAppender()
    {
        super("Test", null, PatternLayout.createDefaultLayout(), false, null);
    }

    @Override
    public void append(final LogEvent event) {
        _log.add(event.getMessage().getFormattedMessage());
    }

    @PluginFactory
    public static TestAppender createAppender(@PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for TestAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new TestAppender(name, filter, layout);
    }

    public String GetLogMessage(int index)
    {
        return _log.get(index);
    }

    public void ClearMessages()
    {
        _log.clear();
    }
}

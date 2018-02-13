package cumulus_message_adapter.message_parser;

/**
 * Class to hold the schema file locations for JSON serialization by GSON
 */
public class SchemaLocations
{
    private final String input;
    private final String output;
    private final String config;

    public SchemaLocations(String inputSchemaLocation, String outputSchemaLocation, String configSchemaLocation)
    {
        input = inputSchemaLocation;
        output = outputSchemaLocation;
        config = configSchemaLocation;
    }
}
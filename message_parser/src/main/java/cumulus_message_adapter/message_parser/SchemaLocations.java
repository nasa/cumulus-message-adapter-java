package cumulus_message_adapter.message_parser;

import com.google.gson.annotations.Expose;

/**
 * Class to hold the schema file locations for JSON serialization by GSON
 */
public class SchemaLocations
{
    @Expose
    private final String input;
    @Expose
    private final String output;
    @Expose
    private final String config;

    public SchemaLocations(String inputSchemaLocation, String outputSchemaLocation, String configSchemaLocation)
    {
        input = inputSchemaLocation;
        output = outputSchemaLocation;
        config = configSchemaLocation;
    }
}

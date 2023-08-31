package cumulus_message_adapter.message_parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

/**
 * Utilities for downloading and cleaning up Cumulus Message Adapter package
 */
public class AdapterUtilities {
    public static final String MESSAGE_ADAPTER_VERSION = "v2.0.3";

    /**
     * Get the json response from a given url
     *
     * @param url The request url
     * @return The json string of the response
     * @throws IOException
     */
    private static String getJsonResponse(String url) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Request Failed. HTTP Error Code: " + conn.getResponseCode());
        }

        // Read response
        StringBuilder jsonStringB = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonStringB.append(line);
            }
        }

        return jsonStringB.toString();
    }

    /**
     * Load a resource file into string
     *
     * @param file The resource file name
     * @return The resource in String format
     * @throws IOException
     */
    public static String loadResourceToString(String file) throws IOException {
        StringBuilder stringB = new StringBuilder();
        InputStream resourceAsStream = AdapterUtilities.class.getClassLoader().getResourceAsStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line;
        while ((line = br.readLine()) != null) {
            stringB.append(line);
        }
        br.close();
        return stringB.toString();
    }

    /**
     * load the example output json message from file and update it with TestTask
     * output
     */
    public static Map<String, Object> getExpectedTestTaskOutputJson() throws IOException {
        String expectedJsonString = loadResourceToString("basic.output.json");
        Map<String, Object> expectedOutputJson = JsonUtils.toMap(expectedJsonString);
        HashMap<String, String> taskMap = new HashMap<String, String>();
        taskMap.put("task", "complete");
        expectedOutputJson.put("payload", taskMap);
        return expectedOutputJson;
    }

}

package cumulus_message_adapter.message_parser;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utilities for downloading and cleaning up Cumulus Message Adapter package
 */
public class AdapterUtilities {
    public static final String MESSAGE_ADAPTER_VERSION = "MESSAGE_ADAPTER_VERSION";
    public static final String CMA_GITHUB_PATH_URL = "https://api.github.com/repos/nasa/cumulus-message-adapter/releases/latest";
    public static final String CMA_DOWNLOAD_URL_PREFIX = "https://github.com/nasa/cumulus-message-adapter/releases/download/";
    public static final String CMA_FILENAME = "cumulus-message-adapter.zip";
    public static final String CMA_ALTERNATE_DIRECTORY = "alternate-cumulus-message-adapter";

    /**
     * Download file from given url
     *
     * @param url           the url of the file
     * @param localFilename the local file name of the downloaded file
     * @throws IOException
     */
    private static void downloadFile(String url, String localFilename) throws IOException {
        InputStream in = new URL(url).openStream();
        Files.copy(in, Paths.get(localFilename), StandardCopyOption.REPLACE_EXISTING);
        in.close();
        System.out.println(url + " is downloaded to " + localFilename);
    }

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
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "@cumulus/deployment");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Request Failed. HTTP Error Code: " + conn.getResponseCode());
        }

        // Read response
        StringBuilder jsonStringB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            jsonStringB.append(line);
        }
        br.close();
        conn.disconnect();

        return jsonStringB.toString();
    }

    /**
     * Get the latest release version info of cumulus message adapter from github
     *
     * @return the latest version info of CMA
     * @throws IOException
     */
    private static String fetchLatestMessageAdapterRelease() throws IOException {
        String url = (System.getenv("GITHUB_TOKEN") != null)
                ? CMA_GITHUB_PATH_URL + "?access_token=" + System.getenv("GITHUB_TOKEN")
                : CMA_GITHUB_PATH_URL;

        String jsonString = getJsonResponse(url);
        Map<String, Object> map = JsonUtilities.convertJsonStringToMap(jsonString);
        return (String) map.get("tag_name");
    }

    /**
     * create a new file for a given zip entry
     *
     * @param destinationDir The destination directory
     * @param zipEntry       The zip entry
     * @return
     * @throws IOException
     */
    private static File createNewFileFromZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * unzip a zip file to destination
     *
     * @param fileZip The name and path of the zip file
     * @param dest    The destination directory for unzip the file
     * @throws IOException
     */
    private static void unzipFile(String fileZip, String dest) throws IOException {
        final byte[] buffer = new byte[1024];
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        FileSystem fileSystem = FileSystems.getDefault();
        Files.createDirectory(fileSystem.getPath(dest));

        while (zipEntry != null) {
            if (zipEntry.isDirectory())
                Files.createDirectories(fileSystem.getPath(dest + File.separator + zipEntry.getName()));
            else {
                final File newFile = createNewFileFromZipEntry(new File(dest), zipEntry);
                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    /**
     * Download the cumulus-message-adapter to local and unzip it
     *
     * @throws IOException
     */
    public static void downloadCMA(String path) throws IOException {
        String version = (System.getenv(MESSAGE_ADAPTER_VERSION) != null) ? System.getenv(MESSAGE_ADAPTER_VERSION)
                : fetchLatestMessageAdapterRelease();
        String url = CMA_DOWNLOAD_URL_PREFIX + version + File.separator + CMA_FILENAME;
        String currentDirectory = System.getProperty("user.dir");
        String zipFile = currentDirectory + File.separator + CMA_FILENAME;
        downloadFile(url, zipFile);
        System.out.println("unzip " + zipFile + " to " + currentDirectory + File.separator + path);
        unzipFile(zipFile, currentDirectory + File.separator + path);
    }

    /**
     * Delete the cumulus-message-adapter from the local directory
     *
     * @throws IOException
     */
    public static void deleteCMA(String path) throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        String zipFile = currentDirectory + File.separator + CMA_FILENAME;
        String zipDirectory = currentDirectory + File.separator + path;

        Files.deleteIfExists(Paths.get(zipFile));
        removeCMAPaths(zipDirectory);
    }

    /**
     * Given a path, recursively removes all files and that directory if it exists.
     *
     * @throws IOException
     */
    private static Void removeCMAPaths(String directory) throws IOException {
        if (Files.exists(Paths.get(directory)))
            Files.walk(Paths.get(directory)).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
        return null;
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
     * load the example output json message from file and update it with TestTask output
     */
    public static Map<String, Object> getExpectedTestTaskOutputJson() throws IOException
    {
        String expectedJsonString = loadResourceToString("basic.output.json");
        Map<String, Object> expectedOutputJson = JsonUtilities.convertJsonStringToMap(expectedJsonString);
        HashMap<String, String> taskMap = new HashMap<String, String>();
        taskMap.put("task", "complete");
        expectedOutputJson.put("payload", taskMap);
        return expectedOutputJson;
    }

}

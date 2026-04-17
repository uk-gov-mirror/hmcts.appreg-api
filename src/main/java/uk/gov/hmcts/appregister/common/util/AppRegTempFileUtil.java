package uk.gov.hmcts.appregister.common.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * A utility that allows us to generate and search for temp files that may have been created across
 * the system.
 */
public class AppRegTempFileUtil {

    public static final String TEMP_FILE_EXTENSION = "appregtmp";

    private AppRegTempFileUtil() {
        // Utility class
    }

    /**
     * generates a temp file.
     *
     * @return The temp file
     */
    public static File generateTempFile() throws IOException {
        // NOSONAR - we want to use the default temp directory as this is guaranteed to be writable
        // and
        // will be cleaned up by the system.
        return File.createTempFile(UUID.randomUUID().toString(), "." + TEMP_FILE_EXTENSION);
    }

    /**
     * Do temp files exist.
     *
     * @return true if they do, false if they don't
     */
    public static boolean doesTempFileExist() {
        File[] files =
                new File(System.getProperty("java.io.tmpdir"))
                        .listFiles(file -> file.getName().endsWith(TEMP_FILE_EXTENSION));

        return files != null && files.length > 0;
    }

    /**
     * Gets the temp files that exist.
     *
     * @return The temp files that exist
     */
    public static File[] getTempFilesThatExist() {
        return new File(System.getProperty("java.io.tmpdir"))
                .listFiles(file -> file.getName().endsWith(TEMP_FILE_EXTENSION));
    }
}

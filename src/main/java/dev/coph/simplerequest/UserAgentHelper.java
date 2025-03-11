package dev.coph.simplerequest;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

import java.util.Arrays;

/**
 * Utility class to assist in parsing user agent strings and retrieving device
 * and browser capabilities. This class utilizes a predefined set of fields
 * to parse user agent strings and extract their corresponding attributes.
 */
public class UserAgentHelper {
    /**
     * Private constructor to prevent instantiation of the UserAgentHelper class.
     * This utility class contains static methods and fields intended to facilitate
     * operations related to parsing user agent strings and retrieving associated
     * device or browser capabilities.
     */
    private UserAgentHelper() {
    }

    /**
     * A static instance of the {@link UserAgentParser} used to parse user agent strings.
     * This variable is initialized through the {@code loadParser} method and is accessed
     * when retrieving user agent capabilities. It remains null until explicitly loaded.
     */
    private static UserAgentParser parser;

    /**
     * Loads the user agent parser with the specified set of fields. If no fields are
     * provided, a default set of fields is used. This method initializes the parser
     * and prepares it for parsing user agent strings.
     *
     * @param fields Optional variable-length argument of {@link BrowsCapField} elements representing
     *               the fields to be used in the parsing process. If no fields are specified,
     *               a default set of fields is applied.
     * @return {@code true} if the parser is successfully loaded, or {@code false} if an error
     *         occurs during loading.
     */
    public static boolean loadParser(BrowsCapField... fields) {
        try {
            if (fields != null && fields.length != 0)
                parser = new UserAgentService().loadParser(Arrays.asList(fields));
            else
                parser = new UserAgentService().loadParser(Arrays.asList(
                        BrowsCapField.BROWSER, BrowsCapField.BROWSER_TYPE, BrowsCapField.BROWSER_MAJOR_VERSION,
                        BrowsCapField.DEVICE_TYPE, BrowsCapField.PLATFORM, BrowsCapField.PLATFORM_VERSION,
                        BrowsCapField.DEVICE_NAME));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the {@link Capabilities} of a given user agent string by parsing it.
     * If the parser is not initialized, it attempts to load the parser before proceeding.
     *
     * @param userAgent the user agent string to be parsed
     * @return the parsed {@link Capabilities} for the given user agent string,
     * or null if the parser could not be loaded or parsing fails
     */
    public static Capabilities getUserAgent(String userAgent) {
        if (parser == null)
            if (!loadParser())
                return null;

        return parser.parse(userAgent);
    }
}

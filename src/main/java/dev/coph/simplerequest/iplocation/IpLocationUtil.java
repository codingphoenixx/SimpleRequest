package dev.coph.simplerequest.iplocation;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.CityResponse;
import dev.coph.simplelogger.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for fetching geographic location information for a given IP address.
 * This utility interacts with the MaxMind GeoIP2 web services to return location data.
 * It supports both the regular MaxMind service and the GeoLite version.
 */
public class IpLocationUtil {


    /**
     * Determines whether a given IP address is a local IP address.
     * A local IP address can be either a loopback address or a site-local address.
     *
     * @param ip the IP address to check; expected in a standard string representation.
     * @return true if the IP address is a loopback or site-local address, false otherwise.
     *         Returns false if the provided IP address is invalid or cannot be resolved.
     */
    public static boolean isLocalIP(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip.replaceAll("\\[","").replaceAll("\\]",""));
            return address.isLoopbackAddress() || address.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Specifies whether to use the GeoLite service instead of the regular MaxMind GeoIP2 service.
     * When set to true, the utility interacts with the GeoLite version, which is typically a
     * lightweight and free option with reduced capabilities compared to the full MaxMind service.
     * When set to false, the full MaxMind service is used for detailed geographic location data.
     */
    private final boolean useLite;
    /**
     * Represents the user ID associated with the MaxMind GeoIP2 web services.
     * This identifier is utilized to authenticate API requests made to the MaxMind
     * or GeoLite services for retrieving geographic location data.
     * <p>
     * The value of this field is provided during the instantiation of the {@code IpLocationUtil}
     * class and is final, ensuring it remains constant throughout the lifetime of the object.
     * <p>
     * This user ID, in conjunction with the license key, is required to establish
     * authenticated WebServiceClient connections to the MaxMind services.
     */
    private final int MAXMIND_USERID;
    /**
     * Represents the license key required to access MaxMind's GeoIP services.
     * This key is used for authenticating requests to the MaxMind service.
     * It is a critical configuration for enabling IP geolocation functionality
     * within the IpLocationUtil class.
     */
    private final String MAXMIND_LICENSE_KEY;
    /**
     * A list of locale strings used for configuring or customizing
     * location-based data retrieval or formatting.
     */
    private final List<String> locales;

    /**
     * A private final instance of the WebServiceClient used for interaction with the MaxMind GeoIP service.
     * This client facilitates communication with the external service to retrieve geographical location
     * information for IP addresses.
     *
     * It is initialized during the construction of the containing class and remains constant throughout
     * the lifecycle of the class. Ensures secure and efficient access to the MaxMind APIs.
     */
    private final WebServiceClient webServiceClient;

    /**
     * Constructs an instance of {@code IpLocationUtil} for interacting with the MaxMind GeoIP services.
     * This constructor initializes the utility with the specified configurations, including
     * whether to use the free GeoLite service or the full MaxMind service, connection preferences,
     * authentication details, and locale settings.
     *
     * @param useLite      a boolean indicating whether the free GeoLite version of the MaxMind
     *                     GeoIP services should be used.
     * @param disableHTTPS a boolean specifying whether HTTPS should be disabled for the connection.
     * @param maxmindUserid the MaxMind user ID required for authenticating requests to the GeoIP services.
     * @param maxmindLicenseKey the MaxMind license key needed for accessing the GeoIP services.
     *                            This key is used in conjunction with the user ID for authentication.
     * @param locales      a list of preferred locale strings that influence the language of
     *                     localized responses from the MaxMind service.
     */
    private IpLocationUtil(boolean useLite,boolean disableHTTPS, int maxmindUserid, String maxmindLicenseKey, List<String> locales) {
        this.useLite = useLite;
        this.MAXMIND_USERID = maxmindUserid;
        this.MAXMIND_LICENSE_KEY = maxmindLicenseKey;
        this.locales = locales;

        WebServiceClient.Builder builder = new WebServiceClient.Builder(MAXMIND_USERID, MAXMIND_LICENSE_KEY);
        if (locales.isEmpty()) {
            locales.add("en");
            locales.add("de");
        }
        builder.locales(locales);

        if (useLite)
            builder.host("geolite.info");
        if(disableHTTPS)
            builder.disableHttps();
        webServiceClient = builder.build();
    }

    /**
     * Retrieves geographical location information for a provided IP address using the MaxMind GeoIP service.
     *
     * @param ip the IP address for which location data is to be fetched.
     * @return a JSONObject containing the geographical location data of the given IP address. If an error occurs, an empty JSONObject is returned.
     */
    public JSONObject getIpLocation(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            return new JSONObject(webServiceClient.city(ipAddress).toJson());
        } catch (Exception e) {
            Logger.getInstance().error("Error getting Location from IP", e);
            return new JSONObject();
        }
    }

    /**
     * Retrieves a CityResponse containing geographical information for a given IP address using the MaxMind GeoIP service.
     *
     * @param ip the IP address for which the geographical location data is to be retrieved.
     * @return a CityResponse object containing the geographical location data for the provided IP address.
     * Returns null if an error occurs during the retrieval.
     */
    public CityResponse getIPLocationResponse(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            return webServiceClient.city(ipAddress);
        } catch (Exception e) {
            Logger.getInstance().error("Error getting Location from IP", e);
            return null;
        }
    }

    /**
     * Builder class for creating and configuring an instance of {@code IpLocationUtil}.
     * The builder provides options to set various properties needed for authenticating and
     * interacting with the MaxMind GeoIP services, such as user credentials and localization preferences.
     * It supports configuring whether to use the GeoLite version of the service, setting locales,
     * and providing the required authentication parameters like MaxMind user ID and license key.
     */
    public static class Builder {

        /**
         * A boolean field determining whether the GeoLite version of MaxMind services
         * should be used.
         *
         * When set to {@code true}, the GeoLite version is enabled, which typically
         * offers a free but less comprehensive database compared to the full MaxMind service.
         * If {@code false}, the full MaxMind service is used, providing more detailed
         * and accurate data.
         *
         * This field is configurable using the builder methods and is utilized when
         * constructing an instance of the {@code IpLocationUtil} class.
         */
        private boolean useLite = false;


        private boolean disableHTTPS = false;

        /**
         * Represents the MaxMind user ID used for authenticating requests to the MaxMind GeoIP services.
         * This identifier, in combination with the MaxMind license key, is utilized to establish authenticated
         * connections to the MaxMind services and retrieve geographical location data.
         *
         * This field is required during the construction of the {@code Builder} for {@code IpLocationUtil}.
         * Once set, it remains constant throughout the lifecycle of the {@code Builder} instance.
         */
        private final int maxmindUserid;

        /**
         * Represents the license key required to authenticate and access
         * MaxMind's GeoIP services. This value is essential for enabling
         * API requests to the MaxMind service, allowing the retrieval of
         * geographic location data based on IP addresses.
         *
         * The license key, in conjunction with the MaxMind user ID, is used
         * to establish authenticated connections to the MaxMind WebServiceClient.
         */
        private final String maxmindLicenceKey;

        /**
         * Represents a list of locale strings used to configure language or regional
         * preferences for the MaxMind GeoIP service. The locale strings in this list
         * may specify desired translations or formats for location data when retrieved
         * from the service.
         *
         * This list can be populated to adjust the output of geographical location data
         * to match the specified locale preferences. If left empty, default locales
         * may be applied by the service.
         *
         * This field is optional and is typically configured during the construction
         * of an {@code IpLocationUtil} instance through the {@code Builder} class.
         */
        private final List<String> locales = new ArrayList<>();

        /**
         * Constructs a new {@code Builder} instance to facilitate the configuration and creation
         * of an {@code IpLocationUtil} object for interacting with MaxMind GeoIP services.
         *
         * @param maxmindUserid      the MaxMind user ID used for authenticating requests to the GeoIP services.
         *                           This field is required and integral for establishing authenticated connections.
         * @param maxmindLicenseKey  the MaxMind license key required for accessing the GeoIP services. This key is
         *                           also mandatory and works in conjunction with the user ID to enable API access.
         */
        public Builder(int maxmindUserid, String maxmindLicenseKey) {
            this.maxmindUserid = maxmindUserid;
            maxmindLicenceKey = maxmindLicenseKey;
        }


        /**
         * Builds and returns an instance of the {@code IpLocationUtil} class configured
         * with the properties defined in this {@code Builder}.
         *
         * @return a new {@code IpLocationUtil} instance configured with the current builder settings,
         *         including usage of GeoLite or full MaxMind service, authentication details,
         *         and locale preferences.
         */
        public IpLocationUtil build() {
            return new IpLocationUtil(useLite, disableHTTPS, maxmindUserid, maxmindLicenceKey, locales);
        }

        /**
         * Indicates whether the GeoLite version of MaxMind services is being used.
         *
         * @return {@code true} if the GeoLite version is enabled; {@code false} otherwise.
         */
        public boolean usingLite() {
            return useLite;
        }

        /**
         * Disables HTTPS for connections made by the builder's configured instance.
         * This method modifies the builder's settings to indicate that HTTPS
         * should not be used for connections.
         *
         * @return the current {@code Builder} instance, allowing for method chaining
         * during the builder configuration process.
         */
        public Builder disableHTTPS() {
            this.disableHTTPS = true;
            return this;
        }

        /**
         * Retrieves the MaxMind user ID used for authenticating requests to the GeoIP services.
         *
         * @return the MaxMind user ID associated with this builder instance.
         */
        public int maxmindUserid() {
            return maxmindUserid;
        }

        /**
         * Retrieves the MaxMind license key associated with this builder instance.
         * The license key is used for authenticating requests to the MaxMind GeoIP services.
         *
         * @return the MaxMind license key as a string.
         */
        public String maxmindLicenceKey() {
            return maxmindLicenceKey;
        }

        /**
         * Retrieves the list of locales configured in the builder. These locales may
         * influence the behavior or output of the associated MaxMind GeoIP service,
         * such as the preferred language for location names or other localized information.
         *
         * @return a list of locale codes as strings that represent the configured locales.
         */
        public List<String> locales() {
            return locales;
        }

        /**
         * Enables the use of the GeoLite version of MaxMind services. The GeoLite database
         * is a free version provided by MaxMind, which may have reduced accuracy or fewer
         * features compared to the full MaxMind service.
         *
         * @return the current {@code IpLocationUtil.Builder} instance, allowing for method
         *         chaining during the builder configuration process.
         */
        public IpLocationUtil.Builder useLite() {
            this.useLite = true;
            return this;
        }

        /**
         * Configures the builder with a set of locales. These locales may be used to
         * determine localized behavior or response details in the associated
         * {@code IpLocationUtil} instance, such as language preferences for location names.
         *
         * @param locales one or more locale strings to be added to the builder configuration.
         *                Each locale should be specified in a standard format, such as "en"
         *                for English or "fr" for French.
         * @return the current {@code IpLocationUtil.Builder} instance, allowing for method
         *         chaining during the builder configuration process.
         */
        public IpLocationUtil.Builder locale(String...locales){
            this.locales.addAll(List.of(locales));
            return this;
        }
    }
}

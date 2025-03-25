package dev.coph.simplerequest.iplocation;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.CityResponse;
import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for fetching geographic location information for a given IP address.
 * This utility interacts with the MaxMind GeoIP2 web services to return location data.
 * It supports both the regular MaxMind service and the GeoLite version.
 */
public class IpLocationUtil {
    /**
     * Retrieves the IP address of the client making the request.
     * If the "X-Forwarded-For" header is present, it extracts the first IP address
     * from the header (considering cases where multiple IPs may be listed due to proxying).
     * If the header is absent or empty, the method falls back to using the remote
     * address directly from the request.
     *
     * @param request the {@code Request} object containing client request information.
     * @return a {@code String} representing the client's IP address. If no IP address
     *         can be determined, the method may return {@code null} or a blank string
     *         depending on the state of the request object.
     */
    public static String getClientIPAddress(Request request) {
        String forwardedFor = request.getHeaders().get("X-Forwarded-For");
        String remoteAddr;
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            remoteAddr = forwardedFor.split(",")[0].trim();
        } else {
            remoteAddr = Request.getRemoteAddr(request);
        }
        System.out.println(remoteAddr);
        return remoteAddr;
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
     * Constructs an instance of the IpLocationUtil class.
     *
     * @param useLite           a boolean indicating whether to use the Lite version of the MaxMind database.
     * @param maxmindUserid     the MaxMind user ID for authentication.
     * @param maxmindLicenseKey the MaxMind license key for authentication.
     */
    private IpLocationUtil(boolean useLite, int maxmindUserid, String maxmindLicenseKey, List<String> locales) {
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
            return new IpLocationUtil(useLite, maxmindUserid, maxmindLicenceKey, locales);
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

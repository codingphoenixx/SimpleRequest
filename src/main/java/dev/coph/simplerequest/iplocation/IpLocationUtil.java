package dev.coph.simplerequest.iplocation;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.CityResponse;
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
     *
     * The value of this field is provided during the instantiation of the {@code IpLocationUtil}
     * class and is final, ensuring it remains constant throughout the lifetime of the object.
     *
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
    private final List<String> locales = new ArrayList<>();

    /**
     * Constructs an instance of the IpLocationUtil class.
     *
     * @param useLite a boolean indicating whether to use the Lite version of the MaxMind database.
     * @param maxmindUserid the MaxMind user ID for authentication.
     * @param maxmindLicenseKey the MaxMind license key for authentication.
     */
    public IpLocationUtil(boolean useLite, int maxmindUserid, String maxmindLicenseKey) {
        this.useLite = useLite;
        MAXMIND_USERID = maxmindUserid;
        MAXMIND_LICENSE_KEY = maxmindLicenseKey;
    }

    /**
     * Retrieves geographical location information for a provided IP address using the MaxMind GeoIP service.
     *
     * @param ip the IP address for which location data is to be fetched.
     * @return a JSONObject containing the geographical location data of the given IP address. If an error occurs, an empty JSONObject is returned.
     */
    public JSONObject getIpLocation(String ip) {
        try {
            WebServiceClient.Builder builder = new WebServiceClient.Builder(MAXMIND_USERID, MAXMIND_LICENSE_KEY);

            if(locales.isEmpty()){
                locales.add("en");
                locales.add("de");
            }
            builder.locales(locales);


            if(useLite)
                builder.host("geolite.info");

            WebServiceClient client = builder.build();

            InetAddress ipAddress = InetAddress.getByName(ip);

            CityResponse response = client.city(ipAddress);
            return new JSONObject(response.toJson());
        }catch (Exception e){
            return new JSONObject();
        }
    }

    /**
     * Retrieves a CityResponse containing geographical information for a given IP address using the MaxMind GeoIP service.
     *
     * @param ip the IP address for which the geographical location data is to be retrieved.
     * @return a CityResponse object containing the geographical location data for the provided IP address.
     *         Returns null if an error occurs during the retrieval.
     */
    public CityResponse getIPLocationResponse(String ip) {
        try {
            WebServiceClient.Builder builder = new WebServiceClient.Builder(MAXMIND_USERID, MAXMIND_LICENSE_KEY);

            if(locales.isEmpty()){
                locales.add("en");
                locales.add("de");
            }
            builder.locales(locales);


            if(useLite)
                builder.host("geolite.info");

            WebServiceClient client = builder.build();

            InetAddress ipAddress = InetAddress.getByName(ip);

            return client.city(ipAddress);
        }catch (Exception e){
            return null;
        }
    }
}

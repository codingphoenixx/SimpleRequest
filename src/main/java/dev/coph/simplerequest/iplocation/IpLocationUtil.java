package dev.coph.simplerequest.iplocation;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.CityResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class IpLocationUtil {
    private final boolean useLite;
    private final int MAXMIND_USERID;
    private final String MAXMIND_LICENSEKEY;
    private List<String> locales = new ArrayList<>();

    public IpLocationUtil(boolean useLite, int maxmindUserid, String maxmindLicenseKey) {
        this.useLite = useLite;
        MAXMIND_USERID = maxmindUserid;
        MAXMIND_LICENSEKEY = maxmindLicenseKey;
    }


    public JSONObject getIpLocation(String ip) {
        try {
            WebServiceClient.Builder builder = new WebServiceClient.Builder(MAXMIND_USERID, MAXMIND_LICENSEKEY);

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

    public CityResponse getIPLocationResponse(String ip) {
        try {
            WebServiceClient.Builder builder = new WebServiceClient.Builder(MAXMIND_USERID, MAXMIND_LICENSEKEY);

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

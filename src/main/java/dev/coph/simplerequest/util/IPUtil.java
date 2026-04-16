package dev.coph.simplerequest.util;

import org.eclipse.jetty.server.Request;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A utility class for processing IP addresses from incoming HTTP requests.
 * This class provides functionality to determine the IP address of the client making the request,
 * handling cases where the client may be behind proxies.
 */
public class IPUtil {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private IPUtil() {
    }

    /**
     * Retrieves the IP address of the client making the request.
     * If the "X-Forwarded-For" header is present, it extracts the first IP address
     * from the header (considering cases where multiple IPs may be listed due to proxying).
     * If the header is absent or empty, the method falls back to using the remote
     * address directly from the request.
     *
     * @param request the {@code Request} object containing client request information.
     * @return a {@code String} representing the client's IP address. If no IP address
     * can be determined, the method may return {@code null} or a blank string
     * depending on the state of the request object.
     */
    public static String clientIPAddress(Request request) {
        String forwardedFor = request.getHeaders().get("X-Forwarded-For");
        String remoteAddr;
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            remoteAddr = forwardedFor.split(",")[0].trim();
        } else {
            remoteAddr = Request.getRemoteAddr(request);
        }
        return remoteAddr;
    }

    /**
     * Determines if the incoming request originates from the local machine.
     * This method checks if the client's IP address corresponds to any local address,
     * the loopback address, or matches the local host address.
     *
     * @param req the {@code Request} object containing client request information
     * @return {@code true} if the request originates from the local machine; {@code false} otherwise
     */
    public static boolean isLocal(Request req) {
        try {
            String remoteAddr = Request.getRemoteAddr(req);
            InetAddress address = InetAddress.getByName(remoteAddr);
            return address.isAnyLocalAddress() || address.isLoopbackAddress() || InetAddress.getLocalHost().equals(address);
        } catch (UnknownHostException e) {
            return false;
        }
    }


}

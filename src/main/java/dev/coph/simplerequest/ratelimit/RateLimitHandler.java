package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.IPUtil;
import dev.coph.simplerequest.util.Time;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

/**
 * A handler for managing and enforcing rate limits on incoming requests in a server application.
 * This class leverages a {@link RateLimitProvider} to determine whether a request exceeds the
 * defined rate limits and responds accordingly.
 * <p>
 * Requests are tracked based on their originating address, and the handler enforces a specified
 * maximum number of requests within a given time span. If a request exceeds the allowed rate,
 * an HTTP 429 (Too Many Requests) status is sent in the response.
 * <p>
 * This class extends {@code ContextHandlerCollection}, allowing it to integrate with a
 * collection of context handlers.
 */
@Getter
@Accessors(fluent = true)
public class RateLimitHandler extends ContextHandlerCollection {

    /**
     * A {@code RateLimitProvider} instance used to manage rate-limiting functionality
     * for incoming requests. This provider evaluates requests against defined rate limits
     * and determines whether they comply with the allowed request thresholds.
     * <p>
     * The {@code RateLimitProvider} associates a {@link RateLimit} with a unique identifier,
     * typically a requester's address, and tracks requests over a configured time window.
     * It provides methods to check if a request is permitted based on the number of requests
     * already processed within the specified time span.
     * <p>
     * This field is initialized in the constructor of the containing class, ensuring that
     * the rate limits are applied consistently across all incoming requests handled by the
     * application.
     */
    private final RateLimitProvider rateLimitProvider;
    @Setter
    private boolean announceRetryAfter;

    /**
     * Constructs a RateLimitHandler that enforces rate limiting on incoming HTTP requests.
     * Initializes a RateLimitProvider to manage the rate-limiting logic based on the provided parameters.
     *
     * @param webServer          the web server instance associated with this handler
     * @param timeSpan           the time span during which requests are monitored and limited
     * @param maxRequestsPerSpan the maximum number of requests allowed during the specified time span
     */
    public RateLimitHandler(WebServer webServer, Time timeSpan, int maxRequestsPerSpan) {
        rateLimitProvider = new RateLimitProvider(webServer, timeSpan, maxRequestsPerSpan);
    }

    /**
     * Handles incoming requests and enforces rate limiting for them. If the request exceeds
     * the defined rate limit, a response with an HTTP 429 (Too Many Requests) status is sent
     * to the client, and the method returns false. Otherwise, the request proceeds to
     * the parent handler.
     *
     * @param request  the incoming HTTP request to be processed
     * @param response the response object used to send data back to the client
     * @param callback the callback invoked upon completion of request processing
     * @return false if the request exceeded the rate limit; otherwise, the return value
     * of the parent handler's handle method
     * @throws Exception if an error occurs during processing
     */
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if(request.getMethod().equals(HttpMethod.OPTIONS.asString()))
            return super.handle(request, response, callback);

        String path = request.getHttpURI().getPath();
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        String key = IPUtil.getClientIPAddress(request);
        if (!rateLimitProvider.allowRequest(key, path)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            response.write(true, ByteBuffer.wrap("Rate limit exceeded".getBytes()), callback);
            if (announceRetryAfter)
                response.getHeaders().put("Retry-After", Math.max(0, rateLimitProvider.getEarliestAllowedTimestamp(key, path) - System.currentTimeMillis()) / 1000);
            callback.succeeded();
            return false;
        }


        return super.handle(request, response, callback);
    }

}

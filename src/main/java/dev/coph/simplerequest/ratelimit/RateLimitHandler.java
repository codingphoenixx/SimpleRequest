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
 * RateLimitHandler is responsible for enforcing rate limiting on incoming HTTP requests.
 * It intercepts requests and determines whether they are allowed based on predefined
 * rate limiting rules. If a request exceeds the configured rate limit, the handler
 * responds with an HTTP status of 429 (Too Many Requests).
 * <p>
 * This class acts as a middleware in the web server and extends
 * {@code ContextHandlerCollection} to integrate with the server's request handling
 * system.
 * <p>
 * The rate limiting logic is delegated to an instance of {@code RateLimitProvider},
 * which manages the actual rate limit calculations and validations.
 * <p>
 * Features:
 * - Supports rate limiting based on client IP and request path.
 * - Responds with an optional "Retry-After" header to indicate how long the client
 *   should wait before retrying.
 * - Allows configuration for request handling behavior, such as enabling or disabling
 *   retry announcements.
 * <p>
 * Constructor:
 * - The constructor initializes the rate limit provider using the provided web server
 *   instance, a time span for rate limiting, and the maximum requests allowed within
 *   the time span.
 * <p>
 * Overrides:
 * - Overrides the {@code handle} method to inspect and process incoming HTTP requests.
 *   Delegates to the super implementation for allowed requests and handles rate-limited
 *   requests by responding with appropriate status and message.
 * <p>
 * Thread-Safety:
 * - Thread-safe operation is ensured by leveraging the thread-safe architecture of
 *   {@code RateLimitProvider} and concurrent collections.
 */
@Getter
@Accessors(fluent = true)
public class RateLimitHandler extends ContextHandlerCollection {

    private static final byte[] RATE_LIMIT_MSG = "Rate limit exceeded".getBytes();

    private final RateLimitProvider rateLimitProvider;
    @Setter
    private boolean announceRetryAfter;

    /**
     * Constructor for the RateLimitHandler class, which initializes rate limiting
     * for web server requests based on a specified time span and maximum allowable
     * requests.
     *
     * @param webServer the instance of the WebServer to apply rate limiting to
     * @param timeSpan the time span over which the maximum requests are allowed
     * @param maxRequestsPerSpan the maximum number of requests allowed during the specified time span
     */
    public RateLimitHandler(WebServer webServer, Time timeSpan, int maxRequestsPerSpan) {
        rateLimitProvider = new RateLimitProvider(webServer, timeSpan, maxRequestsPerSpan);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (request.getMethod().equals(HttpMethod.OPTIONS.asString()))
            return super.handle(request, response, callback);

        String path = request.getHttpURI().getPath();
        if (path.charAt(path.length() - 1) != '/')
            path += "/";

        String key = IPUtil.clientIPAddress(request);
        if (!rateLimitProvider.allowRequest(key, path)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            if (announceRetryAfter) {
                long retryAfter = Math.max(0, rateLimitProvider.getEarliestAllowedTimestamp(key, path) - System.currentTimeMillis()) / 1000;
                response.getHeaders().put("Retry-After", retryAfter);
            }
            response.write(true, ByteBuffer.wrap(RATE_LIMIT_MSG), callback);
            callback.succeeded();
            return false;
        }

        return super.handle(request, response, callback);
    }
}
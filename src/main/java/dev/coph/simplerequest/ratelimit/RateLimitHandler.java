package dev.coph.simplerequest.ratelimit;

import dev.coph.simplerequest.util.Time;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

@Getter
@Accessors(fluent = true)
public class RateLimitHandler extends ContextHandlerCollection {

    public RateLimitHandler(Time timeSpan, int maxRequestsPerSpan) {
        rateLimitProvider = new RateLimitProvider(timeSpan, maxRequestsPerSpan);
    }
    private final RateLimitProvider rateLimitProvider;


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        if(!rateLimitProvider.allowRequest(Request.getRemoteAddr(request))) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            response.write(true, ByteBuffer.wrap("Rate limit exceeded".getBytes()), callback);
            callback.succeeded();
            return false;
        }

        return super.handle(request, response, callback);
    }

}

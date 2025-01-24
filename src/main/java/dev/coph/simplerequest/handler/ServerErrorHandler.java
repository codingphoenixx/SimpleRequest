package dev.coph.simplerequest.handler;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;

public class ServerErrorHandler extends ErrorHandler {


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        callback.succeeded();
        return true;
    }

}

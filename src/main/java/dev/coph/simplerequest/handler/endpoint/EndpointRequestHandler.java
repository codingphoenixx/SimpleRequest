package dev.coph.simplerequest.handler.endpoint;

import dev.coph.simplerequest.handler.RequestHandler;
import dev.coph.simplerequest.handler.RequestMethode;
import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.util.ResponseUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.json.JSONObject;

public class EndpointRequestHandler {
    private final WebServer webServer;

    public EndpointRequestHandler(WebServer webServer) {
        this.webServer = webServer;
    }

    @RequestHandler(path = "/endpoints", methode = RequestMethode.GET)
    public void handleEndpointRequest(Response response, Callback callback) {
        if(!webServer.enableDiscoveryEndpoint()){
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE_503);
            callback.succeeded();
            return;
        }
        JSONObject handlers = new JSONObject();
        webServer.requestDispatcher().handlers().forEach((pattern, methodHandler) -> {
            JSONObject handler = new JSONObject();
            handler.put("methode", methodHandler.requestMethode());
            handler.put("accesslevel", methodHandler.accessLevel());
            handler.put("description", methodHandler.description() != null && !methodHandler.description().isBlank() ? methodHandler.description() : null);
            handlers.put(methodHandler.path(), handler);
        });
        ResponseUtil.writeSuccessfulAnswer(response, callback, handlers);
    }

}

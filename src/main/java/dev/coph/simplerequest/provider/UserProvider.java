package dev.coph.simplerequest.provider;


import dev.coph.simplerequest.handler.RequestHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

public class UserProvider {

    @RequestHandler(path = "/user/{userid}/info")
    public void getUserInfo(Request request, Response response, Callback callback, String userid) throws Exception {
        response.write(true, ByteBuffer.wrap(("User info for " + userid).getBytes()), callback);

    }


    @RequestHandler(path = "/product/{productId}/{category}")
    public void getProductInfo(Request request, Response response, Callback callback, String productId, String category) throws Exception {
        response.write(true, ByteBuffer.wrap(("Product info for " + productId + " in category " + category).getBytes()), callback);
    }
}

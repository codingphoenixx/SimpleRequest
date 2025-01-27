package dev.coph.simplerequest;

import dev.coph.simplerequest.handler.RequestDispatcher;
import dev.coph.simplerequest.handler.ServerErrorHandler;
import dev.coph.simplerequest.provider.UserProvider;
import dev.coph.simplerequest.server.WebServer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

public class Main {
    private static RequestDispatcher dispatcher;

    public static void main(String[] args) throws Exception {
        WebServer webServer = new WebServer(8080);
        webServer.requestDispatcher().register(new UserProvider());
        webServer.start();
    }


}

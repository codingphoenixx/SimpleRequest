package dev.coph.simplerequest;

import dev.coph.simplerequest.provider.UserProvider;
import dev.coph.simplerequest.server.WebServer;

public class Main {

    public static void main(String[] args) {
        WebServer webServer = new WebServer(8080);
        webServer.requestDispatcher().register(new UserProvider());

        webServer.authenticationHandler((path, request) -> true);
        webServer.start();
    }


}

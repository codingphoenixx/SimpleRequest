package dev.coph.simplerequest;

import dev.coph.simplelogger.Logger;
import dev.coph.simplerequest.provider.UserProvider;
import dev.coph.simplerequest.server.WebServer;
import dev.coph.simplerequest.server.WebSocketProvider;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

public class Main {

    public static void main(String[] args) {
        WebServer webServer = new WebServer(8080);
        webServer.requestDispatcher().register(new UserProvider());

        webServer.authenticationHandler((path, request) -> true)
                .registerWebsocket(new WebSocketTest());
        webServer.start();
    }

    @ServerEndpoint(value="/testsocket")
    public static class WebSocketTest implements WebSocketProvider {

        @Override
        public void onOpen(Session session) {
            Logger.getInstance().debug("Opened new websocket");
        }

        @Override
        public void onMessage(Session session, String message) {
            Logger.getInstance().debug("Message from Websocket: " + message);
        }

        @Override
        public void onError(Session session, Throwable t) {
            Logger.getInstance().debug("Error: " + t.toString());
        }

        @Override
        public void onClose(Session session) {
            Logger.getInstance().debug("Closed websocket");

        }
    }
}

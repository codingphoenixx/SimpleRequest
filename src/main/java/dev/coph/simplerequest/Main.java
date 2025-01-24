package dev.coph.simplerequest;

import dev.coph.simplerequest.handler.RequestDispatcher;
import dev.coph.simplerequest.handler.ServerErrorHandler;
import dev.coph.simplerequest.provider.UserProvider;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

public class Main {
    private static RequestDispatcher dispatcher;

    public static void main(String[] args) throws Exception {
        dispatcher = new RequestDispatcher();
        dispatcher.register(new UserProvider());

        Server server = new Server(55325);

        ContextHandler contextHandler = new ContextHandler(new Handler.Abstract() {

            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
                String pathInfo = request.getHttpURI().getPath();
                if (pathInfo != null) {
                    dispatcher.handle(pathInfo, request, response, callback);
                } else {
                    response.setStatus(404);
                }
                return false;
            }
        }, "/");

        server.setHandler(contextHandler);
        server.setErrorHandler(new ServerErrorHandler());

        for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
                if (connectionFactory instanceof HttpConnectionFactory factory) {
                    factory.getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }


        server.start();
        server.join();
    }


}

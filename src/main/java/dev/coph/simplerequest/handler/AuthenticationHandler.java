package dev.coph.simplerequest.handler;

import org.eclipse.jetty.server.Request;

public interface AuthenticationHandler {

    boolean hasAccess(RequestDispatcher.MethodHandler path, Request request);
    boolean hasPermission(String permission, Request request);

}

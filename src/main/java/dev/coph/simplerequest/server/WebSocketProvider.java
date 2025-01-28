package dev.coph.simplerequest.server;


import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

/**
 * The WebSocketProvider interface defines the necessary event handler methods
 * for managing WebSocket connections. It serves as a framework for creating
 * WebSocket endpoints.
 *
 * Implementations of this interface can define behavior for handling WebSocket
 * lifecycle events such as opening connections, receiving messages, encountering
 * errors, and closing connections.
 *
 * An implementation of this interface can be registered with a WebServer to
 * enable WebSocket functionality for a specific endpoint.
 *
 * Requires an {@code @ServerEndpoint(value="/websockets/{key}")}
 */
public interface WebSocketProvider {

    @OnOpen
    default void onOpen(Session session) {

    }

    @OnMessage
    default void onMessage(Session session, String message) {

    }

    @OnError
    default void onError(Session session, Throwable t) {

    }

    @OnClose
    default void onClose(Session session) {

    }
}

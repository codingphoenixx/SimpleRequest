package dev.coph.simplerequest.handler;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;

/**
 * The ServerErrorHandler class is a specialized error handler that extends the base functionality
 * of the {@link ErrorHandler} class to process and handle server errors.
 *
 * This class analyzes the server response and, based on specific conditions, can adjust
 * the status of the response to ensure errors are managed appropriately before notifying
 * a registered callback of the success state.
 */
public class ServerErrorHandler extends ErrorHandler {
    /**
     * Constructs a new instance of {@code ServerErrorHandler}.
     *
     * This constructor initializes the error handler, enabling it to process server errors
     * by implementing specialized logic for evaluating and adjusting server responses.
     */
    public ServerErrorHandler() {
    }

    /**
     * Handles a server error by evaluating the response status and adjusting it if necessary.
     * Calls the callback to indicate the operation succeeded.
     *
     * @param request  the HTTP request being processed
     * @param response the HTTP response to be modified
     * @param callback a callback to notify when the operation is completed
     * @return <code>true</code> indicating the handler has processed the request
     * @throws Exception if an error occurs during handling
     */
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (response.getStatus() == HttpStatus.OK_200) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
        }
        callback.succeeded();
        return true;
    }

}

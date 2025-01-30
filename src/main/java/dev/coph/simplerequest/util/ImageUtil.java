package dev.coph.simplerequest.util;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

public class ImageUtil {

    public static byte[] getImageFromRequest(Request request) {
        Content.Source.asInputStream(request);
        return null;
    }
}

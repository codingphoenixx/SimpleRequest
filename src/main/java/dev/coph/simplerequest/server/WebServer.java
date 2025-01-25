package dev.coph.simplerequest.server;

import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class WebServer {

    private final int port;
    private final Set<String> allowedOrigins = new HashSet<>();
    private boolean enabled = false;
    private Server server;


    public WebServer(int port) {
        this.port = port;
    }

    public void start() {
        System.setProperty("https.protocols", "SSLv3,TLSv1.2,TLSv1.3");
        if (!isPortAvailable(port)) {
            System.out.println("Port is not available. Available: " + findFreePort(49152, 65535));
            return;
        }



    }


    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private int findFreePort(int startPort, int endPort) {
        for (int port = Math.min(startPort, endPort); port <= Math.max(startPort, endPort); port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }
}

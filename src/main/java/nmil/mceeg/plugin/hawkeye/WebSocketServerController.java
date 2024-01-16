package nmil.mceeg.plugin.hawkeye;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;

public class WebSocketServerController extends WebSocketServer {

    private final Hawkeye plugin;
    private final InetSocketAddress address;

    public WebSocketServerController(String usage, Hawkeye plugin, InetSocketAddress address) {
        super(address);
        this.plugin = plugin;
        this.address = address;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        plugin.getLogger().info("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        plugin.getLogger().info("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        plugin.getLogger().info("Message from client: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        plugin.getLogger().info("WebSocket server started successfully on port " + address.getPort());
    }

    // Call this method to start the WebSocket server
    public void startServer() {
        this.start();
    }

    // Call this method to stop the WebSocket server
    public void stopServer() {
        try {
            this.stop();
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Error while stopping WebSocket server: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}

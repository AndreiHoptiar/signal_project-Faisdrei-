package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * An {@link OutputStrategy} that broadcasts patient data to all connected
 * WebSocket clients.
 *
 * <p>On construction a {@link WebSocketServer} is started on the given port.
 * Every call to {@link #output} formats the record as a comma-separated
 * string and sends it to every currently connected client.
 */
public class WebSocketOutputStrategy implements OutputStrategy {

    /** The underlying WebSocket server that manages client connections. */
    private WebSocketServer server;

    /**
     * Creates a new WebSocketOutputStrategy and immediately starts a WebSocket
     * server on the specified port.
     *
     * @param port the TCP port the WebSocket server will listen on
     */
    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port + ", listening for connections...");
        server.start();
    }

    /**
     * Formats a patient data record as {@code "patientId,timestamp,label,data"}
     * and broadcasts it to all connected WebSocket clients.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time the measurement was taken, in milliseconds since the Unix epoch
     * @param label     the type of measurement (e.g. "HeartRate", "SystolicPressure")
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        // Broadcast the message to all connected clients
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
    }

    /**
     * Minimal {@link WebSocketServer} subclass that logs connection events
     * and forwards incoming messages to the broadcast loop.
     */
    private static class SimpleWebSocketServer extends WebSocketServer {

        /**
         * Creates a server bound to the given address.
         *
         * @param address the host/port combination to listen on
         */
        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used in this context
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}

package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A {@link DataReader} that connects to a WebSocket server and continuously
 * receives patient data in real time.
 *
 * <p>The server is expected to broadcast one record per message in the format
 * produced by {@code WebSocketOutputStrategy}:
 * <pre>
 *   patientId,timestamp,label,data
 * </pre>
 * where all four fields are comma-separated. Examples:
 * <pre>
 *   1,1714376789050,HeartRate,82.0
 *   3,1714376789100,Saturation,97.0%
 * </pre>
 *
 * <p><b>Usage:</b>
 * <pre>
 *   DataStorage storage = DataStorage.getInstance();
 *   DataReader reader = new WebSocketClientImpl("ws://localhost:8080");
 *   reader.readData(storage);   // connects; data arrives asynchronously
 * </pre>
 *
 * <p><b>Error handling:</b> malformed messages are logged to
 * {@code System.err} and skipped. Connection losses are reported via
 * {@link #onClose} and errors via {@link #onError}; the caller is responsible
 * for reconnection logic if persistent delivery is required.
 */
public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    /** Storage that incoming records are pushed into. Set by {@link #readData}. */
    private DataStorage dataStorage;

    /**
     * Creates a new {@code WebSocketClientImpl} that will connect to the given URI.
     *
     * @param serverUri the WebSocket server URI, e.g. {@code ws://localhost:8080}
     * @throws URISyntaxException if {@code serverUri} is not a valid URI
     */
    public WebSocketClientImpl(String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
    }

    /**
     * Test-friendly constructor. Accepts a pre-built {@link DataStorage} so
     * tests can call {@link #parseAndStore(String)} directly without going
     * through a live WebSocket connection.
     *
     * @param serverUri   the WebSocket server URI (no connection is made)
     * @param dataStorage the storage to receive parsed records
     * @throws URISyntaxException if {@code serverUri} is not a valid URI
     */
    public WebSocketClientImpl(String serverUri, DataStorage dataStorage) throws URISyntaxException {
        super(new URI(serverUri));
        this.dataStorage = dataStorage;
    }

    /**
     * Connects to the WebSocket server and begins receiving data asynchronously.
     *
     * <p>This method returns immediately after initiating the connection.
     * Incoming messages are delivered via {@link #onMessage(String)} on a
     * background thread.
     *
     * @param dataStorage the storage where parsed records will be saved
     * @throws IOException if the connection cannot be established
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        try {
            connect();
        } catch (Exception e) {
            throw new IOException("Failed to connect to WebSocket server: " + e.getMessage(), e);
        }
    }

    /**
     * Called when the WebSocket connection is successfully opened.
     *
     * @param handshake the server's opening handshake
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocketClientImpl: connection opened to "
                + getURI());
    }

    /**
     * Called for every message received from the server. Parses the message
     * and stores the resulting record in {@link #dataStorage}.
     *
     * @param message a comma-separated record string
     */
    @Override
    public void onMessage(String message) {
        parseAndStore(message);
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code   the status code sent by the remote party
     * @param reason a human-readable explanation
     * @param remote {@code true} if the close was initiated by the remote party
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocketClientImpl: connection closed"
                + (remote ? " by server" : " locally")
                + " — code=" + code + ", reason=" + reason);
    }

    /**
     * Called when an error occurs on the connection. The exception is logged
     * but not re-thrown; the connection may or may not still be usable.
     *
     * @param ex the exception that was raised
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocketClientImpl: error — " + ex.getMessage());
    }

    /**
     * Parses a raw message from the WebSocket server and pushes the record into
     * storage. Malformed messages are silently skipped after logging to
     * {@code System.err}.
     *
     * <p>Expected format: {@code patientId,timestamp,label,data}
     * <ul>
     *   <li>{@code patientId} — integer</li>
     *   <li>{@code timestamp} — long (ms since epoch)</li>
     *   <li>{@code label}     — string (e.g. "HeartRate")</li>
     *   <li>{@code data}      — numeric string, optionally ending with {@code %}</li>
     * </ul>
     *
     * <p>Public so unit tests can call it directly without a live server.
     *
     * @param message the raw message string received from the server
     */
    public void parseAndStore(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        // Split into exactly 4 parts; the data field may contain commas in theory,
        // so we cap the split at 4 to keep the last part intact.
        String[] parts = message.split(",", 4);
        if (parts.length != 4) {
            System.err.println("WebSocketClientImpl: skipping malformed message "
                    + "(expected 4 fields): " + message);
            return;
        }

        try {
            int    patientId = Integer.parseInt(parts[0].trim());
            long   timestamp = Long.parseLong(parts[1].trim());
            String label     = parts[2].trim();
            String rawData   = parts[3].trim();

            if (rawData.endsWith("%")) {
                rawData = rawData.substring(0, rawData.length() - 1);
            }

            double value = Double.parseDouble(rawData);

            if (dataStorage != null) {
                dataStorage.addPatientData(patientId, value, label, timestamp);
            }
        } catch (NumberFormatException e) {
            System.err.println("WebSocketClientImpl: skipping message with unparseable numeric field: "
                    + message + " — " + e.getMessage());
        }
    }
}

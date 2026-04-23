package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Sends patient data over the network using a TCP connection.
 * Starts a small server that waits for one client to connect and then sends data to it.
 */
public class TcpOutputStrategy implements OutputStrategy {

    /** The server socket that listens for a client. */
    private ServerSocket serverSocket;

    /** The socket for the client that connected to us. */
    private Socket clientSocket;

    /** Used to send text to the client. Stays null until a client connects. */
    private PrintWriter out;

    /**
     * Starts the TCP server on the given port and waits for a client in the background.
     *
     * @param port the port number to listen on (for example 9000)
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends one line of patient data to the connected client.
     * The line looks like "patientId,timestamp,label,data".
     * If no client is connected yet, nothing happens.
     *
     * @param patientId the patient's ID number
     * @param timestamp the time the data was made, in milliseconds
     * @param label     the type of data (like "HeartRate")
     * @param data      the value to send
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}

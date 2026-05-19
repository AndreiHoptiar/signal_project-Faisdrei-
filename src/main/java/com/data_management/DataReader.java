package com.data_management;

import java.io.IOException;

/**
 * Interface for classes that read patient data into {@link DataStorage}.
 *
 * <p>Implementations cover both batch and real-time data sources:
 * <ul>
 *   <li>{@link FileDataReader} — reads pre-recorded data from text files.</li>
 *   <li>{@link WebSocketClientImpl} — connects to a live WebSocket server and
 *       continuously ingests records as they arrive in real time.</li>
 * </ul>
 *
 * <p>For real-time implementations, {@link #readData} is expected to initiate
 * the connection and return immediately; data continues to arrive asynchronously
 * until the connection is closed.
 */
public interface DataReader {

    /**
     * Reads (or begins reading) data from the source and stores it in
     * {@code dataStorage}.
     *
     * <p>For file-based readers this method blocks until all data has been
     * read. For streaming readers it initiates the connection
     * and returns while data continues to flow in the background.
     *
     * @param dataStorage the storage where parsed records will be saved
     * @throws IOException if the source cannot be opened or an unrecoverable
     *                     read error occurs
     */
    void readData(DataStorage dataStorage) throws IOException;
}

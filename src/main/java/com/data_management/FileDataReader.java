package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads patient data from text files written by {@code FileOutputStrategy}.
 *
 * <p>Each generated file in the output directory is a plain text file where
 * every line follows this format (one record per line):
 *
 * <pre>
 * Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 85.0
 * </pre>
 *
 * <p>This class opens every {@code .txt} file inside the directory it was
 * given, parses each line, and adds the record to the supplied
 * {@link DataStorage}. Lines that don't match the expected format are
 * skipped and a warning is printed, this keeps a single corrupt line from
 * blowing up the whole import.
 */
public class FileDataReader implements DataReader {

    /** Folder that contains the data files produced by FileOutputStrategy. */
    private final String directoryPath;

    /**
     * Creates a new FileDataReader that will read every {@code .txt} file
     * inside the given directory.
     *
     * @param directoryPath the folder that contains the data files
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Reads every {@code .txt} file inside {@link #directoryPath} and pushes
     * each parsed record into {@code dataStorage}.
     *
     * @param dataStorage the storage where records will be saved
     * @throws IOException if the directory cannot be opened or a file cannot be read
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }

        // Loop through every .txt file in the directory and parse it line by line.
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*.txt")) {
            for (Path file : files) {
                readFile(file, dataStorage);
            }
        }
    }

    /**
     * Reads a single output file and stores each well-formed line in
     * {@code dataStorage}. Bad lines are skipped with a printed warning.
     *
     * @param file        the file to read
     * @param dataStorage the storage to push parsed records into
     * @throws IOException if the file cannot be opened
     */
    private void readFile(Path file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                parseLine(line, dataStorage);
            }
        }
    }

    /**
     * Parses one line of patient data and stores it.
     *
     * <p>Expected format:
     * {@code Patient ID: <int>, Timestamp: <long>, Label: <string>, Data: <string>}
     *
     * <p>If the line is malformed (wrong number of fields, non-numeric ID,
     * non-numeric timestamp, or non-numeric data) the line is skipped and a
     * message is printed to {@code System.err}. We deliberately don't throw,
     * because one bad line shouldn't stop the rest of the import.
     *
     * @param line        a single line from a data file
     * @param dataStorage the storage to push the parsed record into
     */
    private void parseLine(String line, DataStorage dataStorage) {
        try {
            // Split on ", " to get the four "Key: Value" pieces.
            String[] parts = line.split(", ");
            if (parts.length != 4) {
                System.err.println("Skipping malformed line (expected 4 fields): " + line);
                return;
            }

            int patientId = Integer.parseInt(valueAfterColon(parts[0]));
            long timestamp = Long.parseLong(valueAfterColon(parts[1]));
            String label = valueAfterColon(parts[2]);
            String dataString = valueAfterColon(parts[3]);

            // Some labels (e.g. "Alert") emit non-numeric data such as "triggered".
            // We only push numeric values into DataStorage because addPatientData
            // expects a double. For non-numeric data we encode triggered=1.0 and
            // resolved=0.0 so AlertGenerator can still pick it up.
            double measurementValue;
            if (isNumeric(dataString)) {
                measurementValue = Double.parseDouble(dataString);
            } else if ("triggered".equalsIgnoreCase(dataString)) {
                measurementValue = 1.0;
            } else if ("resolved".equalsIgnoreCase(dataString)) {
                measurementValue = 0.0;
            } else {
                // Strip a trailing "%" from saturation readings like "97%".
                String stripped = dataString.replace("%", "").trim();
                if (isNumeric(stripped)) {
                    measurementValue = Double.parseDouble(stripped);
                } else {
                    System.err.println("Skipping line with non-numeric data: " + line);
                    return;
                }
            }

            dataStorage.addPatientData(patientId, measurementValue, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Skipping line that failed to parse: " + line);
        }
    }

    /**
     * Returns the part of a "Key: Value" string after the first colon, trimmed.
     *
     * @param keyValue a string shaped like {@code "Key: Value"}
     * @return everything after the first colon, with surrounding whitespace removed
     */
    private String valueAfterColon(String keyValue) {
        int colon = keyValue.indexOf(':');
        if (colon < 0) {
            return keyValue.trim();
        }
        return keyValue.substring(colon + 1).trim();
    }

    /**
     * Checks whether a string can be parsed as a {@code double}.
     *
     * @param s the string to test
     * @return {@code true} if {@code Double.parseDouble(s)} would succeed
     */
    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves patient data to text files.
 * Each type of data (label) gets its own file inside the base directory.
 * New records are added to the end of the file.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Renamed field from "BaseDirectory" (UpperCamelCase) to "baseDirectory" (lowerCamelCase)
    // per Google Java Style Guide §5.2.5 (Non-constant field names must be in lowerCamelCase).
    /** The folder where the output files are saved. */
    private String baseDirectory;

    // Renamed field from "file_map" (snake_case) to "fileMap" (lowerCamelCase)
    // per Google Java Style Guide §5.2.5 — field names must be in lowerCamelCase, not snake_case.
    /** Remembers the file path for each label so we don't have to build it every time. */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Makes a new FileOutputStrategy that will write files into the given folder.
     *
     * @param baseDirectory the folder to save files into
     */
    // Removed the stray blank line that used to sit at the start of the constructor body.
    // Google Java Style Guide §4.6.1 discourages leading/trailing blank lines inside a block
    // as they serve no organizational purpose.
    public FileOutputStrategy(String baseDirectory) {
        // Updated reference to use the newly renamed "baseDirectory" field (was "this.BaseDirectory").
        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes one line of patient data to the right file.
     * If the folder or file does not exist yet, it is created.
     * If something goes wrong it just prints an error message.
     *
     * @param patientId the patient's ID number
     * @param timestamp the time the data was made, in milliseconds
     * @param label     the type of data, also used as the file name
     * @param data      the value to write
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            // Updated reference to use the renamed "baseDirectory" field (was "BaseDirectory").
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the filePath variable
        // Renamed local variable from "FilePath" (UpperCamelCase) to "filePath" (lowerCamelCase)
        // per Google Java Style Guide §5.2.7 (Local variable names are in lowerCamelCase).
        // Also updated references to the renamed "fileMap" and "baseDirectory" fields.
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                // Updated reference to use the renamed "filePath" local variable.
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            // Updated reference to use the renamed "filePath" local variable.
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}

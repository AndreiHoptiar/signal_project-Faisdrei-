package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

public class FileOutputStrategy implements OutputStrategy {

    // Renamed field from "BaseDirectory" (UpperCamelCase) to "baseDirectory" (lowerCamelCase)
    // per Google Java Style Guide (Non-constant field names must be in lowerCamelCase).
    private String baseDirectory;

    // Renamed field from "file_map" (snake_case) to "fileMap" (lowerCamelCase)
    // per Google Java Style Guide (field names must be in lowerCamelCase, not snake_case.)
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    // Removed the stray blank line that used to sit at the start of the constructor body.
    // Google Java Style Guide discourages leading/trailing blank lines inside a block
    // as they serve no organizational purpose.
    public FileOutputStrategy(String baseDirectory) {
        // Updated reference to use the newly renamed "baseDirectory" field (was "this.BaseDirectory").
        this.baseDirectory = baseDirectory;
    }

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
        // per Google Java Style Guide (Local variable names are in lowerCamelCase).
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
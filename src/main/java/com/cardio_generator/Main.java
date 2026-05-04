package com.cardio_generator;

import com.data_management.DataStorage;

import java.io.IOException;

/**
 * Top-level entry point for the project.
 *
 * <p>The original {@code pom.xml} only knew how to launch
 * {@link HealthDataSimulator}. The Week 3 assignment asks us to also be able
 * to launch {@link DataStorage}. Instead of editing the build to point at one
 * or the other, this class looks at the first command-line argument and
 * dispatches accordingly:
 *
 * <ul>
 *   <li>{@code java -jar app.jar DataStorage} runs {@link DataStorage#main}</li>
 *   <li>anything else (or no args) runs {@link HealthDataSimulator#main}</li>
 * </ul>
 */
public class Main {

    /**
     * Routes execution to either DataStorage or HealthDataSimulator based on
     * the first command-line argument.
     *
     * @param args the command-line arguments
     * @throws IOException if HealthDataSimulator's file output setup fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "DataStorage".equals(args[0])) {
            // Pass any remaining args through in case DataStorage starts using them.
            String[] forwarded = new String[args.length - 1];
            System.arraycopy(args, 1, forwarded, 0, forwarded.length);
            DataStorage.main(forwarded);
        } else {
            HealthDataSimulator.main(args);
        }
    }
}

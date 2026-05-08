package com.cardio_generator.outputs;

/**
 * An {@link OutputStrategy} that prints patient data directly to standard output.
 *
 * <p>Each call to {@link #output} formats one record as a human-readable line,
 * which is useful for quick debugging or demos where no file or network
 * destination is needed.
 */
public class ConsoleOutputStrategy implements OutputStrategy {

    /**
     * Prints a single patient data record to standard output.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time the measurement was taken, in milliseconds since the Unix epoch
     * @param label     the type of measurement (e.g. "HeartRate", "SystolicPressure")
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        System.out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
    }
}

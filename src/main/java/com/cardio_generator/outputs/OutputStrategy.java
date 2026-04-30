package com.cardio_generator.outputs;

/**
 * Interface for classes that send patient data somewhere (console, file, network, etc).
 * Any class that wants to output data in this project must implement this.
 */
public interface OutputStrategy {

    /**
     * Sends one piece of patient data to the output.
     *
     * @param patientId the patient's ID number
     * @param timestamp the time the data was made, in milliseconds
     * @param label     a short name for the type of data (like "HeartRate")
     * @param data      the actual value as a String
     */
    void output(int patientId, long timestamp, String label, String data);
}

package com.alerts;

/**
 * An {@link Alert} representing an ECG / heart-rhythm anomaly.
 *
 * <p>This subclass is produced by {@link ECGAlertFactory}. It carries no extra
 * state; the distinct type allows consumers to identify ECG-related alerts at
 * runtime without inspecting the condition string.
 */
public class ECGAlert extends Alert {

    /**
     * Constructs a new ECGAlert.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the ECG condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     */
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}

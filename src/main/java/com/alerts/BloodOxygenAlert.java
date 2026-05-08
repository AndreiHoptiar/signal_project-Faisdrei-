package com.alerts;

/**
 * An {@link Alert} representing a blood-oxygen (saturation) anomaly.
 *
 * <p>This subclass is produced by {@link BloodOxygenAlertFactory}. Its only
 * purpose beyond the base {@link Alert} is to provide a distinct runtime type
 * so alert consumers can differentiate oxygen alerts from other categories
 * without parsing the condition string.
 */
public class BloodOxygenAlert extends Alert {

    /**
     * Constructs a new BloodOxygenAlert.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the oxygen-level condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     */
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}

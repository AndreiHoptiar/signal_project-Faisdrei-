package com.alerts;

/**
 * An {@link Alert} representing a blood pressure anomaly.
 *
 * <p>This subclass is produced by {@link BloodPressureAlertFactory} and
 * carries no extra state beyond what {@link Alert} already stores. Its
 * purpose is to let callers distinguish blood-pressure alerts from other
 * alert types via {@code instanceof} or pattern matching without having
 * to inspect the condition string.
 */
public class BloodPressureAlert extends Alert {

    /**
     * Constructs a new BloodPressureAlert.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the blood-pressure condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     */
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}

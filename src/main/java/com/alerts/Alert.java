package com.alerts;

/**
 * Represents a medical alert generated for a specific patient.
 *
 * <p>An alert encapsulates three pieces of information: the ID of the patient
 * it concerns, a human-readable description of the condition that caused it,
 * and the timestamp at which it was created. Alerts are produced by
 * {@link AlertGenerator} whenever a patient's vitals exceed a predefined
 * threshold or match a danger pattern.
 *
 * <p>Usage example:
 * <pre>
 *   Alert alert = new Alert("42", "Critical High Systolic Pressure", System.currentTimeMillis());
 *   notifyStaff(alert.getPatientId(), alert.getCondition());
 * </pre>
 */
public class Alert {

    /** The unique identifier of the patient this alert relates to. */
    private String patientId;

    /** A short description of the health condition that triggered this alert. */
    private String condition;

    /** The wall-clock time (milliseconds since the Unix epoch) when the alert was created. */
    private long timestamp;

    /**
     * Constructs a new Alert with the specified patient ID, condition, and timestamp.
     *
     * @param patientId the unique identifier of the affected patient (must not be {@code null})
     * @param condition a short description of the triggering health condition (must not be {@code null})
     * @param timestamp the time this alert was created, in milliseconds since the Unix epoch
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Returns the unique identifier of the patient this alert concerns.
     *
     * @return the patient ID string; never {@code null}
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns a short description of the health condition that triggered this alert.
     *
     * @return the condition string; never {@code null}
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the time at which this alert was created.
     *
     * @return milliseconds since the Unix epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
}

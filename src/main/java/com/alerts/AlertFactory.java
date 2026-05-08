package com.alerts;

/**
 * Abstract base class for the Factory Method pattern in the alert system.
 *
 * <p>Each concrete subclass decides which specific {@link Alert} subclass to
 * instantiate, so callers can create alerts without knowing the exact runtime
 * type. To create a blood-pressure alert, use {@link BloodPressureAlertFactory};
 * for oxygen alerts use {@link BloodOxygenAlertFactory}; for ECG alerts use
 * {@link ECGAlertFactory}.
 *
 * <p>Example usage:
 * <pre>
 *   AlertFactory factory = new BloodPressureAlertFactory();
 *   Alert alert = factory.createAlert("42", "Critical High Systolic Pressure", System.currentTimeMillis());
 * </pre>
 */
public abstract class AlertFactory {

    /**
     * Factory method that creates and returns an {@link Alert}.
     *
     * @param patientId the unique identifier of the patient the alert concerns
     * @param condition a short description of the health condition that triggered the alert
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     * @return a new {@link Alert} (or subclass) instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}

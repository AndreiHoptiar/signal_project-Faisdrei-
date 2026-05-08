package com.alerts;

/**
 * Concrete {@link AlertFactory} that creates {@link BloodOxygenAlert} instances.
 *
 * <p>Use this factory whenever an oxygen-saturation anomaly is detected (low
 * saturation or a rapid drop within a short window).
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    /**
     * Creates a new {@link BloodOxygenAlert} with the given parameters.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the oxygen-level condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     * @return a new {@link BloodOxygenAlert}
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}

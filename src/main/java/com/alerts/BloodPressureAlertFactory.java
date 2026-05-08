package com.alerts;

/**
 * Concrete {@link AlertFactory} that creates {@link BloodPressureAlert} instances.
 *
 * <p>Use this factory whenever a blood-pressure anomaly is detected (critical
 * threshold breaches or sustained trends). The returned object is a
 * {@link BloodPressureAlert}, which can be identified at runtime via
 * {@code instanceof BloodPressureAlert}.
 */
public class BloodPressureAlertFactory extends AlertFactory {

    /**
     * Creates a new {@link BloodPressureAlert} with the given parameters.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the blood-pressure condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     * @return a new {@link BloodPressureAlert}
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}

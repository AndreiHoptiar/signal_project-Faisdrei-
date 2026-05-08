package com.alerts;

/**
 * Concrete {@link AlertFactory} that creates {@link ECGAlert} instances.
 *
 * <p>Use this factory whenever an ECG or heart-rhythm anomaly is detected
 * (abnormal peaks, irregular rhythms, or manually triggered alerts).
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates a new {@link ECGAlert} with the given parameters.
     *
     * @param patientId the unique identifier of the affected patient
     * @param condition a short description of the ECG condition
     * @param timestamp the time the alert was created, in milliseconds since the Unix epoch
     * @return a new {@link ECGAlert}
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}

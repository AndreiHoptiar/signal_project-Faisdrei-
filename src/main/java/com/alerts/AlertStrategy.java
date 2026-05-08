package com.alerts;

import com.data_management.PatientRecord;

import java.util.List;

/**
 * Strategy interface for alert-generation algorithms.
 *
 * <p>Each concrete implementation encapsulates the logic for one category of
 * health monitoring (blood pressure, heart rate / ECG, or oxygen saturation).
 * {@link AlertGenerator} holds a list of strategies and calls each one during
 * {@code evaluateData}, collecting the resulting {@link Alert} objects and
 * forwarding them to {@code triggerAlert}.
 *
 * <p>This design lets new monitoring rules be added by implementing this
 * interface and registering the strategy — no changes to {@link AlertGenerator}
 * are required.
 */
public interface AlertStrategy {

    /**
     * Inspects the patient's records and returns every {@link Alert} that
     * the strategy's rules determine should be triggered.
     *
     * @param patientId the integer ID of the patient being evaluated
     * @param records   all of the patient's records, sorted by timestamp ascending
     * @return a (possibly empty) list of alerts that should be fired; never {@code null}
     */
    List<Alert> checkAlert(int patientId, List<PatientRecord> records);
}

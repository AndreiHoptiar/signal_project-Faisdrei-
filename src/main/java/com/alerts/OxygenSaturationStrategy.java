package com.alerts;

import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AlertStrategy} that monitors blood-oxygen (saturation) readings.
 *
 * <p>Two rules are applied:
 * <ol>
 *   <li><b>Low saturation</b> – any reading below 92 %.</li>
 *   <li><b>Rapid drop</b> – saturation drops by 5 % or more within any
 *       10-minute sliding window.</li>
 * </ol>
 *
 * <p>Alerts are created via {@link BloodOxygenAlertFactory}.
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    private static final double LOW_SATURATION       = 92.0;
    private static final double RAPID_DROP_PERCENT   = 5.0;
    private static final long   RAPID_DROP_WINDOW_MS = 10L * 60L * 1000L;

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    /**
     * Checks oxygen-saturation rules against the supplied records.
     *
     * @param patientId integer patient ID
     * @param records   all patient records, sorted by timestamp ascending
     * @return list of oxygen alerts that fired; never {@code null}
     */
    @Override
    public List<Alert> checkAlert(int patientId, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patientId);

        List<PatientRecord> sat = new ArrayList<>();
        for (PatientRecord r : records) {
            if (isSaturation(r)) sat.add(r);
        }

        // Rule 1 – absolute low
        for (PatientRecord r : sat) {
            if (r.getMeasurementValue() < LOW_SATURATION) {
                alerts.add(factory.createAlert(pid, "Low Blood Saturation", r.getTimestamp()));
            }
        }

        // Rule 2 – rapid drop within 10-minute window
        for (int i = 1; i < sat.size(); i++) {
            PatientRecord later = sat.get(i);
            for (int j = i - 1; j >= 0; j--) {
                PatientRecord earlier = sat.get(j);
                if (later.getTimestamp() - earlier.getTimestamp() > RAPID_DROP_WINDOW_MS) {
                    break;
                }
                double drop = earlier.getMeasurementValue() - later.getMeasurementValue();
                if (drop >= RAPID_DROP_PERCENT) {
                    alerts.add(factory.createAlert(pid, "Rapid Blood Saturation Drop", later.getTimestamp()));
                    break;
                }
            }
        }

        return alerts;
    }

    /** Returns true if the record represents a saturation reading. */
    private boolean isSaturation(PatientRecord r) {
        String t = r.getRecordType();
        return "Saturation".equals(t) || "BloodSaturation".equals(t);
    }
}

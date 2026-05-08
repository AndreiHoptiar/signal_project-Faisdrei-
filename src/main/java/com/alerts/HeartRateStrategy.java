package com.alerts;

import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AlertStrategy} that monitors ECG readings and manual-trigger alerts.
 *
 * <p>Two rules are applied:
 * <ol>
 *   <li><b>Abnormal ECG peak</b> – an ECG sample whose absolute value exceeds
 *       1.5× the moving average of the previous 10 samples.</li>
 *   <li><b>Manual triggered alert</b> – an "Alert" record with value 1.0,
 *       indicating the bedside button was pressed.</li>
 * </ol>
 *
 * <p>Alerts are created via {@link ECGAlertFactory}.
 */
public class HeartRateStrategy implements AlertStrategy {

    private static final int    ECG_WINDOW_SIZE    = 10;
    private static final double ECG_PEAK_MULTIPLIER = 1.5;

    private final AlertFactory factory = new ECGAlertFactory();

    /**
     * Checks ECG and manual-alert rules against the supplied records.
     *
     * @param patientId integer patient ID
     * @param records   all patient records, sorted by timestamp ascending
     * @return list of ECG / heart-rate alerts that fired; never {@code null}
     */
    @Override
    public List<Alert> checkAlert(int patientId, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patientId);

        checkAbnormalEcg(records, pid, alerts);
        checkManualAlerts(records, pid, alerts);

        return alerts;
    }

    /**
     * Fires an alert if any ECG sample exceeds
     * {@link #ECG_PEAK_MULTIPLIER} × the moving average of the preceding window.
     */
    private void checkAbnormalEcg(List<PatientRecord> records,
                                   String pid, List<Alert> alerts) {
        List<PatientRecord> ecg = new ArrayList<>();
        for (PatientRecord r : records) {
            if ("ECG".equals(r.getRecordType())) ecg.add(r);
        }

        for (int i = 1; i < ecg.size(); i++) {
            int    from  = Math.max(0, i - ECG_WINDOW_SIZE);
            double sum   = 0.0;
            int    count = 0;
            for (int j = from; j < i; j++) {
                sum += Math.abs(ecg.get(j).getMeasurementValue());
                count++;
            }
            if (count == 0) continue;
            double avg = sum / count;
            if (avg > 0.0
                    && Math.abs(ecg.get(i).getMeasurementValue()) > avg * ECG_PEAK_MULTIPLIER) {
                alerts.add(factory.createAlert(pid, "Abnormal ECG Peak",
                        ecg.get(i).getTimestamp()));
            }
        }
    }

    /** Fires an alert for every "Alert" record whose value is 1.0 (button pressed). */
    private void checkManualAlerts(List<PatientRecord> records,
                                    String pid, List<Alert> alerts) {
        for (PatientRecord r : records) {
            if ("Alert".equals(r.getRecordType()) && r.getMeasurementValue() == 1.0) {
                alerts.add(factory.createAlert(pid, "Manual Triggered Alert", r.getTimestamp()));
            }
        }
    }
}

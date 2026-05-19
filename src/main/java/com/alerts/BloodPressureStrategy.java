package com.alerts;

import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AlertStrategy} that monitors blood pressure readings.
 *
 * <p>Three rules are applied:
 * <ol>
 *   <li><b>Critical thresholds</b> – single systolic &gt; 180 or &lt; 90 mmHg,
 *       or diastolic &gt; 120 or &lt; 60 mmHg.</li>
 *   <li><b>Sustained trend</b> – three consecutive readings of the same BP
 *       channel each rising or falling by more than 10 mmHg.</li>
 *   <li><b>Hypotensive Hypoxemia</b> – a low-systolic reading paired with a
 *       low-saturation reading that occur within the same 10-minute window.
 *       The old implementation flagged the alert if both events appeared
 *       anywhere in the record set, which produced false positives for
 *       events hours apart; the windowed check addresses the Week 3 feedback
 *       ("the combined hypotensive hypoxemia alert would be stronger if it
 *       checked related readings from a relevant time window").</li>
 * </ol>
 *
 * <p>Alerts are created via {@link BloodPressureAlertFactory} so the returned
 * objects are typed as {@link BloodPressureAlert}.
 */
public class BloodPressureStrategy implements AlertStrategy {

    private static final double SYSTOLIC_HIGH    = 180.0;
    private static final double SYSTOLIC_LOW     = 90.0;
    private static final double DIASTOLIC_HIGH   = 120.0;
    private static final double DIASTOLIC_LOW    = 60.0;
    private static final double BP_TREND_DELTA   = 10.0;
    private static final double LOW_SATURATION   = 92.0;

    /**
     * Two readings (low systolic and low saturation) must occur within this
     * sliding window to be considered a single Hypotensive Hypoxemia event.
     * Ten minutes was chosen to match the saturation "rapid drop" window
     * defined in {@link OxygenSaturationStrategy} and to reflect the timescale
     * on which clinical hypoxic shock typically progresses.
     */
    private static final long HYPOXEMIA_WINDOW_MS = 10L * 60L * 1000L;

    private final AlertFactory factory = new BloodPressureAlertFactory();

    /**
     * Checks all blood-pressure rules against the supplied records.
     *
     * @param patientId integer patient ID used when constructing {@link Alert} objects
     * @param records   all patient records, sorted by timestamp ascending
     * @return list of blood-pressure alerts that fired; never {@code null}
     */
    @Override
    public List<Alert> checkAlert(int patientId, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String pid = String.valueOf(patientId);

        checkCriticalThresholds(records, pid, alerts);
        checkTrend(records, "SystolicPressure",  pid, alerts);
        checkTrend(records, "DiastolicPressure", pid, alerts);
        checkHypotensiveHypoxemia(records, pid, alerts);

        return alerts;
    }

    /** Fires an alert for any single reading outside the safe BP range. */
    private void checkCriticalThresholds(List<PatientRecord> records,
                                         String pid, List<Alert> alerts) {
        for (PatientRecord r : records) {
            String type = r.getRecordType();
            double v    = r.getMeasurementValue();

            if ("SystolicPressure".equals(type)) {
                if (v > SYSTOLIC_HIGH) {
                    alerts.add(factory.createAlert(pid, "Critical High Systolic Pressure", r.getTimestamp()));
                } else if (v < SYSTOLIC_LOW) {
                    alerts.add(factory.createAlert(pid, "Critical Low Systolic Pressure",  r.getTimestamp()));
                }
            } else if ("DiastolicPressure".equals(type)) {
                if (v > DIASTOLIC_HIGH) {
                    alerts.add(factory.createAlert(pid, "Critical High Diastolic Pressure", r.getTimestamp()));
                } else if (v < DIASTOLIC_LOW) {
                    alerts.add(factory.createAlert(pid, "Critical Low Diastolic Pressure",  r.getTimestamp()));
                }
            }
        }
    }

    /**
     * Fires a trend alert when three consecutive readings of {@code type} each
     * move by more than {@link #BP_TREND_DELTA} in the same direction.
     */
    private void checkTrend(List<PatientRecord> records, String type,
                             String pid, List<Alert> alerts) {
        List<PatientRecord> channel = new ArrayList<>();
        for (PatientRecord r : records) {
            if (type.equals(r.getRecordType())) {
                channel.add(r);
            }
        }
        if (channel.size() < 3) return;

        for (int i = 2; i < channel.size(); i++) {
            double a  = channel.get(i - 2).getMeasurementValue();
            double b  = channel.get(i - 1).getMeasurementValue();
            double c  = channel.get(i).getMeasurementValue();
            double d1 = b - a;
            double d2 = c - b;

            if (d1 > BP_TREND_DELTA && d2 > BP_TREND_DELTA) {
                alerts.add(factory.createAlert(pid,
                        "Increasing " + type + " Trend", channel.get(i).getTimestamp()));
            } else if (d1 < -BP_TREND_DELTA && d2 < -BP_TREND_DELTA) {
                alerts.add(factory.createAlert(pid,
                        "Decreasing " + type + " Trend", channel.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Fires a Hypotensive Hypoxemia alert when the record set contains a
     * low-systolic and a low-saturation reading that occur within the same
     * {@link #HYPOXEMIA_WINDOW_MS} sliding window.
     *
     * <p>The previous implementation only checked whether both events appeared
     * anywhere in the patient's history, which produced false positives for
     * unrelated low readings recorded hours or days apart. This windowed
     * version walks the record list (already sorted by timestamp) and only
     * fires when at least one pair of (low systolic, low saturation) readings
     * is at most {@link #HYPOXEMIA_WINDOW_MS} apart in either direction. At
     * most one alert is fired per evaluation to avoid spamming.
     */
    private void checkHypotensiveHypoxemia(List<PatientRecord> records,
                                            String pid, List<Alert> alerts) {
        List<PatientRecord> lowSystolic   = new ArrayList<>();
        List<PatientRecord> lowSaturation = new ArrayList<>();

        for (PatientRecord r : records) {
            if ("SystolicPressure".equals(r.getRecordType())
                    && r.getMeasurementValue() < SYSTOLIC_LOW) {
                lowSystolic.add(r);
            } else if (isSaturation(r) && r.getMeasurementValue() < LOW_SATURATION) {
                lowSaturation.add(r);
            }
        }

        // Find any pair of (low systolic, low saturation) within the window.
        for (PatientRecord sys : lowSystolic) {
            for (PatientRecord sat : lowSaturation) {
                long delta = Math.abs(sys.getTimestamp() - sat.getTimestamp());
                if (delta <= HYPOXEMIA_WINDOW_MS) {
                    // Use the later of the two timestamps so the alert
                    // marks the moment at which both conditions were known.
                    long ts = Math.max(sys.getTimestamp(), sat.getTimestamp());
                    alerts.add(factory.createAlert(pid, "Hypotensive Hypoxemia", ts));
                    return;
                }
            }
        }
    }

    /** Returns true if the record is a blood-saturation reading. */
    private boolean isSaturation(PatientRecord r) {
        String t = r.getRecordType();
        return "Saturation".equals(t) || "BloodSaturation".equals(t);
    }
}

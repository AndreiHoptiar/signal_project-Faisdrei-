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
 *   <li><b>Hypotensive Hypoxemia</b> – a low-systolic reading combined with a
 *       low-saturation reading present anywhere in the record set.</li>
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
     * Fires a Hypotensive Hypoxemia alert when the record set contains both a
     * low-systolic and a low-saturation reading.
     */
    private void checkHypotensiveHypoxemia(List<PatientRecord> records,
                                            String pid, List<Alert> alerts) {
        boolean lowSystolic   = false;
        boolean lowSaturation = false;
        long    lastTs        = 0L;

        for (PatientRecord r : records) {
            if ("SystolicPressure".equals(r.getRecordType())
                    && r.getMeasurementValue() < SYSTOLIC_LOW) {
                lowSystolic = true;
            }
            if (isSaturation(r) && r.getMeasurementValue() < LOW_SATURATION) {
                lowSaturation = true;
            }
            lastTs = r.getTimestamp();
        }

        if (lowSystolic && lowSaturation) {
            alerts.add(factory.createAlert(pid, "Hypotensive Hypoxemia", lastTs));
        }
    }

    /** Returns true if the record is a blood-saturation reading. */
    private boolean isSaturation(PatientRecord r) {
        String t = r.getRecordType();
        return "Saturation".equals(t) || "BloodSaturation".equals(t);
    }
}

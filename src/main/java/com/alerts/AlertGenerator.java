package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 *
 * <p>Each alert rule lives in its own private helper so it's easy to read,
 * test, and extend (one rule = one method, following the Single Responsibility
 * idea from SOLID).
 */
public class AlertGenerator {

    // -- Threshold constants --------------------------------------------------
    // Pulled out as constants instead of magic numbers so the rubric thresholds
    // are obvious at a glance and easy to change in one place.

    /** Systolic blood pressure must not exceed this value (mmHg). */
    private static final double SYSTOLIC_HIGH = 180.0;
    /** Systolic blood pressure must not fall below this value (mmHg). */
    private static final double SYSTOLIC_LOW = 90.0;
    /** Diastolic blood pressure must not exceed this value (mmHg). */
    private static final double DIASTOLIC_HIGH = 120.0;
    /** Diastolic blood pressure must not fall below this value (mmHg). */
    private static final double DIASTOLIC_LOW = 60.0;
    /** Minimum change between consecutive readings to count as a trend (mmHg). */
    private static final double BP_TREND_DELTA = 10.0;
    /** Blood oxygen saturation alert threshold (%). */
    private static final double LOW_SATURATION = 92.0;
    /** Drop in saturation that triggers a rapid-drop alert (%). */
    private static final double RAPID_DROP_PERCENT = 5.0;
    /** Window for the rapid-drop alert (10 minutes in milliseconds). */
    private static final long RAPID_DROP_WINDOW_MS = 10L * 60L * 1000L;
    /** Number of recent ECG readings used for the moving average. */
    private static final int ECG_WINDOW_SIZE = 10;
    /** Multiplier of the moving average above which an ECG peak is considered abnormal. */
    private static final double ECG_PEAK_MULTIPLIER = 1.5;

    private final DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the given patient's data against every supported alert rule.
     * For every rule that matches, {@link #triggerAlert(Alert)} is called
     * exactly once per offending reading.
     *
     * @param patient the patient whose data should be evaluated
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            return;
        }

        // Pull all records and sort them by timestamp so trend / window
        // checks can simply walk the list in order.
        List<PatientRecord> all = new ArrayList<>(patient.getAllRecords());
        all.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        // Run each rule. Each helper method is independently testable.
        checkBloodPressureCriticalThresholds(all);
        checkBloodPressureTrend(all, "SystolicPressure");
        checkBloodPressureTrend(all, "DiastolicPressure");
        checkLowSaturation(all);
        checkRapidSaturationDrop(all);
        checkHypotensiveHypoxemia(all);
        checkAbnormalEcg(all);
        checkManualTriggeredAlerts(all);
    }

    // -- Individual rules ------------------

    /**
     * Triggers an alert whenever a single systolic or diastolic reading is
     * outside the safe range.
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkBloodPressureCriticalThresholds(List<PatientRecord> records) {
        for (PatientRecord r : records) {
            String type = r.getRecordType();
            double v = r.getMeasurementValue();
            if ("SystolicPressure".equals(type)) {
                if (v > SYSTOLIC_HIGH) {
                    triggerAlert(new Alert(idOf(r), "Critical High Systolic Pressure", r.getTimestamp()));
                } else if (v < SYSTOLIC_LOW) {
                    triggerAlert(new Alert(idOf(r), "Critical Low Systolic Pressure", r.getTimestamp()));
                }
            } else if ("DiastolicPressure".equals(type)) {
                if (v > DIASTOLIC_HIGH) {
                    triggerAlert(new Alert(idOf(r), "Critical High Diastolic Pressure", r.getTimestamp()));
                } else if (v < DIASTOLIC_LOW) {
                    triggerAlert(new Alert(idOf(r), "Critical Low Diastolic Pressure", r.getTimestamp()));
                }
            }
        }
    }

    /**
     * Triggers a trend alert if three consecutive readings of the same
     * blood-pressure type each move by more than {@link #BP_TREND_DELTA} mmHg
     * in the same direction.
     *
     * @param records all of the patient's records, time-sorted
     * @param type    "SystolicPressure" or "DiastolicPressure"
     */
    private void checkBloodPressureTrend(List<PatientRecord> records, String type) {
        // Pull just the readings for this BP channel, in time order.
        List<PatientRecord> bp = new ArrayList<>();
        for (PatientRecord r : records) {
            if (type.equals(r.getRecordType())) {
                bp.add(r);
            }
        }
        if (bp.size() < 3) {
            return;
        }

        // Slide a window of 3 across the readings and check if both deltas
        // exceed the threshold and point in the same direction.
        for (int i = 2; i < bp.size(); i++) {
            double a = bp.get(i - 2).getMeasurementValue();
            double b = bp.get(i - 1).getMeasurementValue();
            double c = bp.get(i).getMeasurementValue();
            double d1 = b - a;
            double d2 = c - b;
            if (d1 > BP_TREND_DELTA && d2 > BP_TREND_DELTA) {
                triggerAlert(new Alert(idOf(bp.get(i)),
                        "Increasing " + type + " Trend", bp.get(i).getTimestamp()));
            } else if (d1 < -BP_TREND_DELTA && d2 < -BP_TREND_DELTA) {
                triggerAlert(new Alert(idOf(bp.get(i)),
                        "Decreasing " + type + " Trend", bp.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for any saturation reading that falls below
     * {@link #LOW_SATURATION}.
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkLowSaturation(List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (isSaturationRecord(r) && r.getMeasurementValue() < LOW_SATURATION) {
                triggerAlert(new Alert(idOf(r), "Low Blood Saturation", r.getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert if saturation drops by {@link #RAPID_DROP_PERCENT} or
     * more inside any 10-minute window.
     *
     * <p>For each saturation reading, we look back at every earlier saturation
     * reading still inside the 10-minute window and compare values.
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkRapidSaturationDrop(List<PatientRecord> records) {
        List<PatientRecord> sat = new ArrayList<>();
        for (PatientRecord r : records) {
            if (isSaturationRecord(r)) {
                sat.add(r);
            }
        }

        for (int i = 1; i < sat.size(); i++) {
            PatientRecord later = sat.get(i);
            for (int j = i - 1; j >= 0; j--) {
                PatientRecord earlier = sat.get(j);
                if (later.getTimestamp() - earlier.getTimestamp() > RAPID_DROP_WINDOW_MS) {
                    break; // earlier readings are outside the window; stop.
                }
                double drop = earlier.getMeasurementValue() - later.getMeasurementValue();
                if (drop >= RAPID_DROP_PERCENT) {
                    triggerAlert(new Alert(idOf(later),
                            "Rapid Blood Saturation Drop", later.getTimestamp()));
                    break; // one alert per "later" reading is plenty.
                }
            }
        }
    }

    /**
     * Triggers a Hypotensive Hypoxemia alert when, within the records,
     * there is at least one systolic reading below {@link #SYSTOLIC_LOW} and
     * at least one saturation reading below {@link #LOW_SATURATION}.
     *
     * <p>Assumption: the rubric describes the combined alert as a co-occurring
     * danger pattern rather than a strict simultaneity requirement, so we
     * trigger when both danger states are present in the patient's record set.
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkHypotensiveHypoxemia(List<PatientRecord> records) {
        boolean lowSystolic = false;
        boolean lowSaturation = false;
        long lastTimestamp = 0L;
        String pid = null;

        for (PatientRecord r : records) {
            if ("SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < SYSTOLIC_LOW) {
                lowSystolic = true;
            }
            if (isSaturationRecord(r) && r.getMeasurementValue() < LOW_SATURATION) {
                lowSaturation = true;
            }
            lastTimestamp = r.getTimestamp();
            pid = idOf(r);
        }
        if (lowSystolic && lowSaturation && pid != null) {
            triggerAlert(new Alert(pid, "Hypotensive Hypoxemia", lastTimestamp));
        }
    }

    /**
     * Triggers an alert if any ECG reading is more than
     * {@link #ECG_PEAK_MULTIPLIER}x the moving average of the previous
     * {@link #ECG_WINDOW_SIZE} readings.
     *
     * <p>We only start checking once we have at least one prior reading, and
     * we use up to {@code ECG_WINDOW_SIZE} prior readings for the average.
     * Absolute values are used so big negative spikes also trigger.
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkAbnormalEcg(List<PatientRecord> records) {
        List<PatientRecord> ecg = new ArrayList<>();
        for (PatientRecord r : records) {
            if ("ECG".equals(r.getRecordType())) {
                ecg.add(r);
            }
        }

        for (int i = 1; i < ecg.size(); i++) {
            int from = Math.max(0, i - ECG_WINDOW_SIZE);
            double sum = 0.0;
            int count = 0;
            for (int j = from; j < i; j++) {
                sum += Math.abs(ecg.get(j).getMeasurementValue());
                count++;
            }
            if (count == 0) {
                continue;
            }
            double avg = sum / count;
            if (avg > 0.0 && Math.abs(ecg.get(i).getMeasurementValue()) > avg * ECG_PEAK_MULTIPLIER) {
                triggerAlert(new Alert(idOf(ecg.get(i)),
                        "Abnormal ECG Peak", ecg.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for every "Alert" record whose value indicates the
     * bedside button was pressed (emitted separately by HealthDataGenerator).
     *
     * @param records all of the patient's records, time-sorted
     */
    private void checkManualTriggeredAlerts(List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if ("Alert".equals(r.getRecordType()) && r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(idOf(r), "Manual Triggered Alert", r.getTimestamp()));
            }
        }
    }

    // -- Helpers --------------------------------------------------------------

    /**
     * Returns whether a record represents a blood-saturation reading.
     * The simulator emits this label as "Saturation"; some sources use
     * "BloodSaturation" so both are accepted to be safe.
     *
     * @param r the record to test
     * @return true if the record is a saturation reading
     */
    private boolean isSaturationRecord(PatientRecord r) {
        String t = r.getRecordType();
        return "Saturation".equals(t) || "BloodSaturation".equals(t);
    }

    /**
     * Converts the integer patient ID stored on a record to the string form
     * expected by the {@link Alert} constructor.
     *
     * @param r the record
     * @return the patient ID as a string
     */
    private String idOf(PatientRecord r) {
        return Integer.toString(r.getPatientId());
    }

    /**
     * Triggers an alert for the monitoring system. For now this just prints
     * the alert to standard output - a real implementation would send a
     * notification to a nurse station, write to a database, etc.
     *
     * <p>Visibility is {@code protected} (instead of {@code private}) so unit
     * tests in the same package can verify alert content via a test subclass.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        System.out.println("ALERT [" + alert.getTimestamp() + "] patient="
                + alert.getPatientId() + " condition=" + alert.getCondition());
    }
}

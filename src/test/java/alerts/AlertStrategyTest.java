package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.*;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the Strategy pattern: {@link BloodPressureStrategy},
 * {@link OxygenSaturationStrategy}, and {@link HeartRateStrategy}.
 *
 * <p>Each test builds a minimal list of {@link PatientRecord} objects that
 * should (or should not) trigger a specific alert, then asserts on the
 * returned list.
 */
class AlertStrategyTest {


    @Test
    void testBPStrategy_criticalHighSystolic() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 185.0, "SystolicPressure", 1000L));

        List<Alert> alerts = strategy.checkAlert(1, records);
        assertEquals(1, countWith(alerts, "Critical High Systolic Pressure"));
    }

    @Test
    void testBPStrategy_criticalLowSystolic() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 85.0, "SystolicPressure", 1000L));

        List<Alert> alerts = strategy.checkAlert(1, records);
        assertEquals(1, countWith(alerts, "Critical Low Systolic Pressure"));
    }

    @Test
    void testBPStrategy_criticalHighDiastolic() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 130.0, "DiastolicPressure", 1000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Critical High Diastolic Pressure"));
    }

    @Test
    void testBPStrategy_criticalLowDiastolic() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 50.0, "DiastolicPressure", 1000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Critical Low Diastolic Pressure"));
    }

    @Test
    void testBPStrategy_increasingTrend() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 100.0, "SystolicPressure", 1000L),
                new PatientRecord(1, 115.0, "SystolicPressure", 2000L),
                new PatientRecord(1, 130.0, "SystolicPressure", 3000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Increasing SystolicPressure Trend"));
    }

    @Test
    void testBPStrategy_decreasingTrend() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 100.0, "DiastolicPressure", 1000L),
                new PatientRecord(1, 85.0,  "DiastolicPressure", 2000L),
                new PatientRecord(1, 70.0,  "DiastolicPressure", 3000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Decreasing DiastolicPressure Trend"));
    }

    @Test
    void testBPStrategy_tinyChangesNoTrend() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 100.0, "SystolicPressure", 1000L),
                new PatientRecord(1, 105.0, "SystolicPressure", 2000L),
                new PatientRecord(1, 110.0, "SystolicPressure", 3000L));

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Trend"));
    }

    @Test
    void testBPStrategy_hypotensiveHypoxemia() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 85.0, "SystolicPressure", 1000L),
                new PatientRecord(1, 90.0, "Saturation",       2000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Hypotensive Hypoxemia"));
    }

    @Test
    void testBPStrategy_hypotensiveHypoxemia_notTriggeredWhenOnlyOneLow() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 85.0, "SystolicPressure", 1000L),
                new PatientRecord(1, 96.0, "Saturation",       2000L)); // saturation OK

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Hypotensive Hypoxemia"));
    }

    @Test
    void testBPStrategy_returnsBloodPressureAlertType() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 185.0, "SystolicPressure", 1000L));

        List<Alert> alerts = strategy.checkAlert(1, records);
        assertFalse(alerts.isEmpty());
        assertInstanceOf(BloodPressureAlert.class, alerts.get(0),
                "BloodPressureStrategy should produce BloodPressureAlert instances");
    }


    @Test
    void testOxygenStrategy_lowSaturation() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 90.0, "Saturation", 1000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Low Blood Saturation"));
    }

    @Test
    void testOxygenStrategy_normalSaturationNoAlert() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 96.0, "Saturation", 1000L));

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Low Blood Saturation"));
    }

    @Test
    void testOxygenStrategy_rapidDrop() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 99.0, "Saturation", 1_000L),
                new PatientRecord(1, 93.0, "Saturation", 1_000L + 5 * 60 * 1000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Rapid Blood Saturation Drop"));
    }

    @Test
    void testOxygenStrategy_dropOutsideWindowNoAlert() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 99.0, "Saturation", 1_000L),
                new PatientRecord(1, 93.0, "Saturation", 1_000L + 15 * 60 * 1000L));

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Rapid Blood Saturation Drop"));
    }

    @Test
    void testOxygenStrategy_returnsBloodOxygenAlertType() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 90.0, "Saturation", 1000L));

        List<Alert> alerts = strategy.checkAlert(1, records);
        assertFalse(alerts.isEmpty());
        assertInstanceOf(BloodOxygenAlert.class, alerts.get(0),
                "OxygenSaturationStrategy should produce BloodOxygenAlert instances");
    }


    @Test
    void testHeartRateStrategy_abnormalEcgPeak() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(1, 1.0, "ECG", 1000L + i));
        }
        records.add(new PatientRecord(1, 5.0, "ECG", 1010L)); // spike

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Abnormal ECG Peak"));
    }

    @Test
    void testHeartRateStrategy_flatLineNoAlert() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            records.add(new PatientRecord(1, 1.0, "ECG", 1000L + i));
        }

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Abnormal ECG Peak"));
    }

    @Test
    void testHeartRateStrategy_manualTrigger() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 1.0, "Alert", 1000L));

        assertEquals(1, countWith(strategy.checkAlert(1, records), "Manual Triggered Alert"));
    }

    @Test
    void testHeartRateStrategy_resolvedAlertNotTriggered() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 0.0, "Alert", 1000L));

        assertEquals(0, countWith(strategy.checkAlert(1, records), "Manual Triggered Alert"));
    }

    @Test
    void testHeartRateStrategy_returnsECGAlertType() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 1.0, "Alert", 1000L));

        List<Alert> alerts = strategy.checkAlert(1, records);
        assertFalse(alerts.isEmpty());
        assertInstanceOf(ECGAlert.class, alerts.get(0),
                "HeartRateStrategy should produce ECGAlert instances");
    }

    @Test
    void testEmptyRecordsProducesNoAlerts() {
        List<PatientRecord> empty = List.of();
        assertEquals(0, new BloodPressureStrategy().checkAlert(1, empty).size());
        assertEquals(0, new OxygenSaturationStrategy().checkAlert(1, empty).size());
        assertEquals(0, new HeartRateStrategy().checkAlert(1, empty).size());
    }


    private long countWith(List<Alert> alerts, String keyword) {
        return alerts.stream().filter(a -> a.getCondition().contains(keyword)).count();
    }
}

package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link AlertGenerator}: each test sets up a small data set that
 * should (or should not) trigger one specific alert, then asserts on the
 * captured alerts.
 *
 * <p>We use a tiny test subclass {@code CapturingGenerator} that overrides
 * {@code triggerAlert} to capture alerts in a list. This lets us inspect
 * exactly which alerts fired without parsing console output.
 */
class AlertGeneratorTest {

    /**
     * Test subclass that captures alerts instead of printing them. Lives in
     * the test source set so it can extend AlertGenerator from the same
     * package and access {@code protected} {@code triggerAlert}.
     */
    static class CapturingGenerator extends AlertGenerator {
        final List<Alert> captured = new ArrayList<>();
        CapturingGenerator(DataStorage storage) { super(storage); }
        @Override
        protected void triggerAlert(Alert alert) { captured.add(alert); }
    }

    /** Returns the number of captured alerts that contain the given keyword. */
    private long countWith(List<Alert> alerts, String keyword) {
        return alerts.stream().filter(a -> a.getCondition().contains(keyword)).count();
    }

    // -- Critical thresholds --------------------------------------------------

    @Test
    void testCriticalHighSystolicTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Critical High Systolic Pressure"));
    }

    @Test
    void testCriticalLowSystolicTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Critical Low Systolic Pressure"));
    }

    @Test
    void testCriticalHighDiastolicTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 130.0, "DiastolicPressure", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Critical High Diastolic Pressure"));
    }

    @Test
    void testCriticalLowDiastolicTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 50.0, "DiastolicPressure", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Critical Low Diastolic Pressure"));
    }

    @Test
    void testNormalBloodPressureNoAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Critical"));
    }

    // -- Trends ---------------------------------------------------------------

    @Test
    void testIncreasingSystolicTrendTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 2000L); // +15
        storage.addPatientData(1, 130.0, "SystolicPressure", 3000L); // +15

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Increasing SystolicPressure Trend"));
    }

    @Test
    void testDecreasingDiastolicTrendTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "DiastolicPressure", 1000L);
        storage.addPatientData(1, 85.0, "DiastolicPressure", 2000L);  // -15
        storage.addPatientData(1, 70.0, "DiastolicPressure", 3000L);  // -15

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Decreasing DiastolicPressure Trend"));
    }

    @Test
    void testTinyBpChangesDoNotTriggerTrend() {
        DataStorage storage = new DataStorage();
        // Each step is only 5 mmHg — below the >10 threshold — so no trend alert.
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 105.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 110.0, "SystolicPressure", 3000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Trend"));
    }

    // -- Saturation -----------------------------------------------------------

    @Test
    void testLowSaturationTriggersAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 90.0, "Saturation", 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Low Blood Saturation"));
    }

    @Test
    void testRapidSaturationDropTriggersAlert() {
        DataStorage storage = new DataStorage();
        // Drop of 6% within 10 minutes (600_000 ms).
        storage.addPatientData(1, 99.0, "Saturation", 1_000L);
        storage.addPatientData(1, 93.0, "Saturation", 1_000L + 5L * 60L * 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Rapid Blood Saturation Drop"));
    }

    @Test
    void testRapidDropOutsideWindowDoesNotTrigger() {
        DataStorage storage = new DataStorage();
        // Same 6% drop but 15 minutes apart — outside the 10-minute window.
        storage.addPatientData(1, 99.0, "Saturation", 1_000L);
        storage.addPatientData(1, 93.0, "Saturation", 1_000L + 15L * 60L * 1000L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Rapid Blood Saturation Drop"));
    }

    // -- Combined -------------------------------------------------------------

    @Test
    void testHypotensiveHypoxemiaTriggersWhenBothLow() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L); // < 90
        storage.addPatientData(1, 90.0, "Saturation", 2000L);       // < 92

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaDoesNotTriggerWhenOnlyOneLow() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "Saturation", 2000L); // saturation OK

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Hypotensive Hypoxemia"));
    }

    // -- ECG ------------------------------------------------------------------

    @Test
    void testEcgPeakTriggersAlert() {
        DataStorage storage = new DataStorage();
        // Baseline of 1.0 readings, then a sharp peak of 5.0 (>1.5 * 1.0).
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i);
        }
        storage.addPatientData(1, 5.0, "ECG", 1010L);

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Abnormal ECG Peak"));
    }

    @Test
    void testEcgFlatLineNoAlert() {
        DataStorage storage = new DataStorage();
        for (int i = 0; i < 15; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i);
        }

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Abnormal ECG Peak"));
    }

    // -- Manual trigger -------------------------------------------------------

    @Test
    void testManualTriggeredAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 1.0, "Alert", 1000L); // 1.0 == triggered

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(1, countWith(gen.captured, "Manual Triggered Alert"));
    }

    @Test
    void testResolvedManualAlertDoesNotTrigger() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 0.0, "Alert", 1000L); // 0.0 == resolved

        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(storage.getAllPatients().get(0));

        assertEquals(0, countWith(gen.captured, "Manual Triggered Alert"));
    }

    // -- Edge cases -----------------------------------------------------------

    @Test
    void testNullPatientDoesNothing() {
        DataStorage storage = new DataStorage();
        CapturingGenerator gen = new CapturingGenerator(storage);
        assertDoesNotThrow(() -> gen.evaluateData(null));
        assertTrue(gen.captured.isEmpty());
    }

    @Test
    void testEmptyPatientNoAlerts() {
        DataStorage storage = new DataStorage();
        Patient p = new Patient(99);
        CapturingGenerator gen = new CapturingGenerator(storage);
        gen.evaluateData(p);
        assertTrue(gen.captured.isEmpty());
    }

    @Test
    void testDefaultTriggerAlertPrintsToStdout() {
        // Smoke-test the default printing implementation just so it appears in coverage.
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 200.0, "SystolicPressure", 1000L);
        AlertGenerator gen = new AlertGenerator(storage);
        assertDoesNotThrow(() -> gen.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testAlertGettersReturnConstructorValues() {
        Alert a = new Alert("42", "Some Condition", 9876L);
        assertEquals("42", a.getPatientId());
        assertEquals("Some Condition", a.getCondition());
        assertEquals(9876L, a.getTimestamp());
    }
}

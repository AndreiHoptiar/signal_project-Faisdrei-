package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end integration test added in response to Week 3 feedback
 * ("Add one integration test that reads a small generated-style file and
 * verifies that the expected alert is produced").
 *
 * <p>Each test writes a tiny output file that uses the exact same line
 * format as {@code FileOutputStrategy} produces in a real simulator run.
 * The file is then read back through {@link FileDataReader} into
 * {@link DataStorage}, and {@link AlertGenerator} is run over the loaded
 * patients. The test then asserts on the alerts that were collected by
 * the {@link AlertManager}, which is the same flow used by
 * {@link DataStorage#main(String[])}.
 */
class EndToEndIntegrationTest {

    @Test
    void smallGeneratedFile_triggersExpectedCriticalSystolicAlert(@TempDir Path tempDir)
            throws IOException {
        // Arrange: write a tiny "SystolicPressure.txt" that mimics the
        // simulator's output format and contains one critically high reading.
        Path file = tempDir.resolve("SystolicPressure.txt");
        Files.write(file, List.of(
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0",
                "Patient ID: 1, Timestamp: 2000, Label: SystolicPressure, Data: 125.0",
                // Above the 180 threshold → must trigger "Critical High Systolic Pressure".
                "Patient ID: 1, Timestamp: 3000, Label: SystolicPressure, Data: 195.0"
        ));

        // Act: end-to-end pipeline.
        DataStorage storage = new DataStorage();
        new FileDataReader(tempDir.toString()).readData(storage);

        AlertGenerator generator = new AlertGenerator(storage);
        for (Patient patient : storage.getAllPatients()) {
            generator.evaluateData(patient);
        }

        // Assert: the AlertManager captured exactly one Critical High alert
        // for patient 1, with the timestamp of the offending reading.
        List<Alert> alerts = generator.getAlertManager().getAlertsForPatient("1");
        assertNotNull(alerts, "AlertManager must return a non-null list");
        long criticalHigh = alerts.stream()
                .filter(a -> a.getCondition().equals("Critical High Systolic Pressure"))
                .count();
        assertEquals(1L, criticalHigh,
                "End-to-end run should fire exactly one Critical High Systolic alert");

        // Sanity check: the alert is anchored at the bad reading's timestamp.
        Alert match = alerts.stream()
                .filter(a -> a.getCondition().equals("Critical High Systolic Pressure"))
                .findFirst()
                .orElseThrow();
        assertEquals(3000L, match.getTimestamp(),
                "Alert timestamp should match the offending record");
    }

    @Test
    void smallGeneratedFile_hypotensiveHypoxemiaFiresOnlyWhenWithinTimeWindow(
            @TempDir Path tempDir) throws IOException {
        // Two separate files, one with both low readings within 5 minutes
        // and one with the same readings 30 minutes apart.
        Path within = tempDir.resolve("within");
        Path outside = tempDir.resolve("outside");
        Files.createDirectories(within);
        Files.createDirectories(outside);

        // Within the 10-minute hypoxemia window (5 minutes apart) → must fire.
        Files.write(within.resolve("SystolicPressure.txt"), List.of(
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 85.0"
        ));
        Files.write(within.resolve("Saturation.txt"), List.of(
                "Patient ID: 1, Timestamp: 301000, Label: Saturation, Data: 88.0"
        ));

        // 30 minutes apart → must NOT fire under the new windowed rule.
        Files.write(outside.resolve("SystolicPressure.txt"), List.of(
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 85.0"
        ));
        Files.write(outside.resolve("Saturation.txt"), List.of(
                "Patient ID: 1, Timestamp: 1801000, Label: Saturation, Data: 88.0"
        ));

        // --- Within window ---
        DataStorage inStore = new DataStorage();
        new FileDataReader(within.toString()).readData(inStore);
        AlertGenerator inGen = new AlertGenerator(inStore);
        inStore.getAllPatients().forEach(inGen::evaluateData);
        long withinCount = inGen.getAlertManager().getAlerts().stream()
                .filter(a -> a.getCondition().equals("Hypotensive Hypoxemia"))
                .count();
        assertEquals(1L, withinCount,
                "Within-window readings should fire one Hypotensive Hypoxemia alert");

        // --- Outside window ---
        DataStorage outStore = new DataStorage();
        new FileDataReader(outside.toString()).readData(outStore);
        AlertGenerator outGen = new AlertGenerator(outStore);
        outStore.getAllPatients().forEach(outGen::evaluateData);
        long outsideCount = outGen.getAlertManager().getAlerts().stream()
                .filter(a -> a.getCondition().equals("Hypotensive Hypoxemia"))
                .count();
        assertEquals(0L, outsideCount,
                "Readings more than 10 minutes apart should NOT fire the alert");
    }

    @Test
    void dataStorageMain_runsWithRealFileDirectory(@TempDir Path tempDir) throws IOException {
        // The whole point of this test: prove that DataStorage.main now
        // accepts a directory argument and reads it via FileDataReader.
        // We write a file with one critically low saturation reading and
        // assert that main() completes without throwing.
        Path file = tempDir.resolve("Saturation.txt");
        Files.write(file, List.of(
                "Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 85.0"
        ));

        // DataStorage.main now reads the supplied directory before
        // evaluating alerts; this used to be a no-op smoke test.
        DataStorage.main(new String[] { tempDir.toString() });

        // No exception → success. We additionally verify that the same flow,
        // when run via the public API, produces the expected Low Blood
        // Saturation alert.
        DataStorage storage = new DataStorage();
        new FileDataReader(tempDir.toString()).readData(storage);
        AlertGenerator generator = new AlertGenerator(storage);
        storage.getAllPatients().forEach(generator::evaluateData);
        assertTrue(generator.getAlertManager().getAlerts().stream()
                        .anyMatch(a -> a.getCondition().equals("Low Blood Saturation")),
                "Low saturation reading should fire the Low Blood Saturation alert");
    }
}

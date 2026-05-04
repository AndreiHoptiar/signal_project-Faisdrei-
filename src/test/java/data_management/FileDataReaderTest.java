package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests for {@link FileDataReader}: writes fake output files in a temporary
 * directory and asserts that the reader correctly parses every line into
 * {@link DataStorage}, while gracefully skipping malformed lines.
 */
class FileDataReaderTest {

    @Test
    void testReadsAllLinesIntoStorage(@TempDir Path tempDir) throws IOException {
        // Arrange: write a sample HeartRate file in the format produced
        // by FileOutputStrategy.
        Path file = tempDir.resolve("HeartRate.txt");
        Files.write(file, List.of(
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 80.0",
                "Patient ID: 1, Timestamp: 2000, Label: HeartRate, Data: 82.5",
                "Patient ID: 2, Timestamp: 3000, Label: HeartRate, Data: 90.0"
        ));

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());

        // Act
        reader.readData(storage);

        // Assert
        List<PatientRecord> p1 = storage.getRecords(1, 0L, Long.MAX_VALUE);
        List<PatientRecord> p2 = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(2, p1.size());
        assertEquals(1, p2.size());
        assertEquals(80.0, p1.get(0).getMeasurementValue());
        assertEquals("HeartRate", p1.get(0).getRecordType());
    }

    @Test
    void testHandlesMultipleFiles(@TempDir Path tempDir) throws IOException {
        Files.write(tempDir.resolve("HeartRate.txt"), List.of(
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 80.0"
        ));
        Files.write(tempDir.resolve("Saturation.txt"), List.of(
                "Patient ID: 1, Timestamp: 1500, Label: Saturation, Data: 95.0"
        ));

        DataStorage storage = new DataStorage();
        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size(), "Both files should contribute records");
    }

    @Test
    void testMalformedLineIsSkipped(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("HeartRate.txt");
        Files.write(file, List.of(
                "this line is junk",
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 80.0",
                "Patient ID: notANumber, Timestamp: 2000, Label: HeartRate, Data: 82.5"
        ));

        DataStorage storage = new DataStorage();
        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size(),
                "Only the well-formed line should make it into storage");
    }

    @Test
    void testTriggeredAlertEncodedAsOne(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Alert.txt");
        Files.write(file, List.of(
                "Patient ID: 1, Timestamp: 1000, Label: Alert, Data: triggered",
                "Patient ID: 1, Timestamp: 2000, Label: Alert, Data: resolved"
        ));

        DataStorage storage = new DataStorage();
        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(1.0, records.get(0).getMeasurementValue());
        assertEquals(0.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testMissingDirectoryThrowsIoException() {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader("/path/that/does/not/exist/seriously/abc123");
        assertThrows(IOException.class, () -> reader.readData(storage));
    }
}

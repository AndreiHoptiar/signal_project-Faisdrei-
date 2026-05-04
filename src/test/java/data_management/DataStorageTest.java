package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Tests for {@link DataStorage}: verifies adding, retrieving and bulk
 * access to patient records, including edge cases like an unknown patient
 * ID and empty time ranges.
 */
class DataStorageTest {

    @Test
    void testAddAndGetRecords_returnsAllInRange() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size(), "Both records should fall in the range");
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecords_unknownPatientReturnsEmpty() {
        DataStorage storage = new DataStorage();
        List<PatientRecord> records = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertNotNull(records, "Should return an empty list, never null");
        assertTrue(records.isEmpty(), "Unknown patient should yield no records");
    }

    @Test
    void testGetRecords_outsideTimeWindowReturnsEmpty() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        // Window does not include any record.
        List<PatientRecord> records = storage.getRecords(1, 2000L, 3000L);
        assertTrue(records.isEmpty(), "No records should be in this range");
    }

    @Test
    void testGetAllPatients_returnsAllPatientsAdded() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "HeartRate", 100L);
        storage.addPatientData(2, 110.0, "HeartRate", 100L);
        storage.addPatientData(3, 120.0, "HeartRate", 100L);
        List<Patient> patients = storage.getAllPatients();
        assertEquals(3, patients.size());
    }

    @Test
    void testAddingSamePatientTwiceReusesPatientObject() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(7, 95.0, "HeartRate", 100L);
        storage.addPatientData(7, 96.0, "HeartRate", 200L);
        assertEquals(1, storage.getAllPatients().size(),
                "Same patientId must not create a duplicate Patient");
        List<PatientRecord> records = storage.getRecords(7, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    void testMainMethodRunsWithoutCrashing() {
        // The main method is mostly a smoke test for the "DataStorage" entry point;
        // we just want it to run without exceptions.
        assertDoesNotThrow(() -> DataStorage.main(new String[]{}));
    }
}

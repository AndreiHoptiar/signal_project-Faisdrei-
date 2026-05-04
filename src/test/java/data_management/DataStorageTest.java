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

        List<PatientRecord> records = storage.ge
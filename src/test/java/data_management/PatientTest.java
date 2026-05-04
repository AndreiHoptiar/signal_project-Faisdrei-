package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Tests for {@link Patient}: focuses on the time-range filter in
 * {@code getRecords} since that is the method we just implemented.
 */
class PatientTest {

    @Test
    void testGetRecords_withinExactRange() {
        Patient p = new Patient(1);
        p.addRecord(80.0, "HeartRate", 1000L);
        p.addRecord(82.0, "HeartRate", 1500L);
        p.addRecord(84.0, "HeartRate", 2000L);

        List<PatientRecord> records = p.getRecords(1000L, 2000L);
        assertEquals(3, records.size(), "All three records sit on the boundary or inside");
    }

    @Test
    void testGetRecords_excludesOutsideRange() {
        Patient p = new Patient(1);
        p.addRecord(80.0, "HeartRate", 500L);
        p.addRecord(82.0, "HeartRate", 1500L);
        p.addRecord(84.0, "HeartRate", 2500L);

        List<PatientRecord> records = p.getRecords(1000L, 2000L);
        assertEquals(1, records.size(), "Only the middle record is in range");
        assertEquals(82.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_emptyPatient() {
        Patient p = new Patient(42);
        List<PatientRecord> records = p.getRecords(0L, Long.MAX_VALUE);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecords_zeroLengthWindowIsExclusiveEnoughToCatchExactMatch() {
        Patient p = new Patient(1);
        p.addRecord(99.0, "HeartRate", 1234L);
        // start == end == exact timestamp should still include the record (inclusive bounds).
        List<PatientRecord> records = p.getRecords(1234L, 1234L);
        assertEquals(1, records.size());
    }

    @Test
    void testGetPatientId_returnsConstructorValue() {
        Patient p = new Patient(101);
        assertEquals(101, p.getPatientId());
    }

    @Test
    void testGetAllRecords_returnsDefensiveCopy() {
        Patient p = new Patient(1);
        p.addRecord(70.0, "HeartRate", 100L);
        List<PatientRecord> snapshot = p.getAllRecords();
        snapshot.clear(); // mutate the returned list
        assertEquals(1, p.getAllRecords().size(),
                "Clearing the returned list must not affect internal state");
    }
}

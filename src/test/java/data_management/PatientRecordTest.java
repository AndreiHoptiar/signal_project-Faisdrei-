package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.PatientRecord;

/**
 * Tests for {@link PatientRecord}: simple getter checks.
 */
class PatientRecordTest {

    @Test
    void testAllGettersReturnConstructorValues() {
        PatientRecord r = new PatientRecord(7, 99.5, "HeartRate", 1234567890L);
        assertEquals(7, r.getPatientId());
        assertEquals(99.5, r.getMeasurementValue());
        assertEquals("HeartRate", r.getRecordType());
        assertEquals(1234567890L, r.getTimestamp());
    }
}

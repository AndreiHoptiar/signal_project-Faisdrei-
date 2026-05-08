package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientImpl;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Unit tests for {@link WebSocketClientImpl}.
 *
 * <p>Tests call the package-private {@link WebSocketClientImpl#parseAndStore(String)}
 * method directly via the test-friendly constructor so no live WebSocket
 * connection is needed. This covers all parsing, error-handling and storage
 * paths without network infrastructure.
 */
class WebSocketClientTest {

    /** Creates a client pre-wired to the given storage — no real connection. */
    private WebSocketClientImpl client(DataStorage ds) throws URISyntaxException {
        return new WebSocketClientImpl("ws://localhost:9999", ds);
    }


    @Test
    void testWellFormedMessageStoresRecord() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        client(ds).parseAndStore("1,1000,HeartRate,82.0");

        List<PatientRecord> records = ds.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1,          records.size());
        assertEquals(82.0,       records.get(0).getMeasurementValue());
        assertEquals("HeartRate",records.get(0).getRecordType());
        assertEquals(1000L,      records.get(0).getTimestamp());
        assertEquals(1,          records.get(0).getPatientId());
    }

    @Test
    void testSaturationWithPercentSignParsedCorrectly() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        client(ds).parseAndStore("2,2000,Saturation,97.0%");

        List<PatientRecord> records = ds.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(1,    records.size());
        assertEquals(97.0, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test
    void testMultipleMessagesStoredForSamePatient() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        WebSocketClientImpl c = client(ds);
        c.parseAndStore("3,1000,HeartRate,80.0");
        c.parseAndStore("3,2000,HeartRate,82.0");
        c.parseAndStore("3,3000,HeartRate,84.0");

        assertEquals(3, ds.getRecords(3, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testMessagesForDifferentPatientsStoredSeparately() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        WebSocketClientImpl c = client(ds);
        c.parseAndStore("1,1000,HeartRate,80.0");
        c.parseAndStore("2,1000,HeartRate,90.0");

        assertEquals(1, ds.getRecords(1, 0L, Long.MAX_VALUE).size());
        assertEquals(1, ds.getRecords(2, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testIntegerDataValueParsedCorrectly() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        client(ds).parseAndStore("5,3000,SystolicPressure,120");

        List<PatientRecord> records = ds.getRecords(5, 0L, Long.MAX_VALUE);
        assertEquals(120.0, records.get(0).getMeasurementValue(), 0.001);
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }


    @Test
    void testMalformedMessageTooFewFieldsIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("1,1000,HeartRate")); // only 3 fields
        assertTrue(ds.getAllPatients().isEmpty(),
                "Malformed message with too few fields must not add any record");
    }

    @Test
    void testNonNumericPatientIdIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("abc,1000,HeartRate,80.0"));
        assertTrue(ds.getAllPatients().isEmpty());
    }

    @Test
    void testNonNumericTimestampIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("1,notALong,HeartRate,80.0"));
        assertTrue(ds.getAllPatients().isEmpty());
    }

    @Test
    void testNonNumericDataIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("1,1000,HeartRate,notADouble"));
        assertTrue(ds.getAllPatients().isEmpty());
    }

    @Test
    void testNullMessageIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore(null));
        assertTrue(ds.getAllPatients().isEmpty());
    }

    @Test
    void testBlankMessageIsSkipped() throws URISyntaxException {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("   "));
        assertTrue(ds.getAllPatients().isEmpty());
    }

    @Test
    void testExtraFieldsHandledGracefully() throws URISyntaxException {
        // With split limit 4, any content after the 4th comma stays in field[3].
        // "80.0,extra" cannot be parsed as a double, so the record is skipped.
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> client(ds).parseAndStore("1,1000,HeartRate,80.0,extra"));
        assertTrue(ds.getAllPatients().isEmpty(),
                "Data field containing a comma should fail to parse and be skipped");
    }


    @Test
    void testImplementsDataReader() throws URISyntaxException {
        WebSocketClientImpl c = new WebSocketClientImpl("ws://localhost:9999");
        assertInstanceOf(com.data_management.DataReader.class, c);
    }

    @Test
    void testConstructorAcceptsValidUri() {
        assertDoesNotThrow(() -> new WebSocketClientImpl("ws://localhost:8080"));
    }

    @Test
    void testConstructorRejectsInvalidUri() {
        assertThrows(URISyntaxException.class,
                () -> new WebSocketClientImpl("not a valid uri !!"));
    }
}

package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Singleton pattern on {@link DataStorage} and
 * {@link HealthDataSimulator}.
 */
class SingletonTest {


    @Test
    void testDataStorageGetInstanceReturnsSameObject() {
        DataStorage first  = DataStorage.getInstance();
        DataStorage second = DataStorage.getInstance();
        assertSame(first, second,
                "DataStorage.getInstance() must always return the same instance");
    }

    @Test
    void testDataStorageGetInstanceIsNotNull() {
        assertNotNull(DataStorage.getInstance());
    }

    @Test
    void testDataStorageSingletonRetainsData() {
        DataStorage instance = DataStorage.getInstance();
        // Add a record via the singleton; retrieve it via a second getInstance() call.
        instance.addPatientData(9999, 42.0, "TestLabel", 1L);
        DataStorage same = DataStorage.getInstance();
        assertFalse(same.getRecords(9999, 0L, Long.MAX_VALUE).isEmpty(),
                "Data added through one getInstance() reference must be visible through another");
    }

    @Test
    void testNewDataStorageIsIndependentFromSingleton() {
        DataStorage fresh = new DataStorage();
        fresh.addPatientData(8888, 99.0, "TestLabel", 1L);

        assertNotSame(fresh, DataStorage.getInstance());
    }


    @Test
    void testHealthDataSimulatorGetInstanceReturnsSameObject() {
        HealthDataSimulator first  = HealthDataSimulator.getInstance();
        HealthDataSimulator second = HealthDataSimulator.getInstance();
        assertSame(first, second,
                "HealthDataSimulator.getInstance() must always return the same instance");
    }

    @Test
    void testHealthDataSimulatorGetInstanceIsNotNull() {
        assertNotNull(HealthDataSimulator.getInstance());
    }
}

package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Factory Method pattern: {@link AlertFactory} and its three
 * concrete subclasses ({@link BloodPressureAlertFactory},
 * {@link BloodOxygenAlertFactory}, {@link ECGAlertFactory}).
 */
class AlertFactoryTest {


    @Test
    void testBloodPressureFactoryReturnsBloodPressureAlert() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "Critical High Systolic Pressure", 1000L);

        assertInstanceOf(BloodPressureAlert.class, alert,
                "BloodPressureAlertFactory should return a BloodPressureAlert");
    }

    @Test
    void testBloodPressureFactoryPreservesFields() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("7", "Critical Low Diastolic Pressure", 5000L);

        assertEquals("7",    alert.getPatientId());
        assertEquals("Critical Low Diastolic Pressure", alert.getCondition());
        assertEquals(5000L,  alert.getTimestamp());
    }


    @Test
    void testBloodOxygenFactoryReturnsBloodOxygenAlert() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "Low Blood Saturation", 2000L);

        assertInstanceOf(BloodOxygenAlert.class, alert,
                "BloodOxygenAlertFactory should return a BloodOxygenAlert");
    }

    @Test
    void testBloodOxygenFactoryPreservesFields() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("3", "Rapid Blood Saturation Drop", 3000L);

        assertEquals("3",                           alert.getPatientId());
        assertEquals("Rapid Blood Saturation Drop", alert.getCondition());
        assertEquals(3000L,                         alert.getTimestamp());
    }


    @Test
    void testEcgFactoryReturnsECGAlert() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("5", "Abnormal ECG Peak", 9000L);

        assertInstanceOf(ECGAlert.class, alert,
                "ECGAlertFactory should return an ECGAlert");
    }

    @Test
    void testEcgFactoryPreservesFields() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("9", "Manual Triggered Alert", 1234L);

        assertEquals("9",                       alert.getPatientId());
        assertEquals("Manual Triggered Alert",  alert.getCondition());
        assertEquals(1234L,                     alert.getTimestamp());
    }


    @Test
    void testAllFactoriesReturnAlertSupertype() {
        AlertFactory[] factories = {
            new BloodPressureAlertFactory(),
            new BloodOxygenAlertFactory(),
            new ECGAlertFactory()
        };
        for (AlertFactory f : factories) {
            Alert alert = f.createAlert("1", "test", 0L);
            assertInstanceOf(Alert.class, alert,
                    f.getClass().getSimpleName() + " must return an Alert");
        }
    }
}

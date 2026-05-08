package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Decorator pattern: {@link AlertDecorator},
 * {@link PriorityAlertDecorator}, and {@link RepeatedAlertDecorator}.
 */
class AlertDecoratorTest {

    private static final long TS = 1000L;


    @Test
    void testPriorityDecoratorPrependsTag() {
        Alert base      = new Alert("1", "Low Blood Saturation", TS);
        Alert decorated = new PriorityAlertDecorator(base, "HIGH");

        assertEquals("[HIGH] Low Blood Saturation", decorated.getCondition());
    }

    @Test
    void testPriorityDecoratorPreservesPatientId() {
        Alert base      = new Alert("42", "Some Condition", TS);
        Alert decorated = new PriorityAlertDecorator(base, "MEDIUM");

        assertEquals("42", decorated.getPatientId());
    }

    @Test
    void testPriorityDecoratorPreservesTimestamp() {
        Alert base      = new Alert("1", "Some Condition", 9999L);
        Alert decorated = new PriorityAlertDecorator(base, "LOW");

        assertEquals(9999L, decorated.getTimestamp());
    }

    @Test
    void testPriorityDecoratorGetPriority() {
        PriorityAlertDecorator decorated =
                new PriorityAlertDecorator(new Alert("1", "cond", TS), "HIGH");
        assertEquals("HIGH", decorated.getPriority());
    }

    @Test
    void testPriorityDecoratorIsAlertSubtype() {
        Alert decorated = new PriorityAlertDecorator(new Alert("1", "cond", TS), "HIGH");
        assertInstanceOf(Alert.class, decorated);
    }


    @Test
    void testRepeatedDecoratorAppendsSuffix() {
        Alert base      = new Alert("3", "Critical High Systolic Pressure", TS);
        Alert decorated = new RepeatedAlertDecorator(base, 3);

        assertEquals("Critical High Systolic Pressure (Repeated 3 times)",
                decorated.getCondition());
    }

    @Test
    void testRepeatedDecoratorPreservesPatientId() {
        Alert base      = new Alert("7", "Some Condition", TS);
        Alert decorated = new RepeatedAlertDecorator(base, 2);

        assertEquals("7", decorated.getPatientId());
    }

    @Test
    void testRepeatedDecoratorPreservesTimestamp() {
        Alert base      = new Alert("1", "Some Condition", 5555L);
        Alert decorated = new RepeatedAlertDecorator(base, 1);

        assertEquals(5555L, decorated.getTimestamp());
    }

    @Test
    void testRepeatedDecoratorGetRepeatCount() {
        RepeatedAlertDecorator decorated =
                new RepeatedAlertDecorator(new Alert("1", "cond", TS), 5);
        assertEquals(5, decorated.getRepeatCount());
    }

    @Test
    void testRepeatedDecoratorIsAlertSubtype() {
        Alert decorated = new RepeatedAlertDecorator(new Alert("1", "cond", TS), 1);
        assertInstanceOf(Alert.class, decorated);
    }


    @Test
    void testStackedDecorators() {
        Alert base      = new Alert("1", "Abnormal ECG Peak", TS);
        Alert repeated  = new RepeatedAlertDecorator(base, 2);
        Alert priority  = new PriorityAlertDecorator(repeated, "HIGH");

        // Priority wraps Repeated: "[HIGH] Abnormal ECG Peak (Repeated 2 times)"
        assertEquals("[HIGH] Abnormal ECG Peak (Repeated 2 times)",
                priority.getCondition());
    }

    @Test
    void testDecoratingBloodPressureAlert() {
        Alert base      = new BloodPressureAlertFactory()
                              .createAlert("2", "Critical High Systolic Pressure", TS);
        Alert decorated = new PriorityAlertDecorator(base, "HIGH");

        // Type is still reachable through the public getter on the wrapped alert
        assertInstanceOf(BloodPressureAlert.class,
                ((AlertDecorator) decorated).getDecoratedAlert());
        assertEquals("[HIGH] Critical High Systolic Pressure", decorated.getCondition());
    }
}

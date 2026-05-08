package com.alerts;

/**
 * A decorator that appends a repeat-count suffix to an alert's condition string.
 *
 * <p>Use this decorator when the same dangerous condition has been detected
 * more than once, so clinical staff can see at a glance how persistent the
 * problem is.
 *
 * <p>Example:
 * <pre>
 *   Alert base     = new Alert("7", "Low Blood Saturation", ts);
 *   Alert repeated = new RepeatedAlertDecorator(base, 3);
 *   repeated.getCondition(); // "Low Blood Saturation (Repeated 3 times)"
 * </pre>
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;

    /**
     * Constructs a {@code RepeatedAlertDecorator} wrapping the given alert.
     *
     * @param alert       the alert to decorate; must not be {@code null}
     * @param repeatCount the number of times the condition has been observed
     */
    public RepeatedAlertDecorator(Alert alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the wrapped alert's condition with a repeat-count suffix appended.
     *
     * @return e.g. {@code "Low Blood Saturation (Repeated 3 times)"}
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " (Repeated " + repeatCount + " times)";
    }

    /**
     * Returns how many times the condition has been observed.
     *
     * @return the repeat count
     */
    public int getRepeatCount() {
        return repeatCount;
    }
}

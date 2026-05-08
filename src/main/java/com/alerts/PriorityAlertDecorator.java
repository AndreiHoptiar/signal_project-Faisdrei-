package com.alerts;

/**
 * A decorator that prepends a priority tag to an alert's condition string.
 *
 * <p>Example:
 * <pre>
 *   Alert base      = new Alert("3", "Critical High Systolic Pressure", ts);
 *   Alert priority  = new PriorityAlertDecorator(base, "HIGH");
 *   priority.getCondition(); // "[HIGH] Critical High Systolic Pressure"
 * </pre>
 *
 * <p>Priority values are arbitrary strings; common conventions are
 * {@code "LOW"}, {@code "MEDIUM"}, and {@code "HIGH"}, but any label
 * can be used.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priority;

    /**
     * Constructs a {@code PriorityAlertDecorator} wrapping the given alert.
     *
     * @param alert    the alert to decorate; must not be {@code null}
     * @param priority the priority label to prepend (e.g. {@code "HIGH"})
     */
    public PriorityAlertDecorator(Alert alert, String priority) {
        super(alert);
        this.priority = priority;
    }

    /**
     * Returns the wrapped alert's condition prefixed with the priority tag.
     *
     * @return e.g. {@code "[HIGH] Critical High Systolic Pressure"}
     */
    @Override
    public String getCondition() {
        return "[" + priority + "] " + decoratedAlert.getCondition();
    }

    /**
     * Returns the priority label assigned to this alert.
     *
     * @return the priority string (e.g. {@code "HIGH"})
     */
    public String getPriority() {
        return priority;
    }
}

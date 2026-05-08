package com.alerts;

/**
 * Abstract base class for the Decorator pattern applied to {@link Alert}.
 *
 * <p>An {@code AlertDecorator} wraps an existing {@link Alert} and forwards
 * all getter calls to the wrapped instance. Concrete subclasses override one
 * or more getters to add extra behaviour — for example prefixing the condition
 * string with a priority tag, or appending a repeat-count suffix.
 *
 * <p>Because {@link Alert} is a class (not an interface), decoration is
 * achieved through inheritance: {@code AlertDecorator} extends {@link Alert}
 * and stores the wrapped object in {@link #decoratedAlert}.
 *
 * <p>Usage example:
 * <pre>
 *   Alert base      = new Alert("1", "Low Blood Saturation", ts);
 *   Alert decorated = new PriorityAlertDecorator(base, "HIGH");
 *   System.out.println(decorated.getCondition()); // "[HIGH] Low Blood Saturation"
 * </pre>
 */
public abstract class AlertDecorator extends Alert {

    /** The alert being decorated. */
    protected final Alert decoratedAlert;

    /**
     * Constructs an {@code AlertDecorator} wrapping the given alert.
     * The patient ID, condition, and timestamp are copied from the wrapped
     * alert so that the base-class fields are initialised.
     *
     * @param alert the alert to wrap; must not be {@code null}
     */
    protected AlertDecorator(Alert alert) {
        super(alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
        this.decoratedAlert = alert;
    }

    /**
     * Returns the patient ID of the wrapped alert.
     *
     * @return patient ID string
     */
    @Override
    public String getPatientId() {
        return decoratedAlert.getPatientId();
    }

    /**
     * Returns the condition of the wrapped alert.
     * Subclasses typically override this to augment the string.
     *
     * @return condition string
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition();
    }

    /**
     * Returns the timestamp of the wrapped alert.
     *
     * @return milliseconds since the Unix epoch
     */
    @Override
    public long getTimestamp() {
        return decoratedAlert.getTimestamp();
    }

    /**
     * Returns the inner {@link Alert} that this decorator wraps.
     * Exposed so that tests and external code can inspect the decorated object
     * without needing to be in the same package.
     *
     * @return the wrapped alert; never {@code null}
     */
    public Alert getDecoratedAlert() {
        return decoratedAlert;
    }
}

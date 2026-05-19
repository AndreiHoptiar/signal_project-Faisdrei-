package com.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and dispatches alerts produced by {@link AlertGenerator}.
 *
 * <p>Before this class existed, {@link AlertGenerator#triggerAlert(Alert)} only
 * printed alerts to {@code System.out}, which meant the rest of the
 * application could not inspect or reuse them. That was flagged by the Week 3
 * feedback ("Triggered alerts are printed by default, so the main application
 * cannot easily inspect or reuse generated alerts later"). {@code AlertManager}
 * fixes that by:
 *
 * <ul>
 *   <li>Keeping every dispatched alert in an in-memory list, so callers can
 *       query the alerts after a run via {@link #getAlerts()} or
 *       {@link #getAlertsForPatient(String)}.</li>
 *   <li>Still printing the alert to {@code System.out} as before so existing
 *       console-based smoke tests keep working.</li>
 * </ul>
 *
 * <p>This class is intentionally simple and synchronous — the goal is to give
 * downstream code (and tests) a hook to read back the alerts that fired, not
 * to model the full hospital notification pipeline.
 */
public class AlertManager {

    /** All alerts that have been dispatched so far, in chronological dispatch order. */
    private final List<Alert> alerts = new ArrayList<>();

    /**
     * Dispatches a single alert. The alert is appended to the internal log
     * and a one-line summary is printed to {@code System.out} so that
     * existing console-style usages still see output.
     *
     * @param alert the alert to dispatch; must not be {@code null}
     * @throws NullPointerException if {@code alert} is {@code null}
     */
    public void dispatch(Alert alert) {
        if (alert == null) {
            throw new NullPointerException("alert must not be null");
        }
        alerts.add(alert);
        System.out.println("ALERT [" + alert.getTimestamp() + "] patient="
                + alert.getPatientId() + " condition=" + alert.getCondition());
    }

    /**
     * Returns an unmodifiable snapshot of every alert dispatched so far,
     * in the order they were dispatched.
     *
     * @return a defensive read-only view of all dispatched alerts
     */
    public List<Alert> getAlerts() {
        return Collections.unmodifiableList(new ArrayList<>(alerts));
    }

    /**
     * Returns the alerts that were dispatched for a specific patient.
     *
     * @param patientId the patient ID to filter on (matched by string equality
     *                  against {@link Alert#getPatientId()})
     * @return a new list containing only the alerts for that patient; never {@code null}
     */
    public List<Alert> getAlertsForPatient(String patientId) {
        List<Alert> result = new ArrayList<>();
        for (Alert a : alerts) {
            if (a.getPatientId().equals(patientId)) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Returns how many alerts have been dispatched.
     *
     * @return the number of alerts currently held by this manager
     */
    public int size() {
        return alerts.size();
    }

    /** Clears all stored alerts. Useful for tests and for long-running runs. */
    public void clear() {
        alerts.clear();
    }
}

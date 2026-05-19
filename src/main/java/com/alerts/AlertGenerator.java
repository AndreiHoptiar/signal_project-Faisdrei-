package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 *
 * <p><b>Design pattern — Strategy:</b> the actual alert-detection logic is
 * delegated to a configurable list of {@link AlertStrategy} objects. By default,
 * three strategies are installed: {@link BloodPressureStrategy},
 * {@link OxygenSaturationStrategy}, and {@link HeartRateStrategy}. Custom
 * strategies can be supplied via the multi-argument constructor.
 *
 * <p><b>Design pattern — Factory Method:</b> each strategy uses the appropriate
 * {@link AlertFactory} subclass to create typed {@link Alert} objects
 * ({@link BloodPressureAlert}, {@link BloodOxygenAlert}, {@link ECGAlert}).
 *
 * <p><b>Week 3 next-step fix:</b> every alert is now dispatched through an
 * {@link AlertManager}, so generated alerts can be inspected programmatically
 * (e.g. by {@code DataStorage.main} or by integration tests) instead of only
 * appearing on {@code System.out}.
 */
public class AlertGenerator {

    private final DataStorage dataStorage;
    private final List<AlertStrategy> strategies;
    private final AlertManager alertManager;

    /**
     * Constructs an {@code AlertGenerator} with the default set of strategies
     * ({@link BloodPressureStrategy}, {@link OxygenSaturationStrategy},
     * {@link HeartRateStrategy}) and a fresh {@link AlertManager}.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this(dataStorage, Arrays.asList(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy()
        ), new AlertManager());
    }

    /**
     * Constructs an {@code AlertGenerator} with a caller-supplied
     * {@link AlertManager}. Useful when several generators need to share a
     * single dispatch log, or when the caller wants to inspect the manager
     * after evaluation.
     *
     * @param dataStorage  the data storage system that provides access to patient data
     * @param alertManager the manager that records and dispatches alerts
     */
    public AlertGenerator(DataStorage dataStorage, AlertManager alertManager) {
        this(dataStorage, Arrays.asList(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy()
        ), alertManager);
    }

    /**
     * Constructs an {@code AlertGenerator} with a custom list of strategies.
     * This constructor is primarily useful for unit tests that need to inject
     * specific or mock strategies.
     *
     * @param dataStorage the data storage system that provides access to patient data
     * @param strategies  the alert strategies to run on every {@link #evaluateData} call
     */
    public AlertGenerator(DataStorage dataStorage, List<AlertStrategy> strategies) {
        this(dataStorage, strategies, new AlertManager());
    }

    /**
     * Constructs an {@code AlertGenerator} with a custom list of strategies
     * and a caller-supplied {@link AlertManager}.
     *
     * @param dataStorage  the data storage system that provides access to patient data
     * @param strategies   the alert strategies to run on every {@link #evaluateData} call
     * @param alertManager the manager that records and dispatches alerts
     */
    public AlertGenerator(DataStorage dataStorage,
                          List<AlertStrategy> strategies,
                          AlertManager alertManager) {
        this.dataStorage = dataStorage;
        this.strategies  = new ArrayList<>(strategies);
        this.alertManager = alertManager;
    }

    /**
     * Evaluates the given patient's data against every registered
     * {@link AlertStrategy}. For every alert returned by a strategy,
     * {@link #triggerAlert(Alert)} is called exactly once.
     *
     * @param patient the patient whose data should be evaluated
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            return;
        }

        // Sort all records by timestamp so strategies can rely on time order.
        List<PatientRecord> all = new ArrayList<>(patient.getAllRecords());
        all.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        // Delegate to each strategy and trigger every alert that comes back.
        for (AlertStrategy strategy : strategies) {
            for (Alert alert : strategy.checkAlert(patient.getPatientId(), all)) {
                triggerAlert(alert);
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. The default implementation
     * forwards the alert to the configured {@link AlertManager}, which both
     * logs it to {@code System.out} (preserving previous console behaviour)
     * and stores it for later retrieval via {@link #getAlertManager()}.
     *
     * <p>Visibility is {@code protected} so unit tests can subclass and
     * capture alerts via an overriding implementation.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        alertManager.dispatch(alert);
    }

    /**
     * Returns the {@link AlertManager} that this generator dispatches to.
     * Callers (e.g. {@code DataStorage.main} or integration tests) can use
     * this handle to read back every alert that was produced during the most
     * recent {@link #evaluateData} run.
     *
     * @return the manager instance backing {@link #triggerAlert(Alert)}
     */
    public AlertManager getAlertManager() {
        return alertManager;
    }
}

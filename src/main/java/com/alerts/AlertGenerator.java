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
 * strategies can be supplied via the two-argument constructor.
 *
 * <p><b>Design pattern — Factory Method:</b> each strategy uses the appropriate
 * {@link AlertFactory} subclass to create typed {@link Alert} objects
 * ({@link BloodPressureAlert}, {@link BloodOxygenAlert}, {@link ECGAlert}).
 */
public class AlertGenerator {

    private final DataStorage dataStorage;
    private final List<AlertStrategy> strategies;

    /**
     * Constructs an {@code AlertGenerator} with the default set of strategies
     * ({@link BloodPressureStrategy}, {@link OxygenSaturationStrategy},
     * {@link HeartRateStrategy}).
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this(dataStorage, Arrays.asList(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy()
        ));
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
        this.dataStorage = dataStorage;
        this.strategies  = new ArrayList<>(strategies);
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
     * Triggers an alert for the monitoring system. Currently prints the alert
     * to standard output; a real implementation would notify a nurse station,
     * write to a database, etc.
     *
     * <p>Visibility is {@code protected} so unit tests can subclass and capture
     * alerts via an overriding implementation.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        System.out.println("ALERT [" + alert.getTimestamp() + "] patient="
                + alert.getPatientId() + " condition=" + alert.getCondition());
    }
}

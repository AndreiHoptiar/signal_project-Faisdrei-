package com.data_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alerts.Alert;
import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system. This class serves as a repository for all patient records,
 * organized by patient IDs.
 */
public class DataStorage {

    /** The singleton instance; {@code null} until the first call to {@link #getInstance()}. */
    private static volatile DataStorage instance;

    private Map<Integer, Patient> patientMap; // Stores patient objects indexed by their unique patient ID.

    /**
     * Constructs a new instance of DataStorage, initializing the underlying
     * storage structure.
     */
    public DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Returns the singleton {@code DataStorage} instance, creating it on the
     * first call (thread-safe lazy initialisation with double-checked locking).
     *
     * @return the shared {@code DataStorage} instance
     */
    public static DataStorage getInstance() {
        if (instance == null) {
            synchronized (DataStorage.class) {
                if (instance == null) {
                    instance = new DataStorage();
                }
            }
        }
        return instance;
    }

    /**
     * Adds or updates patient data in the storage. If the patient does not
     * exist, a new {@link Patient} object is created and added to storage;
     * otherwise the new record is appended to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g. "HeartRate", "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of {@link PatientRecord} objects for a specific
     * patient, filtered by an inclusive time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix epoch
     * @return a list of records that fall within the specified time range;
     *         empty list if the patient is unknown
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>(); // return an empty list if no patient is found
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a defensive copy of the list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Entry point used when the application is launched with the
     * {@code DataStorage} argument (see {@code com.cardio_generator.Main}).
     * @param args optional command-line arguments; {@code args[0]}, if
     *             present, is interpreted as the path to a directory of
     *             simulator output files to read.
     * @throws IOException if the directory exists but cannot be read
     */
    public static void main(String[] args) throws IOException {
        DataStorage storage = new DataStorage();

        // 1. Load data from a directory if one was provided.
        if (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            String directoryPath = args[0];
            System.out.println("Loading patient data from: " + directoryPath);
            FileDataReader reader = new FileDataReader(directoryPath);
            reader.readData(storage);
            System.out.println("Loaded " + storage.getAllPatients().size() + " patient(s).");
        } else {
            System.out.println("No input directory supplied, running with empty storage.");
        }

        // 2. Print a small sample for visibility.
        for (Patient patient : storage.getAllPatients()) {
            List<PatientRecord> records = storage.getRecords(
                    patient.getPatientId(), 0L, Long.MAX_VALUE);
            System.out.println("Patient " + patient.getPatientId()
                    + " has " + records.size() + " record(s).");
        }

        // 3. Evaluate every patient and collect alerts via AlertManager.
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }

        // 4. Report the alerts that fired during evaluation.
        List<Alert> firedAlerts = alertGenerator.getAlertManager().getAlerts();
        System.out.println("Total alerts generated: " + firedAlerts.size());
    }
}

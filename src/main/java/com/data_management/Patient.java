package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 * This class stores patient-specific data,
 * allowing for the addition and retrieval
 * of medical records based on specified criteria.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with a specified ID.
     * Initializes an empty list of patient records.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /**
     * Adds a new record to this patient's list of medical records.
     * The record is created with the specified measurement value, record type, and
     * timestamp.
     *
     * @param measurementValue the measurement value to store in the record
     * @param recordType       the type of record, for example, "HeartRate", "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in milliseconds since UNIX epoch
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        this.patientRecords.add(record);
    }

    /**
     * Retrieves a list of PatientRecord objects for this patient that fall within a
     * specified time range.
     * The method filters records based on the start and end times provided.
     *
     * @param startTime the start of the time range, in milliseconds since UNIX epoch
     * @param endTime   the end of the time range, in milliseconds since UNIX epoch
     * @return a list of PatientRecord objects that fall within the specified time range
     */
    public List<PatientRecord> getRecords(long startTime, long endTime) {
        // Walk through every record this patient has and keep only the ones
        // whose timestamp is inside the [startTime, endTime] window (inclusive).
        // Assumption: both bounds are inclusive. This matches the example test
        // in DataStorageTest where records taken at the start and end timestamps
        // are both expected to be returned.
        List<PatientRecord> recordsInRange = new ArrayList<>();
        for (PatientRecord record : patientRecords) {
            long t = record.getTimestamp();
            if (t >= startTime && t <= endTime) {
                recordsInRange.add(record);
            }
        }
        return recordsInRange;
    }

    /**
     * Returns the unique ID of this patient.
     *
     * @return this patient's ID
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Returns every record stored for this patient, regardless of timestamp.
     * Useful for tests and for the AlertGenerator when it wants to look at
     * the entire history at once. Returns a defensive copy so callers cannot
     * mutate the internal list.
     *
     * @return a new list containing all of this patient's records
     */
    public List<PatientRecord> getAllRecords() {
        return new ArrayList<>(patientRecords);
    }
}

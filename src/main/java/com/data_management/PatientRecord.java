package com.data_management;

/**
 * Represents a single immutable record of patient data at a specific point in
 * time. Each {@code PatientRecord} stores all necessary details for a single
 * observation or measurement taken from a patient, including the type of
 * record (e.g. ECG, blood pressure), the measurement value, and the exact
 * timestamp when the measurement was taken.
 *
 * <p>Instances are produced by {@link DataReader} implementations such as
 * {@link FileDataReader} and {@link WebSocketClientImpl}, and they are stored
 * inside a {@link Patient} container.
 *
 * @author Andrei Hoptiar
 * @author Faisal Shadid
 */
public class PatientRecord {
    private int patientId;
    private String recordType; // Example: ECG, blood pressure, etc.
    private double measurementValue; // Example: heart rate
    private long timestamp;

    /**
     * Constructs a new patient record with the specified details. All fields
     * are kept verbatim, no validation is performed here because validation
     * is the responsibility of {@link DataReader} implementations.
     *
     * @param patientId        the unique non-negative identifier for the patient
     * @param measurementValue the numerical value of the recorded measurement
     *                         (units depend on {@code recordType})
     * @param recordType       the non-null type of measurement, e.g. "ECG",
     *                         "SystolicPressure", "Saturation"
     * @param timestamp        the time at which the measurement was recorded,
     *                         in milliseconds since the Unix epoch
     */
    public PatientRecord(int patientId, double measurementValue, String recordType, long timestamp) {
        this.patientId = patientId;
        this.measurementValue = measurementValue;
        this.recordType = recordType;
        this.timestamp = timestamp;
    }

    /**
     * Returns the unique patient ID associated with this record.
     *
     * @return the patient ID; always non-negative
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Returns the numerical measurement value of this record. The unit depends
     * on {@link #getRecordType()} (e.g. mmHg for blood pressure, % for blood
     * oxygen saturation, mV for ECG).
     *
     * @return the measurement value
     */
    public double getMeasurementValue() {
        return measurementValue;
    }

    /**
     * Returns the timestamp when this record was taken.
     *
     * @return the timestamp in milliseconds since the Unix epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the type of record (e.g. "ECG", "SystolicPressure",
     * "DiastolicPressure", "Saturation", "Alert").
     *
     * @return the non-null record type string
     */
    public String getRecordType() {
        return recordType;
    }
}

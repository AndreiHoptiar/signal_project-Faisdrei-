package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood-oxygen (saturation) data for a fixed roster of
 * patients. Each patient starts with a value between 95 % and 100 %, and the
 * value drifts up or down by at most one percentage point on every call so
 * the output looks like realistic sensor data.
 *
 * <p>This class implements {@link PatientDataGenerator} so it can be plugged
 * into the simulator alongside the other generators.
 *
 * @author Andrei Hoptiar
 * @author Faisal Shadid
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Constructs a new generator that tracks {@code patientCount} patients.
     * Every patient is seeded with a starting saturation value drawn
     * uniformly from {@code [95, 100]}.
     *
     * @param patientCount how many patients to set up values for (must be {@code >= 0})
     * @throws NegativeArraySizeException if {@code patientCount} is negative
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates a new saturation reading for the given patient and forwards
     * it to {@code outputStrategy}. The value changes by -1, 0, or +1 from
     * the last reading and is clamped to {@code [90, 100]} so it stays
     * realistic. Any unexpected runtime error is caught and logged so that a
     * single bad patient ID cannot bring down the simulator.
     *
     * @param patientId      the ID of the patient we are generating data for;
     *                       must be in {@code [1, patientCount]}
     * @param outputStrategy where the data gets sent, like console or file;
     *                       must not be {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code patientId} is outside
     *         the range configured in the constructor (caught internally and logged)
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
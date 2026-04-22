package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This class generates blood oxygen saturation data for patients.
 * Each patient starts with a value between 95 and 100 and it goes up or down
 * slightly each time to make it look like real data.
 * It implements PatientDataGenerator so it fits into the rest of the simulation.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Sets up the generator and gives each patient a starting saturation value
     * somewhere between 95 and 100.
     *
     * @param patientCount how many patients we need to set up values for
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates a new saturation reading for the patient.
     * The value changes by -1, 0 or +1 from the last reading and we make sure
     * it stays between 90 and 100 so it stays realistic.
     * Then it sends the result to whatever output we are using.
     *
     * @param patientId      the ID of the patient we are generating data for
     * @param outputStrategy where the data gets sent, like console or file
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
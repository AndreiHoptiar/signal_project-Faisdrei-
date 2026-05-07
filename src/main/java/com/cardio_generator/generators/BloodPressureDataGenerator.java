package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood pressure data (systolic and diastolic) for patients.
 *
 * <p>Each patient is assigned a random baseline at construction time:
 * systolic pressure between 110–130 mmHg and diastolic between 70–85 mmHg.
 * On every call to {@link #generate(int, OutputStrategy)} the values fluctuate
 * by a small random amount (±2 mmHg) around the previous reading and are
 * clamped to physiologically plausible bounds to prevent drift.
 *
 * <p>This class implements {@link PatientDataGenerator} so it integrates with
 * the rest of the simulation framework.
 */
public class BloodPressureDataGenerator implements PatientDataGenerator {

    /** Random number generator shared across all patients. */
    private static final Random random = new Random();

    /** Stores the most recent systolic reading for each patient (indexed by patient ID). */
    private int[] lastSystolicValues;

    /** Stores the most recent diastolic reading for each patient (indexed by patient ID). */
    private int[] lastDiastolicValues;

    /**
     * Constructs a new generator and assigns a random baseline blood pressure to
     * each patient.
     *
     * @param patientCount the total number of patients whose data will be generated
     */
    public BloodPressureDataGenerator(int patientCount) {
        lastSystolicValues = new int[patientCount + 1];
        lastDiastolicValues = new int[patientCount + 1];

        // Initialize with baseline values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSystolicValues[i] = 110 + random.nextInt(20); // Random baseline between 110 and 130
            lastDiastolicValues[i] = 70 + random.nextInt(15); // Random baseline between 70 and 85
        }
    }

    /**
     * Generates one systolic and one diastolic blood pressure reading for the
     * specified patient and sends both to the output strategy.
     *
     * <p>Each new reading changes by at most 2 mmHg from the previous one.
     * Systolic values are clamped to 90–180 mmHg and diastolic values to
     * 60–120 mmHg to stay within realistic human ranges.
     *
     * @param patientId      the ID of the patient for whom data is generated
     * @param outputStrategy the output channel that receives the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            int systolicVariation = random.nextInt(5) - 2; // -2, -1, 0, 1, or 2
            int diastolicVariation = random.nextInt(5) - 2;
            int newSystolicValue = lastSystolicValues[patientId] + systolicVariation;
            int newDiastolicValue = lastDiastolicValues[patientId] + diastolicVariation;
            // Ensure the blood pressure stays within a realistic and safe range
            newSystolicValue = Math.min(Math.max(newSystolicValue, 90), 180);
            newDiastolicValue = Math.min(Math.max(newDiastolicValue, 60), 120);
            lastSystolicValues[patientId] = newSystolicValue;
            lastDiastolicValues[patientId] = newDiastolicValue;

            outputStrategy.output(patientId, System.currentTimeMillis(), "SystolicPressure",
                    Double.toString(newSystolicValue));
            outputStrategy.output(patientId, System.currentTimeMillis(), "DiastolicPressure",
                    Double.toString(newDiastolicValue));
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood pressure data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}

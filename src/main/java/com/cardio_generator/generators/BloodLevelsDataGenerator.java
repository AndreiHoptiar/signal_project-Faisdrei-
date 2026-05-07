package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood-level data (cholesterol, white blood cells, and
 * red blood cells) for patients.
 *
 * <p>Each patient is assigned a random physiological baseline at construction
 * time. On every call to {@link #generate(int, OutputStrategy)} the values
 * fluctuate slightly around that baseline to mimic natural variation:
 * <ul>
 *   <li>Cholesterol: baseline 150–200 mg/dL, ±5 mg/dL per reading</li>
 *   <li>White blood cells: baseline 4–10 × 10⁹/L, ±0.5 per reading</li>
 *   <li>Red blood cells: baseline 4.5–6.0 × 10¹²/L, ±0.1 per reading</li>
 * </ul>
 *
 * <p>This class implements {@link PatientDataGenerator} so it integrates with
 * the rest of the simulation framework.
 */
public class BloodLevelsDataGenerator implements PatientDataGenerator {

    /** Random number generator shared across all patients. */
    private static final Random random = new Random();

    /** Baseline cholesterol level for each patient (indexed by patient ID), in mg/dL. */
    private final double[] baselineCholesterol;

    /** Baseline white blood cell count for each patient (indexed by patient ID), in × 10⁹/L. */
    private final double[] baselineWhiteCells;

    /** Baseline red blood cell count for each patient (indexed by patient ID), in × 10¹²/L. */
    private final double[] baselineRedCells;

    /**
     * Constructs a new generator and assigns a random physiological baseline to
     * each patient for all three blood-level metrics.
     *
     * @param patientCount the total number of patients whose data will be generated
     */
    public BloodLevelsDataGenerator(int patientCount) {
        // Initialize arrays to store baseline values for each patient
        baselineCholesterol = new double[patientCount + 1];
        baselineWhiteCells = new double[patientCount + 1];
        baselineRedCells = new double[patientCount + 1];

        // Generate baseline values for each patient
        for (int i = 1; i <= patientCount; i++) {
            baselineCholesterol[i] = 150 + random.nextDouble() * 50; // Initial random baseline
            baselineWhiteCells[i] = 4 + random.nextDouble() * 6; // Initial random baseline
            baselineRedCells[i] = 4.5 + random.nextDouble() * 1.5; // Initial random baseline
        }
    }

    /**
     * Generates one reading each for cholesterol, white blood cells, and red
     * blood cells for the specified patient and sends all three to the output
     * strategy.
     *
     * <p>Each generated value is the patient's baseline plus a small random
     * variation to simulate realistic fluctuation over time.
     *
     * @param patientId      the ID of the patient for whom data is generated
     * @param outputStrategy the output channel that receives the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Generate values around the baseline for realism
            double cholesterol = baselineCholesterol[patientId] + (random.nextDouble() - 0.5) * 10; // Small variation
            double whiteCells = baselineWhiteCells[patientId] + (random.nextDouble() - 0.5) * 1; // Small variation
            double redCells = baselineRedCells[patientId] + (random.nextDouble() - 0.5) * 0.2; // Small variation

            // Output the generated values
            outputStrategy.output(patientId, System.currentTimeMillis(), "Cholesterol", Double.toString(cholesterol));
            outputStrategy.output(patientId, System.currentTimeMillis(), "WhiteBloodCells",
                    Double.toString(whiteCells));
            outputStrategy.output(patientId, System.currentTimeMillis(), "RedBloodCells", Double.toString(redCells));
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood levels data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}

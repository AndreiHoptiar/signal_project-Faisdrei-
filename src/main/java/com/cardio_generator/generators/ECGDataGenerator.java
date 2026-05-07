package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated ECG (electrocardiogram) waveform data for patients.
 *
 * <p>The signal is approximated by combining three sinusoidal components that
 * roughly mimic the P-wave, QRS complex, and T-wave of a real ECG. Heart rate
 * is randomised between 60 and 80 bpm per call to introduce realistic
 * variability, and a small amount of noise is added to the output.
 *
 * <p>Note: This is a simplified educational simulation; the waveform is not
 * clinically accurate.
 *
 * <p>This class implements {@link PatientDataGenerator} so it integrates with
 * the rest of the simulation framework.
 */
public class ECGDataGenerator implements PatientDataGenerator {

    /** Random number generator shared across all patients. */
    private static final Random random = new Random();

    /** Stores the most recent ECG value for each patient (indexed by patient ID). */
    private double[] lastEcgValues;

    /** Constant for 2π, used in the sinusoidal waveform calculations. */
    private static final double PI = Math.PI;

    /**
     * Constructs a new generator and initialises the last-known ECG value to
     * zero for each patient.
     *
     * @param patientCount the total number of patients whose data will be generated
     */
    public ECGDataGenerator(int patientCount) {
        lastEcgValues = new double[patientCount + 1];
        // Initialize the last ECG value for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastEcgValues[i] = 0; // Initial ECG value can be set to 0
        }
    }

    /**
     * Generates one ECG sample for the specified patient and forwards it to the
     * output strategy.
     *
     * <p>The sample is computed by {@link #simulateEcgWaveform(int, double)} and
     * then stored so it can be referenced on the next call.
     *
     * @param patientId      the ID of the patient for whom data is generated
     * @param outputStrategy the output channel that receives the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        // TODO Check how realistic this data is and make it more realistic if necessary
        try {
            double ecgValue = simulateEcgWaveform(patientId, lastEcgValues[patientId]);
            outputStrategy.output(patientId, System.currentTimeMillis(), "ECG", Double.toString(ecgValue));
            lastEcgValues[patientId] = ecgValue;
        } catch (Exception e) {
            System.err.println("An error occurred while generating ECG data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }

    /**
     * Computes a synthetic ECG sample by summing three sinusoidal components
     * (P-wave, QRS complex, and T-wave) evaluated at the current system time.
     *
     * <p>A small random noise term (up to 0.05 mV) is added on every call to
     * simulate measurement uncertainty.
     *
     * @param patientId    the patient's ID (currently unused; reserved for future
     *                     per-patient personalisation)
     * @param lastEcgValue the previous ECG value for this patient (currently unused;
     *                     retained for API consistency with other generators)
     * @return the computed ECG sample value in arbitrary units (approximately mV)
     */
    private double simulateEcgWaveform(int patientId, double lastEcgValue) {
        // Simplified ECG waveform generation based on sinusoids
        double hr = 60.0 + random.nextDouble() * 20.0; // Simulate heart rate variability between 60 and 80 bpm
        double t = System.currentTimeMillis() / 1000.0; // Use system time to simulate continuous time
        double ecgFrequency = hr / 60.0; // Convert heart rate to Hz

        // Simulate different components of the ECG signal
        double pWave = 0.1 * Math.sin(2 * PI * ecgFrequency * t);
        double qrsComplex = 0.5 * Math.sin(2 * PI * 3 * ecgFrequency * t); // QRS is higher frequency
        double tWave = 0.2 * Math.sin(2 * PI * 2 * ecgFrequency * t + PI / 4); // T wave is offset

        return pWave + qrsComplex + tWave + random.nextDouble() * 0.05; // Add small noise
    }
}

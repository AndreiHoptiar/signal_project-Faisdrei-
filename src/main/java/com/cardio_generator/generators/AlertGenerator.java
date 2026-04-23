package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Makes fake alert events for patients.
 * Each patient can either have an active alert or not.
 * Every time generate() is called there is a small chance an alert starts,
 * and if one is already on there is a big chance it gets resolved.
 */
public class AlertGenerator implements PatientDataGenerator {

    // Note: "randomGenerator" is a static final reference to a mutable Random object.
    // Per Google Java Style Guide, a field is only a "constant" (CONSTANT_CASE)
    // when its contents are deeply immutable. Since Random is mutable, the field is
    // correctly treated as a regular field and kept in lowerCamelCase.
    /** Used to get random numbers so alerts happen randomly. */
    public static final Random randomGenerator = new Random();

    // Renamed field from "AlertStates" (UpperCamelCase) to "alertStates" (lowerCamelCase)
    // per Google Java Style Guide (Non-constant field names must be in lowerCamelCase).
    /** true means the patient has an active alert, false means no alert. */
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Creates a new AlertGenerator for the given number of patients.
     *
     * @param patientCount how many patients we want to track
     */
    public AlertGenerator(int patientCount) {
        // Updated reference to use the renamed "alertStates" field (was "AlertStates").
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Updates the alert for one patient and sends a message if something changes.
     * If the patient already has an alert, there is a 90% chance it gets resolved.
     * If not, there is a small chance a new alert is triggered.
     * If anything goes wrong it just prints an error message.
     *
     * @param patientId      the patient's ID number
     * @param outputStrategy where the alert message is sent
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Updated reference to use the renamed "alertStates" field (was "AlertStates").
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    // Updated reference to use the renamed "alertStates" field (was "AlertStates").
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Renamed local variable from "Lambda" (UpperCamelCase) to "lambda" (lowerCamelCase)
                // per Google Java Style Guide (Local variable names are in lowerCamelCase).
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                // Updated reference to use the renamed "lambda" local variable (was "Lambda").
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    // Updated reference to use the renamed "alertStates" field (was "AlertStates").
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            // Note: e.printStackTrace() is retained as-is; Google Java Style does not forbid it,
            // though production code typically prefers a proper logger.
            e.printStackTrace();
        }
    }
}

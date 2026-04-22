package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This is an interface that all the data generators in this project implement
 * Basically any class that generates health data for a patient needs to follow this
 * It makes it easy to swap out different generators without changing the rest of the code.
 */
public interface PatientDataGenerator {

    /**
     * Generates some health data for a patient and sends it to the output
     *
     * @param patientId      the ID of the patient we are generating data for
     * @param outputStrategy where we want to send the data, like console or a file
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
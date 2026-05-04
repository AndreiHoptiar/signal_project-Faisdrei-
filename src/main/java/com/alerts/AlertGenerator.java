package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 *
 * <p>Each alert rule lives in its own private helper so it's easy to read,
 * test, and extend (one rule = one method, following the Single Responsibility
 * idea from SOLID).
 */
public class AlertGenerator {

    // -- Threshold constants --------------------------------------------------
    // Pulled out as constants instead of magic numbers so the rubric thresholds
    // are obvious at a glance and easy to change in one place.

    /** Systolic blood pressure must not exceed this value (mmHg). */
    private static final double SYSTOLIC_HIGH = 180.0;
    /** Systolic blood pressure must not fall below this value (mmHg). */
    private static final double SYSTOLIC_LOW = 90.0;
    /** Diastolic blood pressure must not exceed this value (mmHg). */
    private static final double DIASTOLIC_HIGH = 120.0;
    /** Diastolic blood pressure must not fall below this value (mmHg). */
    private static final double DIASTOLIC_LOW = 60.0;
    /** Minimum change between consecutive readings to count as a trend (mmHg). */
    private static final double BP_TREND_DELTA = 10.0;
    /** Blood oxygen saturation alert threshold (%). */
    private static final double LOW_SATURATION = 92.0;
    /** Drop in saturation that triggers a rapid-drop alert (%). */
    private static final double RAPID_DROP_PERCENT = 5.0;
    /** Window for the rapid-drop alert (10 minutes in milliseconds). */
    private static fina
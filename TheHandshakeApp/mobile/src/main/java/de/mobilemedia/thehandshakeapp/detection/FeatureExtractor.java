package de.mobilemedia.thehandshakeapp.detection;

/* FeatureExtractor version May 6, 2016
 * Commit 0f75b13 */

import java.util.*;

public class FeatureExtractor {

    // Constants
    public static final short PEAK_TYPE_NONE = 0;
    public static final short PEAK_TYPE_MIN = 1;
    public static final short PEAK_TYPE_MAX = 2;

    // Parameters
    public int NUM_DATA_COLUMNS;
    public int MAJOR_AXIS_COLUMN;
    public int NUM_SAMPLES_FOR_PEAK_DETECTION;
    public float PEAK_AMPLITUDE_THRESHOLD;
    public int PEAK_REPEAT_THRESHOLD;
    public int MOVING_AVERAGE_WINDOW_WIDTH;

    // Data record processing helpers
    public LinkedList<float[]> recordHistory;
    public float[] lastMovingAverageOutput;
    public int numRecordsProcessed;
    public int[] numAscentingSince;
    public int[] numDescendingSince;

    // Maximum detection on all axes
    public int[] maximumCandidateIndex;
    public float[] maximumCandidateValue;
    public LinkedList<Integer>[] maximaIndices;
    public LinkedList<Float>[] maximaValues;

    // Minimum detection on all axes
    public int[] minimumCandidateIndex;
    public float[] minimumCandidateValue;
    public LinkedList<Integer>[] minimaIndices;
    public LinkedList<Float>[] minimaValues;

    // Global handshake detection
    public int ALTERNATION_TIME_MAX_DIFF;
    public int ALTERNATION_COUNT_DETECTION_THRESHOLD;
    public int lastPeakDetectionIndex = Integer.MIN_VALUE;
    public short lastPeakType;
    public int alternationStreak;

    // Action when a handshake was detected
    HandshakeDetectedAction handshakeDetectedAction;

    //-------------------------------------------------------------------------

    public FeatureExtractor(int numDataColumns,
                            int majorAxisColumn,
                            int numSamplesForPeakDetection,
                            float peakAmplitudeThreshold,
                            int peakRepeatThreshold,
                            int movingAverageWindowWidth,
                            int alternationTimeMaxDiff,
                            int alternationCountDetectionThreshold,
                            HandshakeDetectedAction hda) {

		/* Initialize variables */
        NUM_DATA_COLUMNS = numDataColumns;
        MAJOR_AXIS_COLUMN = majorAxisColumn;
        NUM_SAMPLES_FOR_PEAK_DETECTION = numSamplesForPeakDetection;
        PEAK_AMPLITUDE_THRESHOLD = peakAmplitudeThreshold;
        PEAK_REPEAT_THRESHOLD = peakRepeatThreshold;
        MOVING_AVERAGE_WINDOW_WIDTH = movingAverageWindowWidth;
        ALTERNATION_TIME_MAX_DIFF = alternationTimeMaxDiff;
        ALTERNATION_COUNT_DETECTION_THRESHOLD = alternationCountDetectionThreshold;
        handshakeDetectedAction = hda;

        numRecordsProcessed = 0;
        lastPeakType = PEAK_TYPE_NONE;
        alternationStreak = 0;

		/* Initialize arrays and lists */
        recordHistory = new LinkedList<float[]>();
        lastMovingAverageOutput = new float[NUM_DATA_COLUMNS];
        numAscentingSince = new int[NUM_DATA_COLUMNS];
        numDescendingSince = new int[NUM_DATA_COLUMNS];
        maximumCandidateIndex = new int[NUM_DATA_COLUMNS];
        maximumCandidateValue = new float[NUM_DATA_COLUMNS];
        maximaIndices = new LinkedList[NUM_DATA_COLUMNS];
        maximaValues = new LinkedList[NUM_DATA_COLUMNS];
        minimumCandidateIndex = new int[NUM_DATA_COLUMNS];
        minimumCandidateValue = new float[NUM_DATA_COLUMNS];
        minimaIndices = new LinkedList[NUM_DATA_COLUMNS];
        minimaValues = new LinkedList[NUM_DATA_COLUMNS];

        for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
            numAscentingSince[i] = 0;
            numDescendingSince[i] = 0;
            maximumCandidateIndex[i] = -1;
            maximumCandidateValue[i] = 0.0f;
            minimumCandidateIndex[i] = -1;
            minimumCandidateValue[i] = 0.0f;
            maximaIndices[i] = new LinkedList<Integer>();
            maximaValues[i] = new LinkedList<Float>();
            minimaIndices[i] = new LinkedList<Integer>();
            minimaValues[i] = new LinkedList<Float>();
        }

    }

    public void processDataRecord(float[] data) {

        float[] preprocessedData = performPreprocessing(data);

        for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
            processDataColumn(i, preprocessedData[i]);
        }

        lastMovingAverageOutput = preprocessedData;
        ++numRecordsProcessed;

    }

    private void processDataColumn(int column, float value) {

        float currentValue = value;
        float lastValue = lastMovingAverageOutput[column];

        // Detect ascension and descension
        if (currentValue > lastValue) {
            ++numAscentingSince[column];
            numDescendingSince[column] = 0;

            // If enough ascending samples after a peek
            if (numAscentingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION &&
                    minimumCandidateIndex[column] > -1 &&
                    minimumCandidateValue[column] < -PEAK_AMPLITUDE_THRESHOLD) {
                handleDetectedMinimum(column, minimumCandidateIndex[column], minimumCandidateValue[column]);
            }
            // Mark maximum candidate if enough samples
            if (numAscentingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION) {
                maximumCandidateIndex[column] = numRecordsProcessed;
                maximumCandidateValue[column] = currentValue;
            }
        }
        else if (currentValue < lastValue) {
            ++numDescendingSince[column];
            numAscentingSince[column] = 0;

            // If enough descending samples after a peak
            if (numDescendingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION &&
                    maximumCandidateIndex[column] > -1 &&
                    maximumCandidateValue[column] > PEAK_AMPLITUDE_THRESHOLD) {
                handleDetectedMaximum(column, maximumCandidateIndex[column], maximumCandidateValue[column]);
            }
            // Mark minimum candidate if enough samples
            if (numDescendingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION) {
                minimumCandidateIndex[column] = numRecordsProcessed;
                minimumCandidateValue[column] = currentValue;
            }

        }

        // Handshake timeout
        if (column == MAJOR_AXIS_COLUMN) {
            if (numRecordsProcessed - lastPeakDetectionIndex > ALTERNATION_TIME_MAX_DIFF) {
                if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
                    handleDetectedHandshake();
                    alternationStreak = 0;
                }
            }
        }

    }

    private float[] performPreprocessing(float[] data) {

        addToRecordHistory(data);
        float[] output = new float[data.length];

        // moving average over record history
        for (float[] dataRecord : recordHistory) {
            for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
                output[i] += dataRecord[i];
            }
        }

        for (int i = 0; i < output.length; ++i) {
            output[i] = output[i] / (float) (recordHistory.size());
        }

        return output;

    }

    private void handleDetectedMaximum(int column, int sampleID, float value) {

        if (lastPeakType == PEAK_TYPE_MAX &&
                sampleID - lastPeakDetectionIndex < PEAK_REPEAT_THRESHOLD) {
            return;
        }

        maximaIndices[column].addLast(sampleID);
        maximaValues[column].addLast(value);
        maximumCandidateIndex[column] = -1;
        maximumCandidateValue[column] = 0.0f;

        if (column == MAJOR_AXIS_COLUMN) {

            if (sampleID - lastPeakDetectionIndex < ALTERNATION_TIME_MAX_DIFF &&
                    lastPeakType == PEAK_TYPE_MIN) {

                ++alternationStreak;

            } else {

                if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
                    handleDetectedHandshake();
                }

                alternationStreak = 1;
            }

            lastPeakDetectionIndex = sampleID;
            lastPeakType = PEAK_TYPE_MAX;

        }

    }

    private void handleDetectedMinimum(int column, int sampleID, float value) {

        if (lastPeakType == PEAK_TYPE_MIN &&
                sampleID - lastPeakDetectionIndex < PEAK_REPEAT_THRESHOLD) {
            return;
        }

        minimaIndices[column].addLast(sampleID);
        minimaValues[column].addLast(value);
        minimumCandidateIndex[column] = -1;
        minimumCandidateValue[column] = 0.0f;

        if (column == MAJOR_AXIS_COLUMN) {

            if (sampleID - lastPeakDetectionIndex < ALTERNATION_TIME_MAX_DIFF &&
                    lastPeakType == PEAK_TYPE_MAX) {
                ++alternationStreak;

            } else {

                if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
                    handleDetectedHandshake();
                }

                alternationStreak = 1;
            }

            lastPeakDetectionIndex = sampleID;
            lastPeakType = PEAK_TYPE_MIN;

        }

    }

    private void handleDetectedHandshake() {
        // perform defined action on handshake
        handshakeDetectedAction.onHandshakeDetected();
    }

    private void addToRecordHistory(float[] data) {

        recordHistory.addLast(data);

        if (recordHistory.size() > 2 * MOVING_AVERAGE_WINDOW_WIDTH + 1) {
            recordHistory.removeFirst();
        }

    }
}
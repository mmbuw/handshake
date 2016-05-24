package de.mobilemedia.thehandshakeapp.detection;

import android.util.Log;

import java.util.*;

public class MRDFeatureExtractor {

    // Constants
    public final int NUM_DATA_COLUMNS;
    public final int NUM_SAMPLES_FOR_PEAK_DETECTION;
    public final int MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS;
    public final int MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS;
    public final int ANALYSIS_FEATURE_WINDOW_WIDTH;

    public final float HANDSHAKE_Y_AXIS_MIN_RANGE_THRESHOLD = 15.0f;
    public final float HANDSHAKE_X_AXIS_MAX_RANGE_THRESHOLD = 20.0f;
    public final float HANDSHAKE_Z_AXIS_MAX_RANGE_THRESHOLD = 20.0f;
    public final float HANDSHAKE_POSITIVE_WINDOW_FRACTION = 0.1f;

    // Data record processing helpers
    public LinkedList<float[]> dataRecords;
    public int numRecordsProcessed = 0;
    public int[] numAscentingSince;
    public int[] numDescendingSince;
    public float[] lastData;

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

    // Action when a handshake was detected
    HandshakeDetectedAction handshakeDetectedAction;

    //-------------------------------------------------------------------------

    public MRDFeatureExtractor(int numDataColumns,
                               int numSamplesForPeakDetection,
                               int minNumSamplesForHandshakeAnalysis,
                               int maxNumSamplesForHandshakeAnalysis,
                               int analysisFeatureWindowWidth,
                               HandshakeDetectedAction hda) {

        /* Parse parameters */
        NUM_DATA_COLUMNS = numDataColumns;
        NUM_SAMPLES_FOR_PEAK_DETECTION = numSamplesForPeakDetection;
        MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = minNumSamplesForHandshakeAnalysis;
        MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = maxNumSamplesForHandshakeAnalysis;
        ANALYSIS_FEATURE_WINDOW_WIDTH = analysisFeatureWindowWidth;
        handshakeDetectedAction = hda;

        /* Initialize arrays */
        dataRecords = new LinkedList<float[]>();
        numAscentingSince = new int[NUM_DATA_COLUMNS];
        numDescendingSince = new int[NUM_DATA_COLUMNS];
        lastData = new float[NUM_DATA_COLUMNS];
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
            lastData[i] = 0;
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

    public void clearData() {
        dataRecords.clear();

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            numAscentingSince[col] = 0;
            numDescendingSince[col] = 0;
            lastData[col] = 0.0f;
            maximumCandidateIndex[col] = -1;
            maximumCandidateValue[col] = 0.0f;
            maximaIndices[col].clear();
            maximaValues[col].clear();
            minimumCandidateIndex[col] = -1;
            minimumCandidateValue[col] = 0.0f;
            minimaIndices[col].clear();
            minimaValues[col].clear();
        }
    }

    public void startDataEvent() {
        clearData();
    }

    public void endDataEvent() {

        if (    dataRecords.size() > MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS &&
                dataRecords.size() < MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS) {

            System.out.println("Analyzing data package of " + dataRecords.size() + " samples.");
            analyzeDataRecordsFeatures();

        }
        else {
            System.out.println("Omitting data package of " + dataRecords.size() + " samples.");
        }
    }

	/* MAIN FUNCTION FOR NEW INCOMING MEASUREMENTS                           */
	/* --------------------------------------------------------------------- */

    public void processDataRecord(float[] data) {

        updateDataRecordStore(data);

        for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
            checkNewValueForPeaks(i, data[i]);
        }

        lastData = data;
        ++numRecordsProcessed;
    }


	/* DATA RECORD STORE FUNCTIONS                                           */
	/* --------------------------------------------------------------------- */

    public void updateDataRecordStore(float[] newData) {

        dataRecords.addLast(newData);

    }


	/* PEAK DETECTION                                                        */
	/* --------------------------------------------------------------------- */

    public void checkNewValueForPeaks(int column, float value) {

        float currentValue = value;
        float lastValue = lastData[column];

        // Detect ascension and descension
        if (currentValue > lastValue) {
            ++numAscentingSince[column];
            numDescendingSince[column] = 0;

            // If enough ascending samples after a peek
            if (numAscentingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION &&
                    minimumCandidateIndex[column] > -1) {
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
                    maximumCandidateIndex[column] > -1) {
                handleDetectedMaximum(column, maximumCandidateIndex[column], maximumCandidateValue[column]);
            }
            // Mark minimum candidate if enough samples
            if (numDescendingSince[column] >= NUM_SAMPLES_FOR_PEAK_DETECTION) {
                minimumCandidateIndex[column] = numRecordsProcessed;
                minimumCandidateValue[column] = currentValue;
            }

        }

    }

    public void handleDetectedMaximum(int column, int sampleID, float value) {

        maximaIndices[column].addLast(sampleID);
        maximaValues[column].addLast(value);
        maximumCandidateIndex[column] = -1;
        maximumCandidateValue[column] = 0.0f;

    }

    public void handleDetectedMinimum(int column, int sampleID, float value) {

        minimaIndices[column].addLast(sampleID);
        minimaValues[column].addLast(value);
        minimumCandidateIndex[column] = -1;
        minimumCandidateValue[column] = 0.0f;

    }

	/* FEATURE EXTRACTION                                                    */
	/* --------------------------------------------------------------------- */

    public void analyzeDataRecordsFeatures() {

        // move a window through the data records to check for handshake features
        int movingWindowStart = 0;
        int movingWindowEnd = ANALYSIS_FEATURE_WINDOW_WIDTH-1;
        boolean handshakeDetected = false;

        int positiveWindows = 0;

        while (movingWindowEnd < dataRecords.size()) {

            float[] ranges = computeDataRecordRanges(movingWindowStart, movingWindowEnd);

            if (rangesRepresentHandshake(ranges)) {
                positiveWindows++;
            }

            movingWindowStart += 1;
            movingWindowEnd += 1;
        }

        int numOfAllWindows = dataRecords.size() - ANALYSIS_FEATURE_WINDOW_WIDTH + 1;
        float positiveWindowFraction = (float) positiveWindows/numOfAllWindows;
        Log.i("MRDFeatureExtractor", "Positive window fraction: " + positiveWindowFraction);

        // handshake detection criteria
        if (positiveWindowFraction > HANDSHAKE_POSITIVE_WINDOW_FRACTION) {
            handshakeDetected = true;
        }


        // actions performed when handshake was (not) detected
        if (handshakeDetected) {
            handshakeDetectedAction.onHandshakeDetected();
            Log.i("MRDFeatureExtractor", "Classification result: handshake");
        } else {
            Log.i("MRDFeatureExtractor", "Classification Result: nothing");
        }

    }

    public boolean rangesRepresentHandshake(float[] ranges) {

        if (    ranges[0] < HANDSHAKE_X_AXIS_MAX_RANGE_THRESHOLD &&
                ranges[1] > HANDSHAKE_Y_AXIS_MIN_RANGE_THRESHOLD &&
                ranges[2] < HANDSHAKE_Z_AXIS_MAX_RANGE_THRESHOLD) {

            return true;

        }

        return false;

    }

    public float[] computeDataRecordRanges(int startSample, int endSample) {

        float[] highestValues = new float[NUM_DATA_COLUMNS];
        float[] lowestValues = new float[NUM_DATA_COLUMNS];

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            highestValues[col] = -1000000;
            lowestValues[col] = 1000000;
        }

        for (int sample = startSample; sample <= endSample; ++sample) {

            float[] record = dataRecords.get(sample);

            for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
                if (record[col] > highestValues[col]) { highestValues[col] = record[col]; }
                if (record[col] < lowestValues[col]) { lowestValues[col] = record[col]; }
            }

        }

        float[] ranges = new float[NUM_DATA_COLUMNS];
        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            ranges[col] = highestValues[col] - lowestValues[col];
        }
        return ranges;

    }
}
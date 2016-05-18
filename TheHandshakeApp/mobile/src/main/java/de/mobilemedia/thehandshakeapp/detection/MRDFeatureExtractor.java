package de.mobilemedia.thehandshakeapp.detection;

/* FeatureExtractor version May 18, 2016
 * Commit 84e4f86 */

import android.util.Log;

import java.util.*;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class MRDFeatureExtractor {

    // Constants
    public final int NUM_DATA_COLUMNS;
    public final int NUM_SAMPLES_FOR_PEAK_DETECTION;
    public final int FEATURE_WINDOW_WIDTH;

    // Data record processing helpers
    public LinkedList<float[]> windowContent;
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

    // WEKA data structures
    J48 handshakeClassificationTree;
    Instances testDataSet;
    ArrayList<Attribute> attributeLabels;
    ArrayList<String> classLabels;

    // Action when a handshake was detected
    HandshakeDetectedAction handshakeDetectedAction;

    //-------------------------------------------------------------------------

    public MRDFeatureExtractor(int numDataColumns,
                               int numSamplesForPeakDetection,
                               int featureWindowWidth,
                               String J48TrainedDataFile,
                               HandshakeDetectedAction hda) {

        /* Parse parameters */
        NUM_DATA_COLUMNS = numDataColumns;
        NUM_SAMPLES_FOR_PEAK_DETECTION = numSamplesForPeakDetection;
        FEATURE_WINDOW_WIDTH = featureWindowWidth;
        handshakeDetectedAction = hda;

        /* Initialize arrays */
        windowContent = new LinkedList<float[]>();
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

        /* Create WEKA data structures */
        // class labels
        classLabels = new ArrayList<String>();
        classLabels.add("no-handshake");
        classLabels.add("handshake");

        // attribute labels
        attributeLabels = new ArrayList<Attribute>();
        attributeLabels.add(new Attribute("avg_x"));
        attributeLabels.add(new Attribute("avg_y"));
        attributeLabels.add(new Attribute("avg_z"));
        attributeLabels.add(new Attribute("range_x"));
        attributeLabels.add(new Attribute("range_y"));
        attributeLabels.add(new Attribute("range_z"));
        attributeLabels.add(new Attribute("ipd_x"));
        attributeLabels.add(new Attribute("ipd_y"));
        attributeLabels.add(new Attribute("ipd_z"));
        attributeLabels.add(new Attribute("class", classLabels));

        // test data set
        testDataSet = new Instances("handshake-candidates", attributeLabels, 0);
        testDataSet.setClassIndex(testDataSet.numAttributes()-1);

        // load trained decision tree
        try {
            handshakeClassificationTree = (J48) SerializationHelper.read(J48TrainedDataFile);
            Log.i("MRDFeatureExtractor", "Trained J48 tree successfully loaded: " + J48TrainedDataFile);
        } catch (Exception e) {
            Log.e("MRDFeatureExtractor", "Error loading trained J48 tree: " + J48TrainedDataFile);
        }

    }

	/* MAIN FUNCTION FOR NEW INCOMING MEASUREMENTS                           */
	/* --------------------------------------------------------------------- */

    public void processDataRecord(float[] data) {

        updateWindowContent(data);

        for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
            checkNewValueForPeaks(i, data[i]);
        }

        cleanPeaksOutsideWindow();
        analyzeWindowFeatures();

        lastData = data;
        ++numRecordsProcessed;
    }


	/* MOVING WINDOW FUNCTIONS                                               */
	/* --------------------------------------------------------------------- */

    public void updateWindowContent(float[] newData) {

        windowContent.addLast(newData);

        if (windowContent.size() > FEATURE_WINDOW_WIDTH) {
            windowContent.removeFirst();
        }

    }

    public void cleanPeaksOutsideWindow() {

        int lastSampleBeforeWindow = numRecordsProcessed - FEATURE_WINDOW_WIDTH;

        for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {

            if (minimaIndices[i].size() > 0 &&
                    minimaIndices[i].getFirst() == lastSampleBeforeWindow) {

                minimaIndices[i].removeFirst();
                minimaValues[i].removeFirst();
            }

            if (maximaIndices[i].size() > 0 &&
                    maximaIndices[i].getFirst() == lastSampleBeforeWindow) {

                maximaIndices[i].removeFirst();
                maximaValues[i].removeFirst();
            }
        }

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

    public void analyzeWindowFeatures() {

        if (windowContent.size() == FEATURE_WINDOW_WIDTH) {

            float[] currentWindowFeatureVector = createFeatureVector();

            Instance candidate = new DenseInstance(9);
            candidate.setValue(testDataSet.attribute(0), currentWindowFeatureVector[0]);
            candidate.setValue(testDataSet.attribute(1), currentWindowFeatureVector[1]);
            candidate.setValue(testDataSet.attribute(2), currentWindowFeatureVector[2]);
            candidate.setValue(testDataSet.attribute(3), currentWindowFeatureVector[3]);
            candidate.setValue(testDataSet.attribute(4), currentWindowFeatureVector[4]);
            candidate.setValue(testDataSet.attribute(5), currentWindowFeatureVector[5]);
            candidate.setValue(testDataSet.attribute(6), currentWindowFeatureVector[6]);
            candidate.setValue(testDataSet.attribute(7), currentWindowFeatureVector[7]);
            candidate.setValue(testDataSet.attribute(8), currentWindowFeatureVector[8]);

            // add instance to dataset for testing
            testDataSet.add(candidate);
            candidate.setDataset(testDataSet);

            // perform classification
            try {
                double result = handshakeClassificationTree.classifyInstance(candidate);
                Log.i("MRDFeatureExtractor", "Classification result " + result);
            } catch (Exception e) {
                Log.e("MRDFeatureExtractor", "Classification of an instance has failed.");
            }

            // remove instance again after testing
            testDataSet.delete();
            candidate.setDataset(null);

        }

    }


    public float[] createFeatureVector() {

        float[] outputVector = new float[9];

        float[] averages = computeWindowAverages();
        float[] ranges = computeWindowRanges();
        float[] interPeakDistances = computeWindowInterPeakDistances();

        outputVector[0] = averages[0];
        outputVector[1] = averages[1];
        outputVector[2] = averages[2];
        outputVector[3] = ranges[0];
        outputVector[4] = ranges[1];
        outputVector[5] = ranges[2];
        outputVector[6] = interPeakDistances[0];
        outputVector[7] = interPeakDistances[1];
        outputVector[8] = interPeakDistances[2];

        return outputVector;
    }

    public float[] computeWindowAverages() {

        float[] outputVector = new float[NUM_DATA_COLUMNS];

        for (float[] record : windowContent) {

            for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
                outputVector[col] += record[col];
            }
        }

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            outputVector[col] = outputVector[col] / FEATURE_WINDOW_WIDTH;
        }

        return outputVector;
    }

    public float[] computeWindowRanges() {

        float[] highestValues = new float[NUM_DATA_COLUMNS];
        float[] lowestValues = new float[NUM_DATA_COLUMNS];

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            highestValues[col] = -1000000;
            lowestValues[col] = 1000000;
        }

        for (float[] record : windowContent) {

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

    public float[] computeWindowInterPeakDistances() {

        float[] sampleCounters = new float[NUM_DATA_COLUMNS];
        int[] divisors = new int[NUM_DATA_COLUMNS];

        int windowStartId = (numRecordsProcessed-FEATURE_WINDOW_WIDTH+1);
        int windowEndId = numRecordsProcessed;

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {

            LinkedList<Integer> mergedIndexList = new LinkedList<Integer>();

            for (Integer id : maximaIndices[col]) { mergedIndexList.addLast(new Integer(id)); }
            for (Integer id : minimaIndices[col]) { mergedIndexList.addLast(new Integer(id)); }
            Collections.sort(mergedIndexList);

            if (mergedIndexList.size() == 0 ||
                    mergedIndexList.getFirst() != windowStartId) {
                mergedIndexList.addFirst(windowStartId);
            }
            if (mergedIndexList.getLast() != windowEndId) {
                mergedIndexList.addLast(windowEndId);
            }


            if (mergedIndexList.size() > 0) {

                for (int listId = 0; listId < mergedIndexList.size(); ++listId) {

                    if (listId != mergedIndexList.size() - 1) {
                        sampleCounters[col] = sampleCounters[col] + (mergedIndexList.get(listId+1)-mergedIndexList.get(listId));
                    }

                }

                divisors[col] = mergedIndexList.size() - 1;

            } else {
                sampleCounters[col] = FEATURE_WINDOW_WIDTH-1;
                divisors[col] = 1;
            }


        }

        for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
            sampleCounters[col] = sampleCounters[col] / divisors[col];
        }

        return sampleCounters;

    }
}
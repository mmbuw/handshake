import java.io.*;
import java.util.*;


public class FeatureExtractor {

	// Constants
	public static short PEAK_TYPE_NONE = 0;
	public static short PEAK_TYPE_MIN = 1;
	public static short PEAK_TYPE_MAX = 2;

	public static int NUM_SAMPLES_FOR_PEAK_DETECTION;
	public static float PEAK_AMPLITUDE_THRESHOLD = 5.0f;
	public static int NUM_DATA_COLUMNS = 3;
	public static boolean ACTIVATE_PREPROCESSING = true;

	// Data record processing helpers
	public static float[] lastValues;
	public static int numRecordsProcessed = 0;
	public static int[] numAscentingSince;
	public static int[] numDescendingSince;

	// Maximum detection on all axes
	public static int[] maximumCandidateIndex;
	public static float[] maximumCandidateValue;
	public static LinkedList<Integer>[] maximaIndices;
	public static LinkedList<Float>[] maximaValues;

	// Minimum detection on all axes
	public static int[] minimumCandidateIndex;
	public static float[] minimumCandidateValue;
	public static LinkedList<Integer>[] minimaIndices;
	public static LinkedList<Float>[] minimaValues;

	// Global handshake detection
	public static int ALTERNATION_TIME_MAX_DIFF = 30;
	public static int ALTERNATION_COUNT_DETECTION_THRESHOLD = 5;
	public static int lastPeakDetectionIndex = Integer.MIN_VALUE;
	public static short lastPeakType = 0;
	public static int alternationStreak = 0;
	public static LinkedList<Integer> handshakeIndices = new LinkedList<Integer>();

	//-------------------------------------------------------------------------

	public static void main(String[] args) {

		/* Initialize arrays */
		lastValues = new float[NUM_DATA_COLUMNS];
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

		/* Start application logic */
		try {			
			NUM_SAMPLES_FOR_PEAK_DETECTION = Integer.parseInt(args[1]);
			parseInputFile(args[0]);
			saveFeatures();

		} catch (Exception e) {
			e.printStackTrace();
		}



	}

	public static void parseInputFile(String filename) throws FileNotFoundException {

		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			line = line.trim();
			line = line.replace(" ", "");
			String[] splittedLine = line.split(",");
			float[] data = new float[splittedLine.length];

			for (int i = 0; i < data.length; ++i) {
				data[i] = Float.parseFloat(splittedLine[i]);
			}

			processDataRecord(data);
		}

		scanner.close();

	}

	public static void processDataRecord(float[] data) {

		for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
			processDataColumn(i, data[i]);
		}

		lastValues = data;
		++numRecordsProcessed;

	}

	public static void processDataColumn(int column, float value) {

		// Perform preprocessing on current value if activated
		float currentValue;

		if (ACTIVATE_PREPROCESSING) {
			currentValue = performPreprocessing(column, value);
		} else {
			currentValue = value;
		}

		float lastValue = lastValues[column];

		// Detect ascension and descension
		if (currentValue > lastValue) {
			++numAscentingSince[column];
			numDescendingSince[column] = 0;

			// If enough ascending samples after a peek
			if (numAscentingSince[column] > NUM_SAMPLES_FOR_PEAK_DETECTION && 
				minimumCandidateIndex[column] > -1 &&
				minimumCandidateValue[column] < -PEAK_AMPLITUDE_THRESHOLD) {
				handleDetectedMinimum(column, minimumCandidateIndex[column], minimumCandidateValue[column]);
			}
			// Mark maximum candidate if enough samples
			else if (numAscentingSince[column] > NUM_SAMPLES_FOR_PEAK_DETECTION) {
				maximumCandidateIndex[column] = numRecordsProcessed;
				maximumCandidateValue[column] = currentValue;
			}
		}
		else if (currentValue < lastValue) {
			++numDescendingSince[column];
			numAscentingSince[column] = 0;

			// If enough descending samples after a peak
			if (numDescendingSince[column] > NUM_SAMPLES_FOR_PEAK_DETECTION &&
				maximumCandidateIndex[column] > -1 &&
				maximumCandidateValue[column] > PEAK_AMPLITUDE_THRESHOLD) {
				handleDetectedMaximum(column, maximumCandidateIndex[column], maximumCandidateValue[column]);
			}
			// Mark minimum candidate if enough samples
			else if (numDescendingSince[column] > NUM_SAMPLES_FOR_PEAK_DETECTION) {
				minimumCandidateIndex[column] = numRecordsProcessed;
				minimumCandidateValue[column] = currentValue;
			}

		}

		// Handshake timeout
		if (column == 1) {
			if (numRecordsProcessed - lastPeakDetectionIndex > ALTERNATION_TIME_MAX_DIFF) {
				if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
					handshakeIndices.addLast(numRecordsProcessed - ALTERNATION_TIME_MAX_DIFF - 1);
					alternationStreak = 0;
				}
			}
		}

	}

	public static float performPreprocessing(int column, float value) {

		// averaging with previous value
		//float lastValue = lastValues[column];
		//return (value + lastValue) / 2.0f;
		return value;

	}

	public static void handleDetectedMaximum(int column, int sampleID, float value) {

		maximaIndices[column].addLast(sampleID);
		maximaValues[column].addLast(value);
		maximumCandidateIndex[column] = -1;
		maximumCandidateValue[column] = 0.0f;

		if (column == 1) {

			if (sampleID - lastPeakDetectionIndex < ALTERNATION_TIME_MAX_DIFF && 
				lastPeakType == PEAK_TYPE_MIN) {

				++alternationStreak;

			} else {

				if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
					handshakeIndices.addLast(sampleID);
				}

				alternationStreak = 1;
			}

			lastPeakDetectionIndex = sampleID;
			lastPeakType = PEAK_TYPE_MAX;

		}

	}

	public static void handleDetectedMinimum(int column, int sampleID, float value) {

		minimaIndices[column].addLast(sampleID);
		minimaValues[column].addLast(value);
		minimumCandidateIndex[column] = -1;
		minimumCandidateValue[column] = 0.0f;

		if (column == 1) {

			if (sampleID - lastPeakDetectionIndex < ALTERNATION_TIME_MAX_DIFF && 
				lastPeakType == PEAK_TYPE_MAX) {
				++alternationStreak;

			} else {

				if (alternationStreak >= ALTERNATION_COUNT_DETECTION_THRESHOLD) {
					handshakeIndices.addLast(sampleID);
				}

				alternationStreak = 1;
			}

			lastPeakDetectionIndex = sampleID;
			lastPeakType = PEAK_TYPE_MIN;

		}

	}

	public static void saveFeatures() throws FileNotFoundException {

		for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
			saveMaxima(i);
			saveMinima(i);
		}

		saveHandshakes();

	}

	public static void saveMaxima(int index) throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("maxima_" + index + ".txt");

		for (int i = 0; i < maximaIndices[index].size(); ++i) {
			writer.println(maximaIndices[index].get(i) + " " + maximaValues[index].get(i));
		}

		writer.close();

	}

	public static void saveMinima(int index) throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("minima_" + index + ".txt");

		for (int i = 0; i < minimaIndices[index].size(); ++i) {
			writer.println(minimaIndices[index].get(i) + " " + minimaValues[index].get(i));
		}

		writer.close();

	}

	public static void saveHandshakes() throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("handshakes.txt");

		for (int i = 0; i < handshakeIndices.size(); ++i) {
			writer.println(handshakeIndices.get(i) + " 0");
		}

		writer.close();

	}
}
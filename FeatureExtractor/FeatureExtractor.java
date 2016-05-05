import java.io.*;
import java.util.*;


public class FeatureExtractor {

	// Constants
	public static final short PEAK_TYPE_NONE = 0;
	public static final short PEAK_TYPE_MIN = 1;
	public static final short PEAK_TYPE_MAX = 2;

	public static int MAJOR_AXIS_COLUMN;
	public static final int NUM_SAMPLES_FOR_PEAK_DETECTION = 1;
	public static final float PEAK_AMPLITUDE_THRESHOLD = 5.0f;
	public static final int PEAK_REPEAT_THRESHOLD = 15;
	public static int NUM_DATA_COLUMNS;
	public static final int MOVING_AVERAGE_WINDOW_WIDTH = 0;

	// Data record processing helpers
	public static LinkedList<float[]> recordHistory;
	public static float[] lastMovingAverageOutput;
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

		/* Parse parameters */
		NUM_DATA_COLUMNS = Integer.parseInt(args[1]);
		MAJOR_AXIS_COLUMN = Integer.parseInt(args[2]);

		/* Initialize arrays */
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

		/* Start application logic */
		try {			
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

		float[] preprocessedData = performPreprocessing(data);

		for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
			processDataColumn(i, preprocessedData[i]);
		}

		lastMovingAverageOutput = preprocessedData;
		++numRecordsProcessed;

	}

	public static void processDataColumn(int column, float value) {

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
					handshakeIndices.addLast(numRecordsProcessed - ALTERNATION_TIME_MAX_DIFF - 1);
					alternationStreak = 0;
				}
			}
		}

	}

	public static float[] performPreprocessing(float[] data) {

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

	public static void handleDetectedMaximum(int column, int sampleID, float value) {

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
					handshakeIndices.addLast(sampleID);
				}

				alternationStreak = 1;
			}

			lastPeakDetectionIndex = sampleID;
			lastPeakType = PEAK_TYPE_MAX;

		}

	}

	public static void handleDetectedMinimum(int column, int sampleID, float value) {

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
					handshakeIndices.addLast(sampleID);
				}

				alternationStreak = 1;
			}

			lastPeakDetectionIndex = sampleID;
			lastPeakType = PEAK_TYPE_MIN;

		}

	}

	public static void addToRecordHistory(float[] data) {
		
		recordHistory.addLast(data);

		if (recordHistory.size() > 2*MOVING_AVERAGE_WINDOW_WIDTH+1) {
			recordHistory.removeFirst();
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

		PrintWriter writer = new PrintWriter("output_data/maxima_" + index + ".txt");

		for (int i = 0; i < maximaIndices[index].size(); ++i) {
			writer.println(maximaIndices[index].get(i) + " " + maximaValues[index].get(i));
		}

		writer.close();

	}

	public static void saveMinima(int index) throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("output_data/minima_" + index + ".txt");

		for (int i = 0; i < minimaIndices[index].size(); ++i) {
			writer.println(minimaIndices[index].get(i) + " " + minimaValues[index].get(i));
		}

		writer.close();

	}

	public static void saveHandshakes() throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("output_data/handshakes.txt");

		for (int i = 0; i < handshakeIndices.size(); ++i) {
			writer.println(handshakeIndices.get(i) + " 0");
		}

		writer.close();

	}
}
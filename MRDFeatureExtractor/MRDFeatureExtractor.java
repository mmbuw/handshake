import java.io.*;
import java.util.*;

public class MRDFeatureExtractor {

	// Constants
	public static final int NUM_SAMPLES_FOR_PEAK_DETECTION = 1;
	public static final int NUM_DATA_COLUMNS = 3;
	public static final int FEATURE_WINDOW_WIDTH = 150;
	public static final int PEAKMAP_TOP_K = 10;
	public static int LABELLED_HANDSHAKE_START;
	public static int LABELLED_HANDSHAKE_END;
	public static String FEATURE_OUTPUT_PATH;
	public static String FILENAME;

	// Data record processing helpers
	public static LinkedList<float[]> windowContent;
	public static int numRecordsProcessed = 0;
	public static int[] numAscentingSince;
	public static int[] numDescendingSince;
	public static float[] lastData;
	public static PrintWriter featureWriter;

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

	//-------------------------------------------------------------------------

	public static void main(String[] args) {

		/* Parse parameters */
		try {
			LABELLED_HANDSHAKE_START = Integer.parseInt(args[1]);
			LABELLED_HANDSHAKE_END = LABELLED_HANDSHAKE_START + FEATURE_WINDOW_WIDTH - 1;
			FEATURE_OUTPUT_PATH = args[2];
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

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

		

		/* Start application logic */
		try {
			featureWriter = new PrintWriter(FEATURE_OUTPUT_PATH);
			String[] splittedFileName = args[0].split("/");
			FILENAME = splittedFileName[splittedFileName.length-1];
			parseInputFile(args[0]);

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
		featureWriter.close();

	}

	public static void exportPeakMap(short[] peakmap, String nameSuffix) {

		try {
			String fileNameStem = FILENAME.replace(".txt", "");
			PrintWriter peakMapWriter = new PrintWriter("peakmaps/peakmap-" + fileNameStem + "-" + nameSuffix + ".txt");

			for (short s : peakmap) {
				peakMapWriter.println(s);
			}

			peakMapWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* MAIN FUNCTION FOR NEW INCOMING MEASUREMENTS                           */
	/* --------------------------------------------------------------------- */

	public static void processDataRecord(float[] data) {

		updateWindowContent(data);

		for (int i = 0; i < NUM_DATA_COLUMNS; ++i) {
			checkNewValueForPeaks(i, data[i]);
		}

		cleanPeaksOutsideWindow();
		extractAndWriteCurrentWindowFeatureVector();

		//Peak map
		if (LABELLED_HANDSHAKE_START > -1 &&
			numRecordsProcessed == LABELLED_HANDSHAKE_END) {
			short[] peakMapX = createPeakMapForCurrentWindow(0, true);
			short[] peakMapY = createPeakMapForCurrentWindow(1, false);
			short[] peakMapZ = createPeakMapForCurrentWindow(2, true);
			exportPeakMap(peakMapX, "x");
			exportPeakMap(peakMapY, "y");
			exportPeakMap(peakMapZ, "z");
		}

		lastData = data;
		++numRecordsProcessed;
	}


	/* MOVING WINDOW FUNCTIONS                                               */
	/* --------------------------------------------------------------------- */

	public static void updateWindowContent(float[] newData) {

		windowContent.addLast(newData);

		if (windowContent.size() > FEATURE_WINDOW_WIDTH) {
			windowContent.removeFirst();
		}

	}

	public static void cleanPeaksOutsideWindow() {

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

	public static String getGroundTruthClassLabelForCurrentWindow() {

		if (LABELLED_HANDSHAKE_START == -1) {
			return "no-handshake";
		}

		int overlap = Math.max(0, Math.min(numRecordsProcessed, LABELLED_HANDSHAKE_END) - Math.max(LABELLED_HANDSHAKE_START, numRecordsProcessed-FEATURE_WINDOW_WIDTH) );
		float overlapRatio = ((float) overlap) / FEATURE_WINDOW_WIDTH;

		if (overlapRatio > 0.8f) {
			return "handshake";
		} else {
			return "no-handshake";
		}

	}

	public static short[] createPeakMapForCurrentWindow(int column, boolean flipAxis) {

		// Remark: numRecordsProcessed is the last sample ID in the window

		short[] peakMap = new short[FEATURE_WINDOW_WIDTH];
		short maximumValueInMap = flipAxis ? (short) -1 : (short) 1;
		short minimumValueInMap = flipAxis ? (short) 1 : (short) -1;

		// Sort the maxima to retrieve the top ones
		TreeMap<Float, Integer> maximaTree = new TreeMap<Float, Integer>();
		for (int maxId = 0; maxId < maximaIndices[column].size(); ++maxId) { 
			maximaTree.put(new Float(maximaValues[column].get(maxId)), new Integer(maximaIndices[column].get(maxId))); 
		}

		// Sort the minima to retrieve the top ones
		TreeMap<Float, Integer> minimaTree = new TreeMap<Float, Integer>();
		for (int minId = 0; minId < minimaIndices[column].size(); ++minId) { 
			minimaTree.put(new Float(minimaValues[column].get(minId)), new Integer(minimaIndices[column].get(minId))); 
		}

		// Retrieve the top K maxima
		int maximaOutputted = 0;
		for (Map.Entry<Float, Integer> entry : maximaTree.descendingMap().entrySet()) {
			int maxId = entry.getValue();
			int peakMapIndex = maxId - (numRecordsProcessed - FEATURE_WINDOW_WIDTH + 1);
			peakMap[peakMapIndex] = maximumValueInMap;

			if (++maximaOutputted == PEAKMAP_TOP_K) {
				break;
			}
		}

		// Retrieve the top K minima
		int minimaOutputted = 0;
		for (Map.Entry<Float, Integer> entry : minimaTree.entrySet()) {
			int minId = entry.getValue();
			int peakMapIndex = minId - (numRecordsProcessed - FEATURE_WINDOW_WIDTH + 1);
			peakMap[peakMapIndex] = minimumValueInMap;
			
			if (++minimaOutputted == PEAKMAP_TOP_K) {
				break;
			}

		}

		//for (int i = 0; i < peakMap.length; ++i) {
		//	System.out.println( (LABELLED_HANDSHAKE_START + i) + " " + peakMap[i]);
		//}

		return peakMap;

	}


	/* PEAK DETECTION                                                        */
	/* --------------------------------------------------------------------- */

	public static void checkNewValueForPeaks(int column, float value) {

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

	public static void handleDetectedMaximum(int column, int sampleID, float value) {

		maximaIndices[column].addLast(sampleID);
		maximaValues[column].addLast(value);
		maximumCandidateIndex[column] = -1;
		maximumCandidateValue[column] = 0.0f;

	}

	public static void handleDetectedMinimum(int column, int sampleID, float value) {

		minimaIndices[column].addLast(sampleID);
		minimaValues[column].addLast(value);
		minimumCandidateIndex[column] = -1;
		minimumCandidateValue[column] = 0.0f;

	}

	/* FEATURE EXTRACTION                                                    */
	/* --------------------------------------------------------------------- */

	public static void extractAndWriteCurrentWindowFeatureVector() {

		if (windowContent.size() == FEATURE_WINDOW_WIDTH) {

			float[] currentWindowFeatureVector = createFeatureVector();

			for (int i = 0; i < currentWindowFeatureVector.length; ++i) {

				featureWriter.print(currentWindowFeatureVector[i] + ",");
			}

			featureWriter.print(getGroundTruthClassLabelForCurrentWindow() + "\n");

		}

	}


	public static float[] createFeatureVector() {

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

	public static float[] computeWindowAverages() {

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

	public static float[] computeWindowRanges() {

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

	public static float[] computeWindowInterPeakDistances() {

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
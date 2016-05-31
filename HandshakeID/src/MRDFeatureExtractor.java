import java.io.*;
import java.util.*;

public class MRDFeatureExtractor {

    // Constants
    public final int NUM_DATA_COLUMNS;
    public final int NUM_SAMPLES_FOR_PEAK_DETECTION;
    public final int MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS;
    public final int MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS;
    public final int ANALYSIS_FEATURE_WINDOW_WIDTH;

    public final float HANDSHAKE_Y_AXIS_MIN_RANGE_THRESHOLD = 15.0f;
    public final float HANDSHAKE_X_AXIS_MAX_RANGE_THRESHOLD = 120.0f;
    public final float HANDSHAKE_Z_AXIS_MAX_RANGE_THRESHOLD = 120.0f;
    public final float HANDSHAKE_POSITIVE_WINDOW_FRACTION = 0.3f;
    public final int HANDSHAKE_OSCILLATION_MIN_LENGTH = 30/5;
    
    public final int STREAK_FIRST_ID = 0;
    public final int STREAK_LENGTH_ID = 1;
    public final int STREAK_MAX_DIFF = 3;

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


    //-------------------------------------------------------------------------

    public MRDFeatureExtractor(int numDataColumns,
                               int numSamplesForPeakDetection,
                               int minNumSamplesForHandshakeAnalysis,
                               int maxNumSamplesForHandshakeAnalysis,
                               int analysisFeatureWindowWidth) {

        /* Parse parameters */
        NUM_DATA_COLUMNS = numDataColumns;
        NUM_SAMPLES_FOR_PEAK_DETECTION = numSamplesForPeakDetection;
        MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = minNumSamplesForHandshakeAnalysis;
        MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = maxNumSamplesForHandshakeAnalysis;
        ANALYSIS_FEATURE_WINDOW_WIDTH = analysisFeatureWindowWidth;

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
        
        numRecordsProcessed = 0;
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
        LinkedList<Integer> positiveWindowEndIds = new LinkedList<Integer>();

        while (movingWindowEnd < dataRecords.size()) {

            float[] ranges = computeWindowRanges(movingWindowStart, movingWindowEnd);

            if (rangesRepresentHandshake(ranges)) {
                positiveWindowEndIds.add(movingWindowEnd);
            }
            

            movingWindowStart += 1;
            movingWindowEnd += 1;
        }

        int positiveWindows = positiveWindowEndIds.size();
        int numOfAllWindows = dataRecords.size() - ANALYSIS_FEATURE_WINDOW_WIDTH + 1;
        float positiveWindowFraction = (float) positiveWindows/numOfAllWindows;
        int[] longestStreak = findLongestStreak(positiveWindowEndIds);
        
        //for (int i : positiveWindowEndIds) {
        //	System.out.print(i + " ");
        //}
        
        //int handshakeOscillationStart = longestStreak[0]-ANALYSIS_FEATURE_WINDOW_WIDTH+1;
        int handshakeOscillationStart = longestStreak[0];
        int handshakeOscillationEnd = (longestStreak[0] + longestStreak[1] - 1);
        int oscillationRegionLength = (handshakeOscillationEnd-handshakeOscillationStart+1);
        
        // handshake detection criteria
        if (    oscillationRegionLength > HANDSHAKE_OSCILLATION_MIN_LENGTH &&
                positiveWindowFraction > HANDSHAKE_POSITIVE_WINDOW_FRACTION) {
            handshakeDetected = true;
        }

        int[] zeroCrossings;
        float[] RMSValues;
        float[] ranges;
        float[] meanValues;
        float[] stdDevs;
        float totalEnergy;

        // actions performed when handshake was (not) detected
        if (handshakeDetected) {
            System.out.println("Classification result: handshake");
            System.out.println("Positive windows: " + positiveWindows + " of " + numOfAllWindows);
            System.out.println("Positive window fraction: " + positiveWindowFraction);
            System.out.println();
            System.out.println("Longest streak from " + handshakeOscillationStart + " to " + handshakeOscillationEnd);
            System.out.println("Oscillation length: " + oscillationRegionLength);
            
            // print features of handshake region
            System.out.println();
            System.out.println("Handshake region features:");
            zeroCrossings = computeWindowZeroCrossings(handshakeOscillationStart, handshakeOscillationEnd);
            RMSValues = computeWindowRMSValues(handshakeOscillationStart, handshakeOscillationEnd);
            ranges = computeWindowRanges(handshakeOscillationStart, handshakeOscillationEnd);
            meanValues = computeWindowMeans(handshakeOscillationStart, handshakeOscillationEnd);
            stdDevs = computeWindowStdDevs(handshakeOscillationStart, handshakeOscillationEnd);
            totalEnergy = computeWindowTotalEnergy(handshakeOscillationStart, handshakeOscillationEnd);
            
        } else {
            System.out.println("Classification Result: nothing");
            
            System.out.println();
            System.out.println("Features of whole snippet:");
            zeroCrossings = computeWindowZeroCrossings(0, dataRecords.size()-1);
            RMSValues = computeWindowRMSValues(0, dataRecords.size()-1);
            ranges = computeWindowRanges(0, dataRecords.size()-1);
            meanValues = computeWindowMeans(0, dataRecords.size()-1);
            stdDevs = computeWindowStdDevs(0, dataRecords.size()-1);
            totalEnergy = computeWindowTotalEnergy(0, dataRecords.size()-1);
        }
        
        // print feature results
        /*System.out.println("Zero crossings: " + zeroCrossings[0] + ", " + zeroCrossings[1] + ", " + zeroCrossings[2]);
        System.out.println("RMS: " + RMSValues[0] + ", " + RMSValues[1] + ", " + RMSValues[2]);
        System.out.println("Ranges: " + ranges[0] + ", " + ranges[1] + ", " + ranges[2]);
        System.out.println("Means: " + meanValues[0] + ", " + meanValues[1] + ", " + meanValues[2]);
        System.out.println("Standard deviations: " + stdDevs[0] + ", " + stdDevs[1] + ", " + stdDevs[2]);
        System.out.println("Total energy: " + totalEnergy);*/
        
        totalEnergy = totalEnergy / dataRecords.size();
        System.out.println("Zero crossings: " + zeroCrossings[1]);
        System.out.println("RMS: " + RMSValues[1]);
        System.out.println("Range: " + ranges[1]);
        System.out.println("Mean: " + meanValues[1]);
        System.out.println("Standard deviation: "+ stdDevs[1]);
        System.out.println("Total energy: " + totalEnergy);
        
        String fingerprint = computeFingerprint(oscillationRegionLength, zeroCrossings[1], RMSValues[1],
        		                                ranges[1], meanValues[1], stdDevs[1], totalEnergy);
        
        System.out.println("Fingerprint: " + fingerprint);
        


    }
    
    public String computeFingerprint(int windowSize, int zeroCrossings, float RMSValue, 
    		                         float range, float mean, float stdDev, float totalEnergy) {
    	
    	LinkedList<String> fingerprintComponents = new LinkedList<String>();
    	fingerprintComponents.addLast(sortToBin(windowSize, 6, 30, 1));
    	fingerprintComponents.addLast(sortToBin(zeroCrossings, 3, 9, 1));
    	fingerprintComponents.addLast(sortToBin(RMSValue, 8, 14, 1));
    	//fingerprintComponents.addLast(sortToBin(range, 30, 40, 1));
    	//fingerprintComponents.addLast(sortToBin(mean, -5, 0, 2));
    	fingerprintComponents.addLast(sortToBin(stdDev, 5, 15, 1));
    	//fingerprintComponents.addLast(sortToBin(totalEnergy, 50, 100, 2));
    	
    	String finalString = "";
    	for (String component : fingerprintComponents)
    		finalString += component + " ";
    	return finalString;
    	
    }
    
    public String sortToBin(float value, float minValue, float maxValue, int numBits) {
    	
    	// Cap values if they are outside the specified ranges 
    	float valueToProcess = value;
    	if (valueToProcess < minValue)
    		valueToProcess = minValue;
    	if (valueToProcess >= maxValue)
    		valueToProcess = maxValue - 0.00001f;
    	
    	// Compute bin
    	float percentageInRange = (valueToProcess - minValue)/(maxValue - minValue);
    	int numBins = (int) Math.pow(2,  numBits);
    	float binSize = 1.0f/numBins;
    	int binID = (int) Math.floor(percentageInRange / binSize);
    	
    	float lowerBinDistance = (percentageInRange/binSize) - (float) Math.floor(percentageInRange / binSize);
    	float higherBinDistance = (float) Math.ceil(percentageInRange / binSize) - (percentageInRange/binSize);
    	float shortestBinBorderDistance = Math.min(lowerBinDistance, higherBinDistance);

    	
    	// Convert bin ID to binary form
    	String binaryBinID = padBinaryString(Integer.toBinaryString(binID), numBits);
    	
    	if (shortestBinBorderDistance < 0.2) {
    		if (Math.min(lowerBinDistance, higherBinDistance) == lowerBinDistance &&
    			binID-1 >= 0) {
    			
    			String alternative = padBinaryString(Integer.toBinaryString(binID-1), numBits);
    			binaryBinID += "||" + alternative;
    					
    		} else if (Math.min(lowerBinDistance, higherBinDistance) == higherBinDistance &&
    		           binID+1 < numBins) {
    			
    			String alternative = padBinaryString(Integer.toBinaryString(binID+1), numBits);
    			binaryBinID += "||" + alternative;
    			
    		}
    	}
    	
    	return binaryBinID;
    }
    
    public String padBinaryString(String binary, int numBits) {
    	
    	String output = binary;
    	
    	while (output.length() < numBits) {
    		output = "0" + output;
    	}
    	
    	return output;
    }

    public boolean rangesRepresentHandshake(float[] ranges) {

        if (    ranges[0] < HANDSHAKE_X_AXIS_MAX_RANGE_THRESHOLD &&
                ranges[1] > HANDSHAKE_Y_AXIS_MIN_RANGE_THRESHOLD &&
                ranges[2] < HANDSHAKE_Z_AXIS_MAX_RANGE_THRESHOLD) {

            return true;

        }

        return false;

    }
    
    public int[] findLongestStreak(LinkedList<Integer> positiveWindowList) {
    	
    	if (positiveWindowList != null &&
    		positiveWindowList.size() > 0) {
    		
	    	int lastWindowId = 0;
	    	int currentStreakStart = positiveWindowList.getFirst();
	    	int currentStreakLength = 1;
	    	int[] longestStreak = new int[2]; // holds first streak window and length
	    	
	    	for (int winId : positiveWindowList) {
	    		
	    		if (winId-lastWindowId <= STREAK_MAX_DIFF) {
	    			
	    			// the current streak is increased
	    			currentStreakLength = currentStreakLength + (winId-lastWindowId);
	    			
	    		} else {
	    			
	    			// the end of a streak was detected
	    			if (currentStreakLength > longestStreak[STREAK_LENGTH_ID]) {
	    				longestStreak[STREAK_FIRST_ID] = currentStreakStart;
	    				longestStreak[STREAK_LENGTH_ID] = currentStreakLength;
	    			}
	    		
	    			currentStreakLength = 1;
	    			currentStreakStart = winId;
	    		}
	    			
	    		lastWindowId = winId;
	    	}
	    	
	    	// the streak ends with the window
			if (currentStreakLength > longestStreak[STREAK_LENGTH_ID]) {
				longestStreak[STREAK_FIRST_ID] = currentStreakStart;
				longestStreak[STREAK_LENGTH_ID] = currentStreakLength;
			}
	    	
	    	return longestStreak;
    	
    	} else {
    		int[] failure = {-1, -1};
    		return failure;
    	}
    	
    	
    }
    
    /* WINDOW FEATURE EXTRACTION                                             */
	/* --------------------------------------------------------------------- */
    
    
    public int[] computeWindowZeroCrossings(int startSample, int endSample) {
    	
    	int[] numZeroCrossings = new int[NUM_DATA_COLUMNS];
    	float[] lastRecord = new float[NUM_DATA_COLUMNS];
    	
    	for (int sample = startSample; sample <= endSample; ++sample) {
    		
    		float[] record = dataRecords.get(sample);
    		
    		for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
    			if ((record[col] > 0.0f && lastRecord[col] < 0.0f) ||
    			   (record[col] < 0.0f && lastRecord[col] > 0.0f)) {
    				numZeroCrossings[col]++;
    			}
    		}
    		
    		lastRecord = record; 
    	}
    	
    	return numZeroCrossings;
    }
    
    public float[] computeWindowRMSValues(int startSample, int endSample) {
    	
    	float[] RMSValues = new float[NUM_DATA_COLUMNS];
    	
    	for (int sample = startSample; sample <= endSample; ++sample) {
    		
			float[] record = dataRecords.get(sample);
			
			for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
				RMSValues[col] += record[col]*record[col];
			}
			
    	}
    	
    	
    	for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
    		RMSValues[col] = RMSValues[col] / ((float) (endSample-startSample+1));
    		RMSValues[col] = (float) Math.sqrt(RMSValues[col]);
    	}
    	
    	return RMSValues;	
    }
    
    public float[] computeWindowRanges(int startSample, int endSample) {

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
    
    public float[] computeWindowMeans(int startSample, int endSample) {
    	
    	float[] meanValues = new float[NUM_DATA_COLUMNS];
    	
		for (int sample = startSample; sample <= endSample; ++sample) {
		    		
			float[] record = dataRecords.get(sample);
			
			for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
				meanValues[col] += record[col];
			}
			
		}
		
		for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
			meanValues[col] = meanValues[col] / ((float) (endSample-startSample+1));
		}
		
		return meanValues;
    }
    
    public float[] computeWindowStdDevs(int startSample, int endSample) {
    	
    	float[] meanValues = computeWindowMeans(startSample, endSample);
    	float[] standardDeviations = new float[NUM_DATA_COLUMNS];
    	
    	for (int sample = startSample; sample <= endSample; ++sample) {
    		
			float[] record = dataRecords.get(sample);
			
			for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
				standardDeviations[col] += (meanValues[col]-record[col])*(meanValues[col]-record[col]);
			}
    	}
    	
    	for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
    		standardDeviations[col] = standardDeviations[col] / ((float) (endSample-startSample+1));
    		standardDeviations[col] = (float) Math.sqrt(standardDeviations[col]);
		}
    			
    	return standardDeviations;
    	
    }
    
    public float computeWindowTotalEnergy(int startSample, int endSample) {
    	
    	float totalEnergy = 0.0f;
    	
		for (int sample = startSample; sample <= endSample; ++sample) {
		    		
			float[] record = dataRecords.get(sample);
			float contribution = 0.0f;
			
			for (int col = 0; col < NUM_DATA_COLUMNS; ++col) {
				
				// DEBUG: TOTAL ENERGY JUST FOR Y-AXIS
				if (col == 1)
					contribution += record[col]*record[col];
			}
			
			totalEnergy += contribution;
			
		}
    	
    	return totalEnergy;
    }

    
	/* PEAK MAP CREATION                                                     */
	/* --------------------------------------------------------------------- */
    
	public short[] createPeakMapForCurrentDataRecords(int column, boolean flipAxis) {
		
		// Remark: numRecordsProcessed is the last sample ID in the window
		int PEAKMAP_TOP_K = 5;

		short[] peakMap = new short[dataRecords.size()];
		short maximumValueInMap = flipAxis ? (short) -1 : (short) 1;
		short minimumValueInMap = flipAxis ? (short) 1 : (short) -1;

		// Sort the maxima to retrieve the top ones
		TreeMap<Float, Set<Integer>> maximaTree = new TreeMap<Float, Set<Integer>>();
		for (int maxId = 0; maxId < maximaIndices[column].size(); ++maxId) {
			
			float mapKey = new Float(maximaValues[column].get(maxId));
			int mapValue = new Integer(maximaIndices[column].get(maxId)); 
			
			if (!maximaTree.keySet().contains(mapKey)) {
				maximaTree.put(mapKey, new TreeSet<Integer>());
			}
			
			maximaTree.get(mapKey).add(mapValue);
		}

		// Sort the minima to retrieve the top ones
		TreeMap<Float, Set<Integer>> minimaTree = new TreeMap<Float, Set<Integer>>();
		for (int minId = 0; minId < minimaIndices[column].size(); ++minId) {
			
			float mapKey = new Float(minimaValues[column].get(minId));
			int mapValue = new Integer(minimaIndices[column].get(minId)); 
			
			if (!minimaTree.keySet().contains(mapKey)) {
				minimaTree.put(mapKey, new TreeSet<Integer>());
			}
			
			minimaTree.get(mapKey).add(mapValue);
		}
		
		// Retrieve the top K maxima
		int maximaOutputted = 0;
		maximaTreeLoop:
		for (Map.Entry<Float, Set<Integer>> entry : maximaTree.descendingMap().entrySet()) {
			Set<Integer> maxIds = entry.getValue();
			
			for (int id : maxIds) {
				peakMap[id] = maximumValueInMap;
				if (++maximaOutputted == PEAKMAP_TOP_K)
					break maximaTreeLoop;
			}
		}

		// Retrieve the top K minima
		int minimaOutputted = 0;
		minimaTreeLoop:
		for (Map.Entry<Float, Set<Integer>> entry : minimaTree.entrySet()) {
			Set<Integer> minIds = entry.getValue();
			
			for (int id : minIds) {
				peakMap[id] = minimumValueInMap;
				if (++minimaOutputted == PEAKMAP_TOP_K)
					break minimaTreeLoop;
			}

		}
		
		return peakMap;
	}
	
	public void exportPeakMap(short[] peakmap, String originalFilename) {

		try {
			PrintWriter peakMapWriter = new PrintWriter(originalFilename + ".peakmap");
			int writeCount = 0;

			for (short s : peakmap) {
				peakMapWriter.println(writeCount++ + " " + s);
			}

			peakMapWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

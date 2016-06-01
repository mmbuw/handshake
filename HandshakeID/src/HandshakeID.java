import java.io.*;
import java.util.*;

public class HandshakeID {

	public static MRDFeatureExtractor mrdFeatureExtractor;
	
	public static void main(String[] args) throws FileNotFoundException {
		
		System.setOut(new PrintStream(new FileOutputStream("latest-output.txt")));
		
		mrdFeatureExtractor = new MRDFeatureExtractor(3,    // number of data columns
										              1,    // samples for peak detection
										              20,  // minimum handshake window size
										              1000, // maximum handshake window size
										              7    // analysis feature window width
										             );
		
		//use 3,1,100,1000,15
		/*feedFile("data/shake1e.txt");
		feedFile("data/shake1t.txt");
		feedFile("data/shake2e.txt");
		feedFile("data/shake2t.txt");
		feedFile("data/shake3e.txt");
		feedFile("data/shake3t.txt");
		feedFile("data/shake4e.txt");
		feedFile("data/shake4t.txt");
		feedFile("data/shake5e.txt");
		feedFile("data/shake5t.txt");
		feedFile("data/no1.txt");
		feedFile("data/no2.txt");
		feedFile("data/no3.txt");
		feedFile("data/no4.txt");
		feedFile("data/no5.txt");
		feedFile("data/no6.txt");*/
		
		//use 3,1,20,1000,7
		feedFile("data/shake1e-simple.txt");
		feedFile("data/shake1t-simple.txt");
		feedFile("data/shake2e-simple.txt");
		feedFile("data/shake2t-simple.txt");
		feedFile("data/shake3e-simple.txt");
		feedFile("data/shake3t-simple.txt");
		feedFile("data/shake4e-simple.txt");
		feedFile("data/shake4t-simple.txt");
		feedFile("data/shake5e-simple.txt");
		feedFile("data/shake5t-simple.txt");
		
		
	}
	
	public static void feedFile(String filename) throws FileNotFoundException {

		System.out.println();
		System.out.println("-------------------------------------");
		System.out.println(filename);
		System.out.println("-------------------------------------");
		
		File file = new File(filename);
		Scanner scanner = new Scanner(file);
		mrdFeatureExtractor.startDataEvent();

		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			line = line.trim();
			line = line.replace(" ", "");
			String[] splittedLine = line.split(",");
			float[] data = new float[splittedLine.length];

			for (int i = 0; i < 3; ++i) {
				data[i] = Float.parseFloat(splittedLine[i]);
			}

			mrdFeatureExtractor.processDataRecord(data);
		}
		
		mrdFeatureExtractor.endDataEvent();
        
		// create and export peak map
        short[] yPeakMap = mrdFeatureExtractor.createPeakMapForCurrentDataRecords(1, false);
        mrdFeatureExtractor.exportPeakMap(yPeakMap, filename);
        
		scanner.close();

	}
}

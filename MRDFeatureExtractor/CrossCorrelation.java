/* Source: MEAPsoft */
/* https://www.ee.columbia.edu/~ronw/code/dev/MEAPsoft/src/com/meapsoft/DSP.java */

import java.io.*;
import java.util.*;

public class CrossCorrelation {

    public static void main(String[] args) {

        String filename1 = args[0];
        String filename2 = args[1];

        try {
            double[] array1 = parseFile(filename1);
            double[] array2 = parseFile(filename2);
            double[] xcorrResult = xcorr(array1, array2);

            Arrays.sort(xcorrResult);
            System.out.println(xcorrResult[xcorrResult.length-1]);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public static double[] parseFile(String filename) throws FileNotFoundException {

        ArrayList<Double> values = new ArrayList<Double>();

        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            line = line.trim();
            line = line.replace(" ", "");
            values.add(Double.parseDouble(line));
        }

        scanner.close();

        double[] returnArray = new double[values.size()];

        for (int i = 0; i < values.size(); ++i) {
            returnArray[i] = values.get(i);
        }

        return returnArray;
    }

    /**
     * Computes the cross correlation between sequences a and b.
     */
    public static double[] xcorr(double[] a, double[] b)
    {
        int len = a.length;
        if(b.length > a.length)
            len = b.length;

        return xcorr(a, b, len-1);
    }

    /**
     * Computes the auto correlation of a.
     */
    public static double[] xcorr(double[] a)
    {
        return xcorr(a, a);
    }

    /**
     * Computes the cross correlation between sequences a and b.
     * maxlag is the maximum lag to
     */
    public static double[] xcorr(double[] a, double[] b, int maxlag)
    {
        double[] y = new double[2*maxlag+1];
        Arrays.fill(y, 0);
        
        for(int lag = b.length-1, idx = maxlag-b.length+1; 
            lag > -a.length; lag--, idx++)
        {
            if(idx < 0)
                continue;
            
            if(idx >= y.length)
                break;

            // where do the two signals overlap?
            int start = 0;
            // we can't start past the left end of b
            if(lag < 0) 
            {
                start = -lag;
            }

            int end = a.length-1;
            // we can't go past the right end of b
            if(end > b.length-lag-1)
            {
                end = b.length-lag-1;
            }

            for(int n = start; n <= end; n++)
            {
                y[idx] += a[n]*b[lag+n];
            }
        }

        return(y);
    }

}
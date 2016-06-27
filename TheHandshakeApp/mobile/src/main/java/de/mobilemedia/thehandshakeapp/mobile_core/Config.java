package de.mobilemedia.thehandshakeapp.mobile_core;

import android.bluetooth.le.AdvertiseSettings;

public class Config {

    public static String INITIAL_HANDSHAKE_SHORTURL = "http://bit.ly/1mmNNln";
    public static String INITIAL_HANDSHAKE_LONGURL = "http://www.binaryhexconverter.com/hex-to-decimal-converter";

    public static int BLE_SCAN_PERIOD = 4000;
    public static int BLE_ADVERTISE_PERIOD = 4000;
    public static int BLE_TAG = 0x4343;

    public static int BLE_ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
    public static int BLE_ADVERTISE_TX_POWER_LVL = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
    public static int BLE_MIN_RSSI = -75;

    public static String HANDSHAKE_FILE_NAME = "handshakes.map";
    public static String SETTINGS_FILE_NAME = "settings.map";
    public static boolean LOAD_PREV_STATE = true;
    public static boolean SAVE_CURR_STATE = true;

    /* Handshake detection parameters */

    //Number of data columns of the incoming data
    public static int NUM_DATA_COLUMNS = 3;

    //Number of consecutive ascenting or descending samples in order to detect a peak
    public static int NUM_SAMPLES_FOR_PEAK_DETECTION = 1;

    //Number of incoming data events to merge together
    public static int NUM_SAMPLES_TO_MERGE = 1;

    //Minimum number of samples to start checking for a handshake
    public static int MINIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = 100;

    //Maximum number of samples to start checking for a handshake
    public static int MAXIMUM_DATA_SAMPLES_FOR_HANDSHAKE_ANALYSIS = 1000;

    //Width of the moving window on data packages for handshake analysis
    public static int ANALYSIS_FEATURE_WINDOW_WIDTH = 20;

    //Lower bound which must be fulfilled by the y-axis range in the ANALYSIS_FEATURE_WINDOW
    //in order to have a positive window for handshake detection
    public static float HANDSHAKE_Y_AXIS_MIN_RANGE_THRESHOLD = 15.0f;

    //Upper bound which must be fulfilled by the x-axis range in the ANALYSIS_FEATURE_WINDOW
    //in order to have a positive window for handshake detection
    public static float HANDSHAKE_X_AXIS_MAX_RANGE_THRESHOLD = 200.0f; // unbounded

    //Upper bound which must be fulfilled by the z-axis range in the ANALYSIS_FEATURE_WINDOW
    //in order to have a positive window for handshake detection
    public static float HANDSHAKE_Z_AXIS_MAX_RANGE_THRESHOLD = 200.0f; // unbounded

    //Minimum percentage of positive windows in a data package for handshake detection
    public static float HANDSHAKE_POSITIVE_WINDOW_FRACTION = 0.25f;

    //Minimum number of consecutive positive windows in a data package for handshake detection
    public static int HANDSHAKE_OSCILLATION_MIN_LENGTH = 6;

    //Maximum number of non-continuous positive windows a positive window streak is allowed to have
    public static int STREAK_MAX_DIFF = 3;


}

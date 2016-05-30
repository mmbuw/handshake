package de.mobilemedia.thehandshakeapp.mobile_core;

import android.bluetooth.le.AdvertiseSettings;

/**
 * Created by projectsw on 26.05.16.
 */
public class Config {

    public static String INITIAL_HANDSHAKE_SHORTURL = "http://bit.ly/1mmNNln";
    public static String INITIAL_HANDSHAKE_LONGURL = "http://www.binaryhexconverter.com/hex-to-decimal-converter";

    public static int BLE_SCAN_PERIOD = 4000;
    public static int BLE_TAG = 0x4343;

    public static int ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
    public static int ADVERTISE_TX_POWER_LVL = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

    public static String HANDSHAKE_FILE_NAME = "handshakes.map";
    public static String SETTINGS_FILE_NAME = "settings.map";
    public static boolean LOAD_PREV_STATE = true;
    public static boolean SAVE_CURR_STATE = true;

}

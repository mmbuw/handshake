package de.mobilemedia.thehandshakeapp.mobile_core;

import android.bluetooth.le.AdvertiseSettings;

/**
 * Created by projectsw on 26.05.16.
 */
public class Config {

    public static String INITIAL_HANDSHAKE_SHORTURL = "http://bit.ly/1mmNNln";
    public static String INITIAL_HANDSHAKE_LONGURL = "http://www.binaryhexconverter.com/hex-to-decimal-converter";

    public static int BLE_SCAN_PERIOD = 3000;
    public static int BLE_TAG = 0x4343;

    public static int ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    public static int ADVERTISE_TX_POWER_LVL = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

    public static String HANDSHAKE_FILE_NAME = "handshakes.map";
    public static boolean LOAD_HANDSHAKES = false;
    public static boolean SAVE_HANDSHAKES = true;

}

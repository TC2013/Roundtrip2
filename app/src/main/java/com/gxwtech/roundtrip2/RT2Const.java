package com.gxwtech.roundtrip2;

/**
 * Created by geoff on 3/21/16.
 */
public class RT2Const {
    public static final String RT2Preferences="Roundtrip2_Preferences";


    /*
    *
    * This section is for intents used for local broadcast within Roundtrip2's activities
    *
     */

    public static final String INTENT_startScan = "INTENT_startScan";
    public static final String INTENT_scanStarted = "INTENT_scanStarted";
    public static final String INTENT_scanStopped = "INTENT_scanStopped";

    /* INTENT_deviceFound has String parameter "address" which is the MAC address of the device */
    /* means: the scan activity has discovered a device (might not be RileyLink) */
    public static final String INTENT_deviceFound = "INTENT_deviceFound";

    /* INTENT_deviceSelected also has string parameter "address" which is the MAC address */
    /* means: user chose a (rileylink) device */
    public static final String INTENT_deviceSelected = "INTENT_deviceSelected";

    /*
    *
    * This section is for Message passing between the Roundtrip2 app and the RoundtripService
    *
    *
     */

    /* keep this list short, and put the real message type into a bundle field */
    public static final int MSG_ping = 1;
    public static final int MSG_BLE = 2;

    /* indicates that a BLE scan should be started. */
    /* if the optional parameter "address" is given, scan will be for only that MAC address */
    public static final String MSG_BLE_startScan = "MSG_BLE_startScan";

    /* sent from service, to indicate that the scan has been started */
    public static final String MSG_BLE_scanStarted = "MSG_BLE_scanStarted";

    /* sent from service, to indicate that the scan has been stopped */
    public static final String MSG_BLE_scanStopped = "MSG_BLE_scanStopped";

    /* sent from service, to indicate a result was found from the BLE scan */
    /* contains String parameter "address" which is the MAC address of the device */
    public static final String MSG_BLE_deviceFound = "MSG_BLE_deviceFound";

    public static final String MSG_BLE_requestForAccess = "MSG_BLE_iCanHazBluetooth?";

    public static final String MSG_BLE_accessGranted = "MSG_BLE_accessYesYesYes";
    public static final String MSG_BLE_accessDenied = "MSG_BLE_accessNoNoNo";
    public static final String MSG_BLE_useThisDevice = "MSG_BLE_useThisDevice";
    public static final String MSG_BLE_TEST = "MSG_BLE_TEST";


}

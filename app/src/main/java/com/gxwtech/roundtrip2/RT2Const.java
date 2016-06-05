package com.gxwtech.roundtrip2;

/**
 * Created by geoff on 6/5/16.
 */
public class RT2Const {
    public static final String RT2Prefix = "com.gxwtech.roundtrip2.";
    public class IPC {
        public static final String Prefix = RT2Prefix + "IPC.";

        // used in IPC to mark a message as a BLE message
        public static final int MSG_BLE = 66;
        // used by gui to pass the address of the Rileylink to the Service
        // has an 'address' field with the string address.
        public static final String MSG_BLE_useThisDevice = Prefix + "MSG_BLE_useThisDevice";
        // used by gui to tell service that BLE access is denied by user.
        public static final String MSG_BLE_accessDenied = Prefix + "MSG_BLE_accessDenied";
        public static final String MSG_BLE_accessGranted = Prefix + "MSG_BLE_accessGranted";
        // used by service to ask user for Bluetooth permission
        public static final String MSG_BLE_requestAccess = Prefix + "MSG_BLE_requestAccess";
        public static final String MSG_BLE_RileyLinkReady = Prefix + "MSG_BLE_RileyLinkReady";
    }
    public class local {
        // These are local to the GUI activities
        public static final String Prefix = RT2Prefix + "local.";
        public static final String INTENT_RileyLinkReady = Prefix + "INTENT_RileyLinkReady";
    }
    public class serviceLocal {
        public static final String Prefix = RT2Prefix + "serviceLocal.";
        // for local broadcasts annoucing connectivity events
        public static final String bluetooth_connected = Prefix + "bluetooth_connected";
        public static final String bluetooth_disconnected = Prefix + "bluetooth_disconnected";
        public static final String BLE_services_discovered = Prefix + "BLE_services_discovered";

    }
}

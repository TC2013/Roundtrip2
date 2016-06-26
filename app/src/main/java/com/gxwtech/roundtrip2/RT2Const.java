package com.gxwtech.roundtrip2;

/**
 * Created by geoff on 6/5/16.
 */
public class RT2Const {
    public static final String RT2Prefix = "com.gxwtech.roundtrip2.";
    public class IPC {
        public static final String Prefix = RT2Prefix + "IPC.";

        // This message is used to bind the "replyTo" field
        public static final int MSG_clientRegistered = 63; // arbitrary
        public static final int MSG_unregisterClient = 64;
        public static final int MSG_registerClient = 65;

        // used in IPC to mark a message as a generic IPC message
        public static final int MSG_IPC = 66;
        // used as the key to find the message in the bundle.
        public static final String messageKey = Prefix + "messageKey";
        // used as a key for the bundle, when the bundle is packed into an Intent
        public static final String bundleKey = Prefix + "bundleKey";

        // used by gui to pass the address of the Rileylink to the Service
        // has an 'address' field with the string address.
        public static final String MSG_BLE_useThisDevice = Prefix + "MSG_BLE_useThisDevice";
        public static final String MSG_BLE_useThisDevice_addressKey = Prefix + "MSG_BLE_useThisDevice_addressKey";

        // used by gui to tell service that BLE access is denied by user.
        public static final String MSG_BLE_accessDenied = Prefix + "MSG_BLE_accessDenied";
        public static final String MSG_BLE_accessGranted = Prefix + "MSG_BLE_accessGranted";
        // used by service to ask user for Bluetooth permission
        public static final String MSG_BLE_requestAccess = Prefix + "MSG_BLE_requestAccess";
        public static final String MSG_BLE_RileyLinkReady = Prefix + "MSG_BLE_RileyLinkReady";

        // used to pass the pump ID from GUI to service
        // has a 'pumpID' field containing a six digit String
        public static final String MSG_PUMP_useThisAddress = Prefix + "MSG_PUMP_useThisAddress";
        public static final String MSG_PUMP_useThisAddress_pumpIDKey = Prefix + "MSG_PUMP_useThisAddress_pumpIDKey";

        // These are used to pass information about the pump from the service to the GUI.
        // has a 'model' field
        public static final String MSG_PUMP_reportedPumpModel = Prefix + "MSG_PUMP_reportedPumpModel";
        public static final String MSG_PUMP_pumpFound = Prefix + "MSG_PUMP_pumpFound";
        public static final String MSG_PUMP_pumpLost = Prefix + "MSG_PUMP_pumpLost";

        public static final String MSG_PUMP_tunePump = Prefix + "MSG_PUMP_tunePump";
        public static final String MSG_PUMP_quickTune = Prefix + "MSG_PUMP_quickTune";
        public static final String MSG_PUMP_fetchHistory = Prefix + "MSG_PUMP_fetchHistory";

        public static final String MSG_PUMP_history = Prefix + "MSG_PUMP_history";
        public static final String MSG_PUMP_history_key = Prefix + "MSG_PUMP_history_key";
        public static final String MSG_PUMP_fetchSavedHistory = Prefix + "MSG_PUMP_fetchSavedHistory";

        // interface for ServiceCommand/ServiceResult
        public static final String MSG_ServiceCommand = Prefix + "MSG_ServiceCommand";
        public static final String MSG_ServiceResult = Prefix + "MSG_ServiceResult";

    }
    public class local {
        // These are local to the GUI activities
        public static final String Prefix = RT2Prefix + "local.";
        public static final String INTENT_serviceConnected = Prefix + "INTENT_serviceConnected";
        public static final String INTENT_NEW_rileylinkAddressKey = Prefix + "INTENT_NEW_rileylinkAddressKey";
        public static final String INTENT_NEW_pumpIDKey = Prefix + "INTENT_NEW_pumpIDKey";


        public static final String INTENT_historyPageViewerReady = Prefix + "I'm ready, hit me up";
        public static final String INTENT_historyPageBundleIncoming = Prefix + "Here's the kitchen sink";
    }

    public class serviceLocal {
        public static final String Prefix = RT2Prefix + "serviceLocal.";
        // for local broadcasts annoucing connectivity events
        public static final String bluetooth_connected = Prefix + "bluetooth_connected";
        public static final String bluetooth_disconnected = Prefix + "bluetooth_disconnected";
        public static final String BLE_services_discovered = Prefix + "BLE_services_discovered";

        public static final String ipcBound = Prefix + "ipcBound";

        // primary shared preferences file identifier
        public static final String sharedPreferencesKey = Prefix + "sharedPreferencesKey";

        // These are used to identify shared preference items
        public static final String pumpIDKey = Prefix + "PumpIDKey";
        public static final String rileylinkAddressKey = Prefix + "rileylinkAddressKey";
        public static final String prefsLastGoodPumpFrequency = Prefix + "prefsLastGoodPumpFrequency";

        // The the key to identify the hashCode() of the msg.replyTo, when the bundle is moved to an intent.
        public static final String IPCReplyTo_hashCodeKey = Prefix + "IPCReplyTo_hashCodeKey";

    }
}

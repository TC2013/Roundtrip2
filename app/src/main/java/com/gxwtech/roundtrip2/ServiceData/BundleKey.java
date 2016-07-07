package com.gxwtech.roundtrip2.ServiceData;

/**
 * Created by geoff on 7/4/16.
 */
public class BundleKey {
    public static final String BasePrefix = "com.gxwtech.roundtrip2.ServiceData.";

    public class MessageBundle {
        public static final String instantKey = BasePrefix + "instant"; // long
        public static final String commandBundleKey = BasePrefix + "commandBundle"; // Bundle
        public static final String actionKey = BasePrefix + "action"; // String
        public static final String replyToKey = BasePrefix + "replyTo"; // String (hashkey for Messenger)
    }

    // A ServiceMessage may be contained within a MessageBundle
    public class ServiceMessage {
        public static final String serviceMessageTypeKey = BasePrefix + "ServiceMessageType"; // String

        // a CommandBundle is a specialized ServiceMessage
        public class ServiceCommand {
            // if ServiceMessageType == "ServiceCommand"
            public static final String commandKey = BasePrefix + "command"; // String
            public static final String commandIDKey = BasePrefix + "commandID"; // String

            // if command == "SetTempBasal"
            public class SetTempBasal {
                public static final String amountKey = "amountUnitsPerHour"; // Double
                public static final String durationKey = "durationMinutes"; // int
            }

            public class ReadBasalProfile {
                public static final String whichKey = "which"; // String
            }

            public class SendBolus {
                public static final String amountUnitsKey = "amountUnits"; // Double
            }

            public class SetPumpClock {
                public static final String localDateTimeKey = "localDateTime"; // String
            }

            public class UseThisRileylink {
                public static final String rlAddressKey = "rlAddress"; // String
            }

            public class RetrieveHistoryPage {
                public static final String pageNumberKey = "pageNumber"; // int
            }
        }

        // if ServiceMessageType == "ServiceResult"
        public class ServiceResult {
            public static final String commandIDKey = BasePrefix + "commandID"; // String // NOTE: same as in ServiceCommand
            public static final String commandBundleKey = BasePrefix + "commandBundle"; // Bundle
            public static final String responseBundleKey = BasePrefix + "responseBundle"; // Bundle

            public class Response {
                public static final String resultKey = "result"; // String ("OK" or "error")
                public static final String errorCodeKey = "errorCode"; // int
                public static final String errorDescriptionKey = "errorDescription"; // String
            }

        }



    }

}

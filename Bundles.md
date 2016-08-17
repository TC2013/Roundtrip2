# Bundle definitions

## MessageBundle

This is the top level bundle used for passing Service messages over the IPC interface.

(long) "com.gxwtech.roundtrip2.IPC.instantKey"

   Number of milliseconds since 1970 when this message was sent from the client, in a command bundle, or when this message was received by the client, in a result bundle.

(Bundle) "com.gxwtech.roundtrip2.IPC.bundleKey"

   A CommandBundle representing the ServiceCommand and its arguments

(String) "com.gxwtech.roundtrip2.IPC.messageKey"

   The "action" field of the local Intent that this message will be converted to.  This can be used to route messages to different parts of RoundtripService, but currently there is only one value "com.gxwtech.roundtrip2.IPC.MSG_ServiceCommand"

> ### messageKey = "com.gxwtech.roundtrip2.IPC.MSG_ServiceCommand"

>> This is a standard ServiceCommand, from client to service.

> ### messageKey = "com.gxwtech.roundtrip2.IPC.MSG_BLE_RileyLinkReady"

>> This is an old-style IPC message (FIXME: convert to ServiceData style)

> ### messageKey = "com.gxwtech.roundtrip2.IPC.MSG_PUMP_pumpLost"

>> old style (FIXME: converto to ServiceData style)

(String) "com.gxwtech.roundtrip2.serviceLocal.IPCReplyTo_hashCodeKey"

> The key that can be used to find the client's Messenger that sent the command, to enable replies.

## CommandBundle

(String) "ServiceMessageType"

> The type of service message, from the ServiceData package.  For a CommandBundle, this value will be "ServiceCommand"

(String) "commandID"

> This string is set when the command is created, and can be anything the client wants.  The Service promises to pass it back with the result, so that the result can be correlated with the command that started it.  By default, it is a random UUID string.

(String) "command"

> This is the actual service command, and each variant has different parameters.

### command = "SetTempBasal"

(Double) "amountUnitsPerHour"

> Temp basal amount, in Units per hour.

(int) "durationMinutes"

> Temp basal duration, in minutes.

### command = "ReadBasalProfile"

(String) "which"

> which profile of "STD", "A", or "B" to read.

### command = "ReadPumpClock"

> (No parameters)

### command = "SendBolus"

(String) "amountUnits"

> amount (in Units) of the bolus

### command = "SetPumpClock"

(String) "localDateTime"

> Time to set, in format "YYYY-MM-ddTHH:mm:ss"

### command = "ReadISFProfile"

> (No parameters)

### command = "ReadBolusWizardCarbProfile"

> (No parameters)

### command = "ReadDIASetting"

> (No parameters)

### command = "ReadBatteryLevel"

> (No parameters)

### command = "ReadReservoirLevel"

> (No parameters)

### command = "SetPumpID"

(String) "pumpID"

> Six digit pump ID string, to inform RoundtripService of the ID of the pump to use.

### command = "UseThisRileylink"

(String) "rlAddress"

> The MAC address of the Rileylink to use, e.g. "00:07:80:2D:9E:F4"

### command = "RetrieveHistoryPage"

(int) "pageNumber"

> Which history page (0-15) to retrieve.

## ServiceResult Bundle ("com.gxwtech.roundtrip2.IPC.messageKey" = "com.gxwtech.roundtrip2.IPC.MSG_ServiceResult")

> This is indicates a ServiceResult Bundle: a final response to a ServiceCommand.

(String) "commandID"

> This will have the same value as the original command.

(Bundle) "command"

> This is the original command bundle.

(Bundle) "response"

> This is the Response Bundle.  Whereas the ServiceResult Bundle contains routing information (which command, the command ID, the bundle type), the Response Bundle contains information about the actual results of the command.

## Response Bundle

(String) "ServiceMessageType"

> The type of service message, from the ServiceData package.  For a ServiceResult, this value will be "ServiceResult", or more likely, a subclass of ServiceResult indicating other data are present.

(String) "result"

> Either "OK" or "error", indicating the overall result of the command.

(int) "errorCode"

> An error code to help in automatically detecting/handling the errors on the client side.

(String) "errorDescription"

> A text description of the error.

## ReadPumpClockResult ("ServiceMessageType" = "ReadPumpClockResult")

This subclass of result will have an extra field:

(String) "PumpTime"

> The pump's local time (has no time zone).





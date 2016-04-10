package com.gxwtech.roundtrip2;

/**
 * Created by Geoff on 4/28/15.
 */
public enum MedtronicCommandEnum {
    CMD_M_INVALID_CMD((byte) 255),  // ggw: I just made this one up...
    CMD_M_READ_PUMP_STATUS((byte) 206), // 0xCE
    CMD_M_BOLUS((byte) 66), // 0x42
    //
    CMD_M_READ_INSULIN_REMAINING((byte) 115), //0x72
    CMD_M_READ_BATTERY_VOLTAGE((byte) 114), // 0x71 (New!)
    CMD_M_READ_HISTORY((byte) 128), // 0x80
    CMD_M_POWER_CTRL((byte) 93), // 0x5D
    CMD_M_BEGIN_PARAMETER_SETTING((byte) 38), // 0x26
    CMD_M_END_PARAMETER_SETTING((byte) 39), // 0x27
    CMD_M_TEMP_BASAL_RATE((byte) 76), // 0x4C
    CMD_M_READ_ERROR_STATUS((byte) 117), // 0x75
    CMD_M_READ_FIRMWARE_VER((byte) 116), // 0x74
    CMD_M_READ_PUMP_ID((byte) 113), // 0x71
    CMD_M_READ_PUMP_STATE((byte) 131), // 0x83
    CMD_M_READ_REMOTE_CTRL_IDS((byte) 118), // 0x76
    CMD_M_READ_TEMP_BASAL((byte) 152), // 0x98
    // FIXME: BUG! duplicate: CMD_M_READ_RTC((byte) 112), // 0x70
    CMD_M_SET_RF_REMOTE_ID((byte) 81), // 0x51
    CMD_M_SET_ALERT_TYPE((byte) 84), // 0x54
    CMD_M_SET_AUTO_OFF((byte) 78), // 0x4E
    CMD_M_SET_BLOCK_ENABLE((byte) 82), // 0x52
    CMD_M_SET_CURRENT_PATTERN((byte) 74), // 0x4A
    CMD_M_SET_EASY_BOLUS_ENABLE((byte) 79), // 0x4F
    CMD_M_SET_MAX_BOLUS((byte) 65), // 0x41
    CMD_M_SET_PATTERNS_ENABLE((byte) 85), // 0x55
    CMD_M_SET_RF_ENABLE((byte) 87), // 0x57
    CMD_M_SET_RTC((byte) 64), // 0x40
    CMD_M_KEYPAD_PUSH((byte) 91), // 0x5B
    CMD_M_SET_TIME_FORMAT((byte) 92), // 0x5C
    CMD_M_SET_VAR_BOLUS_ENABLE((byte) 69), // 0x45
    CMD_M_READ_STD_PROFILES((byte) 146), // 0x92
    CMD_M_READ_A_PROFILES((byte) 147), // 0x93
    CMD_M_READ_B_PROFILES((byte) 148), // 0x94
    CMD_M_READ_SETTINGS((byte) 145), // 0x91
    CMD_M_SET_STD_PROFILE((byte) 111), // 0x6F
    CMD_M_SET_A_PROFILE((byte) 48), // 0x30
    CMD_M_SET_B_PROFILE((byte) 49), // 0x31
    // FIXME: BUG! duplicate: CMD_M_SET_MAX_BASAL((byte) 112), // 0x70
    CMD_M_READ_BG_ALARM_CLOCKS((byte) 142), // 0x8E
    CMD_M_READ_BG_ALARM_ENABLE((byte) 151), // 0x97
    CMD_M_READ_BG_REMINDER_ENABLE((byte) 144), // 0x90
    CMD_M_READ_BG_TARGETS((byte) 140), // 0x8C
    CMD_M_READ_BG_UNITS((byte) 137), // 0x89
    CMD_M_READ_BOLUS_WIZARD_SETUP_STATUS((byte) 135), // 0x87
    CMD_M_READ_CARB_RATIOS((byte) 138), // 0x8A
    CMD_M_READ_CARB_UNITS((byte) 136), // 0x88
    CMD_M_READ_LOGIC_LINK_IDS((byte) 149), // 0x95
    CMD_M_READ_INSULIN_SENSITIVITIES((byte) 139), // 0x8B
    CMD_M_READ_RESERVOIR_WARNING((byte) 143), // 0x8F
    CMD_M_READ_PUMP_MODEL_NUMBER((byte) 141), // 0x8D
    CMD_M_SET_BG_ALARM_CLOCKS((byte) 107), // 0x6B
    CMD_M_SET_BG_ALARM_ENABLE((byte) 103), // 0x67
    CMD_M_SET_BG_REMINDER_ENABLE((byte) 108), // 0x6C
    CMD_M_SET_BOLUS_WIZARD_SETUP((byte) 94), // 0x5E
    CMD_M_SET_INSULIN_ACTION_TYPE((byte) 88), // 0x58
    CMD_M_SET_LOGIC_LINK_ENABLE((byte) 51), // 0x33
    CMD_M_SET_LOGIC_LINK_ID((byte) 50), // 0x32
    CMD_M_SET_RESERVOIR_WARNING((byte) 106), // 0x6A
    CMD_M_SET_TEMP_BASAL_TYPE((byte) 104), // 0x68
    CMD_M_SUSPEND_RESUME((byte) 77), // 0x4D
    CMD_M_PACKET_LENGTH((byte) 7), // 0x07
    CMD_M_READ_PUMP_SETTINGS((byte) 192); // 0xC0

    byte opcode;

    MedtronicCommandEnum(byte b) {
        opcode = b;
    }
    byte toByte() {
        return opcode;
    }

    public String toString() {
        return desc(this.opcode);
    }
/*
    public static String getDescription(MedtronicCommandEnum opcode) {
        switch(opcode) {
            case CMD_M_INVALID_CMD:
                return "CMD_M_INVALID_CMD";
            case CMD_M_READ_PUMP_STATUS:
                return "CMD_M_READ_PUMP_STATUS";
            case CMD_M_BOLUS:
                return "CMD_M_BOLUS";
            case CMD_M_READ_INSULIN_REMAINING:
                return "CMD_M_READ_INSULIN_REMAINING";
            case CMD_M_READ_BATTERY_VOLTAGE:
                return "CMD_M_READ_BATTERY_VOLTAGE";
            case CMD_M_READ_HISTORY:
                return "CMD_M_READ_HISTORY";
            case CMD_M_POWER_CTRL:
                return "CMD_M_POWER_CTRL";
            case CMD_M_BEGIN_PARAMETER_SETTING:
                return "CMD_M_BEGIN_PARAMETER_SETTING";
            case CMD_M_END_PARAMETER_SETTING:
                return "CMD_M_END_PARAMETER_SETTING";
            case CMD_M_TEMP_BASAL_RATE:
                return "CMD_M_TEMP_BASAL_RATE";
            case CMD_M_READ_ERROR_STATUS:
                return "CMD_M_READ_ERROR_STATUS";
            case CMD_M_READ_FIRMWARE_VER:
                return "CMD_M_READ_FIRMWARE_VER";
            case CMD_M_READ_PUMP_ID:
                return "CMD_M_READ_PUMP_ID";
            case CMD_M_READ_PUMP_STATE:
                return "CMD_M_READ_PUMP_STATE";
            case CMD_M_READ_REMOTE_CTRL_IDS:
                return "CMD_M_READ_REMOTE_CTRL_IDS";
            case CMD_M_READ_TEMP_BASAL:
                return "CMD_M_READ_TEMP_BASAL";
            case CMD_M_READ_RTC:
                return "CMD_M_READ_RTC";
            case CMD_M_SET_RF_REMOTE_ID:
                return "CMD_M_SET_RF_REMOTE_ID";
            case CMD_M_SET_ALERT_TYPE:
                return "CMD_M_SET_ALERT_TYPE";
            case CMD_M_SET_AUTO_OFF:
                return "CMD_M_SET_AUTO_OFF";
            case CMD_M_SET_BLOCK_ENABLE:
                return "CMD_M_SET_BLOCK_ENABLE";
            case CMD_M_SET_CURRENT_PATTERN:
                return "CMD_M_SET_CURRENT_PATTERN";
            case CMD_M_SET_EASY_BOLUS_ENABLE:
                return "CMD_M_SET_EASY_BOLUS_ENABLE";
            case CMD_M_SET_MAX_BOLUS:
                return "CMD_M_SET_MAX_BOLUS";
            case CMD_M_SET_PATTERNS_ENABLE:
                return "CMD_M_SET_PATTERNS_ENABLE";
            case CMD_M_SET_RF_ENABLE:
                return "CMD_M_SET_RF_ENABLE";
            case CMD_M_SET_RTC:
                return "CMD_M_SET_RTC";
            case CMD_M_KEYPAD_PUSH:
                return "CMD_M_KEYPAD_PUSH";
            case CMD_M_SET_TIME_FORMAT:
                return "CMD_M_SET_TIME_FORMAT";
            case CMD_M_SET_VAR_BOLUS_ENABLE:
                return "CMD_M_SET_VAR_BOLUS_ENABLE";
            case CMD_M_READ_STD_PROFILES:
                return "CMD_M_READ_STD_PROFILES";
            case CMD_M_READ_A_PROFILES:
                return "CMD_M_READ_A_PROFILES";
            case CMD_M_READ_B_PROFILES:
                return "CMD_M_READ_B_PROFILES";
            case CMD_M_READ_SETTINGS:
                return "CMD_M_READ_SETTINGS";
            case CMD_M_SET_STD_PROFILE:
                return "CMD_M_SET_STD_PROFILE";
            case CMD_M_SET_A_PROFILE:
                return "CMD_M_SET_A_PROFILE";
            case CMD_M_SET_B_PROFILE:
                return "CMD_M_SET_B_PROFILE";
            case CMD_M_SET_MAX_BASAL:
                return "CMD_M_SET_MAX_BASAL";
            case CMD_M_READ_BG_ALARM_CLOCKS:
                return "CMD_M_READ_BG_ALARM_CLOCKS";
            case CMD_M_READ_BG_ALARM_ENABLE:
                return "CMD_M_READ_BG_ALARM_ENABLE";
            case CMD_M_READ_BG_REMINDER_ENABLE:
                return "CMD_M_READ_BG_REMINDER_ENABLE";
            case CMD_M_READ_BG_TARGETS:
                return "CMD_M_READ_BG_TARGETS";
            case CMD_M_READ_BG_UNITS:
                return "CMD_M_READ_BG_UNITS";
            case CMD_M_READ_BOLUS_WIZARD_SETUP_STATUS:
                return "CMD_M_READ_BOLUS_WIZARD_SETUP_STATUS";
            case CMD_M_READ_CARB_RATIOS:
                return "CMD_M_READ_CARB_RATIOS";
            case CMD_M_READ_CARB_UNITS:
                return "CMD_M_READ_CARB_UNITS";
            case CMD_M_READ_LOGIC_LINK_IDS:
                return "CMD_M_READ_LOGIC_LINK_IDS";
            case CMD_M_READ_INSULIN_SENSITIVITIES:
                return "CMD_M_READ_INSULIN_SENSITIVITIES";
            case CMD_M_READ_RESERVOIR_WARNING:
                return "CMD_M_READ_RESERVOIR_WARNING";
            case CMD_M_READ_PUMP_MODEL_NUMBER:
                return "CMD_M_READ_PUMP_MODEL_NUMBER";
            case CMD_M_SET_BG_ALARM_CLOCKS:
                return "CMD_M_SET_BG_ALARM_CLOCKS";
            case CMD_M_SET_BG_ALARM_ENABLE:
                return "CMD_M_SET_BG_ALARM_ENABLE";
            case CMD_M_SET_BG_REMINDER_ENABLE:
                return "CMD_M_SET_BG_REMINDER_ENABLE";
            case CMD_M_SET_BOLUS_WIZARD_SETUP:
                return "CMD_M_SET_BOLUS_WIZARD_SETUP";
            case CMD_M_SET_INSULIN_ACTION_TYPE:
                return "CMD_M_SET_INSULIN_ACTION_TYPE";
            case CMD_M_SET_LOGIC_LINK_ENABLE:
                return "CMD_M_SET_LOGIC_LINK_ENABLE";
            case CMD_M_SET_LOGIC_LINK_ID:
                return "CMD_M_SET_LOGIC_LINK_ID";
            case CMD_M_SET_RESERVOIR_WARNING:
                return "CMD_M_SET_RESERVOIR_WARNING";
            case CMD_M_SET_TEMP_BASAL_TYPE:
                return "CMD_M_SET_TEMP_BASAL_TYPE";
            case CMD_M_SUSPEND_RESUME:
                return "CMD_M_SUSPEND_RESUME";
            case CMD_M_PACKET_LENGTH:
                return "CMD_M_PACKET_LENGTH";
            case CMD_M_READ_PUMP_SETTINGS:
                return "CMD_M_READ_PUMP_SETTINGS";
            default:
                return "(Unknown Minimed command opcode)";
        }
    }
*/
    public static String desc(byte opcode) {
        switch (opcode) {
            case (byte) 255:
                return "CMD_M_INVALID_CMD";
            case (byte) 206:
                return "CMD_M_READ_PUMP_STATUS";
            case (byte) 66:
                return "CMD_M_BOLUS";
            case (byte) 115:
                return "CMD_M_READ_INSULIN_REMAINING";
            case (byte) 114:
                return "CMD_M_READ_BATTERY_VOLTAGE";
            case (byte) 128:
                return "CMD_M_READ_HISTORY";
            case (byte) 93:
                return "CMD_M_POWER_CTRL";
            case (byte) 38:
                return "CMD_M_BEGIN_PARAMETER_SETTING";
            case (byte) 39:
                return "CMD_M_END_PARAMETER_SETTING";
            case (byte) 76:
                return "CMD_M_TEMP_BASAL_RATE";
            case (byte) 117:
                return "CMD_M_READ_ERROR_STATUS";
            case (byte) 116:
                return "CMD_M_READ_FIRMWARE_VER";
            case (byte) 113:
                return "CMD_M_READ_PUMP_ID";
            case (byte) 131:
                return "CMD_M_READ_PUMP_STATE";
            case (byte) 118:
                return "CMD_M_READ_REMOTE_CTRL_IDS";
            case (byte) 152:
                return "CMD_M_READ_TEMP_BASAL";
            // FIXME: duplicate: case (byte)112:return "CMD_M_READ_RTC";
            case (byte) 81:
                return "CMD_M_SET_RF_REMOTE_ID";
            case (byte) 84:
                return "CMD_M_SET_ALERT_TYPE";
            case (byte) 78:
                return "CMD_M_SET_AUTO_OFF";
            case (byte) 82:
                return "CMD_M_SET_BLOCK_ENABLE";
            case (byte) 74:
                return "CMD_M_SET_CURRENT_PATTERN";
            case (byte) 79:
                return "CMD_M_SET_EASY_BOLUS_ENABLE";
            case (byte) 65:
                return "CMD_M_SET_MAX_BOLUS";
            case (byte) 85:
                return "CMD_M_SET_PATTERNS_ENABLE";
            case (byte) 87:
                return "CMD_M_SET_RF_ENABLE";
            case (byte) 64:
                return "CMD_M_SET_RTC";
            case (byte) 91:
                return "CMD_M_KEYPAD_PUSH";
            case (byte) 92:
                return "CMD_M_SET_TIME_FORMAT";
            case (byte) 69:
                return "CMD_M_SET_VAR_BOLUS_ENABLE";
            case (byte) 146:
                return "CMD_M_READ_STD_PROFILES";
            case (byte) 147:
                return "CMD_M_READ_A_PROFILES";
            case (byte) 148:
                return "CMD_M_READ_B_PROFILES";
            case (byte) 145:
                return "CMD_M_READ_SETTINGS";
            case (byte) 111:
                return "CMD_M_SET_STD_PROFILE";
            case (byte) 48:
                return "CMD_M_SET_A_PROFILE";
            case (byte) 49:
                return "CMD_M_SET_B_PROFILE";
            // FIXME: duplicate: case (byte)112:return "CMD_M_SET_MAX_BASAL";
            case (byte) 142:
                return "CMD_M_READ_BG_ALARM_CLOCKS";
            case (byte) 151:
                return "CMD_M_READ_BG_ALARM_ENABLE";
            case (byte) 144:
                return "CMD_M_READ_BG_REMINDER_ENABLE";
            case (byte) 140:
                return "CMD_M_READ_BG_TARGETS";
            case (byte) 137:
                return "CMD_M_READ_BG_UNITS";
            case (byte) 135:
                return "CMD_M_READ_BOLUS_WIZARD_SETUP_STATUS";
            case (byte) 138:
                return "CMD_M_READ_CARB_RATIOS";
            case (byte) 136:
                return "CMD_M_READ_CARB_UNITS";
            case (byte) 149:
                return "CMD_M_READ_LOGIC_LINK_IDS";
            case (byte) 139:
                return "CMD_M_READ_INSULIN_SENSITIVITIES";
            case (byte) 143:
                return "CMD_M_READ_RESERVOIR_WARNING";
            case (byte) 141:
                return "CMD_M_READ_PUMP_MODEL_NUMBER";
            case (byte) 107:
                return "CMD_M_SET_BG_ALARM_CLOCKS";
            case (byte) 103:
                return "CMD_M_SET_BG_ALARM_ENABLE";
            case (byte) 108:
                return "CMD_M_SET_BG_REMINDER_ENABLE";
            case (byte) 94:
                return "CMD_M_SET_BOLUS_WIZARD_SETUP";
            case (byte) 88:
                return "CMD_M_SET_INSULIN_ACTION_TYPE";
            case (byte) 51:
                return "CMD_M_SET_LOGIC_LINK_ENABLE";
            case (byte) 50:
                return "CMD_M_SET_LOGIC_LINK_ID";
            case (byte) 106:
                return "CMD_M_SET_RESERVOIR_WARNING";
            case (byte) 104:
                return "CMD_M_SET_TEMP_BASAL_TYPE";
            case (byte) 77:
                return "CMD_M_SUSPEND_RESUME";
            case (byte) 7:
                return "CMD_M_PACKET_LENGTH";
            case (byte) 192:
                return "CMD_M_READ_PUMP_SETTINGS";
            default:
                return "(Unknown Medtronic opcode)";
        }
    }
}

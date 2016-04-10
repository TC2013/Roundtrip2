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
    CMD_M_READ_RTC((byte) 112), // 0x70
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
    CMD_M_SET_MAX_BASAL((byte) 112), // 0x70
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
}

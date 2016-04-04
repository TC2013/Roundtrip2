package com.gxwtech.roundtrip2.rtmessage;

import android.os.Message;

/**
 * Created by geoff on 3/22/16.
 */


public class RTMessage {
    private static final String TAG="RTMessage";

    // PUBLIC message numbers
    public static final int MSG_ping = 1;
    public static final int MSG_startScan = 2;

    public static String msgName(int num) {
        switch(num) {
            case MSG_ping:
                return "MSG_ping";
            case MSG_startScan:
                return "MSG_startScan";
            default:
                return "(noname)";
        }
    }

    protected Message mMessage=null;
    public RTMessage() {
        mMessage = new Message();
    }
    public RTMessage(Message m) {
        mMessage = m;
    }
    public String getName() {
        return msgName(mMessage.what);
    }
    public void run() {
        // derived classes override this.
    }
    public static RTMessage constructFromMessage(Message m) {
        RTMessage rval;
        if (m == null) {
            return new RTMessage();
        }
        switch(m.what) {
            case MSG_ping:
                rval = new RTMessagePing(m);
                break;
            case MSG_startScan:
                rval = new RTMessageStartScan(m);
                break;
            default:
                rval = new RTMessage();
                break;
        }
        return rval;
    }
}

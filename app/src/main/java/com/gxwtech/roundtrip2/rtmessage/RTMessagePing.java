package com.gxwtech.roundtrip2.rtmessage;

import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by geoff on 3/22/16.
 */
public class RTMessagePing extends RTMessage {
    public RTMessagePing() {
        super();
    }
    public RTMessagePing(Message m) {
        super(m);
    }
    public void run() {
        /*
        Bundle bundle = mMessage.getData();
        String hello = (String)bundle.get("key_hello");
        Toast.makeText(mContext,hello, Toast.LENGTH_SHORT).show();
        */
    }
}

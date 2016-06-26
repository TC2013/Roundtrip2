package com.gxwtech.roundtrip2.RoundtripService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gxwtech.roundtrip2.RT2Const;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by geoff on 6/11/16.
 */
public class RoundtripServiceIPCConnection {
    private static final String TAG = "RTServiceIPC";
    private Context context;
    //private ArrayList<Messenger> mClients = new ArrayList<>();
    private HashMap<Integer,Messenger> mClients = new HashMap<>();

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    public RoundtripServiceIPCConnection(Context context) {
        this.context = context;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Received message " + msg);
            Bundle bundle = msg.getData();
            switch(msg.what) {
                // This just helps sub-divide the message processing
                case RT2Const.IPC.MSG_registerClient:
                    // send a reply, to let them know we're listening.
                    Message myReply = Message.obtain(null, RT2Const.IPC.MSG_clientRegistered,0,0);
                    try {
                        msg.replyTo.send(myReply);
                        mClients.put(mClients.hashCode(),msg.replyTo);
                        Log.d(TAG,"handleMessage: Registered client");
                    } catch (RemoteException e) {
                        // I guess they aren't registered after all...
                        Log.e(TAG,"handleMessage: failed to send acknowledgement of registration");
                    }

                    break;
                case RT2Const.IPC.MSG_unregisterClient:
                    mClients.remove(msg.replyTo.hashCode());
                case RT2Const.IPC.MSG_IPC:
                    // As the current thread is likely a GUI thread from some app,
                    // rebroadcast the message as a local item.
                    // Convert from Message to Intent
                    if (msg.replyTo != null) {
                        Log.d(TAG, "Received IPC message from client " + msg.replyTo.toString());
                        bundle.putInt(RT2Const.serviceLocal.IPCReplyTo_hashCodeKey, msg.replyTo.hashCode());
                        Intent intent = new Intent(bundle.getString(RT2Const.IPC.messageKey));
                        intent.putExtra(RT2Const.IPC.bundleKey, bundle);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                    break;
                /*
                case RT2Const.MSG_ping:
                    String hello = (String)bundle.get("key_hello");
                    Toast.makeText(mContext,hello, Toast.LENGTH_SHORT).show();
                    break;
                    */
                default:
                    Log.e(TAG,"handleMessage: unknown 'what' in message: "+msg.what);
                    super.handleMessage(msg);
            }
        }
    }

    public IBinder doOnBind(Intent intent) {
        Log.d(TAG, "onBind");
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RT2Const.serviceLocal.ipcBound));
        return mMessenger.getBinder();
    }

    // if clientHashcode is null, broadcast to all clients
    public boolean sendMessage(Message msg, Integer clientHashcode) {
        Messenger clientMessenger = null;

        if (mClients.isEmpty()) {
            if (msg.what == RT2Const.IPC.MSG_IPC) {
                Log.e(TAG, "sendMessage: no clients, cannot send: " + msg.getData().getString(RT2Const.IPC.messageKey, "(unknown)"));
            } else {
                Log.e(TAG, "sendMessage: no clients, cannot send: what="+msg.what);
            }
        } else {
            if (clientHashcode != null) {
                clientMessenger = mClients.get(clientHashcode);
            }
            try {
                if (clientMessenger != null) {
                    // sending to just one client
                    clientMessenger.send(msg);
                } else {
                    // send to all clients
                    for (Integer clientHash : mClients.keySet()) {
                        mClients.get(clientHash).send(msg);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void sendMessage(String messageType) {
        Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC,0,0);
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString(RT2Const.IPC.messageKey, messageType);

        // Set payload
        msg.setData(bundle);
        sendMessage(msg, null);
        Log.d(TAG,"sendMessage: sent "+messageType);
    }

    public void sendMessageBundle(Bundle messageBundle, Integer clientHashcode) {
        Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC,0,0);
        // Set payload
        msg.setData(messageBundle);
        sendMessage(msg, clientHashcode);
    }
}

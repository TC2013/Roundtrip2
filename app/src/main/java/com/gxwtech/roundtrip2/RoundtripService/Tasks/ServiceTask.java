package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class ServiceTask implements Runnable {
    private static final String TAG = "ServiceTask(base)";
    protected ServiceTransport mTransport;
    public ServiceTask() {
        init(new ServiceTransport());
    }
    public ServiceTask(ServiceTransport transport) {
        init(transport);
    }

    public void init(ServiceTransport transport) {
        mTransport = transport;
    }

    @Override
    public void run() {
    }

    public ServiceTransport getServiceTransport() {
        return mTransport;
    }

    protected void sendResponse(ServiceResult result) {
        RoundtripService.getInstance().sendServiceTransportResponse(mTransport,result);
    }
}


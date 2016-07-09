package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class ServiceTaskRunnable implements Runnable {
    private static final String TAG = "ServiceTaskRunnable(base)";
    protected ServiceTransport mTransport;
    public ServiceTaskRunnable() {
        init(new ServiceTransport());
    }
    public ServiceTaskRunnable(ServiceTransport transport) {
        init(transport);
    }

    public void init(ServiceTransport transport) {
        mTransport = transport;
    }

    @Override
    public void run() {
    }
}


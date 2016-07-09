package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.os.AsyncTask;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class ServiceTaskAsync extends AsyncTask<ServiceTransport,Void,ServiceResult> {
    private static final String TAG = "ServiceTaskRunnable(base)";
    protected ServiceTransport mTransport;
    public ServiceTaskAsync() {
        init(new ServiceTransport());
    }
    public ServiceTaskAsync(ServiceTransport transport) {
        init(transport);
    }

    public void init(ServiceTransport transport) {
        mTransport = transport;
    }


    @Override
    protected ServiceResult doInBackground(ServiceTransport... serviceTransports) {
        mTransport = serviceTransports[0];
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setResultError(-1,"Base class doInBackground called");
        return serviceResult;
    }

    @Override
    protected void onPostExecute(ServiceResult result) {
        RoundtripService.getInstance().sendServiceTransportResponse(mTransport,result);
    }
}

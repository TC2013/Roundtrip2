package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.ServiceData.RetrieveHistoryPageResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class RetrieveHistoryPageTaskAsync extends ServiceTaskAsync {
    public RetrieveHistoryPageTaskAsync() { }
    public RetrieveHistoryPageTaskAsync(ServiceTransport transport) {
        super(transport);
    }

    @Override
    protected ServiceResult doInBackground(ServiceTransport... serviceTransports) {
        mTransport = serviceTransports[0];
        int pageNumber = mTransport.getServiceCommand().getMap().getInt("pageNumber");
        Page page = RoundtripService.getInstance().pumpManager.getPumpHistoryPage(pageNumber);
        publishProgress();
        RetrieveHistoryPageResult result = new RetrieveHistoryPageResult();
        result.setResultOK();
        result.setPageBundle(page.pack());
        return result;
    }


}

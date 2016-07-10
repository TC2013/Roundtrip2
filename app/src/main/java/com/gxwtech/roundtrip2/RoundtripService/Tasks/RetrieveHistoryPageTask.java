package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.ServiceData.RetrieveHistoryPageResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class RetrieveHistoryPageTask extends ServiceTask {
    public RetrieveHistoryPageTask() { }
    public RetrieveHistoryPageTask(ServiceTransport transport) {
        super(transport);
    }

    @Override
    public void run() {
        int pageNumber = mTransport.getServiceCommand().getMap().getInt("pageNumber");
        Page page = RoundtripService.getInstance().pumpManager.getPumpHistoryPage(pageNumber);
        RetrieveHistoryPageResult result = new RetrieveHistoryPageResult();
        result.setResultOK();
        result.setPageBundle(page.pack());
        getServiceTransport().setServiceResult(result);
    }

}

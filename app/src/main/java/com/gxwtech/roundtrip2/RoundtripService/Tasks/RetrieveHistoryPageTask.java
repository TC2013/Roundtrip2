package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.ServiceData.RetrieveHistoryPageResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 */
public class RetrieveHistoryPageTask extends PumpTask {
    public RetrieveHistoryPageTask() { }
    public RetrieveHistoryPageTask(ServiceTransport transport) {
        super(transport);
    }

    @Override
    public void preOp() {
        // This is to avoid allocating any memory from async thread, though I'm not sure it's necessary.
        RetrieveHistoryPageResult result = new RetrieveHistoryPageResult();
        getServiceTransport().setServiceResult(result);
    }

    @Override
    public void run() {
        int pageNumber = mTransport.getServiceCommand().getMap().getInt("pageNumber");
        Page page = RoundtripService.getInstance().pumpManager.getPumpHistoryPage(pageNumber);
        RetrieveHistoryPageResult result = (RetrieveHistoryPageResult) getServiceTransport().getServiceResult();
        result.setResultOK();
        result.setPageBundle(page.pack());
    }

}

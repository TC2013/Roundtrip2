package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.ServiceData.RetrieveHistoryPageResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

import java.util.ArrayList;

/**
 * Created by geoff on 7/16/16.
 */
public class FetchPumpHistoryTask extends PumpTask {
    public FetchPumpHistoryTask() { }
    public FetchPumpHistoryTask(ServiceTransport transport) {
        super(transport);
    }

    @Override
    public void run() {
        ArrayList<Page> ra = new ArrayList<>();
        for (int i=0; i<16; i++) {
            Page page = RoundtripService.getInstance().pumpManager.getPumpHistoryPage(i);
            if (page != null) {
                ra.add(page);
            }
        }
        /*
        RetrieveHistoryPageResult result = (RetrieveHistoryPageResult) getServiceTransport().getServiceResult();
        result.setResultOK();
        result.setPageBundle(page.pack());
        */
    }


}

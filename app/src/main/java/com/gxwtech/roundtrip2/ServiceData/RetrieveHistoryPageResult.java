package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

/**
 * Created by geoff on 7/2/16.
 */
public class RetrieveHistoryPageResult extends ServiceResult {
    public RetrieveHistoryPageResult() {}
    public void setPageBundle(Bundle pageBundle) {
        map.putBundle("pageBundle",pageBundle);
    }
    public Bundle getPageBundle() {
        return map.getBundle("pageBundle");
    }
}

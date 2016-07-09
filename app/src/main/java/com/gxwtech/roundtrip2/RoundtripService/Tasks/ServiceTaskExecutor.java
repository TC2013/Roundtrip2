package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by geoff on 7/9/16.
 */
public class ServiceTaskExecutor extends ThreadPoolExecutor {
    private static final String TAG = "ServiceTaskExecutor";
    private static ServiceTaskExecutor instance;
    private static LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    static {
        instance = new ServiceTaskExecutor();
    }
    private ServiceTaskExecutor() {
        super(1,1,10000, TimeUnit.MILLISECONDS,taskQueue);
    }
    protected void beforeExecute(Thread t, Runnable r) {
        ServiceTask task = (ServiceTask) r;
        RoundtripService.getInstance().setCurrentTask(task);
    }
    public static ServiceTask startTask(ServiceTask task) {
        instance.execute(task);
        return task;
    }
    protected void afterExecute(Runnable r, Throwable t) {
        ServiceTask task = (ServiceTask) r;
        RoundtripService.getInstance().finishCurrentTask(task);
    }
}

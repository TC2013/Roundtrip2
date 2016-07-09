package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by geoff on 7/9/16.
 */
public class ServiceTaskExecutor extends ThreadPoolExecutor {
    private static ServiceTaskExecutor instance;
    private static LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    static {
        instance = new ServiceTaskExecutor();
    }
    private ServiceTaskExecutor() {
        super(1,1,10000, TimeUnit.MILLISECONDS,taskQueue);
    }
    public static ServiceTaskRunnable startTask(ServiceTaskRunnable task) {
        instance.execute(task);
        return task;
    }
}

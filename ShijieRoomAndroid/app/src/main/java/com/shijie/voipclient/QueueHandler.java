package com.shijie.voipclient;

/**
 * Created by xm on 16-3-30.
 */
import android.os.Handler;
import android.os.HandlerThread;

public class QueueHandler {

    private static final String TAG = "QueueHandler";
    private static final String HANDLE_ID = "com.shijie.voipclient";
    private static QueueHandler instance;
    private HandlerThread workThread;
    private Handler workHandler;

    public static QueueHandler getInstance() {
        if (instance == null) {
            synchronized (QueueHandler.class) {
                instance = new QueueHandler();
            }
        }
        return instance;
    }

    public QueueHandler() {
        workThread = new HandlerThread(HANDLE_ID);
        workThread.start();
    }

    public void post(Runnable run) {
        if (workHandler == null) {
            workHandler = new Handler(workThread.getLooper());
        }
        workHandler.post(run);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        workThread.getLooper().quit();
        workThread.interrupt();
    }

}
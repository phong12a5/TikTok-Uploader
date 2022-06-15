package pdt.autoreg.app;
import pdt.autoreg.accessibility.LOG;

public class WorkerThread extends Thread {
    private static String TAG = "WorkerThread";
    private boolean isRunning = false;

    public WorkerThread() {
        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.saveUncaughtExceptionLog(TAG,t,e);
            }
        });

        LOG.D(TAG,"Created WorkerThread");
    }

    public void startWorker() {
        isRunning = true;
        this.start();
    }

    public void stopWorker() {
        isRunning = false;
    }

    protected boolean isRunning() {
        return isRunning;
    }
}

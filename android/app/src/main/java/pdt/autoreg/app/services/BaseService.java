package pdt.autoreg.app.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.accessibility.screendefinitions.ScreenInfo;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.AppModel;
import pdt.autoreg.app.BuildConfig;
import pdt.autoreg.app.WorkerThread;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.devicefaker.helper.RootHelper;

public abstract class BaseService extends Service {
    private static final String TAG = "BaseService";
    private static final int RATIO = 100;
    private int m_time_to_update = 10;
    private int m_time_to_gc = 0;
    protected int heightOfScreen = 0;
    protected List<String> m_screenStack = new ArrayList<String>();

    /* --------------------------------------- Abstract methods --------------------------------------- */
    protected abstract void mainOperations();

    @Override
    public void onCreate() {
        super.onCreate();
        m_workerThread.startWorker();
        LOG.D(TAG, "Created " + this.getClass().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppModel.instance().setServiceStarted(false);
        m_workerThread.stopWorker();
        m_workerThread = null;
        LOG.D(TAG,"onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.D(TAG,"onStartCommand");
        AppModel.instance().setServiceStarted(true);
        return START_STICKY;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LOG.E(TAG,"onTrimMemory: " + level);
    }

    private WorkerThread m_workerThread = new WorkerThread() {
        public void run() {

            initEnv(BaseService.this);

            while (isRunning()) {
                try {
                    LOG.D(TAG, "*********************************** New cycle *********************************** ");

                    if(Utils.isScreenOff(BaseService.this)) {
                        RootHelper.execute("input keyevent KEYCODE_POWER");
                    } else if(Utils.isScreenLocked(BaseService.this)) {
                        RootHelper.execute("input keyevent 82");
                    }

                    if (m_time_to_update >= 20) {
                        updateKeywordDefinitions();
                        checkAndUpdateNewVerion();
                        m_time_to_update = 0;
                    } else {
                        m_time_to_update++;
                        mainOperations();
                    }
                } catch (Exception e) {
                    LOG.printStackTrace(TAG, e);
                }
                Utils.delay(RATIO);
            }
            LOG.D(TAG, "*********************************** End cycle *********************************** ");
        }
    };

    private void updateKeywordDefinitions() {
        String content = "";
        try {
            InputStream stream = getAssets().open("definitions.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            content = new String(buffer);
            ASInterface.instance().updateKeywordDefinitions(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void initEnv(Context context) {
        LOG.D(TAG, "initEnv");
        // change screen resolution
        if(BuildConfig.DEBUG)
            RootHelper.execute("settings put system screen_brightness 40");
        else
            RootHelper.execute("settings put system screen_brightness 0");

        // change screen timeout
        RootHelper.execute("wm density 320");
        RootHelper.execute("wm size 720x1280");

        // disable screen lock
        RootHelper.execute("svc power stayon true");
    }

    public boolean detectScreen(boolean save2Stack) {
        try {
            ScreenInfo screenInfo = ASInterface.instance().detectScreen(AppDefines.TIKTOK_APPNAME);
            if (screenInfo != null) {
                AppModel.instance().setCurrScrID(screenInfo.detected_screen_id);
                AppModel.instance().setCurrtScrInfo(screenInfo.nodes_in_screen);
                LOG.D(TAG, "screenInfo.detected_screen_id: " + screenInfo.detected_screen_id);
                if (save2Stack) {
                    m_screenStack.add(AppModel.instance().currScrID());
                }
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
        /* ------------- END Detect screen ------------- */
    }

    private void checkAndUpdateNewVerion() {
        /*
        try {
            PackageInfo pInfo = App.getContext().getPackageManager().getPackageInfo(BaseService.this.getPackageName(), 0);
            int versionCode = pInfo.versionCode;
            int new_version_code = versionCode + 1;
            if (new_version_code > versionCode) {
                String apkFileName = BaseService.this.getPackageName() + "." + new_version_code + ".apk";
                if(DropboxAPI.downloadFileFromDropbox("/apk/" + apkFileName, AppDefines.AUTOREG_FOLDER + apkFileName)) {
                    RootHelper.execute("cp " + AppDefines.AUTOREG_FOLDER + apkFileName + " /data/local");
                    RootHelper.execute("pm install -r /data/local/" + apkFileName);
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "submitClone error: " + e);
        }
        */
    }
}

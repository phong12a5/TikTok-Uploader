package pdt.autoreg.app.services;

import static pdt.autoreg.devicefaker.helper.FileHelper.readFile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.accessibility.screendefinitions.ScreenInfo;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.model.AppModel;
import pdt.autoreg.app.BuildConfig;
import pdt.autoreg.app.WorkerThread;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.app.model.PackageInfo;
import pdt.autoreg.devicefaker.Constants;
import pdt.autoreg.devicefaker.helper.RootHelper;

public abstract class BaseService extends Service {
    private static final String TAG = "BaseService";
    private static final int RATIO = 2000;
    private int m_time_to_update = 20;
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
                        if(AppModel.instance().currPackage() == null) {
                            changePackage();
                        } else if(!AppModel.instance().currPackage().getPackageName().equals(ASInterface.instance().getCurrentForgroundPkg())) {
                            ASInterface.instance().openPackage(AppModel.instance().currPackage().getPackageName());
                        } else {
                            // detect screen
                            detectScreen(true);

                            // check screen lopp
                            String mostAppearsElement = null;
                            int maxAppearCount = 0;
                            for (int i = 0; i < m_screenStack.size(); i++) {
                                String item = m_screenStack.get(i);
                                int occurrences = Collections.frequency(m_screenStack, m_screenStack.get(i));
                                if (occurrences > maxAppearCount) {
                                    mostAppearsElement = item;
                                    maxAppearCount = occurrences;
                                }
                            }

                            int threshold = AppDefines.SCREEN_STACK_SIZE / 2;
                            if (maxAppearCount >= threshold) {
                                LOG.E(TAG, "Loop issue: " + mostAppearsElement + " appears: " + maxAppearCount);
                                changePackage();
                            } else {
                                mainOperations();
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.printStackTrace(TAG, e);
                }
                Utils.delay(RATIO);
            }
            LOG.D(TAG, "*********************************** End cycle *********************************** ");
        }
    };

    protected void changePackage() {
        LOG.I(TAG, "changePackage");
        int nextPkgId = (AppModel.instance().currPackage() == null || (AppModel.instance().currPackage().getPackageId() + 1) >= AppDefines.MAX_PACKAGE_NUM)? 0 : AppModel.instance().currPackage().getPackageId() + 1;

        if(AppModel.instance().currPackage() != null) {
            RootHelper.closePackage(AppModel.instance().currPackage().getPackageName());
            if(!changeIp()) {
                LOG.E(TAG, "change ip failed!");
                return;
            }
        }

        AppModel.instance().setCurrPackage(new PackageInfo(nextPkgId, Constants.REG_PACKAGE));

        // restored package from backup
        restoredPackage();

        // clear screen stack
        if (m_screenStack != null && !m_screenStack.isEmpty()) {
            m_screenStack.clear();
        }

        //open package
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(AppModel.instance().currPackage().getPackageName());
            launchIntent.setFlags(launchIntent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_FROM_BACKGROUND);
            Utils.showToastMessage(this, "Start " + AppModel.instance().currPackage().getPackageName() + ": " + AppModel.instance().currPackage().getPackageId() );
            startActivity(launchIntent);
            Utils.delay(5000);
        } catch (Exception e) {
            Utils.showToastMessage(this, "Start " + AppModel.instance().currPackage().getPackageName() + " failed!");
        }
    }

    private void restoredPackage() {
        if(AppModel.instance().currPackage().getCloneInfo() == null) {
            RootHelper.clearPackage(AppModel.instance().currPackage().getPackageName());

            // generate new device info;
            String definitions = "";
            try {
                InputStream stream = getAssets().open("devices.json");
                int size = stream.available();
                byte[] buffer = new byte[size];
                stream.read(buffer);
                stream.close();
                JSONArray devices = new JSONArray(new String(buffer));
                if(devices.length() <= 0) {
                    Utils.showToastMessage(this, "No device info model");
                    return;
                } else {
                    JSONObject deviceObject = devices.getJSONObject(new Random().nextInt(devices.length()));
                    String target = "/data/data/" + AppModel.instance().currPackage().getPackageName() + "/device_info.json";
                    String tmp = "/sdcard/device_info.json";
                    FileUtils.writeStringToFile(new File(tmp), deviceObject.toString());
                    RootHelper.execute(String.format("cp %s %s", tmp, target));
                }
            } catch (Exception e) {
                LOG.printStackTrace(TAG, e);
            }
        } else {
            String username = AppModel.instance().currPackage().getCloneInfo().username();
            String packageName = AppModel.instance().currPackage().getPackageName();
            File backupData = new File(AppDefines.PDT_BACKUP_DATA_FOLDER, username);
            if(backupData.exists() && backupData.isDirectory()) {
                RootHelper.execute(String.format("cp -rp %s/* /data/data/%s/", backupData, packageName));
            }
        }
    }

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

    private boolean changeIp() {
        boolean success = false;
        switch (AppModel.instance().networkType()) {
            case AppDefines.MOBILE_NETWORK:
                RootHelper.enableAirplane();
                Utils.delay(1000);
                RootHelper.disableAirplane();
                Utils.delay(5000);
                success = true;
                break;
            case AppDefines.PROXY_NETWORK:
                return true;
            case AppDefines.SSHTUNNEL_NETWORK:
                break;
        }

        Utils.showToastMessage(this, "public ip: " + Utils.getPuclicIP());
        return success;
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

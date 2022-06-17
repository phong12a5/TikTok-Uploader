package pdt.autoreg.app.services;

import static pdt.autoreg.devicefaker.helper.FileHelper.readFile;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.WindowManager;


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
import pdt.autoreg.accessibility.screendefinitions.ScreenNode;
import pdt.autoreg.app.App;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.model.AppModel;
import pdt.autoreg.app.BuildConfig;
import pdt.autoreg.app.WorkerThread;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.app.model.PackageInfo;
import pdt.autoreg.devicefaker.Constants;
import pdt.autoreg.devicefaker.helper.FileHelper;
import pdt.autoreg.devicefaker.helper.RootHelper;

public abstract class BaseService extends Service {
    private static final String TAG = "BaseService";
    private static final int RATIO = 2000;
    private int m_time_to_update = 20;
    private int m_time_to_gc = 0;
    protected int widthOfScreen = 0;
    protected int heightOfScreen = 0;
    protected List<String> m_screenStack = new ArrayList<String>();

    /* --------------------------------------- Abstract methods --------------------------------------- */
    protected abstract void mainOperations();

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wmgr.getDefaultDisplay().getRealMetrics(metrics);
        widthOfScreen = metrics.widthPixels;
        heightOfScreen = metrics.heightPixels;

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
                Utils.delayRandom(RATIO - 1000, RATIO + 1000);
            }
            LOG.D(TAG, "*********************************** End cycle *********************************** ");
        }
    };

    protected void changePackage() {
        LOG.I(TAG, "changePackage");

        int nextPkgId = 0;
        int lastestPkgId = -1;

        if(AppModel.instance().currPackage() == null) {
            SharedPreferences prefs = getSharedPreferences(AppDefines.PDT_PREFS_NAME, MODE_PRIVATE);
            lastestPkgId = prefs.getInt("curr_package_id", -1);
        } else {
            lastestPkgId = AppModel.instance().currPackage().getPackageId();
        }

        if((lastestPkgId + 1)  >= AppDefines.MAX_PACKAGE_NUM)  nextPkgId = 0;
        else nextPkgId = lastestPkgId + 1;

        if(AppModel.instance().currPackage() != null) {
            RootHelper.closePackage(AppModel.instance().currPackage().getPackageName());
            if(!changeIp()) {
                LOG.E(TAG, "change ip failed!");
                return;
            }
        }

        AppModel.instance().setCurrPackage(new PackageInfo(nextPkgId, Constants.REG_PACKAGE));

        // restored package from backup
        while (!restoredPackage()) {
            Utils.showToastMessage(this, "Restored failed -> retry");
            Utils.delay(1000);
        }

        // clean all pictures and videos
        cleanMedia();

        // accept permissions
        RootHelper.acceptPermission("android.permission.WRITE_EXTERNAL_STORAGE", AppModel.instance().currPackage().getPackageName());
        RootHelper.acceptPermission("android.permission.READ_EXTERNAL_STORAGE", AppModel.instance().currPackage().getPackageName());
        RootHelper.acceptPermission("android.permission.READ_CONTACTS", AppModel.instance().currPackage().getPackageName());
        RootHelper.acceptPermission("android.permission.RECORD_AUDIO", AppModel.instance().currPackage().getPackageName());
        RootHelper.acceptPermission("android.permission.CAMERA", AppModel.instance().currPackage().getPackageName());

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

    protected boolean restoredPackage() {
        try {
            RootHelper.clearPackage(AppModel.instance().currPackage().getPackageName());
            if (AppModel.instance().currPackage().getCloneInfo() == null) {
                // generate new device info;
                generateNewDeviceInfo(AppModel.instance().currPackage().getPackageName());
            } else {
                String username = AppModel.instance().currPackage().getCloneInfo().username();
                String packageName = AppModel.instance().currPackage().getPackageName();
                String backupFolderPath = getBackupFolderPath(username);
                if (FileHelper.exist(backupFolderPath)) {
                    RootHelper.execute(String.format("cp -rp %s/* /data/data/%s/", backupFolderPath, packageName));
                    RootHelper.execute(String.format("chmod 777 -R /data/data/%s", packageName));
                    Utils.showToastMessage(this, "Restore succeed");
                } else {
                    generateNewDeviceInfo(AppModel.instance().currPackage().getPackageName());
                }
            }
            return true;
        } catch (Exception e) {
            LOG.E(TAG, e.getMessage());
            return false;
        }
    }

    protected boolean backupPackage() {
        try {
            String username = AppModel.instance().currPackage().getCloneInfo().username();
            String backupFolderPath = getBackupFolderPath(username);
            String packageName = AppModel.instance().currPackage().getPackageName();
            if (FileHelper.exist(backupFolderPath)) {
                LOG.I(TAG, "backup data existed already");
            } else {
                RootHelper.execute(String.format("cp -rp /data/data/%s %s", packageName, backupFolderPath));
                Utils.showToastMessage(this, "Backup succeed");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getBackupFolderPath(String username) {
        return AppDefines.BACKUP_DATA_FOLDER + username;
    }

    protected void generateNewDeviceInfo(String packageName) {
        LOG.D(TAG, "generateNewDeviceInfo");
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
                String target = "/data/data/" + packageName + "/device_info.json";
                String tmp = "/sdcard/device_info.json";
                FileUtils.writeStringToFile(new File(tmp), deviceObject.toString());
                RootHelper.execute(String.format("cp %s %s", tmp, target));
                RootHelper.execute("chmod 777 " + target);
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    protected void cleanMedia() {
        RootHelper.execute("rm -rf " + AppDefines.DCIM_FOLDER + "*");
        RootHelper.execute("rm -rf " + AppDefines.MOVIES_FOLDER + "*");
        RootHelper.execute("rm -rf " + AppDefines.PICTURES_FOLDER + "*");
        RootHelper.execute("rm " + AppDefines.PDT_FOLDER + "*.png");
        RootHelper.execute("rm " + AppDefines.PDT_FOLDER + "*.mp4");
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
                for(ScreenNode node : screenInfo.nodes_in_screen) LOG.I(TAG, node + "");
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

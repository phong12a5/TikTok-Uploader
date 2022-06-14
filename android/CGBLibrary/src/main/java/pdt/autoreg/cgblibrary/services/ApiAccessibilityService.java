package pdt.autoreg.cgblibrary.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import pdt.autoreg.cgblibrary.LOG;

public class ApiAccessibilityService extends AccessibilityService {
    private static final String TAG = "ACCSBLT";
    private static ApiAccessibilityService sInstance = null;
    private final HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private AssetManager m_assetManger;

    @Override
    public void onCreate() {
        Thread.currentThread().setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.saveUncaughtExceptionLog(TAG,t,e);
            }
        });
        super.onCreate();
        sInstance = this;
        ASBLBridgeService.setASBLInstance(sInstance);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        m_assetManger = getResources().getAssets();
        LOG.D(TAG,"onCreate");
    }

    static public void getInstance(ASBLBridgeService instance) {
        if(instance != null) {
            ASBLBridgeService.setASBLInstance(sInstance);
        }
    }

    static public boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + ApiAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            LOG.D(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            LOG.E(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LOG.D(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    LOG.D(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        LOG.D(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            LOG.D(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        LOG.D("TAG", "onInterrupt: " + "nduiisnaiansauisnauisna");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }


    @Override
    protected void onServiceConnected() {
        LOG.I(TAG, "onServiceConnected");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public Handler getHandler() {
        return (handler);
    }
}


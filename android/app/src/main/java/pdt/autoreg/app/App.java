package pdt.autoreg.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.chilkatsoft.CkGlobal;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.devicefaker.Constants;
import pdt.autoreg.devicefaker.helper.RootHelper;

public class App extends Application {
    private static String TAG = "AutofarmerApp";
    private static Application sApplication;
    private static VolumeObserver volumeObserver;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static Application getApplication() {
        return sApplication;
    }
    public static Context getContext() {
        return sApplication.getApplicationContext();
    }

    static {
        try {
            System.loadLibrary("chilkat");
        } catch (UnsatisfiedLinkError e) {
            LOG.E(TAG,"Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    @Override
    public void onCreate() {
        LOG.D(TAG,"AutoFarmerApp Created");
        super.onCreate();
        sApplication = this;
        unlockChilkat();
        initApplication(this);
        copyProxyTools(getContext());

        volumeObserver = new VolumeObserver(handler);
        getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                volumeObserver
        );
    }

    @Override
    public void onLowMemory() {
        LOG.E(TAG,  "------------------------> Low Memory <------------------------");
        super.onLowMemory();
        System.gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LOG.E(TAG,"onTrimMemory: " + level);
    }

    @Override
    public void onTerminate() {
        if (volumeObserver != null) getContentResolver().unregisterContentObserver(volumeObserver);
        super.onTerminate();
    }

    public static void initApplication(Context context) {
        LOG.D(TAG,"initApplicatione");

        if(!RootHelper.isRootAccess()) {
            Utils.showToastMessage(App.getContext(), "Root Access required.");
            System.exit(1);
        }

        ASInterface.instance().init(context);

        File fPdtFolder = new File(AppDefines.PDT_FOLDER);
        if (!fPdtFolder.exists()) {
            RootHelper.execute("mkdir " + AppDefines.PDT_FOLDER);
        }

        File fBackupDataFolder = new File(AppDefines.PDT_BACKUP_DATA_FOLDER);
        if (!fBackupDataFolder.exists()) {
            RootHelper.execute("mkdir " + AppDefines.PDT_BACKUP_DATA_FOLDER);
        }
    }

    private static void copyProxyTools(Context context) {
        LOG.D(TAG, "copyProxyTools");
        Utils.copyAssetFolder(context.getAssets(), "proxy/" + Build.CPU_ABI,context.getFilesDir().getAbsolutePath());
    }

    public static boolean unlockChilkat() {
        CkGlobal glob = new CkGlobal();
        boolean success = glob.UnlockBundle("VONGTH.CB4082020_9kru5rnD5R2h");
        if (success != true) {
            LOG.E(TAG,glob.lastErrorText());
            return false;
        }

        int status = glob.get_UnlockStatus();
        if (status == 2) {
            LOG.D(TAG,"Unlocked using purchased unlock code.");
            return true;
        }
        else {
            LOG.E(TAG,"Unlocked in trial mode.");
        }

        // The LastErrorText can be examined in the success case to see if it was unlocked in
        // trial more, or with a purchased unlock code.
        LOG.E(TAG,glob.lastErrorText());
        return false;
    }
}

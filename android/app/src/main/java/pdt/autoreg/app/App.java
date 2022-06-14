package pdt.autoreg.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.chilkatsoft.CkGlobal;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pdt.autoreg.cgblibrary.CGBInterface;
import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.devicefaker.Constants;

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

        CGBInterface.getInstance().init(context);

        /* ------------- tạo thư mục /sdcard/pdt.autoreg.app ------------------------------- */
        File afFolder = new File(AppDefines.AUTOREG_FOLDER);
        if (!afFolder.exists()) {
            if (afFolder.mkdir()) ; //directory is created;
        }

        File afDataFolder = new File(AppDefines.AUTOREG_DATA_FOLDER);
        if (!afDataFolder.exists()) {
            if (afDataFolder.mkdir()) ; //directory is created;
        }

        copyProxyTools(getContext());
    }

    private static void copyProxyTools(Context context) {
        LOG.D(TAG, "copyProxyTools");
        copyAssetFolder(context.getAssets(), "proxy/" + Build.CPU_ABI,context.getFilesDir().getAbsolutePath());
    }



    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                res &= copyAsset(assetManager,
                        fromAssetPath + "/" + file,
                        toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
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

package pdt.autoreg.cgblibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pdt.autoreg.cgblibrary.screendefinitions.ScreenInfo;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenNode;
import pdt.autoreg.cgblibrary.services.ASBLBridgeService;

public class CGBInterface {
    private static CGBInterface instance = null;
    private static String TAG = "CGBInterface";
    private Context context;

    public static final String ACTION_WAITING_RESULT = "ACTION_WAITING_RESULT";
    public static final String ACTION_CANCELED_TIMEOUT = "ACTION_CANCELED_TIMEOUT";
    public static final String ACTION_SUCCESS = "ACTION_SUCCESS";
    public static final String ACTION_UNKNOWN = "ACTION_UNKNOWN";

    private IASBLInterface mService;
    private boolean mIsServiceConnected;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IASBLInterface.Stub.asInterface(iBinder);
            mIsServiceConnected = true;
            LOG.I(TAG, " -------------- ASBLBridgeService connected --------------" );
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsServiceConnected = false;
            LOG.I(TAG, " -------------- ASBLBridgeService disconnected --------------" );
        }
    };

    private CGBInterface() {
        //FOR GET 2FA: Because by default, the library uses ‘SUN’ provider as the default SecureRandom algorithm provider, which is deprecated in the android system. So you can change it like this:
        Provider[] secureRandomProviders = Security.getProviders("SecureRandom.SHA1PRNG");
        System.setProperty("com.warrenstrange.googleauth.rng.algorithmProvider",secureRandomProviders[0].getName());

        LOG.D(TAG,"Created CGBInterface");
    }

    public static CGBInterface getInstance() {
        if(instance == null) {
            instance = new CGBInterface();
        }
        return instance;
    }

    public boolean init(Context context){
        this.context = context;
        ModuleData.getInstance().setContext(context);
        bindASBLBridgeService();
        return true;
    }

    public void bindASBLBridgeService() {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), ASBLBridgeService.class.getName());
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public static String get2fa(String secretkey) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        String code = String.valueOf(gAuth.getTotpPassword(secretkey));
        while(code.length() < 6) {
            code = ("0" + code);
        }
        return code;
    }

    /* -------------- */
    private native boolean initCGBInterface(String token, String appName);


    public boolean clickByPos(int x, int y, boolean longPress) throws RemoteException {
        return mService.clickByPos(x, y, longPress);
    }

    public boolean clickByComp(String screenID, String compId) throws RemoteException {
        return mService.clickByComp(screenID, compId);
    }

    public boolean swipe(int x1, int y1, int x2, int y2, int duration) throws RemoteException {
        return mService.swipe(x1, y1, x2, y2, duration);
    }

    public boolean openPackage(String pckg) throws RemoteException {
        return mService.openPackage(pckg);
    }

    public boolean inputText(String txt, ScreenNode targetObj, boolean delay) throws RemoteException {
        return mService.inputText(txt, targetObj, delay);
    }


    public boolean scrollForward() throws RemoteException {
        return mService.scrollForward();
    }

    public boolean scrollBackward() throws RemoteException {
        return mService.scrollBackward();
    }

    public boolean globalBack() throws RemoteException {
        return mService.globalBack();
    }
    public ScreenInfo detectScreen(String appName) throws RemoteException {
        return mService.detectScreen(appName);
    }

    public void updateKeywordDefinitions() throws RemoteException {
        mService.updateKeywordDefinitions();
    }

    public String getCurrentForgroundPkg() throws RemoteException {
        return mService.getCurrentForgroundPkg();
    }
}

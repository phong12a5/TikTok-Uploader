package pdt.autoreg.app;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenNode;

public class AppModel {
    private static AppModel model = null;
    private String TAG = "AppModel";

    /* Properites */
    private String deviceName = "";
    private String appName = "";
    private String imei = "";
    private String androidID = "";
    private boolean autoStart = false;
    private JSONObject config = null;
    private String machineID = "";
    private String base = "Base";
    private boolean serviceStarted = false;
    private JSONArray phonePrefixList = null;
    private int m_networkType = AppDefines.MOBILE_NETWORK;
    private SharedPreferences m_prefs = null;
    private String m_currSreenID = null;
    private List<ScreenNode> m_currentScreenInfo = new ArrayList<>();

    private AppModel() {
        m_prefs = App.getContext().getSharedPreferences(AppDefines.PDT_PREFS_NAME, MODE_PRIVATE);

        LOG.D(TAG, "Created Model");
    }

    public static AppModel instance() {
        if(model == null) {
            model = new AppModel();
        }
        return model;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public int networkType() {
        return m_prefs.getInt("netwok_type", AppDefines.MOBILE_NETWORK);
    }

    public void setNetworkType(int data) {
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putInt("netwok_type", data);
        editor.apply();
    }

    public String getCurrentScreenID() {
        return m_currSreenID;
    }

    public void setCurrentScreenID(String screenID) {
        m_currSreenID = screenID;
    }

    public List<ScreenNode> getCurrentScreenInfo() {
        return m_currentScreenInfo;
    }

    public void setCurrentScreenInfo(List<ScreenNode> info) {
        m_currentScreenInfo = info;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }
}

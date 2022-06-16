package pdt.autoreg.app.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.accessibility.screendefinitions.ScreenNode;
import pdt.autoreg.app.App;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.devicefaker.Constants;

public class AppModel {
    private static AppModel model = null;
    private String TAG = "AppModel";

    /* Properites */
    private String imei = "";
    private String androidID = "";
    private boolean serviceStarted = false;
    private int m_networkType = AppDefines.MOBILE_NETWORK;
    private SharedPreferences m_prefs = null;
    private String m_currSrcID = null;
    private List<ScreenNode> m_currScrInfo = new ArrayList<>();
    private PackageInfo m_currPackage = null;

    private AppModel() {
        m_prefs = App.getContext().getSharedPreferences(AppDefines.PDT_PREFS_NAME, MODE_PRIVATE);

//        int currentPackageId = m_prefs.getInt("curr_package_id", -1);
//        if(currentPackageId >= 0) {
//            LOG.I(TAG, "Loaded current packaged id: " + currentPackageId);
//            setCurrPackage(new PackageInfo(currentPackageId, Constants.REG_PACKAGE));
//        }
        LOG.D(TAG, "Created Model");
    }

    public static AppModel instance() {
        if(model == null) {
            model = new AppModel();
        }
        return model;
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
        return m_prefs.getInt("netwok_type", AppDefines.PROXY_NETWORK);
    }

    public void setNetworkType(int data) {
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putInt("netwok_type", data);
        editor.apply();
    }

    public PackageInfo currPackage() {
        return m_currPackage;
    }

    public void setCurrPackage(PackageInfo packageInfo) {
        m_currPackage = packageInfo;
        if(m_currPackage != null) {
            SharedPreferences.Editor editor = m_prefs.edit();
            editor.putInt("curr_package_id", m_currPackage.getPackageId());
            editor.apply();
        }
    }

    public String currScrID() {
        return m_currSrcID;
    }

    public void setCurrScrID(String screenID) {
        m_currSrcID = screenID;
    }

    public List<ScreenNode> currScrInfo() {
        return m_currScrInfo;
    }

    public void setCurrtScrInfo(List<ScreenNode> info) {
        m_currScrInfo = info;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }
}

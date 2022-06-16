package pdt.autoreg.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import pdt.autoreg.app.api.DBPApi;
import pdt.autoreg.app.helpers.ClonesDBHelper;
import pdt.autoreg.devicefaker.LOG;

public class PackageInfo {
    private static final String TAG = "PackageInfo";


    private int packageId = -1;
    private String packageName;
    private CloneInfo cloneInfo = null;
    private boolean isVerifiedLogin = false;
    private JSONArray actions = null;

    public PackageInfo(int packageId, String packageName) {
        this.packageId = packageId;
        this.packageName = packageName;

        String cloneInfo = ClonesDBHelper.instance().getCloneInfo(packageId);
        LOG.I(TAG, "packageId: " + packageId + " -- clone_info: " + cloneInfo);
        setCloneInfo(cloneInfo);
    }

    public int getPackageId() {
        return packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isVerifiedLogin() {return isVerifiedLogin; }

    public void setIsVerifiedLogin(boolean status) { isVerifiedLogin = status; }

    public CloneInfo getCloneInfo() {
        return cloneInfo;
    }

    public void setCloneInfo(String cloneInfoJson) {
        try {
            this.cloneInfo = new CloneInfo(cloneInfoJson) {
                @Override
                void onCloneInfoChanged() {
                    LOG.I(TAG, "onCloneInfoChanged: " + this);
                    LOG.D(TAG, "updateCloneInfo: " + DBPApi.instance().updateCloneInfo(this));
                    if(CLONE_STATUS_STORED.equals(this.status())) {
                        ClonesDBHelper.instance().updateCloneInfo(PackageInfo.this.getPackageId(), this.toString());
                    } else {
                        ClonesDBHelper.instance().updateCloneInfo(PackageInfo.this.getPackageId(), "");
                    }
                }
            };
        } catch (Exception e) {
            LOG.E(TAG, String.format("setCloneInfo(%s) failed",cloneInfoJson));
        }
    }

    public JSONObject takeAction() {
        if(actions == null || actions.length() == 0) return null;

        int index = new Random().nextInt(actions.length());
        try {
            JSONObject action = actions.getJSONObject(index);
            actions.remove(index);
            return action;
        } catch (JSONException e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    public JSONArray getActions() {
        return actions;
    }

    public void setActions(JSONArray array) {
        actions = array;
    }
}

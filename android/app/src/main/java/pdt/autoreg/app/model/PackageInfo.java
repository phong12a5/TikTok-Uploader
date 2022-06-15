package pdt.autoreg.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import pdt.autoreg.app.api.DBPApi;
import pdt.autoreg.app.helpers.ClonesDBHelper;
import pdt.autoreg.devicefaker.LOG;

public class PackageInfo {
    private static final String TAG = "PackageInfo";


    private int packageId = -1;
    private String packageName;
    private CloneInfo cloneInfo = null;

    public PackageInfo(int packageId, String packageName) {
        this.packageId = packageId;
        this.packageName = packageName;

        setCloneInfo(ClonesDBHelper.instance().getCloneInfo(packageId));
    }

    public int getPackageId() {
        return packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public CloneInfo getCloneInfo() {
        return cloneInfo;
    }

    void setCloneInfo(String cloneInfoJson) {
        try {
            this.cloneInfo = new CloneInfo(cloneInfoJson) {
                @Override
                void onCloneInfoChanged() {
                    LOG.D(TAG, "updateCloneInfo: " + DBPApi.instance().updateCloneInfo(this));
                    if(CLONE_STATUS_STORED.equals(this.status())) {
                        ClonesDBHelper.instance().updateCloneInfo(PackageInfo.this.getPackageId(), this.toString());
                    } else {
                        ClonesDBHelper.instance().updateCloneInfo(PackageInfo.this.getPackageId(), "");
                    }
                }
            };
        } catch (JSONException e) {
            LOG.printStackTrace(TAG, e);
        }
    }
}

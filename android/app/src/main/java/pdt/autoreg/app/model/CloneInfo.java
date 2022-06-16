package pdt.autoreg.app.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import pdt.autoreg.devicefaker.LOG;

public abstract class CloneInfo extends JSONObject {
    private static final String TAG = "CloneInfo";

    public static final String CLONE_STATUS_FREE = "free";
    public static final String CLONE_STATUS_STORED = "stored";
    public static final String CLONE_STATUS_GETTING = "getting";

    CloneInfo(String cloneInfo) throws JSONException {
        super(cloneInfo);
    }

    public String username() {
        try {
            return this.getString("username");
        } catch (JSONException e) {
            return null;
        }
    }

    public String password() {
        try {
            return this.getString("password");
        } catch (JSONException e) {
            return null;
        }
    }

    public String email() {
        try {
            return this.getString("email");
        } catch (JSONException e) {
            return null;
        }
    }

    public String status() {
        try {
            return this.getString("status");
        } catch (JSONException e) {
            return null;
        }
    }

    public void setStatus(String status) {
        try {
            this.put("status", status);
            onCloneInfoChanged();
        } catch (JSONException e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    public long lastUploadTime() {
        try {
            String timeStr = this.getString("last_upload_time");
            return Long.valueOf(timeStr);
        } catch (JSONException e) {
            return -1;
        }
    }

    public void setLastUploadTime(long time) {
        try {
            this.put("last_upload_time", String.valueOf(time));
            onCloneInfoChanged();
        } catch (JSONException e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    public String clonedFrom() {
        try {
            return this.getString("cloned_from");
        } catch (JSONException e) {
            return null;
        }
    }

    public String videoFolderPath() {
        try {
            return this.getString("video_path");
        } catch (JSONException e) {
            return null;
        }
    }

    abstract void onCloneInfoChanged();
}

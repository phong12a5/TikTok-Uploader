package pdt.autoreg.app.api;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import pdt.autoreg.app.model.CloneInfo;
import pdt.autoreg.devicefaker.LOG;

public class DBPApi {
    private static final String TAG = "DBPApi";
    private static DBPApi sInstance = null;

    private DBPApi() { }

    public static DBPApi instance() {
        if(sInstance == null) {
            sInstance = new DBPApi();
        }
        return sInstance;
    }

    public JSONObject getClone() {
        try {
            JSONObject body = new JSONObject();
            body.put("api", "get_clone");
            return sendRequest(body);
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    public JSONObject getCloneInfo(String username) {
        try {
            JSONObject body = new JSONObject();
            body.put("api", "get_clone_info");
            body.put("username", username);
            return sendRequest(body);
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    public JSONObject updateCloneInfo(CloneInfo cloneInfo) {
        if(cloneInfo == null) {
            LOG.E(TAG, "updateCloneInfo: rejected (reason: cloneInfo is null)");
        }

        try {
            JSONObject body = new JSONObject();
            body.put("api", "update_clone_info");
            body.put("clone_info", cloneInfo);
            return sendRequest(body);
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    public JSONObject getVideoPath(String author) {
        try {
            JSONObject body = new JSONObject();
            body.put("api", "get_video_path");
            body.put("author", author);
            return sendRequest(body);
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    public JSONObject updateVideoStatus(String video_id, String status) {
        try {
            JSONObject body = new JSONObject();
            body.put("api", "update_video_status");
            body.put("video_id", video_id);
            body.put("status", status);
            return sendRequest(body);
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }

    private JSONObject sendRequest(JSONObject bodyJson) {
        OkHttpClient client = new OkHttpClient();
        client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyJson.toString());
        Request request = new Request.Builder()
                .url("https://dangbaphong.com/api/tiktok/mm-tiktok-api.php")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        JSONObject retval = null;
        try {
            Response response = client.newCall(request).execute();
            retval = new JSONObject(response.body().string());
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }

        return retval;
    }
}

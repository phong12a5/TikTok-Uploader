package pdt.autoreg.app;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pdt.autoreg.cgblibrary.LOG;

public class CloneSubmitter {
    private static String TAG = "CloneSubmitter";
    private static RequestQueue reqQueue = null;
    private static List<JSONObject> pendingCloneList = new ArrayList<>();

    public static void submitClone(Context context, final String app_name,  final String uid, final String username, final String password, final String phone_number, final String email, final String passmail, final String cookie, final String secretkey, final boolean novery, String country) {
        LOG.D(TAG, "app_name: " + app_name + "\nuid: " + uid + "\nusername: " + username + "\npassword: " + password + "\nphone_number: " + phone_number + "\nemail: " + email + "\npassmail: " + passmail + "\nsecretkey: " + secretkey + "\nnovery: " + novery);
        try {

        } catch (Exception e) {
            LOG.E(TAG, "submitClone error: " + e);
        }
    }
}

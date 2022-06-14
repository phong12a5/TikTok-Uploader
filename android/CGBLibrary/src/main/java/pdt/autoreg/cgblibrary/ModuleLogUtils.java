package pdt.autoreg.cgblibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

public class ModuleLogUtils {
    private static final boolean bDEBUG = true;

    private static RequestQueue reqQueue = null;

    public static void LogD(String TAG, String message) {
        if (bDEBUG) {
            LOG.D(TAG, message);
        }

    }

    public static void LogE(String TAG, String message) {
        if (bDEBUG) {
            LOG.E(TAG, message);
        }
    }

    public static void LogI(String TAG, String message) {
        if (bDEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void writeLog2Server(Context context, final String sPackage , final String tag, final String sPage, final String message, String screenInfo, String screenshot) {
//        try {
//            if(reqQueue == null){
//                reqQueue = Volley.newRequestQueue(context);
//            }
//
//            JSONObject jsonLogObject = new JSONObject();
//            jsonLogObject.put("action", "SaveJasmine");
//            jsonLogObject.put("token", ModuleData.getInstance().getToken());
//            jsonLogObject.put("appname", ModuleData.getInstance().getAppName());
//            jsonLogObject.put("info", ModuleData.getInstance());
//            jsonLogObject.put("machine",ModuleData.getInstance());
//            jsonLogObject.put("devicename", ModuleData.getInstance() );
//            jsonLogObject.put("package_name", sPackage );
//            jsonLogObject.put("module", "CGBLibrary");
//            jsonLogObject.put("tag", tag);
//
//            jsonLogObject.put("page", sPage);
//
//            jsonLogObject.put("message", message);
//            jsonLogObject.put("screenInfo", screenInfo);
//            if (screenshot!=null && !screenshot.isEmpty()) {
//                Bitmap bm = BitmapFactory.decodeFile(screenshot);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] b = baos.toByteArray();
//                String encodedString = Base64.encodeToString(b, Base64.DEFAULT);
//                jsonLogObject.put("image", encodedString);
//            }
//
//                String URL = "https://log.autofarmer.xyz/api/Jasmine";
//
//                JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, URL, jsonLogObject,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//                                    JSONObject jsonObject = response;
//                                    LOG.D("writeLog2Server", jsonObject + "");
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//
//                                }
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                LOG.E("VolleyError",  " ");
//                            }
//                        }
//                ) { };
//                reqQueue.add(jsonobj);
//        }catch (Exception _ex){
//            LOG.E("Errror ",_ex +"");
//        }
    }
}

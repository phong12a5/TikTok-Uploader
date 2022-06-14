package pdt.autoreg.cgblibrary;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOG {
    final static int PRODUCTION_MODE = 0;
    final static int TEST_MODE = 1;
    final static int FULL_TEST_MODE = 2;
    final static String TAG = "LOG";
    final static int PID = android.os.Process.myPid();
    private static boolean m_debug = BuildConfig.DEBUG;
    private static List<String> m_logList = new ArrayList<String>();
    private static int m_debugMode = PRODUCTION_MODE;
    private static RequestQueue reqQueue = null;
    private static String device_label = null;


    public static void setConfig(JSONObject config) {
        try {
            if (config != null) {
                if (config.has("debug_mode")) {
                    String debugMode = config.getString("debug_mode");
                    switch (debugMode) {
                        case "test":
                            m_debugMode = TEST_MODE;
                            break;
                        case "Full_Test":
                            m_debugMode = FULL_TEST_MODE;
                            break;
                        default:
                            m_debugMode = PRODUCTION_MODE;
                            break;
                    }
                    Log.d(TAG, "setConfig -> m_debugMode: " + m_debugMode);
                }

                if(config.has("label")) {
                    try {
                        JSONObject device_info = config.getJSONObject("device_info");
                        device_label = device_info.getString("label");
                    } catch (Exception e){
                        Log.e(TAG, "setConfig error: " + e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("LOG","setConfig Error: " + e);
        }
    }

    public static void  D(String TAG, String content) {
        if(m_debug) {
            Log.d(TAG,"[" + getTime() + "]" + "[" + PID + "] [" + content + "]");
        }
        if(m_debug || m_debugMode == TEST_MODE || m_debugMode == FULL_TEST_MODE) {
//            writeMsgLog2Server(ModuleData.getInstance().getContext(), "DEBUG", device_label, "CGBLibrary", TAG, "D/ " + "[" + getTime() + "]" + "[" + PID + "] [" + TAG + ":" + content + "]");
        }
    }

    public static void I(String TAG, String content) {
        if(m_debug) {
            Log.i(TAG,"[" + getTime() + "]" + "[" + PID + "] [" + content + "]");
        }
        if(m_debug || m_debugMode == TEST_MODE || m_debugMode == FULL_TEST_MODE) {
//            writeMsgLog2Server(ModuleData.getInstance().getContext(), "INFO", device_label, "CGBLibrary", TAG, "I/ " + "[" + getTime() + "]" + "[" + PID + "] [" + TAG + ":" + content + "]");
        }
    }

    public static void E(String TAG, String content) {
        if(m_debug) {
            Log.e(TAG,"[" + getTime() + "]" + "[" + PID + "] [" + content + "]");
        }
        if(m_debug || m_debugMode == TEST_MODE || m_debugMode == FULL_TEST_MODE) {
//            writeMsgLog2Server(ModuleData.getInstance().getContext(), "ERROR", device_label, "CGBLibrary", TAG, "E/ " + "[" + getTime() + "]" + "[" + PID + "] [" + TAG + ":" + content + "]");
        }
    }

    public static void W(String TAG, String content) {
        if(m_debug) {
            Log.w(TAG,"[" + getTime() + "]" + "[" + PID + "] [" + content + "]");
        }
        if(m_debug || m_debugMode == TEST_MODE || m_debugMode == FULL_TEST_MODE) {
//            writeMsgLog2Server(ModuleData.getInstance().getContext(), "WARN", device_label, "CGBLibrary", TAG, "W/ " + "[" + getTime() + "]" + "[" + PID + "] [" + TAG + ":" + content + "]");
        }
    }

    public static void printStackTrace(String TAG, Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if(m_debug) {
            e.printStackTrace();
        }

        if(m_debug || m_debugMode == TEST_MODE || m_debugMode == FULL_TEST_MODE) {
            String stackStrace = "";
            for (int i = 1; i < stackTrace.length; i++) {
                stackStrace += stackTrace[i] + "\n";
            }
//            writeMsgLog2Server(ModuleData.getInstance().getContext(), "ERROR", m_device_label, "CGBLibrary", TAG, "E/ " + "[" + getTime() + "]" + "[" + PID + "] [" + TAG + ":" + stackStrace + "]");
        }
    }

    public static String getTime() {
        Date currentDate = Calendar.getInstance().getTime();
        int date = currentDate.getDate();
        int month = currentDate.getMonth();
        int year = currentDate.getYear();
        int hours = currentDate.getHours();
        int min = currentDate.getMinutes();
        int sec = currentDate.getSeconds();
        return (1900 + year) + "-" + (month + 1) + "-" + date + " " + hours + ":" + min + ":" + sec;
    }

    public static void clearListMsg() {
        m_logList.clear();
    }

    public static String getListMsg() {
        return m_logList.toString();
    }

    private static void add(String log) {
        synchronized (m_logList) {
            if(m_logList != null) {
                while (m_logList.size() > 200) {
                    m_logList.remove(0);
                }
                m_logList.add(log);
            } else {
                m_logList = new ArrayList<String>();
            }
        }
    }

    public static void writeMsgLog2Server(Context context, final String sLevel, final String device_label, final String sModule, final String tag, final String message) {
        if(context == null)
            return;
        try {
            if (reqQueue == null) {
                reqQueue = Volley.newRequestQueue(context);
            }

            JSONObject jsonLogObject = new JSONObject();
            jsonLogObject.put("message", message);
            jsonLogObject.put("source", "android_phong");
            jsonLogObject.put("token", ModuleData.getInstance().getToken());
            jsonLogObject.put("action", "AFLog");
            jsonLogObject.put("android_id", ModuleData.getInstance().getAndroidId());
            jsonLogObject.put("device_label", device_label);
            jsonLogObject.put("device_model", ModuleData.getInstance().getDeviceModel());
            jsonLogObject.put("module", sModule);
            jsonLogObject.put("tag", tag);
            jsonLogObject.put("level", sLevel);

            String URL = "https://log-api.congaubeo.us/api/v1/log-debug/create";

            JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, URL, jsonLogObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Log.d(TAG,"writeMsgLog2Server response: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG,"writeMsgLog2Server error: " + error);
                        }
                    }
            ) {
            };
            reqQueue.add(jsonobj);
        } catch (Exception _ex) {
            Log.e("Volley Errror ", _ex + "");
        }
    }

    public static void writeScreenLog2Server(Context context, final String screen_id, final String screenInfo, final String screenshot) {
        try {
            if (reqQueue == null) {
                reqQueue = Volley.newRequestQueue(context);
            }

            String url = "https://log-api.congaubeo.us/api/v1/log-screen/create";
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String resultResponse = new String(response.data);
//                    Log.d(TAG,"writeScreenLog2Server response: " + resultResponse);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG,"writeScreenLog2Server error: " + error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("source", "android_phong");
                    params.put("token", ModuleData.getInstance().getToken());
                    params.put("action", "SaveJasmine");
                    params.put("token", ModuleData.getInstance().getToken());
                    params.put("android_id", ModuleData.getInstance().getAndroidId());
                    params.put("device_label", device_label);
                    params.put("device_model", ModuleData.getInstance().getDeviceModel());
                    params.put("screen_id", screen_id);
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView

                    try {
                        File file = new File(screenshot);
                        int size = (int) file.length();
                        byte[] bytes = new byte[size];
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                        params.put("image", new DataPart("current_screen.png", bytes, "image/png"));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        params.put("image", new DataPart("current_screen.png", new byte[0], "image/png"));
                    }

                    params.put("screen_info",  new DataPart("screen_info.txt", screenInfo.getBytes(), "text/plain"));
                    return params;
                }
            };
            reqQueue.add(multipartRequest);
        } catch (Exception _ex) {
            LOG.E(TAG, "writeScreenLog2Server exception: " + _ex);
        }
    }

    static public void saveUncaughtExceptionLog(String TAG, Thread t, Throwable e) {
        e.printStackTrace();
    }

    static private void postCrashedLog(String tag, String crashedLog) {
        class postAsync extends AsyncTask<String,Void,Boolean> {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    String tag = params[0];
                    String message = params[1];

                    Date currentDate = Calendar.getInstance().getTime();
                    int date = currentDate.getDate();
                    int month = currentDate.getMonth() + 1;
                    int year = currentDate.getYear();
                    int hours = currentDate.getHours();
                    int minutes = currentDate.getMinutes();
                    int seconds = currentDate.getSeconds();

                    String timeStr = "[" + year + "-" + month + "-" + date + " " + hours + ":" + minutes + ":" + seconds + "]";

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("action", "AFCrashedLog");
                    jsonParam.put("token", ModuleData.getInstance().getToken());
                    jsonParam.put("info", ModuleData.getInstance().getDeviceName());
                    jsonParam.put("machine", ModuleData.getInstance().getDeviceName());
                    jsonParam.put("devicename", ModuleData.getInstance().getDeviceName());
                    jsonParam.put("message", message);
                    jsonParam.put("tag", tag);
                    jsonParam.put("time", timeStr);

                    URL url = new URL("https://log.autofarmer.xyz/api/AFCrashedLog");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.connect();

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while ((line = serverAnswer.readLine()) != null) {
                        Log.d(TAG,"LINE: " + line);
                    }
                    serverAnswer.close();
                    LOG.D(TAG,"HttpURLConnection: " + conn.getResponseCode());
                    os.flush();
                    os.close();
                    if(conn.getResponseCode() == 200) {
                        conn.disconnect();
                        return true;
                    } else {
                        conn.disconnect();
                        return false;
                    }
                } catch (Exception e) {
                    Log.e(TAG,"postCrashedLog: " + e);
                    return false;
                }
            }
        }
        try {
            boolean result = new postAsync().execute(tag,crashedLog).get();
        } catch (Exception e) {
            Log.e(TAG,"error: " + e);
        }
    }
}

class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;

    /**
     * Default constructor with predefined header and post method.
     *
     * @param url           request destination
     * @param headers       predefined custom header
     * @param listener      on success achieved 200 code from request
     * @param errorListener on error http or library timeout
     */
    public VolleyMultipartRequest(String url, Map<String, String> headers,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
    }

    /**
     * Constructor with option method and default header configuration.
     *
     * @param method        method for now accept POST and GET only
     * @param url           request destination
     * @param listener      on success event handler
     * @param errorListener on error event handler
     */
    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(dos, params, getParamsEncoding());
            }

            // populate data byte payload
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(dos, data);
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Custom method handle data payload.
     *
     * @return Map data part label with data byte
     * @throws AuthFailureError
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    /**
     * Parse string map into data output stream by key and value.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param params           string inputs collection
     * @param encoding         encode the inputs, default UTF-8
     * @throws IOException
     */
    private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    /**
     * Parse data into data output stream.
     *
     * @param dataOutputStream data output stream handle file attachment
     * @param data             loop through data
     * @throws IOException
     */
    private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Write string data into header and data output stream.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param parameterName    name of input
     * @param parameterValue   value of input
     * @throws IOException
     */
    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    /**
     * Write data file into header and data output stream.
     *
     * @param dataOutputStream data output stream handle data parsing
     * @param dataFile         data byte as DataPart from collection
     * @param inputName        name of data input
     * @throws IOException
     */
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
        if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
        }
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * Simple data container use for passing byte file
     */
    public class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        /**
         * Default data part
         */
        public DataPart() {
        }

        /**
         * Constructor with data.
         *
         * @param name label of data
         * @param data byte data
         */
        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        /**
         * Constructor with mime data type.
         *
         * @param name     label of data
         * @param data     byte data
         * @param mimeType mime data like "image/jpeg"
         */
        public DataPart(String name, byte[] data, String mimeType) {
            fileName = name;
            content = data;
            type = mimeType;
        }

        /**
         * Getter file name.
         *
         * @return file name
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Setter file name.
         *
         * @param fileName string file name
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Getter content.
         *
         * @return byte file data
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Setter content.
         *
         * @param content byte file data
         */
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Getter mime type.
         *
         * @return mime type
         */
        public String getType() {
            return type;
        }

        /**
         * Setter mime type.
         *
         * @param type mime type
         */
        public void setType(String type) {
            this.type = type;
        }
    }
}

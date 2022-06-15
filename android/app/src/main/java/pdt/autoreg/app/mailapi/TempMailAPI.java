package pdt.autoreg.app.mailapi;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pdt.autoreg.accessibility.LOG;

public class TempMailAPI {
    private static String TAG = "TempMailAPI";
    private static List<String>  DOMAIN_LIST = getDomains();
    private static String LIST_CHAR = "abcdefghijklmnopqrstuvwxyz";
    private static String LIST_NUMBER = "0123456789";

    private String hashMD5(String input) {
        String hashtext = null;
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            LOG.D(TAG,"hashMD5 "  + input + " : " + hashtext);
        } catch (Exception e) {
            LOG.E(TAG, "hashMD5: " + e);
        }
        return hashtext;
    }


    public static String generateRandomEmail() {
        String email = null;
        if(DOMAIN_LIST.isEmpty()) {
            DOMAIN_LIST = getDomains();
        }

        if(!DOMAIN_LIST.isEmpty()) {
            try {
                email = new String();
                int lenOfText = new Random().nextInt(5) + 8;
                for (int i = 0; i < 15; i++) {
                    if (i < lenOfText) {
                        email += LIST_CHAR.charAt(new Random().nextInt(LIST_CHAR.length()));
                    } else {
                        email += LIST_NUMBER.charAt(new Random().nextInt(LIST_NUMBER.length()));
                    }
                }

                email += "@";
                email += DOMAIN_LIST.get(new Random().nextInt(DOMAIN_LIST.size()));
            } catch (Exception e) {
                LOG.E(TAG,"generateRandomEmail: " + e);
                email = null;
            }
        }
        LOG.D(TAG,"generateRandomEmail: " + email);
        return email;
    }

    public static String getFBVerifyCode(String emai) {
        String code = null;
        JSONArray message = getMessage(emai);
        if(message != null) {
            for (int i = 0; i < message.length(); i++) {
                try {
                    JSONObject emailObj = message.getJSONObject(i);
                    if(emailObj.getString("from").equals("Facebook &lt;registration@facebookmail.com&gt;")) {
                        String subject = emailObj.getString("subject");
                        if(subject.contains("is your Facebook confirmation code")) {
                            code = subject.split(" ")[0];
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getFBVerifyCode error: " + e);
                }
            }
        }
        return code;
    }

    public static JSONArray getMessage(String email) {
        JSONArray messages = null;
        try {
            String username = email.split("@")[0];
            String domain = email.split("@")[1];
            LOG.D(TAG, "getMessage " + username +  "@" + domain);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://tempmailgen.com/api/getMessages?username=" + username + "&domain=" + domain)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                String resStr = response.body().string();
                JSONObject responseObj = new JSONObject(resStr);
                if(responseObj.has("success") && responseObj.getBoolean("success")) {
                    JSONObject result = responseObj.getJSONObject("result");
                    if(result.has("email")) {
                        messages = result.getJSONArray("email");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"getMessage: " + e);
        }
        Log.d(TAG,"messages: " + messages);
        return messages;
    }

    public static List<String> getDomains() {
        List<String> domains = new ArrayList<>();
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://tempmailgen.com/")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                String html = response.body().string();
                String[] list = html.split("<option value=\"");
                for (int i = 0 ; i < list.length; i ++ ) {
                    if(i == 0)  continue;;

                    String element = list[i];
                    if (element != null && element.contains("</option>")) {
                        String[] tmpList = element.split("\"");
                        if(tmpList.length > 1 && !domains.contains(tmpList[0])) {
                            domains.add(tmpList[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "getDomains: " + e);
        }
        Log.d(TAG,"getDomains: " + domains.toString());
        // Hard code
        domains.clear();
        domains.add("mailpoly.xyz");
        return domains;
    }
}

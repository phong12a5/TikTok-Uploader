package pdt.autoreg.app.mailapi;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pdt.autoreg.accessibility.LOG;

public class NadaMailAPI {
    private static String TAG = "NadaMailAPI";
    private static List<String>  DOMAIN_LIST = getDomains();
    private static String LIST_CHAR = "abcdefghijklmnopqrstuvwxyz";
    private static String LIST_NUMBER = "0123456789";

    public static String generateRandomEmail() {
        LOG.D(TAG, "generateRandomEmail");
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
                    if(emailObj.getString("fe").equals("registration@facebookmail.com")) {
                        //"s":"13765 is your Facebook confirmation code"
                        String subject = emailObj.getString("s");
                        if(subject.contains("Facebook")) {
                            Pattern pattern = Pattern.compile("\\d{4,7}");
                            Matcher matcher = pattern.matcher(subject);

                            while (matcher.find()) {
                                code = matcher.group();
                                LOG.D(TAG, "code: " + code);
                                // s now contains "BAR"
                            }
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
                    .url("https://getnada.com/api/v1/inboxes/" + email)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                String resStr = response.body().string();
                JSONObject responseObj = new JSONObject(resStr);
                if(responseObj.has("msgs") ) {
                    messages = responseObj.getJSONArray("msgs");
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
        domains.add("dropjar.com"); // OK
        domains.add("givmail.com"); // OK
        domains.add("inboxbear.com"); // OK
        domains.add("zetmail.com"); // OK
        return domains;
    }
}

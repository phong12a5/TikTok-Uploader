package pdt.autoreg.app;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class FBUtils {

    private static final String FACE_PACKAGE = "com.facebook.katana";

    public static String getToken(){
        return getToken(FACE_PACKAGE);
    }

    public static String getToken(String packageName){
        String content = getAuthencationContent(packageName);

        if (content.length() == 0){
            return null;
        }

        String token = content.substring(content.indexOf("access_token") + 14,content.indexOf("uid") - 3);

        if (token.endsWith("ZD")){
            return token;
        }
        return token;
    }

    public static String getUID() {
        return FACE_PACKAGE;
    }

    public static String getUID(String packageName){
        String content = getAuthencationContent(packageName);

        if (content.length() <= 50){
            return null;
        }

        return content.substring(content.indexOf(",\"value\":\"") + 10,content.indexOf("\",\"expires\":"));
    }

    public static String getCookies(){
        return getCookies(FACE_PACKAGE);
    }

    public static String getCookies(String packageName){
        String content = getAuthencationContent(packageName);

        if (content.length() == 0){
            return null;
        }

        String jsonCookie = content.substring(content.indexOf("[{\""),content.indexOf("}]") + 2);

        return convertCookieJsonFormat(jsonCookie);
    }

    public static String convertCookieJsonFormat(String json){
        ArrayList<Cookie> cookies = new Gson().fromJson(json,
                new TypeToken<ArrayList<Cookie>>(){}.getType());

        return  cookies.get(1).getName() + "=" + cookies.get(0).getValue() + ";" +
                cookies.get(1).getName() + "=" + cookies.get(1).getValue() + ";" +
                cookies.get(2).getName() + "=" + cookies.get(2).getValue() + ";" +
                cookies.get(3).getName() + "=" + cookies.get(3).getValue() + ";";
    }

    private static String getAuthencationContent(String packageName){
        deleteAuthencationFile();

        @SuppressLint("SdCardPath")
        String fbAuthFolder = "/data/data/" + packageName + "/app_light_prefs/" + packageName + "/";
        String fbAuthFile = fbAuthFolder + "authentication";

        String command1 = "mount -o rw,remount -t rootfs " + fbAuthFolder;
        String command2 = "cp " + fbAuthFile + " " + "/sdcard/";
        String command3 = "chmod 644 " + fbAuthFile;

        String[] commands = {command1,command2,command3};
        execute(commands);

        String text = readTextFromFile("/sdcard/authentication");

        deleteAuthencationFile();

        return text;
    }

    private static void deleteAuthencationFile(){
        File fileMTP = new File("/sdcard/authentication");

        if (fileMTP.exists()){
            fileMTP.delete();
        }
    }

    public static String execute(String[] commands, Charset type) {

        String result = "null";
        try {
            Process exec = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());

            for (String command : commands) {
                if (type != null){
                    dataOutputStream.writeBytes(Arrays.toString(command.getBytes(type)) + "\n");
                }else {
                    dataOutputStream.writeBytes(command + "\n");
                }
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            dataOutputStream.close();
            try {
                exec.waitFor();
            } catch (InterruptedException ignored) {
                Log.d("VDRootError",ignored.toString());
            }
            if (bufferedReader.ready()) {
                StringBuilder builder = new StringBuilder();
                String aux;
                while ((aux = bufferedReader.readLine()) != null) {
                    builder.append(aux);
                }
                result = builder.toString();
            }
        } catch (Exception ignored) {
            Log.d("VDRootError",ignored.toString());
        }

        return result;
    }

    public static String execute(String command) {
        String[] commands = {command};
        return execute(commands);
    }

    public static String execute(String[] command) {
        return execute(command,null);
    }

    public static String execute(String command, Charset type) {
        String[] commands = {command};
        return execute(commands,type);
    }

    public static String readTextFromFile(String filePath){
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(filePath);

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            Log.d("VDTools",e.toString());
            e.printStackTrace();
        }finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return builder.toString();
    }
}


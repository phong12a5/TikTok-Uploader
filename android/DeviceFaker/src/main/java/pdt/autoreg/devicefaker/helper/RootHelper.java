package pdt.autoreg.devicefaker.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import pdt.autoreg.devicefaker.Constants;
import pdt.autoreg.devicefaker.LOG;
import com.stericson.RootShell.RootShell;

public class RootHelper {
    private static final String TAG = "RootHelper";

    public static boolean isRootAccess() {
        return  RootShell.isAccessGiven();
    }

    public static void enableAirplane() {
        LOG.D("enableAirplane", "enableAirplane");
        String command1 = "settings put global airplane_mode_on 1";
        String command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
        String[] commands = {command1, command2};

        for (int i = 0; i < 3; i++) {
            execute(commands);
        }
    }

    public static void disableAirplane() {
        LOG.D("disableAirplane", "disableAirplane");
        String command1 = "settings put global airplane_mode_on 0";
        String command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
        String[] commands = {command1, command2};

        for (int i = 0; i < 3; i++) {
            execute(commands);
        }
    }

    public static void acceptPermission(String permission, String packageName) {
        String command = "pm grant " + packageName + " " + permission;
        execute(command);
    }

    private void revokePermission(String permission, String packageName) {
        String command = "pm revoke " + packageName + " " + permission;
        execute(command);
    }

    public static void clearPackage(String packageName) {
        execute("pm clear " + packageName);
    }

    public static String execute(String command) {
        LOG.D(TAG, "command: " + command);
        String result = null;
        try {
            String retour = "";
            Runtime runtime = Runtime.getRuntime();

            Process p = runtime.exec("su -c " + command);

            java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            java.io.BufferedReader errorIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                retour += line + "\n";
            }
            while ((line = errorIn.readLine()) != null) {
                retour += line + "\n";
            }
            result = retour;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String execute(String[] commands, Charset type) {
        String result = null;
        try {
            Process exec = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());

            for (String command : commands) {
                LOG.D(TAG, "" + command);
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
            } catch (InterruptedException e) {
                LOG.printStackTrace(TAG, e);
            }
            if (bufferedReader.ready()) {
                StringBuilder builder = new StringBuilder();
                String aux;
                while ((aux = bufferedReader.readLine()) != null) {
                    builder.append(aux);
                }
                result = builder.toString();
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }

        return result;
    }


    public static String execute(String[] command) {
        return execute(command,null);
    }

    public static String execute(String command, Charset type) {
        String[] commands = {command};
        return execute(commands,type);
    }
}

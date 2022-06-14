package pdt.autoreg.devicefaker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class VDRoot {

    public static String execute(String... commands) {

        String result = "null";
        try {
            Process exec = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());

            for (String command : commands) {
                dataOutputStream.writeBytes(command + "\n");
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
}

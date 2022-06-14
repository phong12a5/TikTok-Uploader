package pdt.autoreg.cgblibrary;

import android.content.Context;

public class ModuleData {
    private Context m_context = null;
    private String m_token = null;
    private String m_appName = null;
    private static ModuleData instance = null;
    private DeviceInfo m_deviceInfo = null;
    private String m_deviceName = null;

    public static ModuleData getInstance() {
        if(instance == null)
            instance = new ModuleData();
        return instance;
    }

    public Context getContext() {
        return m_context;
    }

    public void setContext(Context m_context) {
        this.m_context = m_context;
        m_deviceInfo = new DeviceInfo(m_context);
    }

    public String getToken() {
        return m_token;
    }

    public void setToken(String m_token) {
        this.m_token = m_token;
    }

    public String getAppName() {
        return m_appName;
    }

    public void setAppName(String m_appName) {
        this.m_appName = m_appName;
    }

    public DeviceInfo getDeviceInfo() { return m_deviceInfo; }

    public String getDeviceName(){ return m_deviceInfo ==null? "NULL": m_deviceInfo.getDeviceName(); }

    public String getDeviceIMEI(){ return m_deviceInfo ==null? "NULL": m_deviceInfo.getIMEI(); }

    public String getAndroidId(){ return m_deviceInfo ==null? "NULL": m_deviceInfo.getAndroidId(); }

    public String getDeviceModel(){ return m_deviceInfo ==null? "NULL": m_deviceInfo.getModel(); }
}

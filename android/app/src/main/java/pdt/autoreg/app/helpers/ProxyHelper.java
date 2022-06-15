package pdt.autoreg.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.app.App;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.devicefaker.helper.RootHelper;

public class ProxyHelper {
    private static final String TAG = "ProxyHelper";

    public static final String PROXY_PROTOCOL_HTTP = "http";
    public static final String PROXY_PROTOCOL_HTTPS = "https";
    public static final String PROXY_PROTOCOL_SOCKS4 = "socks4";
    public static final String PROXY_PROTOCOL_SOCKS5 = "socks5";

    static final String CMD_IPTABLES_REDIRECT_ADD_HTTP = "/system/bin/iptables -t nat -A OUTPUT -p tcp --dport 80 -j REDIRECT --to 8123\niptables -t nat -A OUTPUT -p tcp --dport 443 -j REDIRECT --to 8124\niptables -t nat -A OUTPUT -p tcp --dport 5228 -j REDIRECT --to 8124\n";
    static final String CMD_IPTABLES_REDIRECT_ADD_HTTP_TUNNEL = "/system/bin/iptables -t nat -A OUTPUT -p tcp --dport 80 -j REDIRECT --to 8123\niptables -t nat -A OUTPUT -p tcp --dport 443 -j REDIRECT --to 8123\niptables -t nat -A OUTPUT -p tcp --dport 5228 -j REDIRECT --to 8123\n";
    static final String CMD_IPTABLES_REDIRECT_ADD_SOCKS = "/system/bin/iptables -t nat -A OUTPUT -p tcp -j REDIRECT --to 8123\n";


    public static void starProxySwitch(Context context, String host, int port, String protocol) {
        LOG.D(TAG, "starProxySwitch -- host: "+ host + " -- port: " + port + " -- protocol: " + protocol);
        // /system/bin/iptables -t nat -A OUTPUT -p tcp -d 189.231.202.141 -j RETURN
        LOG.D(TAG,"starProxySwitch");
        String cmd1 = "chmod 700 /data/data/pdt.autoreg.app/files/redsocks";
        String cmd2 = "chmod 700 /data/data/pdt.autoreg.app/files/proxy.sh";
        String cmd3 = "chmod 700 /data/data/pdt.autoreg.app/files/gost.sh";
        String cmd4 = "chmod 700 /data/data/pdt.autoreg.app/files/cntlm";
        String cmd5 = "chmod 700 /data/data/pdt.autoreg.app/files/gost";


        switch (protocol) {
            case PROXY_PROTOCOL_HTTP: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start %s %s %d false \"\" \"\"", protocol, host, port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd8 = CMD_IPTABLES_REDIRECT_ADD_HTTP;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            case PROXY_PROTOCOL_HTTPS: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " -L=http://127.0.0.1:8126 -F=https://%s:%d?ip=%s", host, port, host);
                String cmd7 = context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start http 127.0.0.1 8126 false \"\" \"\"";
                String cmd8 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd9 = CMD_IPTABLES_REDIRECT_ADD_HTTP;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8, cmd9});
            }
            break;
            case PROXY_PROTOCOL_SOCKS4:
            case PROXY_PROTOCOL_SOCKS5: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start %s %s %d false \"\" \"\"", protocol, host, port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd8 = CMD_IPTABLES_REDIRECT_ADD_SOCKS;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            default:
                LOG.E(TAG, "starProxySwitch: " + protocol + " is not supported!");
                break;
        }
    }

    public static void stopProxySwitch() {
        LOG.D(TAG,"stopProxySwitch");
        String cmd1 = "/system/bin/iptables -t nat -F OUTPUT";
        String cmd2 = "/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ stop";
        String[] commands = {cmd1,cmd2};
        RootHelper.execute(commands);
    }

    JSONObject scanProxyFromGeonode(int limit, int page, String protocol, String countryPrefer) {
        //https://proxylist.geonode.com/api/proxy-list?limit=50&page=1&sort_by=lastChecked&sort_type=desc&fbclid=IwAR1svtJ4Y3C_INVfgD8ai8QCzMFagQioNHikd2VaUo2iEIKLLtiRLWH39Nw
        //http://pubproxy.com/api/proxy?limit=20
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            String url = new Random().nextBoolean()? String.format("https://proxylist.geonode.com/api/proxy-list?limit=%d&page=%d&sort_by=lastChecked&sort_type=desc",limit,page) :
                    "http://pubproxy.com/api/proxy?limit=20";
            LOG.D(TAG, "url: " + url);
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(url)
                    .get()
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);
            LOG.D(TAG, "code: " + response.code());
            if (response.code() == 200) {
                JSONObject responseJson = new JSONObject(resStr);
                JSONArray proxyList = responseJson.getJSONArray("data");
                LOG.D(TAG, "proxyList: " + proxyList);
                JSONObject retVal = null;

                while (proxyList.length() > 0) {
                    int index = new Random().nextInt(proxyList.length());
                    JSONArray protocols = proxyList.getJSONObject(index).getJSONArray("protocols");
                    String country = proxyList.getJSONObject(index).getString("country");
                    if(protocols.toString().contains(protocol) && (countryPrefer == null || countryPrefer.equals(country))) {
                        retVal = new JSONObject();
                        retVal.put("ip", proxyList.getJSONObject(index).getString("ip"));
                        retVal.put("port", proxyList.getJSONObject(index).getInt("port"));
                        retVal.put("country", proxyList.getJSONObject(index).getString("country"));
                        break;
                    } else {
                        proxyList.remove(index);
                    }
                }
                return retVal;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
            return null;
        }
    }

    public static JSONObject scanProxyFromWeb() {
        LOG.D(TAG, "scanProxyFromWeb");
        JSONObject proxy = null;
        try {
            OkHttpClient client = new OkHttpClient();
            client.setCache(null);
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            String url = "http://free-proxy.cz/en/proxylist/main/3";
            url = url.replace(" ","+") + (url.contains("?") ? "&" : "?") + "_=" + System.currentTimeMillis();

            LOG.D(TAG, "url: " + url);
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);

            List<String> listIps = Utils.regex(resStr,"(?<=Base64.decode\\(\\\")(.*?)(?=\\\"\\))");
            LOG.D(TAG, "listIps: " + listIps);

            List<String> listPort = Utils.regex(resStr,"(?<=<td style=\\\"\\\"><span class=\\\"fport\\\" style=\\'\\'>)(.*?)(?=</span></td>)");;
            LOG.D(TAG, "listPort: " + listPort);

            List<String> listType = Utils.regex(resStr,"(?<=</span></td><td><small>)(.*?)(?=</small></td>)");;
            LOG.D(TAG, "listType: " + listType);
            if(listIps.size() == listPort.size() &&
                    listPort.size() == listType.size()) {
                LOG.D(TAG, "listType: " + listType);
                for (int i = 0; i < listIps.size(); i++ ) {
                    String ip =  new String(java.util.Base64.getDecoder().decode(listIps.get(i)), "UTF-8");
                    if(Utils.ping(ip)) {
                        proxy = new JSONObject();
                        proxy.put("ip",ip);
                        proxy.put("port",Integer.valueOf(listPort.get(i)));
                        proxy.put("type",listType.get(i));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
        }
        return proxy;
    }

    protected JSONObject scanProxy() {
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url("https://api.getproxylist.com/proxy?protocol[]=http&minUptime=75")
                    .get()
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);
            LOG.D(TAG, "code: " + response.code());
            if (response.code() == 200) {
                return new JSONObject(resStr);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
            return null;
        }
    }
}

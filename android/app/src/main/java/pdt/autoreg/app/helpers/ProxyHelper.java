package pdt.autoreg.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.chilkatsoft.CkHttp;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    public static class ProxyInfo {
        public static final int STATUS_UNCHECK = 0;
        public static final int STATUS_ALIVE = 1;
        public static final int STATUS_DIED = 2;

        String ip;
        int port;
        String country;
        String type;
        int status;

        public ProxyInfo(String ip, int port, String country, String type, int status) {
            this.ip = ip;
            this.port = port;
            this.country = country;
            this.type = type;
            this.status = status;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public String getCountry() {
            return country;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "ProxyInfo{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", country='" + country + '\'' +
                    ", type='" + type + '\'' +
                    ", status=" + status +
                    '}';
        }
    }

    public static void starProxySwitch(ProxyInfo proxyInfo) {
        LOG.D(TAG, "starProxySwitch: " + proxyInfo);
        // /system/bin/iptables -t nat -A OUTPUT -p tcp -d 189.231.202.141 -j RETURN
        LOG.D(TAG,"starProxySwitch");
        String cmd1 = "chmod 700 /data/data/pdt.autoreg.app/files/redsocks";
        String cmd2 = "chmod 700 /data/data/pdt.autoreg.app/files/proxy.sh";
        String cmd3 = "chmod 700 /data/data/pdt.autoreg.app/files/gost.sh";
        String cmd4 = "chmod 700 /data/data/pdt.autoreg.app/files/cntlm";
        String cmd5 = "chmod 700 /data/data/pdt.autoreg.app/files/gost";


        switch (proxyInfo.type) {
            case PROXY_PROTOCOL_HTTP: {
                String cmd6 = String.format("/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ start %s %s %d false \"\" \"\"", proxyInfo.type, proxyInfo.ip, proxyInfo.port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", proxyInfo.ip);
                String cmd8 = CMD_IPTABLES_REDIRECT_ADD_HTTP;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            case PROXY_PROTOCOL_HTTPS: {
                String cmd6 = String.format("/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ -L=http://127.0.0.1:8126 -F=https://%s:%d?ip=%s", proxyInfo.ip, proxyInfo.port, proxyInfo.ip);
                String cmd7 = "/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ start http 127.0.0.1 8126 false \"\" \"\"";
                String cmd8 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", proxyInfo.ip);
                String cmd9 = CMD_IPTABLES_REDIRECT_ADD_HTTP;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8, cmd9});
            }
            break;
            case PROXY_PROTOCOL_SOCKS4:
            case PROXY_PROTOCOL_SOCKS5: {
                String cmd6 = String.format("/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ start %s %s %d false \"\" \"\"", proxyInfo.type, proxyInfo.ip, proxyInfo.port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", proxyInfo.ip);
                String cmd8 = CMD_IPTABLES_REDIRECT_ADD_SOCKS;
                RootHelper.execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            default:
                LOG.E(TAG, "starProxySwitch: " + proxyInfo.type + " is not supported!");
                break;
        }
    }

    public static List<ProxyInfo> scanProxyFromFreeProxyList(String country, String[] types) {
        // Scan from https://free-proxy-list.net
        List<ProxyInfo> list = new ArrayList<>();
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://free-proxy-list.net/")
                    .method("GET", null)
                    .addHeader("authority", "free-proxy-list.net")
                    .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .addHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8")
                    .addHeader("cache-control", "max-age=0")
                    .addHeader("if-modified-since", "Fri, 17 Jun 2022 09:42:02 GMT")
                    .addHeader("referer", "https://free-proxy-list.net/")
                    .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"102\", \"Google Chrome\";v=\"102\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"macOS\"")
                    .addHeader("sec-fetch-dest", "document")
                    .addHeader("sec-fetch-mode", "navigate")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-user", "?1")
                    .addHeader("upgrade-insecure-requests", "1")
                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.61 Safari/537.36")
                    .build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            String start = "<thead><tr><th>IP Address</th><th>Port</th><th>Code</th><th class='hm'>Country</th><th>Anonymity</th><th class='hm'>Google</th><th class='hx'>Https</th><th class='hm'>Last Checked</th></tr></thead>";
            body = body.substring(body.indexOf(start) + start.length());
            body = body.substring(0, body.indexOf("</tbody>"));

            List<String> listObj = Utils.regex(body,"(?<=<tr>)(.*?)(?=</tr>)");
            for (String obj : listObj) {
                List<String> propList = new ArrayList<>();

                try {
                    while (true) {
                        int startIndex = obj.indexOf(">");
                        if(startIndex == -1) break;
                        int endIndex = obj.indexOf("</td>");
                        if(endIndex == -1) break;
                        propList.add(obj.substring(startIndex + 1, endIndex));
                        obj = obj.substring(endIndex + 5);
                    }

                    if (propList != null && propList.size() == 8) {
                        String proxy_country = propList.get(2);
                        String proxy_type = "yes".equals(propList.get(6)) ? PROXY_PROTOCOL_HTTPS : PROXY_PROTOCOL_HTTP;
                        if((country == null || country.equals(proxy_country)) && (types == null || Arrays.asList(types).contains(proxy_type)))
                            list.add(new ProxyInfo(propList.get(0), Integer.parseInt(propList.get(1)), propList.get(2), proxy_type, ProxyInfo.STATUS_UNCHECK));
                    }
                } catch (Exception e) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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

    public static boolean checkProxyALive(ProxyInfo proxyInfo) {
        LOG.I(TAG, "checkProxyALive: " + proxyInfo);
        CkHttp http = new CkHttp();
        http.put_ConnectTimeout(3);
        http.put_ConnectTimeout(3);

        if(PROXY_PROTOCOL_SOCKS5.equals(proxyInfo.type)) {
            http.put_SocksVersion(5);
            http.put_SocksUsername("myUsername");
            http.put_SocksPassword("myPassword");
            http.put_SocksHostname(proxyInfo.ip);
            http.put_SocksPort(proxyInfo.port);
        } else if(PROXY_PROTOCOL_SOCKS4.equals(proxyInfo.type))  {
            http.put_SocksVersion(4);
            http.put_SocksHostname(proxyInfo.ip);
            http.put_SocksPort(proxyInfo.port);
        } else {
            http.put_ProxyDomain(proxyInfo.ip);
            http.put_ProxyPort(proxyInfo.port);
        }

        // Now do whatever it is you need to do.  All communications will go through the proxy.
        String html = http.quickGetStr("https://api64.ipify.org/?format=text");
        return html != null && http.get_LastMethodSuccess();
    }
}

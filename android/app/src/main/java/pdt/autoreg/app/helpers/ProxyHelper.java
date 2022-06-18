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

    public static void stopProxySwitch() {
        LOG.D(TAG,"stopProxySwitch");
        String cmd1 = "/system/bin/iptables -t nat -F OUTPUT";
        String cmd2 = "/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ stop";
        String[] commands = {cmd1,cmd2};
        RootHelper.execute(commands);
    }

    public static List<ProxyInfo> scanProxyFromFreeProxyList(String[] countries, String[] types) {
        LOG.D(TAG, "scanProxyFromFreeProxyList");
        List<ProxyInfo> list = new ArrayList<>();
        try {
            String url = "https://free-proxy-list.net/";
            LOG.D(TAG, "url:" + url);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
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
                        if((countries == null || Arrays.asList(countries).contains(proxy_country)) && (types == null || Arrays.asList(types).contains(proxy_type)))
                            list.add(new ProxyInfo(propList.get(0), Integer.parseInt(propList.get(1)), propList.get(2), proxy_type, ProxyInfo.STATUS_UNCHECK));
                    }
                } catch (Exception e) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ProxyInfo> scanProxyFromGeonode(String[] countries, String[] protocols) {
        LOG.D(TAG, "scanProxyFromGeonode");
        List<ProxyInfo> results = new ArrayList<>();
        try {
            String url = String.format("https://proxylist.geonode.com/api/proxy-list?limit=200&page=%d&sort_by=lastChecked&sort_type=desc&country=US",1);
            LOG.D(TAG, "url: " + url);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            if (response.code() == 200) {
                JSONObject responseJson = new JSONObject(resStr);
                JSONArray proxyList = responseJson.getJSONArray("data");

                for (int i = 0; i < proxyList.length(); i++) {
                    JSONArray supportProtocols = proxyList.getJSONObject(i).getJSONArray("protocols");
                    boolean checkProtocol = false;
                    for (int j = 0; j < supportProtocols.length(); j++) {
                        if(Arrays.asList(protocols).contains(supportProtocols.getString(j))) {
                            checkProtocol = true;
                            break;
                        }
                    }

                    if(!checkProtocol) continue;

                    JSONObject proxyObject = proxyList.getJSONObject(i);
                    String country = proxyObject.getString("country");
                    if(countries == null || Arrays.asList(countries).contains(country)) {
                        results.add(new ProxyInfo(proxyObject.getString("ip"), proxyObject.getInt("port"), country, supportProtocols.getString(0), ProxyInfo.STATUS_UNCHECK));
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }

        return results;
    }

    public static List<ProxyInfo> scanProxyFromFreeProxyCz(String country, String protocol) {
        LOG.D(TAG, "scanProxyFromFreeProxyCz");
        List<ProxyInfo> results = new ArrayList<>();
        try {
//            String url = "http://free-proxy.cz/en";
//            url = url.replace(" ","+") + (url.contains("?") ? "&" : "?") + "_=" + System.currentTimeMillis();

            String url = String.format("http://free-proxy.cz/en/proxylist/country/%s/%s/ping/all",country == null? "all":country, protocol== null? "all" : protocol);
            LOG.D(TAG, "url:" + url);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .addHeader("Accept-Language", "en-US,en;q=0.9,vi;q=0.8")
                    .addHeader("Cache-Control", "max-age=0")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Cookie", "fp=27ef3f4f743abadf92a17cc9d198dccd")
                    .addHeader("Referer", "http://free-proxy.cz/en/")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.61 Safari/537.36")
                    .build();
            Response response = client.newCall(request).execute();

            String resStr = response.body().string();
            List<String> listIps = Utils.regex(resStr,"(?<=Base64.decode\\(\\\")(.*?)(?=\\\"\\))");
            List<String> listPort = Utils.regex(resStr,"(?<=<td style=\\\"\\\"><span class=\\\"fport\\\" style=\\'\\'>)(.*?)(?=</span></td>)");;
            List<String> listType = Utils.regex(resStr,"(?<=</span></td><td><small>)(.*?)(?=</small></td>)");;
            if(listIps.size() == listPort.size() &&
                    listPort.size() == listType.size()) {
                for (int i = 0; i < listIps.size(); i++ ) {
                    String ip =  new String(java.util.Base64.getDecoder().decode(listIps.get(i)), "UTF-8");
                    results.add(new ProxyInfo(ip, Integer.valueOf(listPort.get(i)), protocol, listType.get(i).toLowerCase(), ProxyInfo.STATUS_UNCHECK));
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxyFromFreeProxyCz error: " + e);
        }
        return results;
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
        String html = http.quickGetStr("https://www.google.com");
        return html != null && http.get_LastMethodSuccess();
    }
}

package pdt.autoreg.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.chilkatsoft.CkHttp;
import com.chilkatsoft.CkSsh;
import com.chilkatsoft.CkSshTunnel;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    results.add(new ProxyInfo(ip, Integer.valueOf(listPort.get(i)), country, listType.get(i).toLowerCase(), ProxyInfo.STATUS_UNCHECK));
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxyFromFreeProxyCz error: " + e);
        }
        return results;
    }



    public static boolean checkProxyALive(ProxyInfo proxyInfo) {
        LOG.I(TAG, "checkProxyALive: " + proxyInfo);
//        CkHttp http = new CkHttp();
//        http.put_ConnectTimeout(3);
//        http.put_ConnectTimeout(3);
//
//        if(PROXY_PROTOCOL_SOCKS5.equals(proxyInfo.type)) {
//            http.put_SocksVersion(5);
//            http.put_SocksUsername("myUsername");
//            http.put_SocksPassword("myPassword");
//            http.put_SocksHostname(proxyInfo.ip);
//            http.put_SocksPort(proxyInfo.port);
//        } else if(PROXY_PROTOCOL_SOCKS4.equals(proxyInfo.type))  {
//            http.put_SocksVersion(4);
//            http.put_SocksHostname(proxyInfo.ip);
//            http.put_SocksPort(proxyInfo.port);
//        } else {
//            http.put_ProxyDomain(proxyInfo.ip);
//            http.put_ProxyPort(proxyInfo.port);
//        }

        OkHttpClient client = new OkHttpClient();
        if(PROXY_PROTOCOL_SOCKS5.equals(proxyInfo.type) ||
                PROXY_PROTOCOL_SOCKS4.equals(proxyInfo.type)) {
            client.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyInfo.ip, proxyInfo.port)));
        } else {
            client.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.ip, proxyInfo.port)));
        }
        client.setConnectTimeout(3, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(3, TimeUnit.SECONDS);    // socket timeout

        Request request = new Request.Builder().url("https://api.ipify.org?format=text").build();
        try {
            Response response = client.newCall(request).execute();
            if(response != null && response.code() == 200) {
                LOG.D(TAG,"ip: " + response.body().string());
                return true;
            } else {
                LOG.D(TAG,"startProxy failed -> proxy: " + proxyInfo);
            }
        } catch (IOException e) {
            LOG.E(TAG, "startProxy failed -> error: " + e.getMessage());
        }
        return false;

        // Now do whatever it is you need to do.  All communications will go through the proxy.
//        String html = http.quickGetStr("https://www.google.com");
//        return html != null && http.get_LastMethodSuccess();
    }

    public static void scanSsh() {
        JSONArray m_listSSH;
        try {
            LOG.D(TAG, " ---------------------------- Re-scanning ssh server ---------------------------- ");
            CkHttp http = new CkHttp();
            String body = http.quickGetStr("https://ssh24h.com/APIv2?token=3d4fd16cf3b472cf91ea0ed9b813c805&code=US");

            JSONObject respObj = new JSONObject(body);
            m_listSSH = respObj.getJSONArray("listSSH");

            LOG.D(TAG, "listSSH: " + m_listSSH.length());


            LOG.D(TAG, " ----------------------------- checking ----------------------------- ");
            for (int i = 0; i < m_listSSH.length(); i++) {
                try {
                        String sshStr = m_listSSH.getString(i);
                        String[] split = sshStr.split("\\|");
                        String countryPart = split[3];
                        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(countryPart);
                        String countryCode = null;
                        while (m.find()) {
                            countryCode = m.group(1);
                        }

                        String ip = split[0];
                        String username = split[1];
                        String password = split[2];
                        String IPADDRESS_PATTERN =
                                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

                        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                        Matcher matcher = pattern.matcher(ip);
                        if (matcher.find()) {
                            ip = matcher.group(0);
                        }

                        if(checkSSHConnection(ip, 22, countryCode) && dynamicForwardPort(ip, 22, username, password)) {
                            starProxySwitch();
                            Utils.delay(2000);
                            String publicUp = Utils.getPuclicIP();
                            if(publicUp != null) {
                                Utils.showToastMessage(App.getContext(), "public ip: " + publicUp);
                                LOG.I(TAG, "ngon");
                                break;
                            }
                        }

                        closeTunnel();
                        stopProxySwitch();
                } catch (Exception e) {
                    LOG.E(TAG, "scanSsh error: " + e);
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanSsh: " + e);
        }
    }

    public static boolean checkSSHConnection(String ipAddress, int port, String countryCode) {
        LOG.D(TAG, "checkSSHConnection ipAddress: " + ipAddress + " -- countryCode: " + countryCode);
        CkSsh ssh = new CkSsh();
        ssh.put_ConnectTimeoutMs(2000);
        boolean connected = false;
        if (ssh.Connect(ipAddress,port)) {

            //  Am I connected?
            connected = ssh.get_IsConnected();

            //  Disconnect.
            ssh.Disconnect();
        }
        LOG.D(TAG, "checkSSHConnection -- ipAddress: " + ipAddress + " -- connect: " + (connected?  "success" : "failed"));
        return connected;
    }

    static CkSshTunnel sTunnel;
    public static boolean dynamicForwardPort(String sshHostname, int sshPort, String userName, String password) {
        LOG.D(TAG, "dynamicForwardPort -> sshHostname: " + sshHostname + " -- sshPort: " + sshPort + " -- userName: " + userName + " -- password: "  + password);
        boolean success = false;

        try {
            if (sTunnel == null) {
                sTunnel = new CkSshTunnel();
            }

            //  Connect to an SSH server and establish the SSH tunnel:
            sTunnel.put_ConnectTimeoutMs(5000);
            success = sTunnel.Connect(sshHostname, sshPort);
            if (success != true) {
                LOG.E(TAG, "Connect: " + sTunnel.lastErrorText());
                return false;
            }

            //  Authenticate with the SSH server via a login/password
            //  or with a public key.
            //  This example demonstrates SSH password authentication.
            success = sTunnel.AuthenticatePw(userName, password);
            if (success != true) {
                LOG.E(TAG, "AuthenticatePw: " + sTunnel.lastErrorText());
                closeTunnel();
                return false;
            }

            //  Indicate that the background SSH tunnel thread will behave as a SOCKS proxy server
            //  with dynamic port forwarding:
            sTunnel.put_DynamicPortForwarding(true);

            //  We may optionally require that connecting clients authenticate with our SOCKS proxy server.
            //  To do this, set an inbound username/password.  Any connecting clients would be required to
            //  use SOCKS5 with the correct username/password.
            //  If no inbound username/password is set, then our SOCKS proxy server will accept both
            //  SOCKS4 and SOCKS5 unauthenticated connections.

//        tunnel.put_InboundSocksUsername("chilkat123");
//        tunnel.put_InboundSocksPassword("password123");

            //  Start the listen/accept thread to begin accepting SOCKS proxy client connections.
            //  Listen on port 1080.
            success = sTunnel.BeginAccepting(1080);
            if (success != true) {
                LOG.E(TAG, "BeginAccepting: " + sTunnel.lastErrorText());
                closeTunnel();
                return false;
            } else {
                LOG.D(TAG, "Forword port successfully");
                return true;
            }
        } catch (Exception e) {
            LOG.E(TAG, "dynamicForwardPort error: " + e);
            return false;
        }
    }

    public static boolean closeTunnel() {
        LOG.D(TAG,"closeTunnel");
        try {
            //  Stop the background listen/accept thread:
            boolean waitForThreadExit = true;
            if (sTunnel != null) {
                boolean success = sTunnel.StopAccepting(waitForThreadExit);
                if (success != true) {
                    LOG.E(TAG, "StopAccepting: " + sTunnel.lastErrorText());
                    return false;
                }

                Utils.delay(1000);
                LOG.D(TAG, "get_IsAccepting: " + sTunnel.get_IsAccepting());

                //  Close the SSH tunnel (would also kick any remaining connected clients).
                sTunnel.DisconnectAllClients(waitForThreadExit);
                while(!sTunnel.CloseTunnel(waitForThreadExit)) {
                    LOG.E(TAG, "CloseTunnel: " + sTunnel.lastErrorText());
                    Utils.delay(1000);
                }

                sTunnel.delete();
                sTunnel = null;
            }
            return true;
        } catch (Exception e) {
            LOG.E(TAG, "closeTunnel error: " + e);
            return false;
        }
    }

    public static void starProxySwitch() {
        LOG.D(TAG,"starProxySwitch");
        String cmd1 = "chmod 700 /data/data/pdt.autoreg.app/redsocks";
        String cmd2 = "chmod 700 /data/data/pdt.autoreg.app/proxy.sh";
        String cmd4 = "chmod 700 /data/data/pdt.autoreg.app/cntlm";
        String cmd6 = "chmod 700 /data/data/pdt.autoreg.app/gost";
        String cmd7 = "/data/user/0/pdt.autoreg.app/files/proxy.sh /data/user/0/pdt.autoreg.app/files/ start socks5 127.0.0.1 1080 false \"\" \"\"";
        String cmd8 = "/system/bin/iptables -t nat -A OUTPUT -p tcp -d 127.0.0.1 -j RETURN\n" +
                "/system/bin/iptables -t nat -A OUTPUT -p tcp -j REDIRECT --to 8123";
        String[] commands = {cmd1,cmd2,cmd4,cmd6,cmd7,cmd8};
        RootHelper.execute(commands);
    }

//    public static void stopProxySwitch() {
//        LOG.D(TAG,"stopProxySwitch");
//        String cmd1 = "/system/bin/iptables -t nat -F OUTPUT";
//        String cmd2 = "/data/data/pdt.autoreg.app/proxy.sh stop";
//        String[] commands = {cmd1,cmd2};
//        RootHelper.execute(commands);
//    }
}

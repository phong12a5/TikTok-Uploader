package pdt.autoreg.app;

public class Cookie {
    private String name,
            value,
            expires;
    private long expires_timestamp;
    private String domain,
            path;
    private boolean secure,
            httponly;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getExpires() {
        return expires;
    }

    public long getExpires_timestamp() {
        return expires_timestamp;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttponly() {
        return httponly;
    }
}

package jv.http;

import java.util.HashMap;

public class HTTPContentType {
    private static final HashMap<String, String> types = new HashMap<>(0);
    static {
        types.put("pdf", "application/pdf");
        types.put("txt", "text/plain");
        types.put("html", "text/html");
        types.put("exe", "application/octet-stream");
        types.put("zip", "application/zip");
        types.put("doc", "application/msword");
        types.put("xls", "application/vnd.ms-excel");
        types.put("ppt", "application/vnd.ms-powerpoint");
        types.put("gif", "image/gif");
        types.put("png", "image/png");
        types.put("jpeg", "image/jpg");
        types.put("jpg", "image/jpg");
        types.put("php", "text/plain");

        types.put("m3u8", "application/vnd.apple.mpegurl");
        types.put("ts",	"video/mp2t");
    }

    public static String getContentTypeFor(String extension) {
        String contentType = types.get(extension);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }
}

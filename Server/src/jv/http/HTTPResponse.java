package jv.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class HTTPResponse {

    private static class HTTPStatus {
        private static final HashMap<Integer, String> statuses = new HashMap<>(0);
        static {
            statuses.put(100, "Continue");

            statuses.put(200, "OK");
            statuses.put(201, "Created"); // Created file
            statuses.put(204, "No Content"); // Only file info

            statuses.put(400, "Bad Request");
            statuses.put(403, "Forbidden");
            statuses.put(404, "Not Found"); // File not found

            statuses.put(500, "Internal Server Error");
            statuses.put(501, "Not Implemented");
        }

        public static String getStringFor(int code) {
            String statusString = statuses.get(code);
            if (statusString == null) {
                code = 501;
                statusString = statuses.get(code);
            }
            return code + " " + statusString;
        }
    }

    private final String responseLine;
    private final HashMap<String, String> headers = new HashMap<>(0);
    private byte[] data = new byte[]{};

    public HTTPResponse(int code) {
        responseLine = "HTTP/1.1 " + HTTPStatus.getStringFor(code);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setData(byte[] data, String contentTypeExtension) {
        // add content length header
        this.data = data;
        addHeader("Content-Length", String.valueOf(data.length));
        addHeader("Content-Type", HTTPContentType.getContentTypeFor(contentTypeExtension));
    }

    public void send(DataOutputStream outputStream) throws IOException {
        StringBuilder responseString = new StringBuilder(responseLine + "\r\n");
        for(HashMap.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            String headerString = key + ": " + value;
            responseString.append(headerString).append("\r\n");
        }
        responseString.append("\r\n");

        outputStream.writeBytes(responseString.toString());
        if (data != null) {
            outputStream.write(data);
        }
    }
}
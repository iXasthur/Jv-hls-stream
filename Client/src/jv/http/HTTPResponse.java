package jv.http;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

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

    public HTTPResponse(DataInputStream inputStream) throws IOException {
        Scanner scanner = new Scanner(inputStream);
        String line;
        if (scanner.hasNextLine()) {
            line = scanner.nextLine();
            responseLine = line;
        } else {
            throw new IOException("Nothing to read from socket");
        }

        // Read headers
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (!line.equals("")) {
                int spaceIndex = line.indexOf(" ");
                headers.put(line.substring(0, spaceIndex - 1), line.substring(spaceIndex + 1));
            } else {
                break;
            }
        }

        if (headers.get("Content-Length") != null) {
            int count = Integer.parseInt(headers.get("Content-Length"));
            data = inputStream.readNBytes(count);
        } else if (headers.get("Transfer-Encoding") != null && headers.get("Transfer-Encoding").equals("chunked")) {
            // Read chunked data
            int count = readChunkSize(inputStream);
            while (count > 0) {
                byte[] buffer = inputStream.readNBytes(count);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(data);
                outputStream.write(buffer);
                data = outputStream.toByteArray();

                inputStream.readNBytes(2); // skip \r\n
                count = readChunkSize(inputStream);
            }
            inputStream.readNBytes(2);
        }
    }

    private int readChunkSize(DataInputStream inputStream) throws IOException {
        Vector<Byte> bytes = new Vector<>(0);
        while (true) {
            byte b = inputStream.readByte();
            if (bytes.size() > 0) {
                if (bytes.lastElement() == 13 && b == 10) {
                    bytes.remove(bytes.lastElement());
                    break;
                }
            }
            bytes.add(b);
        }
        byte[] buffer = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            buffer[i] = bytes.elementAt(i);
        }
        return Integer.parseInt(new String(buffer),16);
    }

    private Integer getResponseResultCode() {
        String[] split = responseLine.split(" ");
        if (split.length == 3) {
            try {
                return Integer.parseInt(split[1]);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public byte[] getData() {
        return data.clone();
    }
}
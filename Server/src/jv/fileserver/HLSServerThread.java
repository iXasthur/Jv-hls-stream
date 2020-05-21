package jv.fileserver;

import jv.http.HTTPRequest;
import jv.http.HTTPResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HLSServerThread extends Thread {

    private final Socket socket;
    private final String serverFilesFolderPath;

    public HLSServerThread(Socket socket, String severFilesFolderPath) {
        this.socket = socket;
        this.serverFilesFolderPath = severFilesFolderPath;
    }

    @Override
    public void run() {
        super.run();

        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())){

            HTTPRequest request = new HTTPRequest(inputStream);
            request.outputRequest();
            System.out.println();
            HTTPResponse response = new APIHandler(request, serverFilesFolderPath).getResponse();
            response.send(outputStream);

        } catch (IOException e) {
            // Everything will be closed by try-with-resources
            // e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
import jv.fileserver.FileServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        // Set 8080 as default value in IDE

        if (args.length != 2) {
            System.out.println("Invalid args. Args: <port> <files directory name>");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid args. Args: <port> <files directory name>");
            return;
        }

        final String serverFilesFolderPathString;
        try {
            serverFilesFolderPathString = new java.io.File(args[1]).getCanonicalPath().replace("\\", "/");
            Path serverFilesFolderPath = Paths.get(serverFilesFolderPathString);
            if (Files.exists(serverFilesFolderPath)) {
                if (!Files.isDirectory(serverFilesFolderPath)) {
                    throw new Exception(args[1] + " is not directory");
                }
            } else {
                throw new Exception(args[1] + " does not exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Invalid args. Args: <port> <files directory name>");
            return;
        }
        System.out.println("Server files folder: " + serverFilesFolderPathString);

        // The Java runtime automatically closes the input and output streams, the client socket,
        // and the server socket because they have been created in the try-with-resources statement.
        // + JVM/OS will close everything on exit
        try (ServerSocket server = new ServerSocket(port)) {

            System.out.println("Created file server on localhost:" + port);

            while (true) {
                Socket socket = server.accept();

                // For testing in browser
                // https://www.hlstester.com/
                // http://inspectstream.theoplayer.com/

                FileServerThread thread = new FileServerThread(socket, serverFilesFolderPathString);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

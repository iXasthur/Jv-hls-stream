package jv.hlsclient;

import javafx.event.EventHandler;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import jv.http.HTTPRequest;
import jv.http.HTTPResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;

public class HLSClient {

    // Root folder of the server must contain folders with .m3u8 file + .ts files
    // name of folder == name of video
    // every folder in root will be recognized as video folder
    // directory name must not contain spaces

    final public URL domain; // "http://localhost:8080/
                                                     // test4/index.m3u8";
    final private String playlistFileName = "index.m3u8";
    final private Vector<String> videoFolderNames = new Vector<>(0);

    public HLSClient(URL domain) throws Exception {
        this.domain = domain;

        try {
            getAvailableVideos();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void getAvailableVideos() throws Exception {
        videoFolderNames.clear();

        String host = domain.getHost();
        int port = domain.getPort();
        if (port == -1) {
            port = 80;
        }

        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new Exception("Unable to connect to " + domain.toString());
        }

        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("GET");
        requestStringBuilder.append(" ");
        requestStringBuilder.append("/");
        requestStringBuilder.append(" ");
        requestStringBuilder.append("HTTP/1.1");
        requestStringBuilder.append("\r\n");
        requestStringBuilder.append("Host");
        requestStringBuilder.append(":");
        requestStringBuilder.append(" ");
        requestStringBuilder.append(host);
        requestStringBuilder.append(":");
        requestStringBuilder.append(port);
        requestStringBuilder.append("\r\n");
        requestStringBuilder.append("\r\n");

        InputStream is = new ByteArrayInputStream(requestStringBuilder.toString().getBytes());
        DataInputStream dis = new DataInputStream(is);
        HTTPRequest request = null;
        try {
            request = new HTTPRequest(dis);
        } catch (IOException e) {
            throw new Exception("Unable to create request");
        }

        byte[] data = null;

        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())){

            request.send(outputStream);
            HTTPResponse response = new HTTPResponse(inputStream);
            data = response.getData();

        } catch (Exception e) {
            throw new Exception("Can't send data to " + host + ":" + port);
        }

        try {
            fillPlaylistVector(data);
        } catch (Exception e) {
            throw new Exception("Received invalid XML data");
        }
    }

//    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
//    <root path="/">
//      <file>ffmpeg_script.txt</file>
//      <dir>test1</dir>
//      <dir>test2</dir>
//      <dir>test3</dir>
//      <dir>test4</dir>
//    </root>
    private void fillPlaylistVector(byte[] data) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(data));

        System.out.println(new String(data));

        Element root = document.getDocumentElement();
        NodeList videoFolders = root.getElementsByTagName("dir");
        for (int i = 0; i < videoFolders.getLength(); i++) {
            videoFolderNames.add(videoFolders.item(i).getTextContent());
        }
    }

    public Vector<String> getVideoFolderNames() {
        return videoFolderNames;
    }

    public HLSMedia getMedia(String playlistDirectory) {
        try {
            getAvailableVideos();
        } catch (Exception e) {
            return null;
        }
        if (!videoFolderNames.contains(playlistDirectory)) {
            return null;
        }

        String source = domain + playlistDirectory + "/" + playlistFileName;
        Media media;
        MediaPlayer mediaPlayer;
        MediaView mediaView;
        try {
            media = new Media(source);
            if (media.getError() == null) {
                media.setOnError(new Runnable() {
                    public void run() {
                        // Handle asynchronous error in Media object.
                    }
                });
                try {
                    mediaPlayer = new MediaPlayer(media);
                    if (mediaPlayer.getError() == null) {
                        mediaPlayer.setOnError(new Runnable() {
                            public void run() {
                                // Handle asynchronous error in MediaPlayer object.
                            }
                        });
                        mediaView = new MediaView(mediaPlayer);
                        mediaView.setOnError(new EventHandler<MediaErrorEvent>() {
                            public void handle(MediaErrorEvent t) {
                                // Handle asynchronous error in MediaView.
                            }
                        });

                        HLSMedia retMedia = new HLSMedia();
                        retMedia.media = media;
                        retMedia.mediaPlayer = mediaPlayer;
                        retMedia.mediaView = mediaView;

                        return retMedia;
                    } else {
                        // Handle synchronous error creating MediaPlayer.
                    }
                } catch (Exception mediaPlayerException) {
                    // Handle exception in MediaPlayer constructor.
                }
            } else {
                // Handle synchronous error creating Media.
            }
        } catch (Exception mediaException) {
            // Handle exception in Media constructor.
        }

        return null;
    }

}

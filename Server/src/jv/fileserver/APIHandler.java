package jv.fileserver;

import jv.http.HTTPContentType;
import jv.http.HTTPRequest;
import jv.http.HTTPResponse;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class APIHandler {

    private final String serverFilesFolderPath;

    private final HTTPRequest request;
    private HTTPResponse response = new HTTPResponse(501);

    public APIHandler(HTTPRequest request, String serverFilesFolderPath) {
        this.request = request;
        this.serverFilesFolderPath = serverFilesFolderPath;

        parseRequest();
    }

    public String getDirectoryStructureXML(String relativePath) throws IOException, ParserConfigurationException, TransformerException {
        String filePathString = serverFilesFolderPath + request.getRelativePath();
        Path filePath = Paths.get(filePathString);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.newDocument();

        Element rootElement = document.createElement("root");
        document.appendChild(rootElement);

        Attr pathAttribute = document.createAttribute("path");
        pathAttribute.setValue(relativePath);
        rootElement.setAttributeNode(pathAttribute);

        Files.list(filePath)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        Element element = document.createElement("file");
                        element.appendChild(document.createTextNode(String.valueOf(path.getFileName())));
                        rootElement.appendChild(element);
                    } else if (Files.isDirectory(path)) {
                        Element element = document.createElement("dir");
                        element.appendChild(document.createTextNode(String.valueOf(path.getFileName())));
                        rootElement.appendChild(element);
                    }
                });


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.getBuffer().toString();
    }

    private String extractFileExtension(String fileName) {
        String fileExtension = "";
        if (fileName.contains(".")) {
            int index = fileName.lastIndexOf(".");
            if (index < fileName.length() -1) {
                fileExtension = fileName.substring(index + 1);
            }
        }
        return fileExtension;
    }

    synchronized public void parseRequest() {
        String method = request.getRequestMethod();
        String filePathString = serverFilesFolderPath + request.getRelativePath();
        Path filePath = Paths.get(filePathString);

        switch (method) {
            case "GET": {

                // For downloading files
                // + to get available videos

                if (Files.exists(filePath)) {
                    // Send file
                    if (Files.isRegularFile(filePath)) {
                        response = new HTTPResponse(500);

                        try {
                            byte[] bytes = Files.readAllBytes(filePath);
                            String fileName = filePath.getFileName().toString();
                            String fileExtension = extractFileExtension(fileName);

                            response = new HTTPResponse(200);
                            response.setData(bytes, fileExtension);

                            response.addHeader("Access-Control-Allow-Origin", "*");
                            response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
                            response.addHeader("Access-Control-Allow-Credentials", "true");
                            response.addHeader("Access-Control-Allow-Methods", "GET");

                        } catch (IOException e) {
//                            e.printStackTrace();
                        }
                    }
                } else {
                    response = new HTTPResponse(404);
                    response.setData("File does not exist".getBytes(), "txt");
                }
                break;
            }
        }
        response.addHeader("Connection", "Closed");
        response.addHeader("Server", "Jv-file-server");
    }

    public HTTPResponse getResponse() {
        return response;
    }

}

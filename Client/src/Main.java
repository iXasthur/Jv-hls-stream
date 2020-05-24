import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import jv.hlsclient.HLSClient;
import jv.http.HTTPRequest;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends Application {

    //        root.getChildren().add(mediaView);
//        mediaPlayer.play();

    private final Color backgroundColor = Color.rgb(24,24,24);
    private final Color textColor = Color.rgb(253,216,53);

    private Group root = null;
    private HLSClient client = null;

    @Override
    public void start(Stage primaryStage) throws Exception{

        primaryStage.setTitle("^_^");

        final double windowSizeFactor = 1.0f/3.0f;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sceneSize = new Dimension();
        sceneSize.setSize(screenSize.width*windowSizeFactor, screenSize.height*windowSizeFactor);

        System.out.println("Screen size:\n" + screenSize);
        System.out.println("Scene size:\n" + sceneSize);

        root = new Group();
        Scene scene = new Scene(root, sceneSize.getWidth(), sceneSize.getHeight(), backgroundColor);
        primaryStage.setMinHeight(scene.getHeight());
        primaryStage.setMinWidth(scene.getWidth());
        primaryStage.setHeight(primaryStage.getMinHeight());
        primaryStage.setWidth(primaryStage.getMinWidth());
        primaryStage.setScene(scene);

        createUI(scene);

        primaryStage.show();
    }

    private void createUI(Scene scene) {
        double sceneWidth = scene.getWidth();
        double sceneHeight = scene.getHeight();
        double fontSize = sceneHeight/26;

        TextField inputAddressTextField = new TextField();
        inputAddressTextField.setPromptText("Address");
        inputAddressTextField.setText("localhost:8080");

        Label connectionResultLabel = new Label("");
        connectionResultLabel.setTextFill(textColor);
        connectionResultLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize/3*2));
        connectionResultLabel.setTextAlignment(TextAlignment.CENTER);

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(action -> {
            if (client == null) {
                try {
                    connectionResultLabel.setText("Connecting...");
                    connect(inputAddressTextField.getText());
                    connectionResultLabel.setText("Successfully connected");
                    inputAddressTextField.setEditable(false);
                    connectButton.setText("Disconnect");
                } catch (Exception e) {
                    connectionResultLabel.setText(e.getMessage());
                }
            } else {
//                String playlistFileName = "index.m3u8";
                client = null;
                connectButton.setText("Connect");
                connectionResultLabel.setText("");
                inputAddressTextField.setText("");
                inputAddressTextField.setEditable(true);
            }
        });

        HBox hbox = new HBox(inputAddressTextField, connectButton);
        hbox.setSpacing(5);

        Label inputAddressLabel = new Label("Input server address");
        inputAddressLabel.setTextFill(textColor);
        inputAddressLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        VBox vBox = new VBox(inputAddressLabel, hbox, connectionResultLabel);

        vBox.setLayoutX(sceneHeight/20);
        vBox.setLayoutY(sceneHeight/20);

        vBox.setSpacing(10);

        root.getChildren().add(vBox);
    }

    private void connect(String address) throws Exception {
        if (!address.contains("/") && !address.isEmpty()) {
            address = "http://" + address + "/";
            System.out.println("Connecting to " + address);

            URL domain = null;
            try {
                domain = new URL(address);
            } catch (MalformedURLException e) {
                throw new Exception("Invalid address string");
            }

            try {
                client = new HLSClient(domain);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        } else {
            throw new Exception("Invalid address string");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

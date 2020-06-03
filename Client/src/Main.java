import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jv.hlsclient.HLSClient;
import jv.hlsclient.HLSMedia;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class Main extends Application {

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

        Vector<Button> videoButtons = new Vector<>(0);

        Button connectButton = new Button("Connect");

        HBox hBox = new HBox(inputAddressTextField, connectButton);
        hBox.setSpacing(5);

        Label inputAddressLabel = new Label("Input server address");
        inputAddressLabel.setTextFill(textColor);
        inputAddressLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        VBox vBox = new VBox(inputAddressLabel, hBox, connectionResultLabel);

        vBox.setLayoutX(sceneHeight/20);
        vBox.setLayoutY(sceneHeight/20);

        vBox.setSpacing(10);

        root.getChildren().add(vBox);


        // Actions
        connectButton.setOnAction(action -> {
            if (client == null) {
                try {
                    connectionResultLabel.setText("Connecting...");
                    connect(inputAddressTextField.getText());
                    connectionResultLabel.setText("Successfully connected");
                    inputAddressTextField.setDisable(true);
                    connectButton.setText("Reset");

                    Vector<String> videoFolders = client.getVideoFolderNames();
                    for (int i = 0; i < videoFolders.size(); i++) {
                        Button button = new Button(videoFolders.elementAt(i));
                        videoButtons.add(button);

                        button.setOnAction(actionEvent -> {
                            String videoName = button.getText();
                            HLSMedia hlsMedia = client.getMedia(videoName);
                            if (hlsMedia != null) {
                                Stage videoStage = new Stage();
                                videoStage.setTitle(videoName);
                                VBox videoBox = new VBox(hlsMedia.mediaView);
                                videoBox.setAlignment(Pos.CENTER);
                                videoBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                                Scene videoScene = new Scene(videoBox, sceneWidth, sceneHeight, backgroundColor);
                                videoStage.setScene(videoScene);

                                hlsMedia.mediaView.getParent().layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                                        if (hlsMedia.mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                                            hlsMedia.mediaView.setFitHeight(newValue.getHeight());
                                            hlsMedia.mediaView.setFitWidth(newValue.getWidth());
                                        }
                                    }
                                });

                                hlsMedia.mediaPlayer.setOnEndOfMedia(() -> {
                                    hlsMedia.mediaPlayer.dispose();

                                    videoBox.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

                                    Label label = new Label("Video ended");
                                    label.setTextFill(textColor);
                                    label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize*2));

                                    videoBox.getChildren().remove(hlsMedia.mediaView);
                                    videoBox.getChildren().add(label);
                                });
                                videoStage.setMinWidth(sceneWidth/2.0);
                                videoStage.setMinHeight(sceneHeight/2.0);

                                videoStage.setWidth(sceneWidth);
                                videoStage.setHeight(sceneHeight);

                                videoStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                                                 @Override
                                                                 public void handle(WindowEvent windowEvent) {
                                                                     hlsMedia.mediaPlayer.dispose();
                                                                 }
                                                             }
                                );

                                videoStage.show();

                                hlsMedia.mediaPlayer.play();
                            } else {
                                button.setDisable(true);
                            }
                        });
                    }

                    vBox.getChildren().addAll(videoButtons);
                } catch (Exception e) {
                    connectionResultLabel.setText(e.getMessage());
                }
            } else {
                vBox.getChildren().removeAll(videoButtons);
                videoButtons.clear();

                client = null;
                connectButton.setText("Connect");
                connectionResultLabel.setText("");
//                inputAddressTextField.setText("");
                inputAddressTextField.setDisable(false);
            }
        });
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

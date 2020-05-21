package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Group root = new Group();
        primaryStage.setTitle("^_^");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        String source = "http://localhost:8080/test4/index.m3u8";
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

                        root.getChildren().add(mediaView);
                        mediaPlayer.play();

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
    }


    public static void main(String[] args) {
        launch(args);
    }
}

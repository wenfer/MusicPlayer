package app.fxplayer;

import app.fxplayer.source.MusicSource;
import app.fxplayer.util.Resources;
import app.fxplayer.views.ImportMusicDialogController;
import app.fxplayer.views.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Timer;

@Slf4j
public class Bootstrap extends Application {

    private Timer timer;

    private int timerCounter;

    private int secondsPlayed;

    @Getter
    private static MainController mainController;
    /**
     * main window
     */
    @Getter
    private static Stage stage;

    private NewPlayer newPlayer;


    public static void main(String[] args) {
        log.error("开始启动");
        Application.launch(Bootstrap.class);
    }


    private void initializePlayer() {
        MusicSource musicSource = AppConfig.getInstance().getMusicSource();
        if (musicSource == null) {
            //
            log.info("未配置服务器源");
            createSource();
        }
    }


    private void createSource() {
        try {
            FXMLLoader loader = new FXMLLoader(MusicPlayer.class.getResource(Resources.FXML + "ImportMusicDialog.fxml"));
            BorderPane importView = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Music Player Configuration");
            // Forces user to focus on dialog.
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Sets minimal decorations for dialog.
            //dialogStage.initStyle(StageStyle.UTILITY);
            // Prevents the alert from being re-sizable.
            dialogStage.setResizable(false);
            dialogStage.initOwner(stage);
            dialogStage.centerOnScreen();
            // Sets the import music dialog scene in the stage.
            dialogStage.setScene(new Scene(importView));

            // Set the dialog into the controller.
            ImportMusicDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Show the dialog and wait until the user closes it.
            dialogStage.showAndWait();

            // Checks if the music was imported successfully. Closes the application otherwise.
            boolean musicImported = controller.isMusicImported();
            if (!musicImported) {
                System.exit(0);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("error", e);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        timer = new Timer();
        timerCounter = 0;
        secondsPlayed = 0;
        this.stage = stage;
        stage.setTitle("Music Player");
        stage.getIcons().add(new Image(this.getClass().getResource(Resources.IMG + "Icon.png").toString()));
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        try {
            // Load main layout from fxml file.
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "SplashScreen.fxml"));
            VBox view = loader.load();

            // Shows the scene containing the layout.
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();

            initializePlayer();

            // Calls the function to check in the library.xml file exists. If it does not, the file is created.
            //checkLibraryXML();
        } catch (Exception ex) {
            log.error("启动失败", ex);
            System.exit(0);
        }
        this.newPlayer = NewPlayer.getInstance();


        Thread thread = new Thread(() -> {
            NewPlayer newPlayer = NewPlayer.getInstance();
            newPlayer.initializeList();
            //nowPlaying.setPlaying(true);
            timer = new Timer();
            timerCounter = 0;
            secondsPlayed = 0;
            //newPlayer.play();
/*            File imgFolder = new File(Resources.JAR + "/img");
            if (!imgFolder.exists()) {

                Thread thread1 = new Thread(() -> {
                    Library.getArtists().forEach(Artist::downloadArtistImage);
                });

                Thread thread2 = new Thread(() -> {
                    Library.getAlbums().forEach(Album::downloadArtwork);
                });

                thread1.start();
                thread2.start();
            }*/

/*            new Thread(() -> {
                XMLEditor.getNewSongs().forEach(song -> {
                    try {
                        Library.getArtist(song.getArtist()).downloadArtistImage();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }).start();*/

            // Calls the function to initialize the main layout.
            Platform.runLater(this::initMain);
        });

        thread.start();
    }


    private void initMain() {
        try {
            // Load main layout from fxml file.
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "Main.fxml"));
            BorderPane view = loader.load();
            mainController = loader.getController();

            // Shows the scene containing the layout.
            double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();

            view.setPrefWidth(width);
            view.setPrefHeight(height);

            Scene scene = new Scene(view);
            stage.setScene(scene);

            // Gives the controller access to the music player main application.


            //mediaPlayer.volumeProperty().bind(mainController.getVolumeSlider().valueProperty().divide(200));

        } catch (Exception ex) {
            //ex.printStackTrace();
            log.error("error ", ex);
        }
    }
}

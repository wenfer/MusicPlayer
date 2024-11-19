package app.fxplayer;

import app.fxplayer.model.*;
import app.fxplayer.source.MusicSource;
import app.fxplayer.views.ImportMusicDialogController;
import app.fxplayer.views.MainController;
import app.fxplayer.util.Resources;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

import static app.fxplayer.DbConstants.SOURCE_SERVER_URL;

@Slf4j
public class MusicPlayer{


    private MusicSource musicSource;

    /**
     * -- GETTER --
     *  Gets main controller object.
     *
     * @return MainController
     */
    @Getter
    private MainController mainController;
    private static MediaPlayer mediaPlayer;
    private static int nowPlayingIndex;
    @Getter
    private static Song nowPlaying;
    private static Timer timer;
    private static int timerCounter;
    private static int secondsPlayed;
    private static boolean isLoopActive = false;
    private static boolean isShuffleActive = false;
    private static boolean isMuted = false;
    @Setter
    @Getter
    private static Object draggedItem;


    private MusicPlayer musicPlayer;

    // Stores the number of files in library.xml.
    // This will then be compared to the number of files in the music directory when starting up the application to
    // determine if the xml file needs to be updated by adding or deleting songs.
    private static int xmlFileNum;

    // Stores the last id that was assigned to a song.
    // This is important when adding new songs after others have been deleted because the last id assigned
    // may not necessarily be equal to the number of songs in the xml file if songs have been deleted.
    @Setter
    @Getter
    private static int lastIdAssigned;



/*    private static class TimeUpdater extends TimerTask {
        private int length = (int) getNowPlaying().getLengthInSeconds() * 4;

        @Override
        public void run() {
            Platform.runLater(() -> {
                if (timerCounter < length) {
                    if (++timerCounter % 4 == 0) {
                        mainController.updateTimeLabels();
                        secondsPlayed++;
                    }
                    if (!mainController.isTimeSliderPressed()) {
                        mainController.updateTimeSlider();
                    }
                }
            });
        }
    }*/

    /**
     * Plays selected song.
     */
/*
    public static void play() {
        if (mediaPlayer != null && !isPlaying()) {
            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
            mainController.updatePlayPauseIcon(true);
        }
    }
*/

    /**
     * Checks if a song is playing.
     */
    public static boolean isPlaying() {
        return mediaPlayer != null && MediaPlayer.Status.PLAYING.equals(mediaPlayer.getStatus());
    }

    /**
     * Pauses selected song.
     */
/*    public static void pause() {
        if (isPlaying()) {
            mediaPlayer.pause();
            timer.cancel();
            timer = new Timer();
            mainController.updatePlayPauseIcon(false);
        }
    }*/

/*    public static void seek(int seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(seconds * 1000));
            timerCounter = seconds * 4;
            mainController.updateTimeLabels();
        }
    }*/

    /**
     * Skips song.
     */
//    public static void skip() {
//        if (nowPlayingIndex < nowPlayingList.size() - 1) {
//            boolean isPlaying = isPlaying();
//            mainController.updatePlayPauseIcon(isPlaying);
//            setNowPlaying(nowPlayingList.get(nowPlayingIndex + 1));
//            if (isPlaying) {
//                play();
//            }
//        } else if (isLoopActive) {
//            boolean isPlaying = isPlaying();
//            mainController.updatePlayPauseIcon(isPlaying);
//            nowPlayingIndex = 0;
//            setNowPlaying(nowPlayingList.get(nowPlayingIndex));
//            if (isPlaying) {
//                play();
//            }
//        } else {
//            mainController.updatePlayPauseIcon(false);
//            nowPlayingIndex = 0;
//            setNowPlaying(nowPlayingList.get(nowPlayingIndex));
//        }
//    }

//    public static void back() {
//        if (timerCounter > 20 || nowPlayingIndex == 0) {
//            mainController.initializeTimeSlider();
//            seek(0);
//        } else {
//            boolean isPlaying = isPlaying();
//            setNowPlaying(nowPlayingList.get(nowPlayingIndex - 1));
//            if (isPlaying) {
//                play();
//            }
//        }
//    }

    public static void mute(boolean isMuted) {
        MusicPlayer.isMuted = !isMuted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(!isMuted);
        }
    }

    public static void toggleLoop() {
        isLoopActive = !isLoopActive;
    }

    public static boolean isLoopActive() {
        return isLoopActive;
    }

//    public static void toggleShuffle() {
//
//        isShuffleActive = !isShuffleActive;
//
//        if (isShuffleActive) {
//            Collections.shuffle(nowPlayingList);
//        } else {
//            Collections.sort(nowPlayingList, (first, second) -> {
//                int result = Library.getAlbum(first.getAlbum()).compareTo(Library.getAlbum(second.getAlbum()));
//                if (result != 0) {
//                    return result;
//                }
//                result = Library.getAlbum(first.getAlbum()).compareTo(Library.getAlbum(second.getAlbum()));
//                if (result != 0) {
//                    return result;
//                }
//                result = first.compareTo(second);
//                return result;
//            });
//        }
//
//        nowPlayingIndex = nowPlayingList.indexOf(nowPlaying);
//
//        if (mainController.getSubViewController() instanceof NowPlayingController) {
//            mainController.loadView("nowPlaying");
//        }
//    }

    public static boolean isShuffleActive() {
        return isShuffleActive;
    }

    /**
     * Gets currently playing song list.
     *
     * @return arraylist of now playing songs
     */
//    public static ArrayList<Song> getNowPlayingList() {
//        return nowPlayingList == null ? new ArrayList<>() : new ArrayList<>(nowPlayingList);
//    }
//
//    public static void addSongToNowPlayingList(Song song) {
//        if (!nowPlayingList.contains(song)) {
//            nowPlayingList.add(song);
//            Library.savePlayingList();
//        }
//    }

//    public static void setNowPlayingList(List<Song> list) {
//        nowPlayingList = new ArrayList<>(list);
//        Library.savePlayingList();
//    }
//
//    public static void setNowPlaying(Song song) {
//        if (nowPlayingList.contains(song)) {
//
//            updatePlayCount();
//            nowPlayingIndex = nowPlayingList.indexOf(song);
//            if (nowPlaying != null) {
//                nowPlaying.setPlaying(false);
//            }
//            nowPlaying = song;
//            nowPlaying.setPlaying(true);
//            if (mediaPlayer != null) {
//                mediaPlayer.stop();
//            }
//            if (timer != null) {
//                timer.cancel();
//            }
//            timer = new Timer();
//            timerCounter = 0;
//            secondsPlayed = 0;
//            String path = song.getLocation();
//            Media media = new Media(Paths.get(path).toUri().toString());
//            mediaPlayer = new MediaPlayer(media);
//            mediaPlayer.volumeProperty().bind(mainController.getVolumeSlider().valueProperty().divide(200));
//            mediaPlayer.setOnEndOfMedia(new SongSkipper());
//            mediaPlayer.setMute(isMuted);
//            mainController.updateNowPlayingButton();
//            mainController.initializeTimeSlider();
//            mainController.initializeTimeLabels();
//        }
//    }

    private static void updatePlayCount() {
//        if (nowPlaying != null) {
//            int length = (int) nowPlaying.getLengthInSeconds();
//            if ((100 * secondsPlayed / length) > 50) {
//                nowPlaying.played();
//            }
//        }
    }

    public static String getTimePassed() {
        int secondsPassed = timerCounter / 4;
        int minutes = secondsPassed / 60;
        int seconds = secondsPassed % 60;
        return Integer.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Integer.toString(seconds));
    }

    public static String getTimeRemaining() {
        long secondsPassed = timerCounter / 4;
        //long totalSeconds = getNowPlaying().getLengthInSeconds();
        //long secondsRemaining = totalSeconds - secondsPassed;
        //long minutes = secondsRemaining / 60;
        //long seconds = secondsRemaining % 60;
        //return Long.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
        return "10:00";
    }

    public static int getXMLFileNum() {
        return xmlFileNum;
    }

    public static void setXMLFileNum(int i) {
        xmlFileNum = i;
    }

}

package app.fxplayer;

import app.fxplayer.enums.PlayMode;
import app.fxplayer.model.Album;
import app.fxplayer.model.Playlist;
import app.fxplayer.model.Song;
import app.fxplayer.views.MainController;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NewPlayer {

    private static NewPlayer instance;

    /**
     * 随机播放
     */
    private PlayMode mode = PlayMode.NORMAL;

    @Getter
    private List<Song> nowPlayingList = new ArrayList<>();

    private List<Integer> shuffleSequence = null;

    private final AppConfig appConfig = AppConfig.getInstance();

    private int nowPlayingIndex = 0;

    private MediaPlayer mediaPlayer;

    private int timerCounter;

    private Timer timer;

    private final ExecutorService downloadThread = Executors.newSingleThreadExecutor();

    //private final ExecutorService playThread = Executors.newSingleThreadExecutor();

    private boolean mute = false;
    private DownloadTask downloadTask;

    private int secondsPlayed;


    private NewPlayer() {
    }


    public static NewPlayer getInstance() {
        if (instance == null) {
            instance = new NewPlayer();
        }
        return instance;
    }


    public List<Playlist> getPlaylists() {
        return List.of();
    }

    public void play(Song song) {
        if (!this.nowPlayingList.isEmpty()) {
            int i = this.nowPlayingList.indexOf(song);
            if (i > -1) {
                this.nowPlayingIndex = i;
                this.play();
                return;
            }
        }
        this.nowPlayingList.add(song);
        if (this.mode == PlayMode.SHUFFLE) {
            this.shuffleSequence.add(this.nowPlayingList.size());
            Collections.shuffle(this.shuffleSequence);
            this.nowPlayingIndex = shuffleSequence.indexOf(this.nowPlayingList.size() - 1);
        } else {
            this.nowPlayingIndex = this.nowPlayingList.size() - 1;
        }
        this.play();
    }

    public void pause() {
        if (isPlaying()) {
            mediaPlayer.pause();
            timer.cancel();
            timer = new Timer();
            Bootstrap.getMainController().updatePlayPauseIcon(false);
        }
    }

    public Song nowPlaying() {
        return nowPlayingList.get(nowPlayingIndex);
    }

    public void initializeList() {
        if (this.nowPlayingList.isEmpty()) {
            for (Album album : appConfig.getMusicSource().listAlbums()) {
                log.info("开始获取:{}的歌曲", album.getTitle());
                nowPlayingList.addAll(appConfig.getMusicSource().listByAlbum(album));
            }
            nowPlayingList.sort((first, second) -> {
                Album firstAlbum = first.getAlbumObj();
                Album secondAlbum = second.getAlbumObj();
                if (firstAlbum.compareTo(secondAlbum) != 0) {
                    return firstAlbum.compareTo(secondAlbum);
                } else {
                    return first.compareTo(second);
                }
            });
        }

    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public void skip() {
        MainController mainController = Bootstrap.getMainController();
        Song nowPlaying = getNowPlaying();
        if (this.mode != PlayMode.REPEAT) {
            nowPlaying.setPlaying(false);
            if (nowPlayingIndex < nowPlayingList.size() - 1) {
                boolean isPlaying = isPlaying();
                mainController.updatePlayPauseIcon(isPlaying);
                this.nowPlayingIndex = nowPlayingIndex + 1;
            } else {
                mainController.updatePlayPauseIcon(false);
                nowPlayingIndex = 0;
            }
        }
        play();
    }

    public Song getNowPlaying() {
        if (this.nowPlayingList.isEmpty()) {
            return null;
        }
        int index = this.mode == PlayMode.SHUFFLE ? this.shuffleSequence.get(nowPlayingIndex) : nowPlayingIndex;
        return this.nowPlayingList.get(index);
    }

    /**
     * play函数，只播放 nowPlayingIndex 对应的歌曲
     */
    public void play() {
        if (this.nowPlayingList.isEmpty()) {
            throw new RuntimeException("播放列表为空");
        }
        if (isPlaying()) {
            mediaPlayer.stop();
        }
        if (this.downloadTask != null) {
            this.downloadTask.stop();
        }
        MainController mainController = Bootstrap.getMainController();
        Song song = getNowPlaying();
        String uri = getOrCache(song);
        Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        song.setPlaying(true);
        mediaPlayer.setOnEndOfMedia(this::skip);
        mediaPlayer.volumeProperty().bind(mainController.getVolumeSlider().valueProperty().divide(200));
        mediaPlayer.setMute(this.mute);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
        mainController.updatePlayPauseIcon(true);
    }

    private String getOrCache(Song song) {
        Path path = Paths.get(FileUtil.getTmpDirPath(), "fx_ player", "cache_music", song.getId() + ".mp3");
        File tempMusic = path.toFile();
        if (tempMusic.exists()) {
            log.info("使用缓存的音乐文件播放");
            return tempMusic.toURI().toString();
        }
        File tempFile = FileUtil.createTempFile();
        this.downloadTask = new DownloadTask(song, appConfig.getMusicSource(), tempFile);
        CompletableFuture.runAsync(downloadTask).thenRun(() -> {
            if (this.downloadTask.isFinish()) {
                log.info("缓存完成，文件移动到:{}", tempMusic.getAbsolutePath());
                FileUtil.move(tempFile, tempMusic, true);
            } else {
                FileUtil.del(tempFile);
            }
            this.downloadTask = null;
        });
        while (this.downloadTask.getDownloaded() < 50_000) {
            ThreadUtil.sleep(100);
        }
        return tempFile.toURI().toString();
    }

    public void seek(int seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(seconds * 1000));
            timerCounter = seconds * 4;
            Bootstrap.getMainController().updateTimeLabels();
        }
    }

    public void setNowPlayingList(List<Song> songs) {
        this.nowPlayingList.clear();
        this.nowPlayingIndex = 0;
        this.nowPlayingList = new ArrayList<>(songs);
    }

    /**
     * 随机
     */
    public void toggleShuffle() {
        this.mode = PlayMode.SHUFFLE;
        this.shuffleSequence = new ArrayList<>();
        for (int i = 0; i < getNowPlayingList().size(); i++) {
            shuffleSequence.add(i);
        }
        Collections.shuffle(shuffleSequence); // 随机打乱下标列表
        nowPlayingIndex = 0;
    }

    public boolean isShuffleActive() {
        return this.mode == PlayMode.SHUFFLE;
    }

    public String getTimePassed() {
        int secondsPassed = timerCounter / 4;
        int minutes = secondsPassed / 60;
        int seconds = secondsPassed % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : Integer.toString(seconds));
    }

    public String getTimeRemaining() {
        long secondsPassed = timerCounter / 4;
        long totalSeconds = getNowPlaying().getLengthInSeconds();
        long secondsRemaining = totalSeconds - secondsPassed;
        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
    }

    public void toggleLoop() {
        this.mode = PlayMode.LOOP;
    }

    public boolean isLoopActive() {
        return this.mode == PlayMode.LOOP;
    }

    public void mute(boolean isMuted) {
        this.mute = isMuted;
    }

    public void back() {

    }

    private static class TimeUpdater extends TimerTask {


        private final int length = (int) NewPlayer.getInstance().nowPlaying().getLengthInSeconds() * 4;

        @Override
        public void run() {
            Platform.runLater(() -> {
                if (NewPlayer.getInstance().timerCounter < length) {
                    if (++NewPlayer.getInstance().timerCounter % 4 == 0) {
                        Bootstrap.getMainController().updateTimeLabels();
                        NewPlayer.getInstance().secondsPlayed++;
                    }
                    if (!Bootstrap.getMainController().isTimeSliderPressed()) {
                        Bootstrap.getMainController().updateTimeSlider();
                    }
                }
            });
        }
    }
}

package app.fxplayer;

import app.fxplayer.model.Album;
import app.fxplayer.model.Playlist;
import app.fxplayer.model.Song;
import cn.hutool.core.io.FileUtil;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NewPlayer {

    private static NewPlayer instance;

    @Getter
    private final List<Song> nowPlayingList = new ArrayList<>();

    private final AppConfig appConfig = AppConfig.getInstance();

    private int nowPlayingIndex = 0;

    private MediaPlayer mediaPlayer;
    private final ExecutorService downloadThread = Executors.newSingleThreadExecutor();
    private final ExecutorService playThread = Executors.newSingleThreadExecutor();


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
        this.nowPlayingList.add(song);
        this.nowPlayingIndex = this.nowPlayingList.size() + 1;
        this.playAndCache(song);
    }

    public void play() {
        if (this.nowPlayingList.isEmpty()) {
            throw new RuntimeException("播放列表为空");
        }
        this.playAndCache(this.nowPlayingList.get(this.nowPlayingIndex));
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


    private void playAndCache(Song song) {

        downloadThread.execute(() -> {
            InputStream stream = null;
            try {
                song.setPlaying(true);
                stream = appConfig.getMusicSource().stream(song);
                File tempFile = FileUtil.createTempFile();
                // 获取输入流
                InputStream inputStream = new BufferedInputStream(stream);
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                log.info("开始播放音乐:{}  缓存路径:{}", song.getTitle(), tempFile.getAbsolutePath());
                byte[] buffer = new byte[4096];
                int bytesRead;
                boolean startedPlaying = false;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // 写入本地文件
                    outputStream.write(buffer, 0, bytesRead);

                    // 如果文件达到一定大小，就开始播放
                    if (!startedPlaying && tempFile.length() > 100_000) { // 例如 100 KB
                        startedPlaying = true;
                        playThread.execute(() -> {
                            Media media = new Media(tempFile.toURI().toString());
                            mediaPlayer = new MediaPlayer(media);
                            mediaPlayer.setAutoPlay(true);
                            mediaPlayer.setOnEndOfMedia(() -> {
                                log.info("播放结束");
                            });
                        });
                    }
                }

                // 完成下载
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                log.error("播放失败", e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        log.info("关闭流失败");
                    }
                }
            }
        });

    }


}

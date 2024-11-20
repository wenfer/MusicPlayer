package app.fxplayer;

import app.fxplayer.model.Song;
import app.fxplayer.source.MusicSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Slf4j
public class DownloadTask implements Runnable {

    private final Song song;

    private final MusicSource musicSource;

    private final File targetFile;

    private boolean running = true;

    @Getter
    private long downloaded = 0;
    private boolean finished = false;

    public DownloadTask(Song song, MusicSource musicSource, File targetFile) {
        this.song = song;
        this.targetFile = targetFile;
        this.musicSource = musicSource;
    }

    public void stop() {
        this.running = false;
    }


    @Override
    public void run() {
        // 或者
        try {
            InputStream inputStream = new BufferedInputStream(musicSource.stream(song));
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            log.info("开始下载音乐:{}  缓存路径:{}", song.getTitle(), targetFile.getAbsolutePath());
            byte[] buffer = new byte[8192];
            int bytesRead;
            while (running && (bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                if (bytesRead != 8192) {
                    finished = true;
                }
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            log.error("播放失败", e);
        }
        log.info("下载任务结束 ：{}  ------- {}", this.downloaded, song.getSize());
    }

    public boolean isFinish() {
        return finished;
    }
}

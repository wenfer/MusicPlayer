package app.musicplayer;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class JavaFXBootstrap {

    public static void main(String[] args) {
        log.error("开始启动");
        Application.launch(MusicPlayer.class);
    }
}

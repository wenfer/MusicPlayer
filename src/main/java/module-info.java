module app.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires java.desktop;
    requires okhttp3;
    requires jaudiotagger;
    requires jintellitype;
    requires jlayer;
    requires java.logging;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires org.slf4j;

    opens app.musicplayer to javafx.fxml;
    opens app.musicplayer.model to javafx.base;
    opens app.musicplayer.views to javafx.fxml;
    exports app.musicplayer;
    exports app.musicplayer.views to javafx.fxml;

}
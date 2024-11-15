module app.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
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
    requires ormlite.jdbc;
    requires java.persistence;
    requires subsonic.client;
    requires java.sql;
    requires org.json;
    requires jdk.jdi;
    requires org.apache.commons.codec;
    requires org.xerial.sqlitejdbc;

    opens app.musicplayer to javafx.fxml;
    opens app.musicplayer.model to javafx.base, ormlite.jdbc;
    opens app.musicplayer.views to javafx.fxml;

    exports app.musicplayer;
    exports app.musicplayer.source;
    exports app.musicplayer.views to javafx.fxml;
    exports app.musicplayer.model to ormlite.jdbc;

}
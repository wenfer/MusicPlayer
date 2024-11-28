module app.musicplayer {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires static lombok;
    requires java.desktop;
    requires jintellitype;
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
    requires cn.hutool.core;
    requires pinyin4j;

    opens app.fxplayer to javafx.fxml;
    opens app.fxplayer.model to javafx.base, ormlite.jdbc;
    opens app.fxplayer.views to javafx.fxml;

    exports app.fxplayer;
    exports app.fxplayer.source;
    exports app.fxplayer.views to javafx.fxml;
    exports app.fxplayer.model to ormlite.jdbc;

}
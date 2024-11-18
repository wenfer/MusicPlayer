package app.fxplayer.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.fxplayer.util.Resources;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

@Getter
public final class Song implements Comparable<Song> {

    private static final Logger log = LoggerFactory.getLogger(Song.class);
    private final String id;
    private SimpleStringProperty title;
    private SimpleStringProperty artist;
    private SimpleStringProperty album;
    private SimpleStringProperty length;
    private SimpleStringProperty format;
    private long lengthInSeconds;
    private int trackNumber;
    private int discNumber;
    private SimpleIntegerProperty playCount;
    private SimpleBooleanProperty playing;
    private SimpleBooleanProperty selected;


    public Song(String id, String title, String artist, String album, int length,
                int trackNumber, int discNumber, int playCount) {

        if (album == null) {
            album = "Unknown Album";
        }

        if (artist == null) {
            artist = "Unknown Artist";
        }
        this.format = new SimpleStringProperty("mp3");
        this.id = id;
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.lengthInSeconds = length.getSeconds();
        long seconds = length.getSeconds() % 60;
        this.length = new SimpleStringProperty(length.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds));
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.playCount = new SimpleIntegerProperty(playCount);
        this.playing = new SimpleBooleanProperty(false);
        this.selected = new SimpleBooleanProperty(false);
    }


    public String getTitle() {
        return this.title.get();
    }

    public StringProperty titleProperty() {
        return this.title;
    }

    public String getArtist() {
        return this.artist.get();
    }

    public StringProperty artistProperty() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album.get();
    }

    public Image getArtwork() {
        return Library.getAlbum(this.album.get()).getArtwork();
    }

    public StringProperty albumProperty() {
        return this.album;
    }

    public String getLength() {
        return this.length.get();
    }

    public StringProperty lengthProperty() {
        return this.length;
    }


    public int getPlayCount() {
        return this.playCount.get();
    }

    public IntegerProperty playCountProperty() {
        return this.playCount;
    }

    public BooleanProperty playingProperty() {
        return this.playing;
    }

    public boolean getPlaying() {
        return this.playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public BooleanProperty selectedProperty() {
        return this.selected;
    }

    public boolean getSelected() {
        return this.selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public void played() {
        this.playCount.set(this.playCount.get() + 1);
    }

    @Override
    public int compareTo(Song other) throws NullPointerException {
        int discComparison = Integer.compare(this.discNumber, other.discNumber);

        if (discComparison != 0) {
            return discComparison;
        } else {
            return Integer.compare(this.trackNumber, other.trackNumber);
        }
    }
}
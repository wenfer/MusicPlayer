package app.fxplayer.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
public final class Song implements Comparable<Song> {

    private static final Logger log = LoggerFactory.getLogger(Song.class);
    private final String id;
    private final Album albumObj;
    private final SimpleStringProperty title;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final SimpleStringProperty length;
    private final SimpleStringProperty format;
    private final long lengthInSeconds;
    private final int trackNumber;
    private final int discNumber;
    private final SimpleIntegerProperty playCount;
    private final SimpleBooleanProperty playing;
    private final SimpleBooleanProperty selected;

    private final long size;

    private final String artistId;

    public Song(String id, String title, String artist, String artistId, int length,
                int trackNumber, int discNumber, int playCount,long size, Album album) {
        this.artistId = artistId;
        if (artist == null) {
            artist = "Unknown Artist";
        }
        this.format = new SimpleStringProperty("mp3");
        this.id = id;
        this.size = size;
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        if (album == null) {
            this.album = new SimpleStringProperty("Unknown Album");
        } else {
            this.album = new SimpleStringProperty(album.getTitle());
        }
        this.lengthInSeconds = length;
        Duration duration = Duration.of(length, ChronoUnit.SECONDS);
        long seconds = length % 60;
        this.length = new SimpleStringProperty(duration.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds));
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.playCount = new SimpleIntegerProperty(playCount);
        this.playing = new SimpleBooleanProperty(false);
        this.selected = new SimpleBooleanProperty(false);
        this.albumObj = album;
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
        return null;
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
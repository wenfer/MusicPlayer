package app.fxplayer.model;

import app.fxplayer.AppConfig;
import app.fxplayer.util.Resources;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONPropertyIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
public final class Album implements Comparable<Album>, SourceData {

    /**
     * -- GETTER --
     * Gets album ID.
     *
     * @return album ID
     */
    private String id;

    private String title;

    private String artist;

    private String artistId;

    private String source;

    private Image artwork;

    private String coverArtId;

    private List<Song> songs;

    private SimpleObjectProperty<Image> artworkProperty;


    /**
     * Constructor for the Album class.
     * Creates an album object and obtains the album artwork.
     */
    public Album(String id, String title, String source, String artist, String artistId, String coverArtId) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.source = source;
        this.artist = artist;
        this.coverArtId = coverArtId;
        //this.artworkProperty = new SimpleObjectProperty<>(getArtwork());
    }

    @JSONPropertyIgnore
    public ArrayList<Song> getSongs() {
        if(this.songs==null || this.songs.isEmpty()){
            this.songs = AppConfig.getInstance().getMusicSource().listByAlbum(this);
        }
        return new ArrayList<>(this.songs);
    }

    @JSONPropertyIgnore
    public ObjectProperty<Image> artworkProperty() {
        if (this.artworkProperty == null) {
            this.artworkProperty = new SimpleObjectProperty<>(getArtwork());
        }
        return this.artworkProperty;
    }

    public Image getArtwork() {
        File coverArt = AppConfig.getInstance().getMusicSource().getCoverArt(this.getCoverArtId());
        if (coverArt != null && coverArt.exists()) {
            this.artwork = new Image(coverArt.toURI().toString());
        } else {
            this.artwork = new Image(Resources.IMG + "albumsIcon.png");
        }
        return this.artwork;
    }

    @Override
    public int compareTo(Album other) {
        String first = removeArticle(this.title);
        String second = removeArticle(other.title);
        return first.compareTo(second);
    }

    private String removeArticle(String title) {
        String[] arr = title.split(" ", 2);
        if (arr.length < 2) {
            return title;
        } else {
            String firstWord = arr[0];
            String theRest = arr[1];
            return switch (firstWord) {
                case "A", "An", "The" -> theRest;
                default -> title;
            };
        }
    }
}

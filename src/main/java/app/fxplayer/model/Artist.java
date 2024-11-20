package app.fxplayer.model;

import app.fxplayer.AppConfig;
import app.fxplayer.util.Resources;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.List;

/**
 * Model class for an Artist
 */
public final class Artist implements Comparable<Artist>, SourceData {

    @Setter
    @Getter
    private String id;

    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private String source;

    private String coverArtId;

    private Image artistImage;

    private List<Album> albums;

    private SimpleObjectProperty<Image> artistImageProperty;

    public Artist(String id, String title, String coverArtId) {
        this.id = id;
        this.title = title;
        this.coverArtId = coverArtId;
    }


    public List<Album> getAlbums() {
        if (this.albums == null || this.albums.isEmpty()) {
            this.albums = AppConfig.getInstance().getMusicSource().listAlbumsByArtist(this.getId());
        }
        return albums;
    }

    public ObjectProperty<Image> artistImageProperty() {
        if(this.artistImageProperty == null){
            this.artistImageProperty = new SimpleObjectProperty<>(getArtistImage());
        }
        return this.artistImageProperty;
    }

    /**
     * Gets images for artists
     *
     * @return artist image
     */
    public Image getArtistImage() {
        File coverArt = AppConfig.getInstance().getMusicSource().getCoverArt(this.coverArtId);
        if (coverArt != null && coverArt.exists()) {
            this.artistImage = new Image(coverArt.toURI().toString());
        } else {
            this.artistImage = new Image(Resources.IMG + "artistsIcon.png");
        }
        return artistImage;
    }


    @Override
    public int compareTo(Artist other) {
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
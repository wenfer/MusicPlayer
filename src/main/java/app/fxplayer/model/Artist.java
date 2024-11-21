package app.fxplayer.model;

import app.fxplayer.AppConfig;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static app.fxplayer.Constants.DEFAULT_ARTISTS;

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

    private final String coverArtId;

    @Getter
    private Image artistImage;

    @Getter
    private List<Album> albums;

    private SimpleObjectProperty<Image> artistImageProperty;

    public Artist(String id, String title, String coverArtId) {
        this.id = id;
        this.title = title;
        this.coverArtId = coverArtId;
        CompletableFuture.supplyAsync(() ->
                        AppConfig.getInstance().getMusicSource().getCoverArt(coverArtId))
                .thenAccept((file) -> {
                    this.artistImage = new Image(file.toURI().toString());
                    this.artistImageProperty.setValue(this.artistImage);
                });
        CompletableFuture.supplyAsync(() ->
                        AppConfig.getInstance().getMusicSource().listAlbumsByArtist(this.getId()))
                .thenAccept((albums) -> {
                    this.albums = albums;
                });
    }


    public ObjectProperty<Image> artistImageProperty() {
        if (this.artistImageProperty == null) {
            this.artistImageProperty = new SimpleObjectProperty<>(DEFAULT_ARTISTS);
        }
        return this.artistImageProperty;
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
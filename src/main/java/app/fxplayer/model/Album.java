package app.fxplayer.model;

import app.fxplayer.AppConfig;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONPropertyIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static app.fxplayer.Constants.DEFAULT_ALBUM;


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

    @Getter
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
        this.artworkProperty = new SimpleObjectProperty<>(DEFAULT_ALBUM);
        CompletableFuture.supplyAsync(() ->
                        AppConfig.getInstance().getMusicSource().getCoverArt(this.getCoverArtId()))
                .thenAccept((file) -> {
                    this.artwork = new Image(file.toURI().toString());
                    log.info("task finish  {}   {}", file.getAbsolutePath(), this.artwork.getUrl());
                    this.artworkProperty.setValue(this.artwork);
                });
    CompletableFuture.supplyAsync(() ->
                    AppConfig.getInstance().getMusicSource().listByAlbum(this)
            )
            .thenAccept((songs) -> this.songs = songs);

    }

    @JSONPropertyIgnore
    public ArrayList<Song> getSongs() {
        return new ArrayList<>(this.songs);
    }

    @JSONPropertyIgnore
    public ObjectProperty<Image> artworkProperty() {
        return this.artworkProperty;
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

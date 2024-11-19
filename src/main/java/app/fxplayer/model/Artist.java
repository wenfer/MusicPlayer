package app.fxplayer.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import app.fxplayer.util.Resources;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class for an Artist
 */
@NoArgsConstructor
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

    private SimpleObjectProperty<Image> artistImageProperty;

    public Artist(String id,String title, String coverArtId) {
        this.id = id;
        this.title = title;
        this.coverArtId = coverArtId;
        this.artistImageProperty = new SimpleObjectProperty<>(getArtistImage());
    }



    public ObjectProperty<Image> artistImageProperty() {
        return this.artistImageProperty;
    }

    /**
     * Gets images for artists
     *
     * @return artist image
     */
    public Image getArtistImage() {
        if (artistImage == null) {
            try {
                File file = new File(Resources.JAR + "/img/" + this.title + ".jpg");
                artistImage = new Image(file.toURI().toURL().toString());
                if (artistImage.isError()) {
                    file.delete();
                    artistImage = new Image(Resources.IMG + "artistsIcon.png");
                }
            } catch (Exception ex) {
                File file = new File(Resources.JAR + "/img/" + this.title + ".jpg");
                file.delete();
                artistImage = new Image(Resources.IMG + "artistsIcon.png");
            }
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
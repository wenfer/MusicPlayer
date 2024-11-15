package app.musicplayer.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import app.musicplayer.util.Resources;
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
@DatabaseTable(tableName = "artist")
@NoArgsConstructor
public final class Artist implements Comparable<Artist>, SourceData {

    @DatabaseField(generatedId = true)
    @Setter
    @Getter
    private int id;

    @DatabaseField
    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private String source;

    @Setter
    @Getter
    private String sourceInfo;

    private ArrayList<Album> albums;

    private Image artistImage;

    private SimpleObjectProperty<Image> artistImageProperty;

    public Artist(String title, ArrayList<Album> albums) {
        this.title = title;
        this.albums = albums;
        this.artistImageProperty = new SimpleObjectProperty<>(getArtistImage());
    }


    /**
     * Gets array list of artist albums
     *
     * @return artist albums
     */
    public ArrayList<Album> getAlbums() {
        return new ArrayList<>(this.albums);
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

    public void downloadArtistImage() {
        try {
            File file = new File(Resources.JAR + "/img/" + this.title + ".jpg");
            file.mkdirs();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            URL xmlData = new URL(Resources.APIBASE
                    + "method=artist.getinfo"
                    + "&artist=" + URLEncoder.encode(this.title, "UTF-8")
                    + "&api_key=" + Resources.APIKEY);
            XMLStreamReader reader = factory.createXMLStreamReader(xmlData.openStream(), "UTF-8");
            boolean imageFound = false;

            while (reader.hasNext() && !imageFound) {
                reader.next();

                if (reader.isStartElement()
                        && reader.getName().getLocalPart().equals("image")
                        && reader.getAttributeValue(0).equals("extralarge")) {

                    reader.next();

                    if (reader.hasText()) {
                        BufferedImage bufferedImage = ImageIO.read(new URL(reader.getText()));
                        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                        ImageIO.write(newBufferedImage, "jpg", file);
                        imageFound = true;
                    }
                }
            }

            artistImage = new Image(file.toURI().toURL().toString());
            if (artistImage.isError()) {
                file.delete();
                artistImage = new Image(Resources.IMG + "artistsIcon.png");
            }
            this.artistImageProperty.setValue(artistImage);

        } catch (Exception ex) {
            File file = new File(Resources.JAR + "/img/" + this.title + ".jpg");
            file.delete();
        }
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
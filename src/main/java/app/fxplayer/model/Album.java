package app.fxplayer.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import app.fxplayer.util.Resources;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;


@NoArgsConstructor
@Data
@DatabaseTable(tableName = "album")
public final class Album implements Comparable<Album>, SourceData {

    /**
     * -- GETTER --
     * Gets album ID.
     *
     * @return album ID
     */
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String artist;

    @DatabaseField
    private String source;

    @DatabaseField
    private String sourceInfo;

    private Image artwork;

    private ArrayList<Song> songs;

    private SimpleObjectProperty<Image> artworkProperty;


    /**
     * Constructor for the Album class.
     * Creates an album object and obtains the album artwork.
     */
    public Album(int id, String title, String artist, ArrayList<Song> songs) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songs = songs;
        this.artworkProperty = new SimpleObjectProperty<>(getArtwork());
    }


    public ArrayList<Song> getSongs() {
        return new ArrayList<>(this.songs);
    }

    public ObjectProperty<Image> artworkProperty() {
        return this.artworkProperty;
    }

    public Image getArtwork() {
        if (this.artwork == null) {
            try {
                String location = this.songs.get(0).getLocation();
                AudioFile audioFile = AudioFileIO.read(new File(location));
                Tag tag = audioFile.getTag();
                byte[] bytes = tag.getFirstArtwork().getBinaryData();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                this.artwork = new Image(in, 300, 300, true, true);

                if (this.artwork.isError()) {
                    this.artwork = new Image(Resources.IMG + "albumsIcon.png");
                }

            } catch (Exception ex) {
                this.artwork = new Image(Resources.IMG + "albumsIcon.png");
            }
        }
        return this.artwork;
    }

    public void downloadArtwork() {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            URL xmlData = new URL(Resources.APIBASE
                    + "method=album.getinfo"
                    + "&artist=" + URLEncoder.encode(this.artist, StandardCharsets.UTF_8)
                    + "&album=" + URLEncoder.encode(this.title, StandardCharsets.UTF_8)
                    + "&api_key=" + Resources.APIKEY);

            XMLStreamReader reader = factory.createXMLStreamReader(xmlData.openStream(), "UTF-8");

            while (reader.hasNext()) {

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
                        File file = File.createTempFile("temp", "temp");
                        ImageIO.write(newBufferedImage, "jpg", file);

                        for (Song song : this.songs) {

                            AudioFile audioFile = AudioFileIO.read(new File(song.getLocation()));
                            Tag tag = audioFile.getTag();
                            tag.deleteArtworkField();

                            Artwork artwork = ArtworkFactory.createArtworkFromFile(file);
                            tag.setField(artwork);
                            AudioFileIO.write(audioFile);
                        }

                        file.delete();
                    }
                }
            }
            String location = this.songs.get(0).getLocation();
            AudioFile audioFile = AudioFileIO.read(new File(location));
            Tag tag = audioFile.getTag();
            byte[] bytes = tag.getFirstArtwork().getBinaryData();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            this.artwork = new Image(in, 300, 300, true, true);

            if (this.artwork.isError()) {

                this.artwork = new Image(Resources.IMG + "albumsIcon.png");
            }

            this.artworkProperty.setValue(artwork);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

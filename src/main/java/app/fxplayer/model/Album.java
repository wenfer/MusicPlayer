package app.fxplayer.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONPropertyIgnore;

import java.util.ArrayList;


@NoArgsConstructor
@Data
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

    private ArrayList<Song> songs;

    private SimpleObjectProperty<Image> artworkProperty;


    /**
     * Constructor for the Album class.
     * Creates an album object and obtains the album artwork.
     */
    public Album(String id, String title, String artist, String artistId, ArrayList<Song> songs) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.artist = artist;
        this.songs = songs;
        this.artworkProperty = new SimpleObjectProperty<>(getArtwork());
    }

    @JSONPropertyIgnore
    public ArrayList<Song> getSongs() {
        return new ArrayList<>(this.songs);
    }

    @JSONPropertyIgnore
    public ObjectProperty<Image> artworkProperty() {
        return this.artworkProperty;
    }

    public Image getArtwork() {
/*        if (this.artwork == null) {
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
        }*/
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

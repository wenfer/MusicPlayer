package musicplayer;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

public final class Library {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String LENGTH = "length";
    private static final String TRACKNUMBER = "trackNumber";
    private static final String PLAYCOUNT = "playCount";
    private static final String PLAYDATE = "playDate";
    private static final String LOCATION = "location";
    private static final String SONGID = "songId";

    private static ObservableList<Song> songs;
    private static ObservableList<Playlist> playlists;
    private static Map<String, List<Song>> artists;
    private static Map<String, List<Song>> albums;

    public static ObservableList<String> getArtists() {
    
        if (artists == null) {

            if (songs == null) {
                getSongs();
            }

            artists = songs.stream()
                .filter(song -> song.getArtist() != null)
                .collect(Collectors.groupingBy(Song::getArtist));
        }

        return FXCollections.observableArrayList(artists.keySet());
    }

    public static ObservableList<String> getAlbums() {

        if (albums == null) {

            if (songs == null) {
                getSongs();
            }

            albums = songs.stream()
                .filter(song -> song.getAlbum() != null)
                .collect(Collectors.groupingBy(Song::getAlbum));
        }

        return FXCollections.observableArrayList(albums.keySet());
    }

    public static ObservableList<Song> getSongs() {

        if (songs == null) {

            songs = FXCollections.observableArrayList();

            try {

                XMLInputFactory factory = XMLInputFactory.newInstance();
                File library = new File("musicplayer\\" + Resources.XML + "library.xml");
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(library), "UTF-8");

                String element = "";
                int id = -1;
                String title = null;
                String artist = null;
                String album = null;
                Duration length = null;
                int trackNumber = -1;
                int playCount = -1;
                LocalDateTime playDate = null;
                String location = null;

                while(reader.hasNext()) {

                    reader.next();

                    if (reader.isWhiteSpace()) {

                        continue;

                    } else if (reader.isCharacters()) {

                        String value = reader.getText();

                        switch (element) {

                            case ID:
                                id = Integer.parseInt(value);
                                break;
                            case TITLE:
                                title = value;
                                break;
                            case ARTIST:
                                artist = value;
                                break;
                            case ALBUM:
                                album = value;
                                break;
                            case LENGTH:
                                length = Duration.ofSeconds(Long.parseLong(value));
                                break;
                            case TRACKNUMBER:
                                trackNumber = Integer.parseInt(value);
                                break;
                            case PLAYCOUNT:
                                playCount = Integer.parseInt(value);
                                break;
                            case PLAYDATE:
                                playDate = LocalDateTime.parse(value);
                                break;
                            case LOCATION:
                                location = value;
                                break;
                        }

                    } else if (reader.isStartElement()) {

                        element = reader.getName().getLocalPart();

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("song")) {

                        songs.add(new Song(id, title, artist, album, length, trackNumber, playCount, playDate, location));
                        id = -1;
                        title = null;
                        artist = null;
                        album = null;
                        length = null;
                        trackNumber = -1;
                        playCount = -1;
                        playDate = null;
                        location = null;

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("songs")) {

                        reader.close();
                        break;
                    }
                }

            } catch (Exception ex) {

                System.out.println(ex.getMessage());
            }
        }

        return songs;
    }

    public static ObservableList<Playlist> getPlaylists() {

       if (playlists == null) {

            playlists = FXCollections.observableArrayList();

            try {

                XMLInputFactory factory = XMLInputFactory.newInstance();
                File library = new File("musicplayer\\" + Resources.XML + "library.xml");
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(library), "UTF-8");

                String element = "";
                boolean isPlaylist = false;
                int id = -1;
                String title = null;
                ArrayList<Integer> songIds = new ArrayList<Integer>();

                while(reader.hasNext()) {

                    reader.next();

                    if (reader.isWhiteSpace()) {

                        continue;

                    } else if (reader.isCharacters() && isPlaylist) {

                        String value = reader.getText();

                        switch (element) {

                            case ID:
                                id = Integer.parseInt(value);
                                break;
                            case TITLE:
                                title = value;
                                break;
                            case SONGID:
                                songIds.add(Integer.parseInt(value));
                                break;
                        }

                    } else if (reader.isStartElement()) {

                        element = reader.getName().getLocalPart();
                        if (element == "playlists") {
                            isPlaylist = true;
                        }

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("playlist")) {

                        playlists.add(new Playlist(id, title, songIds));
                        id = -1;
                        title = null;
                        songIds = new ArrayList<Integer>();

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("playlists")) {

                        reader.close();
                        break;
                    }
                }

            } catch (Exception ex) {

                System.out.println(ex.getMessage());
            }
        }

        return playlists;
    }

    public static ObservableList<Song> getSongsByArtist(String artist) {

        if (artists == null) {
            getArtists();
        }

        ObservableList<Song> songsByArtist = FXCollections.observableArrayList();

        songs.stream()
            .filter(song -> song.getArtist().equals(artist))
            .forEach(song -> songsByArtist.add(song));

        return songsByArtist;
    }

    public static ObservableList<Song> getSongsByAlbum(String album) {
        
        if (albums == null) {
            getAlbums();
        }

        ObservableList<Song> songsByAlbum = FXCollections.observableArrayList();

        songs.stream()
            .filter(song -> song.getAlbum().equals(album))
            .forEach(song -> songsByAlbum.add(song));

        return songsByAlbum;
    }

    public static ObservableList<Song> getSongsByPlaylist(Playlist playlist) {

        if (playlists == null) {
            getPlaylists();
        }

        ObservableList<Song> songsByPlaylist = FXCollections.observableArrayList();
        ArrayList<Integer> songIds = playlist.getSongIds();

        songs.stream()
            .filter(song -> songIds.contains(song.getId()))
            .forEach(song -> songsByPlaylist.add(song));

        return songsByPlaylist;
    }

    public static void importMusic(String path) throws Exception {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element library = doc.createElement("library");
        Element songs = doc.createElement("songs");
        Element playlists = doc.createElement("playlists");

        doc.appendChild(library);
        library.appendChild(songs);
        library.appendChild(playlists);

        File directory = new File(Paths.get(path).toUri());

        writeXML(directory, doc, songs, 0);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        File xmlFile = new File("musicplayer/" + Resources.XML + "library.xml");
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }

    private static int writeXML(File directory, Document doc, Element songs, int i) {

        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                try {
                    
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();
                    AudioHeader header = audioFile.getAudioHeader();

                    Element song = doc.createElement("song");
                    songs.appendChild(song);

                    Element id = doc.createElement("id");
                    Element title = doc.createElement("title");
                    Element artist = doc.createElement("artist");
                    Element album = doc.createElement("album");
                    Element length = doc.createElement("length");
                    Element trackNumber = doc.createElement("trackNumber");
                    Element playCount = doc.createElement("playCount");
                    Element playDate = doc.createElement("playDate");
                    Element location = doc.createElement("location");

                    id.setTextContent(Integer.toString(i++));
                    title.setTextContent(tag.getFirst(FieldKey.TITLE));
                    artist.setTextContent(tag.getFirst(FieldKey.ARTIST));
                    album.setTextContent(tag.getFirst(FieldKey.ALBUM));
                    length.setTextContent(Integer.toString(header.getTrackLength()));
                    trackNumber.setTextContent(tag.getFirst(FieldKey.TRACK));
                    playCount.setTextContent("0");
                    playDate.setTextContent(LocalDateTime.now().toString());
                    //location.setTextContent(file.toURI().toString());
                    location.setTextContent(Paths.get(file.getAbsolutePath()).toString());

                    song.appendChild(id);
                    song.appendChild(title);
                    song.appendChild(artist);
                    song.appendChild(album);
                    song.appendChild(length);
                    song.appendChild(trackNumber);
                    song.appendChild(playCount);
                    song.appendChild(playDate);
                    song.appendChild(location);

                } catch (Exception ex) {
                    
                    System.out.println(ex.getMessage());
                }

            } else if (file.isDirectory()) {

                i = writeXML(file, doc, songs, i);
            }
        }

        return i;
    }
}
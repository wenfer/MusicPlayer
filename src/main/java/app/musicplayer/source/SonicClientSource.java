package app.musicplayer.source;

import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Song;
import com.sun.jdi.PathSearchingVirtualMachine;
import lombok.extern.slf4j.Slf4j;
import net.beardbot.subsonic.client.Subsonic;
import net.beardbot.subsonic.client.SubsonicPreferences;
import net.beardbot.subsonic.client.base.SubsonicIncompatibilityException;
import org.json.JSONObject;
import org.subsonic.restapi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SonicClientSource implements ISource {

    private final SubsonicPreferences preferences;

    private Subsonic subsonic;

    private String name;

    private List<Child> albumListCache = null;

    private List<ArtistID3> artistID3List = null;

    public SonicClientSource(String name, String serverUrl, String username, String password, int streamBitRate) {
        this.preferences = new SubsonicPreferences(serverUrl, username, password);
        this.name = name;
        preferences.setStreamBitRate(streamBitRate);
        preferences.setClientName(name);
        this.subsonic = new Subsonic(preferences);
        try {
            if (subsonic.testConnection()) {
                log.info("Succesfully connected to server!");
            } else {
                log.error("Failed to connect to server!");
            }
        } catch (SubsonicIncompatibilityException e) {
            log.error("The server is not compatible with the client! Please upgrade you server!");
        }
    }


/*    @Override
    public List<Song> list() {
        List<MusicFolder> musicFolders = subsonic.browsing().getMusicFolders();
        log.info("获取到音乐目录共{}个",musicFolders.size());
        for (MusicFolder musicFolder : musicFolders) {
            log.info("开始获取目录{}下的音乐",musicFolder.getName());
            Directory directory = subsonic.browsing().getMusicDirectory(String.valueOf(musicFolder.getId()));
            directory.getchildren()

        }
        return List.of();
    }*/

    @Override
    public List<Song> listByAlbum(Album album) {
        return List.of();
    }

    @Override
    public List<Song> listByArtist(Artist artist) {
        return List.of();
    }

    @Override
    public List<Album> listAlbums() {
        if (this.albumListCache == null) {
            AlbumList albumList = subsonic.lists().getAlbumList();
            log.info("get album result {}", JSONObject.valueToString(albumList.getAlbums()));
            this.albumListCache = new ArrayList<>(albumList.getAlbums());
        }
        return this.albumListCache.stream().map(child -> {
            Album album = new Album();
            album.setArtist(child.getArtist());
            album.setTitle(child.getTitle());
            album.setSourceInfo(JSONObject.valueToString(child));
            return album;
        }).toList();
    }

    @Override
    public List<Artist> listArtists() {
        List<Artist> artists = new ArrayList<>();
        if (this.artistID3List == null) {
            List<IndexID3> artistList = subsonic.browsing().getArtists();
            this.artistID3List = new ArrayList<>();
            artistList.forEach(indexID3 -> {
                log.info("获取到歌手{}", indexID3.getName());
                this.artistID3List.addAll(indexID3.getArtists());
            });
        }
        this.artistID3List.forEach(art -> {
            Artist artist = new Artist();
            artist.setTitle(art.getName());
            artists.add(artist);
        });
        return artists;
    }


}

package app.fxplayer.source;

import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Playlist;
import app.fxplayer.model.Song;
import lombok.extern.slf4j.Slf4j;
import net.beardbot.subsonic.client.Subsonic;
import net.beardbot.subsonic.client.SubsonicPreferences;
import net.beardbot.subsonic.client.api.lists.AlbumListParams;
import net.beardbot.subsonic.client.api.lists.AlbumListType;
import net.beardbot.subsonic.client.base.SubsonicIncompatibilityException;
import org.json.JSONObject;
import org.subsonic.restapi.*;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SonicClientSource implements MusicSource {

    private final SubsonicPreferences preferences;

    private final Subsonic subsonic;

    private final String name;

    private Map<String, Album> albumListCache = null;

    private Map<String, Artist> artistCache = null;


    private final Map<String, Playlist> playlistCache = new HashMap<>();

    private final Map<String, Song> songsCache = new HashMap<>();

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

    private Map<String, Album> getAlbumMap() {
        if (this.albumListCache == null || this.albumListCache.isEmpty()) {
            AlbumList albumList = subsonic.lists().getAlbumList();
            log.info("get album result {}", JSONObject.valueToString(albumList.getAlbums()));
            this.albumListCache = albumList.getAlbums().stream().map(child -> {
                Album album = new Album();
                album.setId(child.getId());
                album.setSource(this.name);
                album.setArtist(child.getArtist());
                album.setTitle(child.getTitle());
                return album;
            }).collect(Collectors.toMap(Album::getId, album -> album));
        }
        return this.albumListCache;
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

    private Song childToSong(Child child, Album album) {
        return new Song(child.getId(),
                child.getTitle(),
                child.getArtist(),
                child.getArtistId(),
                child.getDuration(),
                child.getTrack() == null ? 1 : child.getTrack(),
                child.getDiscNumber() == null ? 1 : child.getDiscNumber(),
                child.getPlayCount() == null ? 0 : child.getPlayCount().intValue(),
                album);
    }


    @Override
    public List<Song> listByAlbum(Album album) {
        AlbumWithSongsID3 withSongsID3 = this.subsonic.browsing().getAlbum(album.getId());
        return withSongsID3.getSongs().stream().map(child -> {
            log.info("song info :{}", new JSONObject(child));
            return childToSong(child, album);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Song> listByArtist(Artist artist) {
        return List.of();
    }

    @Override
    public List<Album> listAlbums() {
        return new ArrayList<>(this.getAlbumMap().values());
    }


    private Map<String, Artist> getArtistCache() {
        if (this.artistCache == null || this.artistCache.isEmpty()) {
            this.artistCache = new HashMap<>();
            List<IndexID3> artistList = subsonic.browsing().getArtists();
            artistList.forEach(indexID3 -> {
                indexID3.getArtists().forEach(artistID3 -> {
                    Artist artist = new Artist(artistID3.getId(), artistID3.getName(), artistID3.getCoverArtId());
                    this.artistCache.put(artistID3.getId(), artist);
                });
            });
        }
        return this.artistCache;
    }

    @Override
    public List<Artist> listArtists() {
        return new ArrayList<>(getArtistCache().values());
    }

    @Override
    public boolean ping() {
        try {
            this.subsonic.system().ping();
        } catch (Exception e) {
            log.error("ping failed  exception type is :{}", e.getClass().getName(), e);
            return false;
        }
        return true;
    }

    @Override
    public InputStream stream(Song song) {
        return this.subsonic.media().stream(song.getId());
    }


    @Override
    public void createPlaylist(String text) {

    }

    @Override
    public Playlist getPlaylist(String playlistId) {
        Playlist playlist = playlistCache.get(playlistId);
        if (playlist == null) {
            PlaylistWithSongs playlistWithSongs = subsonic.playlists().getPlaylist(playlistId);
            Map<String, Album> albumMap = getAlbumMap();
            List<Song> songs = playlistWithSongs.getEntries().stream().map(child -> childToSong(child, albumMap.get(child.getAlbumId()))).toList();
            playlist = new Playlist(playlistWithSongs.getId(), playlistWithSongs.getName(), songs);
            playlistCache.put(playlistWithSongs.getId(), playlist);
        }
        return playlist;
    }

    @Override
    public Collection<Song> getSongs() {
        return this.songsCache.values();
    }


    @Override
    public Artist getArtist(String artistId) {
        return this.getArtistCache().get(artistId);
    }

    @Override
    public List<Album> listAlbumsByArtist(String artistId) {
        subsonic.searching().

        AlbumListParams albumListParams = AlbumListParams.create();
        albumListParams.type(AlbumListType.ALPHABETICAL_BY_ARTIST);
        subsonic.lists().getAlbumList()
        return List.of();
    }


    private void cachePlaylist() {
        subsonic.playlists().getPlaylists().forEach(playlist -> {
            Playlist list = new Playlist(playlist.getId(), playlist.getName(), null);
            playlistCache.put(playlist.getId(), list);
        });

    }


}

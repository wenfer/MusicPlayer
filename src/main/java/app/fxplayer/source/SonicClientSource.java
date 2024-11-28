package app.fxplayer.source;

import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Playlist;
import app.fxplayer.model.Song;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.beardbot.subsonic.client.Subsonic;
import net.beardbot.subsonic.client.SubsonicPreferences;
import net.beardbot.subsonic.client.api.lists.AlbumListParams;
import net.beardbot.subsonic.client.api.media.MediaStream;
import net.beardbot.subsonic.client.api.playlist.UpdatePlaylistParams;
import net.beardbot.subsonic.client.base.SubsonicIncompatibilityException;
import org.json.JSONObject;
import org.subsonic.restapi.AlbumWithSongsID3;
import org.subsonic.restapi.Child;
import org.subsonic.restapi.IndexID3;
import org.subsonic.restapi.PlaylistWithSongs;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class SonicClientSource implements MusicSource {

    private final Subsonic subsonic;

    private final String name;

    private final Map<String, Album> albumListCache = new HashMap<>();

    private Map<String, Artist> artistCache = null;

    private final Map<String, Playlist> playlistCache = new HashMap<>();

    private final Map<String, Song> songsCache = new HashMap<>();

    public SonicClientSource(String name, String serverUrl, String username, String password, int streamBitRate) {
        SubsonicPreferences preferences = new SubsonicPreferences(serverUrl, username, password);
        preferences.setEstimateContentLength(true);
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
        if (this.albumListCache.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                int page = 0;
                int size = 50;
                boolean hasNext = true;
                while (hasNext) {
                    AlbumListParams albumListParams = AlbumListParams.create();
                    albumListParams.size(size);
                    albumListParams.offset(page * size);
                    List<Child> albumList = subsonic.lists().getAlbumList(albumListParams).getAlbums();
                    if (albumList.size() != size) {
                        hasNext = false;
                    }
                    albumList.forEach(child -> this.albumListCache.put(child.getId(),
                            new Album(
                                    child.getId(),
                                    child.getTitle(),
                                    this.name,
                                    child.getArtist(),
                                    child.getArtistId(),
                                    child.getCoverArtId()
                            )
                    ));
                    page++;
                }
            });
        }
        return this.albumListCache;
    }

    private void downloadCoverArt(String coverArtId) {
        File tempFile = new File(FileUtil.getTmpDirPath(), "cover_" + coverArtId + ".tmp");
        if (!tempFile.exists()) {
            FileUtil.writeFromStream(subsonic.media().getCoverArt(coverArtId).getInputStream(), tempFile);
        }
    }


    private Song childToSong(Child child, Album album) {
        Song song = new Song(child.getId(),
                child.getTitle(),
                child.getArtist(),
                child.getArtistId(),
                child.getDuration(),
                child.getTrack() == null ? 1 : child.getTrack(),
                child.getDiscNumber() == null ? 1 : child.getDiscNumber(),
                child.getPlayCount() == null ? 0 : child.getPlayCount().intValue(),
                child.getSize(),
                child.getCoverArtId(),
                album);
        this.songsCache.putIfAbsent(song.getId(), song);
        return song;
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
    public List<Album> listAlbums() {
        return new ArrayList<>(this.getAlbumMap().values());
    }


    private Map<String, Artist> getArtistCache() {
        if (this.artistCache == null || this.artistCache.isEmpty()) {
            this.artistCache = new HashMap<>();
            List<IndexID3> artistList = subsonic.browsing().getArtists();
            artistList.forEach(indexID3 -> indexID3.getArtists().forEach(artistID3 -> {
                Artist artist = new Artist(artistID3.getId(), artistID3.getName(), artistID3.getCoverArtId());
                this.artistCache.put(artistID3.getId(), artist);
            }));
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
    public MediaStream stream(Song song) {
        return this.subsonic.media().stream(song.getId());
    }


    @Override
    public void createPlaylist(String text) {
        CompletableFuture.runAsync(() -> subsonic.playlists().createPlaylist(text, List.of()));
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
        return getAlbumMap().values().stream().filter(album -> album.getArtistId().equals(artistId)).collect(Collectors.toList());
    }

    @Override
    public File getCoverArt(String coverArtId) {
        File tempFile = new File(FileUtil.getTmpDirPath(), "cover_" + coverArtId + ".tmp");
        if (tempFile.exists()) {
            return tempFile;
        }
        MediaStream coverArt = subsonic.media().getCoverArt(coverArtId);
        FileUtil.writeFromStream(coverArt.getInputStream(), tempFile);
        return tempFile;
    }

    @Override
    public void deleteSongFromPlaylist(String selectedPlayListId, String songId) {
        Playlist playlist = playlistCache.get(selectedPlayListId);
        int index = playlist.removeSong(songId);
        if (index >= 0) {
            UpdatePlaylistParams updatePlaylistParams = UpdatePlaylistParams.create().removeSong(index);
            subsonic.playlists().updatePlaylist(selectedPlayListId, updatePlaylistParams);
        }
    }

    @Override
    public void deletePlaylist(String playlistId) {
        this.playlistCache.remove(playlistId);
        CompletableFuture.runAsync(() -> subsonic.playlists().deletePlaylist(playlistId));
    }


    private void cachePlaylist() {
        subsonic.playlists().getPlaylists().forEach(playlist -> {
            Playlist list = new Playlist(playlist.getId(), playlist.getName(), null);
            playlistCache.put(playlist.getId(), list);
        });

    }


}

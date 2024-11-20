package app.fxplayer.source;

import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Playlist;
import app.fxplayer.model.Song;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface MusicSource {

    List<Song> listByAlbum(Album album);

    List<Song> listByArtist(Artist artist);

    List<Album> listAlbums();

    List<Artist> listArtists();

    boolean ping();

    InputStream stream(Song song);


    void createPlaylist(String text);

    Playlist getPlaylist(String playlistId);

    Collection<Song> getSongs();

    Artist getArtist(String artistId);

    List<Album> listAlbumsByArtist(String artistId);

    File getCoverArt(String coverArtId);

    void deleteSongFromPlaylist(String selectedPlayListId, String selectedSongId);

    void deletePlaylist(String playlistId);
}

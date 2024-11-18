package app.fxplayer.source;

import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Song;

import java.io.InputStream;
import java.util.List;

public interface MusicSource {

    List<Song> listByAlbum(Album album);

    List<Song> listByArtist(Artist artist);

    List<Album> listAlbums();

    List<Artist> listArtists();

    boolean ping();

    InputStream stream(Song song);
}

package app.musicplayer.source;

import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Song;

import java.util.List;

public interface ISource {

    List<Song> listByAlbum(Album album);

    List<Song> listByArtist(Artist artist);

    List<Album> listAlbums();

    List<Artist> listArtists();
}

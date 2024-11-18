package app.fxplayer.model;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResult {

    private List<Song> songResults;
    private List<Album> albumResults;
    private List<Artist> artistResults;

    public SearchResult(List<Song> songResults, List<Album> albumResults, List<Artist> artistResults) {
        this.songResults = songResults;
        this.albumResults = albumResults;
        this.artistResults = artistResults;
    }

}

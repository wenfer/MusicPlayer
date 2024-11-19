package app.fxplayer.model;

import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@DatabaseTable(tableName = "playlist")
public class Playlist {

    @Setter
    @Getter
    private String id;

    @DatabaseField
    @Setter
    @Getter
    private String title;
    private List<Song> songs;

    @Getter
    private String placeholder =
            "Add songs to this playlist by dragging items to the sidebar\n" +
            "or by clicking the Add to Playlist button";

    public Playlist(String id, String title, List<Song> songs) {
        this.id = id;
        this.title = title;
        this.songs = songs;
    }

    protected Playlist(String id, String title, String placeholder) {
        this.id = id;
        this.title = title;
        this.songs = null;
        this.placeholder = placeholder;
    }


    public ObservableList<Song> getSongs() {
        return FXCollections.observableArrayList(this.songs);
    }
    
    public void addSong(Song song) {
    	if (!songs.contains(song)) {
    		songs.add(song);
    	}
    }
    
//    public void removeSong(int songId) {
//      // Loops through the songs in the play list.
//      // When the song with an ID matching the selectedSongId is found, it is deleted.
//        songs.removeIf(song -> song.getId() == songId);
//    }

    @Override
    public String toString() {
        return this.title;
    }
}
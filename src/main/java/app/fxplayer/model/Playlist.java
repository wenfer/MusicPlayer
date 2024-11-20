package app.fxplayer.model;

import java.util.List;
import java.util.Objects;

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
    
    public int removeSong(String songId) {
      // Loops through the songs in the play list.
      // When the song with an ID matching the selectedSongId is found, it is deleted.
        for (int i = 0; i < songs.size(); i++) {
            if(Objects.equals(songs.get(i).getId(), songId)){
                songs.remove(i);
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
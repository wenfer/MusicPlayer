package app.fxplayer.model;

import app.fxplayer.AppConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MostPlayedPlaylist extends Playlist {

    MostPlayedPlaylist(String id) {
        super(id, "Most Played", "You have not played any songs yet");
    }

    @Override
    public ObservableList<Song> getSongs() {
        List<Song> songs = new ArrayList<>(AppConfig.getInstance().getMusicSource().getSongs());
        songs = songs.stream()
                .filter(x -> x.getPlayCount() > 0)
                .sorted((x, y) -> Integer.compare(y.getPlayCount(), x.getPlayCount()))
                .collect(Collectors.toList());

        if (songs.size() > 100) {
            songs = songs.subList(0, 100);
        }
        return FXCollections.observableArrayList(songs);
    }
}

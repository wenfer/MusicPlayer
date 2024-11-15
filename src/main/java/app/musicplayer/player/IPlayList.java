package app.musicplayer.player;

import app.musicplayer.model.Song;

import java.util.List;

public interface IPlayList {

    List<Song> list();


    void play();


}

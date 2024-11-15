package app.musicplayer.player;

import app.musicplayer.model.Song;

import java.io.InputStream;

public interface IPlayer {

    void play(InputStream in);

    void pause();

    void next();

    void prev();

    void volume();

    void volume(int volume);

    void mute();

    Song playing();

}

package app.fxplayer.util;

import app.fxplayer.model.Song;

public interface SubView {

	void scroll(char letter);
	void play();
	Song getSelectedSong();
}

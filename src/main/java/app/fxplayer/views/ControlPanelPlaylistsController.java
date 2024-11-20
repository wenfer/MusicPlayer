package app.fxplayer.views;

import java.net.URL;
import java.util.ResourceBundle;


import app.fxplayer.AppConfig;
import app.fxplayer.Bootstrap;
import app.fxplayer.util.SubView;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class ControlPanelPlaylistsController implements Initializable {
	
	@FXML private Pane playButton;
	@FXML private Pane deleteButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	@FXML
	private void playSong(Event e) {
		SubView controller = Bootstrap.getMainController().getSubViewController();
		controller.play();
		e.consume();
	}
	
	@FXML
	private void deleteSong(Event e) {		
		// Gets the play lists controller sub view, which keeps track of the currently selected song.
		// A PlayListsController object will always be returned since this button will only be visible
		// when the user selects a song while in a play list.
		PlaylistsController controller = (PlaylistsController) Bootstrap.getMainController().getSubViewController();
		// Retrieves play list and song id to search for the song in the xml file.
		String selectedPlayListId = controller.getSelectedPlaylist().getId();
		String selectedSongId = controller.getSelectedSong().getId();
		AppConfig.getInstance().getMusicSource().deleteSongFromPlaylist(selectedPlayListId, selectedSongId);
		controller.deleteSelectedRow();
		e.consume();
	}
}

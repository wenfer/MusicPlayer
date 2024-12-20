package app.fxplayer.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.fxplayer.Bootstrap;
import app.fxplayer.NewPlayer;
import app.fxplayer.model.Song;
import app.fxplayer.util.ClippedTableCell;
import app.fxplayer.util.ControlPanelTableCell;
import app.fxplayer.util.PlayingTableCell;
import app.fxplayer.util.SubView;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import lombok.Getter;

public class NowPlayingController implements Initializable, SubView {

    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
    @Getter
    private Song selectedSong;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ObservableList<Song> songs = FXCollections.observableArrayList(NewPlayer.getInstance().getNowPlayingList());

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        albumColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));

        playingColumn.setCellFactory(x -> new PlayingTableCell<>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<>());
        albumColumn.setCellFactory(x -> new ClippedTableCell<>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<>("playCount"));
        
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        	tableView.requestFocus();
        	event.consume();
        });

        tableView.setItems(songs);

        tableView.setRowFactory(x -> {

            TableRow<Song> row = new TableRow<>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) ->
                    row.pseudoClassStateChanged(playing, newValue);

            row.itemProperty().addListener((obs, previousSong, currentSong) -> {
            	if (previousSong != null) {
            		previousSong.playingProperty().removeListener(changeListener);
            	}
            	if (currentSong != null) {
                    currentSong.playingProperty().addListener(changeListener);
                    row.pseudoClassStateChanged(playing, currentSong.getPlaying());
                } else {
                    row.pseudoClassStateChanged(playing, false);
                }
            });

            row.setOnMouseClicked(event -> {
            	TableViewSelectionModel<Song> sm = tableView.getSelectionModel();
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    play();
                } else if (event.isShiftDown()) {
                	ArrayList<Integer> indices = new ArrayList<>(sm.getSelectedIndices());
                	if (indices.isEmpty()) {
                        row.getIndex();
                        sm.select(row.getItem());
                    } else {
                		sm.clearSelection();
	                	indices.sort(Integer::compareTo);
	                	int max = indices.get(indices.size() - 1);
	                	int min = indices.get(0);
	                	if (min < row.getIndex()) {
	                		for (int i = min; i <= row.getIndex(); i++) {
	                			sm.select(i);
	                		}
	                	} else {
	                		for (int i = row.getIndex(); i <= max; i++) {
	                			sm.select(i);
	                		}
	                	}
                	}
                	
                } else if (event.isControlDown()) {
                	if (sm.getSelectedIndices().contains(row.getIndex())) {
                		sm.clearSelection(row.getIndex());
                	} else {
                		sm.select(row.getItem());
                	}
                } else {
                	if (sm.getSelectedIndices().size() > 1) {
                		sm.clearSelection();
                    	sm.select(row.getItem());
                	} else if (sm.getSelectedIndices().contains(row.getIndex())) {
                		sm.clearSelection();
                	} else {
                		sm.clearSelection();
                    	sm.select(row.getItem());
                	}
                }
            });
            
            row.setOnDragDetected(event -> {
            	Dragboard db = row.startDragAndDrop(TransferMode.ANY);
            	ClipboardContent content = new ClipboardContent();
            	if (tableView.getSelectionModel().getSelectedIndices().size() > 1) {
            		content.putString("List");
                    db.setContent(content);
                	Bootstrap.setDraggedItem(tableView.getSelectionModel().getSelectedItems());
            	} else {
            		content.putString("Song");
                    db.setContent(content);
                	Bootstrap.setDraggedItem(row.getItem());
            	}
            	ImageView image = new ImageView(row.snapshot(null, null));
            	Rectangle2D rectangle = new Rectangle2D(0, 0, 250, 50);
            	image.setViewport(rectangle);
            	db.setDragView(image.snapshot(null, null), 125, 25);
                event.consume();
            });

            return row ;
        });
        
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        	if (oldSelection != null) {
        		oldSelection.setSelected(false);
        	}
        	if (newSelection != null && tableView.getSelectionModel().getSelectedIndices().size() == 1) {
        		newSelection.setSelected(true);
        		selectedSong = newSelection;
        	}
        });
        
        // Plays selected song when enter key is pressed.
        tableView.setOnKeyPressed(event -> {
        	if (event.getCode().equals(KeyCode.ENTER)) {
        		play();
        	}
        });
    }
    
    @Override
    public void play() {
    	Song song = selectedSong;
        ObservableList<Song> songList = tableView.getItems();
        NewPlayer player = NewPlayer.getInstance();
        player.updatePlaylist(songList);
        player.play(song);
    }
    
    @Override
    public void scroll(char letter) {}

}

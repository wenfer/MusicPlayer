package app.fxplayer.views;

import app.fxplayer.AppConfig;
import app.fxplayer.Bootstrap;
import app.fxplayer.Constants;
import app.fxplayer.NewPlayer;
import app.fxplayer.model.*;
import app.fxplayer.util.Search;
import app.fxplayer.util.SubView;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class MainController implements Initializable, IntellitypeListener {

    private boolean isSideBarExpanded = true;
    private final double expandedWidth = 250;
    private final double collapsedWidth = 50;
    private final double expandedHeight = 50;
    private final double collapsedHeight = 0;
    private final double searchExpanded = 180;
    private final double searchCollapsed = 0;
    @Getter
    private SubView subViewController;
    private SliderSkin sliderSkin;
    private Stage volumePopup;
    private Stage searchPopup;
    private VolumePopupController volumePopupController;
    private CountDownLatch viewLoadedLatch;

    @FXML
    private ScrollPane subViewRoot;
    @FXML
    private VBox sideBar;
    @FXML
    private VBox playlistBox;
    @FXML
    private ImageView nowPlayingArtwork;
    @FXML
    private Label nowPlayingTitle;
    @FXML
    private Label nowPlayingArtist;
    @FXML
    private Slider timeSlider;
    @FXML
    private Region frontSliderTrack;
    @FXML
    private Label timePassed;
    @FXML
    private Label timeRemaining;

    @FXML
    private HBox letterBox;
    @FXML
    private Separator letterSeparator;

    @FXML
    private Pane playButton;
    @FXML
    private Pane pauseButton;
    @FXML
    private Pane loopButton;
    @FXML
    private Pane shuffleButton;
    @FXML
    private HBox controlBox;

    @FXML
    private TextField searchBox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        resetLatch();
        controlBox.getChildren().remove(2);
        frontSliderTrack.prefWidthProperty().bind(timeSlider.widthProperty().multiply(timeSlider.valueProperty().divide(timeSlider.maxProperty())));
        sliderSkin = new SliderSkin(timeSlider);
        timeSlider.setSkin(sliderSkin);
        createVolumePopup();
        createSearchPopup();
        PseudoClass active = PseudoClass.getPseudoClass("active");
        loopButton.setOnMouseClicked(x -> {
            sideBar.requestFocus();
            NewPlayer.getInstance().toggleLoop();
            loopButton.pseudoClassStateChanged(active, NewPlayer.getInstance().isLoopActive());
        });
        shuffleButton.setOnMouseClicked(x -> {
            sideBar.requestFocus();
            NewPlayer.getInstance().toggleShuffle();
            shuffleButton.pseudoClassStateChanged(active, NewPlayer.getInstance().isShuffleActive());
        });

        timeSlider.setFocusTraversable(false);
        timeSlider.valueChangingProperty().addListener(
                (slider, wasChanging, isChanging) -> {
                    if (wasChanging) {
                        int seconds = (int) Math.round(timeSlider.getValue() / 4.0);
                        timeSlider.setValue(seconds * 4);
                        NewPlayer.getInstance().seek(seconds);
                    }
                }
        );

        timeSlider.valueProperty().addListener(
                (slider, oldValue, newValue) -> {

                    double previous = oldValue.doubleValue();
                    double current = newValue.doubleValue();
                    if (!timeSlider.isValueChanging() && current != previous + 1) {
                        int seconds = (int) Math.round(current / 4.0);
                        timeSlider.setValue(seconds * 4);
                        NewPlayer.getInstance().seek(seconds);
                    }
                }
        );

        unloadLettersAnimation.setOnFinished(x -> {
            letterBox.setPrefHeight(0);
            letterSeparator.setPrefHeight(0);
        });

        searchBox.textProperty().addListener((observable, oldText, newText) -> {
            String text = newText.trim();
            if (text.isEmpty()) {
                if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Status.RUNNING)) {
                    searchHideAnimation.play();
                }
            } else {
                Search.search(text);
            }
        });

        Search.hasResultsProperty().addListener((observable, hadResults, hasResults) -> {
            if (hasResults) {
                SearchResult result = Search.getResult();
                Platform.runLater(() -> {
                    showSearchResults(result);
                    Bootstrap.getStage().toFront();
                });
                int height = 0;
                int artists = result.getArtistResults().size();
                int albums = result.getAlbumResults().size();
                int songs = result.getSongResults().size();
                if (artists > 0) height += (artists * 50) + 50;
                if (albums > 0) height += (albums * 50) + 50;
                if (songs > 0) height += (songs * 50) + 50;
                if (height == 0) height = 50;
                searchPopup.setHeight(height);
            }
        });

        Bootstrap.getStage().xProperty().addListener((observable, oldValue, newValue) -> {
            if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Status.RUNNING)) {
                searchHideAnimation.play();
            }
        });

        Bootstrap.getStage().yProperty().addListener((observable, oldValue, newValue) -> {
            if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Status.RUNNING)) {
                searchHideAnimation.play();
            }
        });

        for (Node node : letterBox.getChildren()) {
            Label label = (Label) node;
            label.prefWidthProperty().bind(letterBox.widthProperty().subtract(50).divide(26).subtract(1));
        }
        updatePlayInfo(null);
        initializePlaylists();
        // Register media keys on Windows
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            JIntellitype.getInstance().addIntellitypeListener(this);
        }

        // Loads the default view: artists.
        loadView("artists");
    }

    @Override
    public void onIntellitype(int key) {
        // Skip/play/pause/back using Windows media keys
        Platform.runLater(() -> {
            switch (key) {
                case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                    skip();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                    playPause();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                    back();
                    break;
            }
        });
    }

    void resetLatch() {
        viewLoadedLatch = new CountDownLatch(1);
    }

    CountDownLatch getLatch() {
        return viewLoadedLatch;
    }

    private void createVolumePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Constants.FXML + "VolumePopup.fxml"));
            HBox view = loader.load();
            volumePopupController = loader.getController();
            Stage stage = Bootstrap.getStage();
            Stage popup = new Stage();
            popup.setScene(new Scene(view));
            popup.initStyle(StageStyle.UNDECORATED);
            popup.initOwner(stage);
            popup.setX(stage.getWidth() - 270);
            popup.setY(stage.getHeight() - 120);
            popup.focusedProperty().addListener((x, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) {
                    volumeHideAnimation.play();
                }
            });
            volumeHideAnimation.setOnFinished(x -> popup.hide());

            popup.show();
            popup.hide();
            volumePopup = popup;

        } catch (Exception ex) {
            log.error("create volume popup failed", ex);
        }
    }

    private void createSearchPopup() {
        try {
            VBox view = new VBox();
            view.getStylesheets().add(Constants.CSS + "MainStyle.css");
            view.getStyleClass().add("searchPopup");
            Stage stage = Bootstrap.getStage();
            Stage popup = new Stage();
            popup.setScene(new Scene(view));
            popup.initStyle(StageStyle.UNDECORATED);
            popup.initOwner(stage);
            searchHideAnimation.setOnFinished(x -> popup.hide());

            popup.show();
            popup.hide();
            searchPopup = popup;

        } catch (Exception ex) {
            log.error("load style failed", ex);
        }
    }


    public void updatePlayInfo(Song song) {
        timeSlider.setMin(0);
        if (song != null) {
            nowPlayingTitle.setText(song.getTitle());
            nowPlayingArtist.setText(song.getArtist());
            nowPlayingArtwork.setImage(song.getArtwork());
            timeSlider.setMax(song.getLengthInSeconds() * 4);
            timePassed.setText("0:00");
            timeRemaining.setText(song.getLength());
        } else {
            timeSlider.setMax(1);
            timePassed.setText("");
            timeRemaining.setText("");
            nowPlayingTitle.setText("");
            nowPlayingArtist.setText("");
            nowPlayingArtwork.setImage(null);
        }
        timeSlider.setValue(0);
        timeSlider.setBlockIncrement(1);

    }

    public void updateTimeSlider() {
        timeSlider.increment();
    }


    public void updateTimeLabels() {
        timePassed.setText(NewPlayer.getInstance().getTimePassed());
        timeRemaining.setText(NewPlayer.getInstance().getTimeRemaining());
    }

    @SuppressWarnings("unchecked")
    private void initializePlaylists() {

        for (Playlist playlist : NewPlayer.getInstance().getPlaylists()) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Constants.FXML + "PlaylistCell.fxml"));
                HBox cell = loader.load();
                Label label = (Label) cell.getChildren().get(1);
                label.setText(playlist.getTitle());

                cell.setOnMouseClicked(x -> {
                    selectView(x);
                    ((PlaylistsController) subViewController).selectPlaylist(playlist);
                });

                cell.setOnDragDetected(event -> {
                    PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
                    cell.pseudoClassStateChanged(pressed, false);
                    Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("Playlist");
                    db.setContent(content);
                    Bootstrap.setDraggedItem(playlist);
                    db.setDragView(cell.snapshot(null, null), 125, 25);
                    event.consume();
                });

                PseudoClass hover = PseudoClass.getPseudoClass("hover");

                cell.setOnDragEntered(event -> {
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        cell.pseudoClassStateChanged(hover, true);
                        //cell.getStyleClass().setAll("sideBarItemSelected");
                    }
                });

                cell.setOnDragExited(event -> {
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        cell.pseudoClassStateChanged(hover, false);
                        //cell.getStyleClass().setAll("sideBarItem");
                    }
                });

                cell.setOnDragOver(event -> {
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        event.acceptTransferModes(TransferMode.ANY);
                    }
                    event.consume();
                });

                cell.setOnDragDropped(event -> {
                    String dragString = event.getDragboard().getString();
                    new Thread(() -> {
                        switch (dragString) {
                            case "Artist":
                                Artist artist = (Artist) Bootstrap.getDraggedItem();
                                for (Album album : artist.getAlbums()) {
                                    for (Song song : album.getSongs()) {
                                        if (!playlist.getSongs().contains(song)) {
                                            playlist.addSong(song);
                                        }
                                    }
                                }
                                break;
                            case "Album":
                                Album album = (Album) Bootstrap.getDraggedItem();
                                for (Song song : album.getSongs()) {
                                    if (!playlist.getSongs().contains(song)) {
                                        playlist.addSong(song);
                                    }
                                }
                                break;
                            case "Playlist":
                                Playlist list = (Playlist) Bootstrap.getDraggedItem();
                                for (Song song : list.getSongs()) {
                                    if (!playlist.getSongs().contains(song)) {
                                        playlist.addSong(song);
                                    }
                                }
                                break;
                            case "Song":
                                Song song = (Song) Bootstrap.getDraggedItem();
                                if (!playlist.getSongs().contains(song)) {
                                    playlist.addSong(song);
                                }
                                break;
                            case "List":
                                ObservableList<Song> songs = (ObservableList<Song>) Bootstrap.getDraggedItem();
                                for (Song s : songs) {
                                    if (!playlist.getSongs().contains(s)) {
                                        playlist.addSong(s);
                                    }
                                }
                                break;
                        }
                    }).start();

                    event.consume();
                });
                playlistBox.getChildren().add(cell);
            } catch (Exception e) {
                log.error("initialize playlists failed", e);
            }
        }
    }

    @FXML
    private void selectView(Event e) {
        HBox eventSource = ((HBox) e.getSource());
        eventSource.requestFocus();
        Optional<Node> previous = sideBar.getChildren().stream()
                .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox) previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        } else {
            previous = playlistBox.getChildren().stream()
                    .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();
            if (previous.isPresent()) {
                HBox previousItem = (HBox) previous.get();
                previousItem.getStyleClass().setAll("sideBarItem");
            }
        }

        ObservableList<String> styles = eventSource.getStyleClass();

        if (styles.get(0).equals("sideBarItem")) {
            styles.setAll("sideBarItemSelected");
            loadView(eventSource.getId());
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource.getId());
        }
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void newPlaylist() {
        if (!newPlaylistAnimation.getStatus().equals(Status.RUNNING)) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Constants.FXML + "PlaylistCell.fxml"));
                HBox cell = loader.load();

                Label label = (Label) cell.getChildren().get(1);
                label.setVisible(false);
                HBox.setMargin(label, new Insets(0, 0, 0, 0));

                TextField textBox = new TextField();
                textBox.setPrefHeight(30);
                cell.getChildren().add(textBox);
                HBox.setMargin(textBox, new Insets(10, 10, 10, 9));

                textBox.focusedProperty().addListener((obs, oldValue, newValue) -> {
                    if (oldValue && !newValue) {
                        String text = textBox.getText().isEmpty() ? "New Playlist" : textBox.getText();
                        text = checkDuplicatePlaylist(text, 0);
                        label.setText(text);
                        cell.getChildren().remove(textBox);
                        HBox.setMargin(label, new Insets(10, 10, 10, 10));
                        label.setVisible(true);
                        AppConfig.getInstance().getMusicSource().createPlaylist(text);
                    }
                });

                textBox.setOnKeyPressed(x -> {
                    if (x.getCode() == KeyCode.ENTER) {
                        sideBar.requestFocus();
                    }
                });
                String playlistId = (String) label.getUserData();
                cell.setOnMouseClicked(x -> {
                    selectView(x);
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    ((PlaylistsController) subViewController).selectPlaylist(playlist);
                });

                cell.setOnDragDetected(event -> {
                    PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
                    cell.pseudoClassStateChanged(pressed, false);
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("Playlist");
                    db.setContent(content);
                    Bootstrap.setDraggedItem(playlist);
                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setTransform(Transform.scale(1.5, 1.5));
                    db.setDragView(cell.snapshot(sp, null));
                    event.consume();
                });

                PseudoClass hover = PseudoClass.getPseudoClass("hover");

                cell.setOnDragEntered(event -> {
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        cell.pseudoClassStateChanged(hover, true);
                    }
                });

                cell.setOnDragExited(event -> {
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        cell.pseudoClassStateChanged(hover, false);
                    }
                });

                cell.setOnDragOver(event -> {
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    if (!(playlist instanceof MostPlayedPlaylist)
                            && !(playlist instanceof RecentlyPlayedPlaylist)
                            && event.getGestureSource() != cell
                            && event.getDragboard().hasString()) {

                        event.acceptTransferModes(TransferMode.ANY);
                    }
                    event.consume();
                });

                cell.setOnDragDropped(event -> {
                    Playlist playlist = AppConfig.getInstance().getMusicSource().getPlaylist(playlistId);
                    String dragString = event.getDragboard().getString();
                    new Thread(() -> {
                        switch (dragString) {
                            case "Artist":
                                Artist artist = (Artist) Bootstrap.getDraggedItem();
                                for (Album album : artist.getAlbums()) {
                                    for (Song song : album.getSongs()) {
                                        if (!playlist.getSongs().contains(song)) {
                                            playlist.addSong(song);
                                        }
                                    }
                                }
                                break;
                            case "Album":
                                Album album = (Album) Bootstrap.getDraggedItem();
                                for (Song song : album.getSongs()) {
                                    if (!playlist.getSongs().contains(song)) {
                                        playlist.addSong(song);
                                    }
                                }
                                break;
                            case "Playlist":
                                Playlist list = (Playlist) Bootstrap.getDraggedItem();
                                for (Song song : list.getSongs()) {
                                    if (!playlist.getSongs().contains(song)) {
                                        playlist.addSong(song);
                                    }
                                }
                                break;
                            case "Song":
                                Song song = (Song) Bootstrap.getDraggedItem();
                                if (!playlist.getSongs().contains(song)) {
                                    playlist.addSong(song);
                                }
                                break;
                            case "List":
                                ObservableList<Song> songs = (ObservableList<Song>) Bootstrap.getDraggedItem();
                                for (Song s : songs) {
                                    if (!playlist.getSongs().contains(s)) {
                                        playlist.addSong(s);
                                    }
                                }
                                break;
                        }
                    }).start();

                    event.consume();
                });

                cell.setPrefHeight(0);
                cell.setOpacity(0);

                playlistBox.getChildren().add(1, cell);

                textBox.requestFocus();
            } catch (Exception e) {
                log.error("error", e);
            }

            newPlaylistAnimation.play();
        }
    }

    private String checkDuplicatePlaylist(String text, int i) {
        for (Playlist playlist : NewPlayer.getInstance().getPlaylists()) {
            if (playlist.getTitle().equals(text)) {

                int index = text.lastIndexOf(' ') + 1;
                if (index != 0) {
                    try {
                        i = Integer.parseInt(text.substring(index));
                    } catch (Exception ex) {
                        // do nothing
                    }
                }

                i++;

                if (i == 1) {
                    text = checkDuplicatePlaylist(text + " " + i, i);
                } else {
                    text = checkDuplicatePlaylist(text.substring(0, index) + i, i);
                }
                break;
            }
        }

        return text;
    }

    public SubView loadView(String viewName) {
        try {

            boolean loadLetters;
            boolean unloadLetters;

            switch (viewName.toLowerCase()) {
                case "artists":
                case "artistsmain":
                case "albums":
                case "songs":
                    loadLetters = !(subViewController instanceof ArtistsController)
                            && !(subViewController instanceof ArtistsMainController)
                            && !(subViewController instanceof AlbumsController)
                            && !(subViewController instanceof SongsController);
                    unloadLetters = false;
                    break;
                default:
                    if (subViewController instanceof ArtistsController
                            || subViewController instanceof ArtistsMainController
                            || subViewController instanceof AlbumsController
                            || subViewController instanceof SongsController) {
                        loadLetters = false;
                        unloadLetters = true;
                    } else {
                        loadLetters = false;
                        unloadLetters = false;
                    }
                    break;
            }

            final boolean loadLettersFinal = loadLetters;
            final boolean unloadLettersFinal = unloadLetters;

            String fileName = viewName.substring(0, 1).toUpperCase() + viewName.substring(1) + ".fxml";

            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
            Node view = loader.load();

            CountDownLatch latch = new CountDownLatch(1);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        //Library.getSongs().stream().filter(Song::getSelected).forEach(x -> x.setSelected(false));
                        subViewRoot.setVisible(false);
                        subViewRoot.setContent(view);
                        subViewRoot.getContent().setOpacity(0);
                        latch.countDown();
                    });
                    return null;
                }
            };

            task.setOnSucceeded(x -> new Thread(() -> {
                try {
                    latch.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    subViewRoot.setVisible(true);
                    if (loadLettersFinal) {
                        loadLettersAnimation.play();
                    }
                    loadViewAnimation.play();
                });
            }).start());

            Thread thread = new Thread(task);

            unloadViewAnimation.setOnFinished(x -> thread.start());

            loadViewAnimation.setOnFinished(x -> viewLoadedLatch.countDown());

            if (subViewRoot.getContent() != null) {
                if (unloadLettersFinal) {
                    unloadLettersAnimation.play();
                }
                unloadViewAnimation.play();
            } else {
                subViewRoot.setContent(view);
                if (loadLettersFinal) {
                    loadLettersAnimation.play();
                }
                loadViewAnimation.play();
            }

            subViewController = loader.getController();
            return subViewController;

        } catch (Exception ex) {
            log.error("load subview error", ex);
            return null;
        }
    }

    @FXML
    private void navigateToCurrentSong() {

        Optional<Node> previous = sideBar.getChildren().stream()
                .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox) previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        } else {
            previous = playlistBox.getChildren().stream()
                    .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();
            if (previous.isPresent()) {
                HBox previousItem = (HBox) previous.get();
                previousItem.getStyleClass().setAll("sideBarItem");
            }
        }

        sideBar.getChildren().get(2).getStyleClass().setAll("sideBarItemSelected");

        ArtistsMainController artistsMainController = (ArtistsMainController) loadView("ArtistsMain");

        Song song = NewPlayer.getInstance().nowPlaying();
        Artist artist = AppConfig.getInstance().getMusicSource().getArtist(song.getArtistId());
        Album album = artist.getAlbums().stream().filter(x -> x.getTitle().equals(song.getAlbum())).findFirst().get();
        artistsMainController.selectArtist(artist);
        artistsMainController.selectAlbum(album);
        artistsMainController.selectSong(song);
    }

    @FXML
    private void slideSideBar(Event e) {
        sideBar.requestFocus();
        searchBox.setText("");
        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
    }

    private void collapseSideBar() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
                && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            collapseAnimation.play();
        }
    }

    private void expandSideBar() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
                && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            expandAnimation.play();
        }
    }

    @FXML
    public void playPause() {
        sideBar.requestFocus();
        if (!NewPlayer.getInstance().isPlaying()) {
            NewPlayer.getInstance().play();
        } else {
            NewPlayer.getInstance().pause();
        }
    }

    @FXML
    private void back() {
        sideBar.requestFocus();
        NewPlayer.getInstance().back();
    }

    @FXML
    private void skip() {
        sideBar.requestFocus();
        NewPlayer.getInstance().skip();
    }

    @FXML
    private void letterClicked(Event e) {
        sideBar.requestFocus();
        Label eventSource = ((Label) e.getSource());
        char letter = eventSource.getText().charAt(0);
        subViewController.scroll(letter);
    }

    public void volumeClick() {
        if (!volumePopup.isShowing()) {
            Stage stage = Bootstrap.getStage();
            volumePopup.setX(stage.getX() + stage.getWidth() - 265);
            volumePopup.setY(stage.getY() + stage.getHeight() - 115);
            volumePopup.show();
            volumeShowAnimation.play();
        }
    }

    public void showSearchResults(SearchResult result) {
        VBox root = (VBox) searchPopup.getScene().getRoot();
        ObservableList<Node> list = root.getChildren();
        list.clear();
        if (!result.getArtistResults().isEmpty()) {
            Label header = new Label("Artists");
            list.add(header);
            VBox.setMargin(header, new Insets(10, 10, 10, 10));
            result.getArtistResults().forEach(artist -> {
                HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
                ImageView image = new ImageView();
                image.setFitHeight(40);
                image.setFitWidth(40);
                image.setImage(artist.artistImageProperty().getValue());
                Label label = new Label(artist.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
                cell.getChildren().addAll(image, label);
                HBox.setMargin(image, new Insets(5, 5, 5, 5));
                HBox.setMargin(label, new Insets(10, 10, 10, 5));
                cell.getStyleClass().add("searchResult");
                cell.setOnMouseClicked(event -> {
                    loadView("ArtistsMain");
                    ArtistsMainController artistsMainController = (ArtistsMainController) loadView("ArtistsMain");
                    artistsMainController.selectArtist(artist);
                    searchBox.setText("");
                    sideBar.requestFocus();
                });
                list.add(cell);
            });
            Separator separator = new Separator();
            separator.setPrefWidth(206);
            list.add(separator);
            VBox.setMargin(separator, new Insets(10, 10, 0, 10));
        }
        if (!result.getAlbumResults().isEmpty()) {
            Label header = new Label("Albums");
            list.add(header);
            VBox.setMargin(header, new Insets(10, 10, 10, 10));
            result.getAlbumResults().forEach(album -> {
                HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
                ImageView image = new ImageView();
                image.setFitHeight(40);
                image.setFitWidth(40);
                image.setImage(album.getArtwork());
                Label label = new Label(album.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
                cell.getChildren().addAll(image, label);
                HBox.setMargin(image, new Insets(5, 5, 5, 5));
                HBox.setMargin(label, new Insets(10, 10, 10, 5));
                cell.getStyleClass().add("searchResult");
                cell.setOnMouseClicked(event -> {
                    loadView("ArtistsMain");
                    Artist artist = AppConfig.getInstance().getMusicSource().getArtist(album.getArtistId());
                    ArtistsMainController artistsMainController = (ArtistsMainController) loadView("ArtistsMain");
                    artistsMainController.selectArtist(artist);
                    artistsMainController.selectAlbum(album);
                    searchBox.setText("");
                    sideBar.requestFocus();
                });
                list.add(cell);
            });
            Separator separator = new Separator();
            separator.setPrefWidth(206);
            list.add(separator);
            VBox.setMargin(separator, new Insets(10, 10, 0, 10));
        }
        if (!result.getSongResults().isEmpty()) {
            Label header = new Label("Songs");
            list.add(header);
            VBox.setMargin(header, new Insets(10, 10, 10, 10));
            result.getSongResults().forEach(song -> {
                HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
                Label label = new Label(song.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
                cell.getChildren().add(label);
                HBox.setMargin(label, new Insets(10, 10, 10, 10));
                cell.getStyleClass().add("searchResult");
                cell.setOnMouseClicked(event -> {
                    loadView("ArtistsMain");
                    Artist artist = AppConfig.getInstance().getMusicSource().getArtist(song.getArtistId());
                    Album album = artist.getAlbums().stream().filter(x -> x.getTitle().equals(song.getAlbum())).findFirst().get();
                    ArtistsMainController artistsMainController = (ArtistsMainController) loadView("ArtistsMain");
                    artistsMainController.selectArtist(artist);
                    artistsMainController.selectAlbum(album);
                    artistsMainController.selectSong(song);
                    searchBox.setText("");
                    sideBar.requestFocus();
                });
                list.add(cell);
            });
        }
        if (list.isEmpty()) {
            Label label = new Label("No Results");
            list.add(label);
            VBox.setMargin(label, new Insets(10, 10, 10, 10));
        }
        if (!searchPopup.isShowing()) {
            Stage stage = Bootstrap.getStage();
            searchPopup.setX(stage.getX() + 18);
            searchPopup.setY(stage.getY() + 80);
            searchPopup.show();
            searchShowAnimation.play();
        }
    }

    public Slider getVolumeSlider() {
        return volumePopupController.getSlider();
    }

//    public boolean isTimeSliderPressed() {
//        return sliderSkin.p || sliderSkin.getTrack().isPressed();
//        return true;
//    }


    ScrollPane getScrollPane() {
        return this.subViewRoot;
    }

    VBox getPlaylistBox() {
        return playlistBox;
    }

    public void updatePlayPauseIcon(boolean isPlaying) {
        controlBox.getChildren().remove(1);
        if (isPlaying) {
            controlBox.getChildren().add(1, pauseButton);
        } else {
            controlBox.getChildren().add(1, playButton);
        }
    }

    private void setSlideDirection() {
        isSideBarExpanded = !isSideBarExpanded;
    }

    private final Animation volumeShowAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            volumePopup.setOpacity(frac);
        }
    };

    private final Animation volumeHideAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            volumePopup.setOpacity(1.0 - frac);
        }
    };

    private final Animation searchShowAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            searchPopup.setOpacity(frac);
        }
    };

    private final Animation searchHideAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            searchPopup.setOpacity(1.0 - frac);
        }
    };

    private final Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
            setOnFinished(x -> setSlideDirection());
        }

        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            double searchWidth = searchCollapsed + (searchExpanded - searchCollapsed) * (1.0 - frac);
            sideBar.setPrefWidth(curWidth);
            searchBox.setPrefWidth(searchWidth);
            searchBox.setOpacity(1.0 - frac);
        }
    };

    private final Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
            setOnFinished(x -> setSlideDirection());
        }

        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            double searchWidth = searchCollapsed + (searchExpanded - searchCollapsed) * (frac);
            sideBar.setPrefWidth(curWidth);
            searchBox.setPrefWidth(searchWidth);
            searchBox.setOpacity(frac);
        }
    };

    private final Animation loadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            subViewRoot.setVvalue(0);
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(frac);
        }
    };

    private final Animation unloadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(1 - frac);
        }
    };

    private final Animation loadLettersAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            letterBox.setPrefHeight(50);
            letterBox.setOpacity(frac);
            letterSeparator.setPrefHeight(25);
            letterSeparator.setOpacity(frac);
        }
    };

    private final Animation unloadLettersAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            letterBox.setOpacity(1.0 - frac);
            letterSeparator.setOpacity(1.0 - frac);
        }
    };

    private final Animation newPlaylistAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(500));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            HBox cell = (HBox) playlistBox.getChildren().get(1);
            if (frac < 0.5) {
                cell.setPrefHeight(frac * 100);
            } else {
                cell.setPrefHeight(50);
                cell.setOpacity((frac - 0.5) * 2);
            }
        }
    };

}

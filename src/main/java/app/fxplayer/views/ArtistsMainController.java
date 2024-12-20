package app.fxplayer.views;

import app.fxplayer.AppConfig;
import app.fxplayer.Bootstrap;
import app.fxplayer.NewPlayer;
import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Song;
import app.fxplayer.util.ClippedTableCell;
import app.fxplayer.util.ControlPanelTableCell;
import app.fxplayer.util.PlayingTableCell;
import app.fxplayer.util.SubView;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class ArtistsMainController implements Initializable, SubView {

    private static final Logger log = LoggerFactory.getLogger(ArtistsMainController.class);

    private class ArtistCell extends ListCell<Artist> {

        private final HBox cell = new HBox();
        private final ImageView artistImage = new ImageView();
        private final Label title = new Label();
        private Artist artist;

        ArtistCell() {
            super();
            artistImage.setFitWidth(40);
            artistImage.setFitHeight(40);
            artistImage.setPreserveRatio(true);
            artistImage.setSmooth(true);
            artistImage.setCache(true);
            title.setTextOverrun(OverrunStyle.CLIP);
            cell.getChildren().addAll(artistImage, title);
            cell.setAlignment(Pos.CENTER_LEFT);
            HBox.setMargin(artistImage, new Insets(0, 10, 0, 0));
            this.setPrefWidth(248);

            this.setOnMouseClicked(event -> artistList.getSelectionModel().select(artist));

            this.setOnDragDetected(event -> {
                Dragboard db = this.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("Artist");
                db.setContent(content);
                Bootstrap.setDraggedItem(artist);
                db.setDragView(this.snapshot(null, null), 125, 25);
                event.consume();
            });
        }

        @Override
        protected void updateItem(Artist artist, boolean empty) {

            super.updateItem(artist, empty);
            this.artist = artist;

            if (empty) {

                setGraphic(null);

            } else {

                title.setText(artist.getTitle());
                artistImage.imageProperty().bind(artist.artistImageProperty());
                setGraphic(cell);
            }
        }
    }

    private class AlbumCell extends ListCell<Album> {

        private final ImageView albumArtwork = new ImageView();
        private Album album;

        AlbumCell() {
            super();
            setAlignment(Pos.CENTER);
            setPrefHeight(140);
            setPrefWidth(140);
            albumArtwork.setFitWidth(130);
            albumArtwork.setFitHeight(130);
            albumArtwork.setPreserveRatio(true);
            albumArtwork.setSmooth(true);
            albumArtwork.setCache(true);
            //albumArtwork.setImage(album.getArtwork());
            this.setOnMouseClicked(event -> albumList.getSelectionModel().select(album));

            this.setOnDragDetected(event -> {
                Dragboard db = this.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("Album");
                db.setContent(content);
                Bootstrap.setDraggedItem(album);
                db.setDragView(this.snapshot(null, null), 75, 75);
                event.consume();
            });
        }

        @Override
        protected void updateItem(Album album, boolean empty) {
            super.updateItem(album, empty);
            this.album = album;
            if (empty) {
                setGraphic(null);
            } else {
                log.info(album.getArtwork().getUrl());
                albumArtwork.setImage(album.getArtwork());
                setGraphic(albumArtwork);
            }
        }
    }

    @FXML
    private ListView<Artist> artistList;
    @FXML
    private ListView<Album> albumList;
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, Boolean> playingColumn;
    @FXML
    private TableColumn<Song, String> titleColumn;
    @FXML
    private TableColumn<Song, String> lengthColumn;
    @FXML
    private TableColumn<Song, Integer> playsColumn;
    @FXML
    private Label artistLabel;
    @FXML
    private Label albumLabel;
    @FXML
    private Separator separator;
    @FXML
    private VBox subViewRoot;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane artistListScrollPane;

    @Getter
    private Song selectedSong;
    private Album selectedAlbum;
    private Artist selectedArtist;
    private final double expandedHeight = 50;
    private final double collapsedHeight = 0;
    private CountDownLatch loadedLatch;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        songTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        loadedLatch = new CountDownLatch(1);

        artistLoadAnimation.setOnFinished(x -> loadedLatch.countDown());

        albumLoadAnimation.setOnFinished(x -> loadedLatch.countDown());

        titleColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));

        playingColumn.setCellFactory(x -> new PlayingTableCell<>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<>("playCount"));

        albumList.setCellFactory(listView -> new AlbumCell());
        artistList.setCellFactory(listView -> new ArtistCell());

        artistList.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

        albumList.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

        songTable.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            songTable.requestFocus();
            event.consume();
        });

        ObservableList<Artist> artists = FXCollections.observableArrayList(AppConfig.getInstance().getMusicSource().listArtists());
        Collections.sort(artists);

        artistList.setItems(artists);

        artistList.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                ObservableList<Song> songs = FXCollections.observableArrayList();
                ObservableList<Album> albums = FXCollections.observableArrayList();
                for (Album album : selectedArtist.getAlbums()) {
                    albums.add(album);
                    songs.addAll(album.getSongs());
                }
                NewPlayer player = NewPlayer.getInstance();
                songs.sort((first, second) -> {
                    Album firstAlbum = albums.stream().filter(x -> x.getTitle().equals(first.getAlbum())).findFirst().get();
                    Album secondAlbum = albums.stream().filter(x -> x.getTitle().equals(second.getAlbum())).findFirst().get();
                    if (firstAlbum.compareTo(secondAlbum) != 0) {
                        return firstAlbum.compareTo(secondAlbum);
                    } else {
                        return first.compareTo(second);
                    }
                });

                Song song = songs.get(0);
                player.updatePlaylist(songs);
                player.play(song);

            } else {
                Thread thread = getThread();
                artistUnloadAnimation.setOnFinished(x -> thread.start());
                artistUnloadAnimation.play();
            }
        });

        albumList.setOnMouseClicked(event -> {
            Album album = albumList.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (album != selectedAlbum) {
                    selectAlbum(album);
                }
                ArrayList<Song> songs = selectedAlbum.getSongs();
                NewPlayer player = NewPlayer.getInstance();
                Collections.sort(songs);
                player.updatePlaylist(songs);
                player.play();
            } else {
                Thread thread = getThread(album);
                albumUnloadAnimation.setOnFinished(x -> thread.start());
                albumUnloadAnimation.play();
            }
        });

        songTable.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<>();
            PseudoClass playing = PseudoClass.getPseudoClass("playing");
            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> {
                row.pseudoClassStateChanged(playing, newValue);
            };

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
                TableViewSelectionModel<Song> sm = songTable.getSelectionModel();
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
                if (songTable.getSelectionModel().getSelectedIndices().size() > 1) {
                    content.putString("List");
                    db.setContent(content);
                    Bootstrap.setDraggedItem(songTable.getSelectionModel().getSelectedItems());
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

            return row;
        });

        songTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                oldSelection.setSelected(false);
            }
            if (newSelection != null && songTable.getSelectionModel().getSelectedIndices().size() == 1) {
                newSelection.setSelected(true);
                selectedSong = newSelection;
            }
        });

        // Plays selected song when enter key is pressed.
        songTable.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                play();
            }
        });

        artistList.setMinHeight(0);
        artistList.setPrefHeight(0);
        double height = artists.size() * 50;
        Animation artistListLoadAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
                setInterpolator(Interpolator.EASE_BOTH);
            }

            protected void interpolate(double frac) {
                artistList.setMinHeight(frac * height);
                artistList.setPrefHeight(frac * height);
            }
        };
        artistListLoadAnimation.play();
    }

    private Thread getThread() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    subViewRoot.setVisible(false);
                    selectedArtist = artistList.getSelectionModel().getSelectedItem();
                    showAllSongs(selectedArtist, false);
                    artistLabel.setText(selectedArtist.getTitle());
                    albumList.setPrefWidth(albumList.getItems().size() * 150 + 2);
                    albumList.setMaxWidth(albumList.getItems().size() * 150 + 2);
                    albumList.scrollTo(0);
                });
                return null;
            }
        };

        task.setOnSucceeded(x -> Platform.runLater(() -> {
            subViewRoot.setVisible(true);
            artistLoadAnimation.play();
        }));
        return new Thread(task);
    }

    private Thread getThread(Album album) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    songTable.setVisible(false);
                    selectAlbum(album);
                });
                return null;
            }
        };

        task.setOnSucceeded(x -> Platform.runLater(() -> {
            songTable.setVisible(true);
            albumLoadAnimation.play();
        }));

        return new Thread(task);
    }

    void selectAlbum(Album album) {
        if (selectedAlbum == album) {
            albumList.getSelectionModel().clearSelection();
            showAllSongs(artistList.getSelectionModel().getSelectedItem(), false);
        } else {
            if (selectedSong != null) {
                selectedSong.setSelected(false);
            }
            selectedSong = null;
            selectedAlbum = album;
            albumList.getSelectionModel().select(selectedAlbum);
            ObservableList<Song> songs = FXCollections.observableArrayList();
            songs.addAll(album.getSongs());
            Collections.sort(songs);
            songTable.getSelectionModel().clearSelection();
            songTable.setItems(songs);
            scrollPane.setVvalue(0);
            albumLabel.setText(album.getTitle());
            songTable.setMinHeight(0);
            songTable.setPrefHeight(0);
            songTable.setVisible(true);
            double height = (songs.size() + 1) * 50 + 2;
            Animation songTableLoadAnimation = new Transition() {
                {
                    setCycleDuration(Duration.millis(250));
                    setInterpolator(Interpolator.EASE_BOTH);
                }

                protected void interpolate(double frac) {
                    songTable.setMinHeight(frac * height);
                    songTable.setPrefHeight(frac * height);
                }
            };
            new Thread(() -> {
                try {
                    loadedLatch.await();
                    loadedLatch = new CountDownLatch(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                songTableLoadAnimation.play();
            }).start();
        }
    }

    void selectArtist(Artist artist) {

        selectedArtist = artist;
        artistList.getSelectionModel().select(artist);
        CountDownLatch latch = new CountDownLatch(1);
        artistListScrollPane.heightProperty().addListener((x, y, z) -> {
            if (z.doubleValue() != 0) {
                latch.countDown();
            }
        });
        new Thread(() -> {
            try {
                latch.await();
                int selectedCell = artistList.getSelectionModel().getSelectedIndex();
                List<Artist> artists = AppConfig.getInstance().getMusicSource().listArtists();
                double vValue = (selectedCell * 50) / (artists.size() * 50 - artistListScrollPane.getHeight());
                artistListScrollPane.setVvalue(vValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        showAllSongs(artist, true);
        albumList.setPrefWidth(artist.getAlbums().size() * 150 + 2);
        albumList.setMaxWidth(artist.getAlbums().size() * 150 + 2);
        artistLabel.setText(artist.getTitle());
        separator.setVisible(true);
    }

    void selectSong(Song song) {

        new Thread(() -> {
            try {
                loadedLatch.await();
                loadedLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    songTable.getSelectionModel().select(song);
                    scrollPane.requestFocus();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showAllSongs(Artist artist, boolean fromMainController) {

        ObservableList<Album> albums = FXCollections.observableArrayList();
        ObservableList<Song> songs = FXCollections.observableArrayList();
        for (Album album : AppConfig.getInstance().getMusicSource().listAlbumsByArtist(artist.getId())) {
            albums.add(album);
            songs.addAll(album.getSongs());
        }

        songs.sort((first, second) -> {
            Album firstAlbum = albums.stream().filter(x -> x.getTitle().equals(first.getAlbum())).findFirst().get();
            Album secondAlbum = albums.stream().filter(x -> x.getTitle().equals(second.getAlbum())).findFirst().get();
            if (firstAlbum.compareTo(secondAlbum) != 0) {
                return firstAlbum.compareTo(secondAlbum);
            } else {
                return first.compareTo(second);
            }
        });

        Collections.sort(albums);

        if (selectedSong != null) {
            selectedSong.setSelected(false);
        }
        selectedSong = null;
        selectedAlbum = null;
        albumList.getSelectionModel().clearSelection();
        albumList.setItems(albums);
        songTable.setItems(songs);
        songTable.getSelectionModel().clearSelection();
        scrollPane.setVvalue(0);
        albumLabel.setText("All Songs");
        songTable.setMinHeight(0);
        songTable.setPrefHeight(0);
        songTable.setVisible(true);
        Animation songTableLoadAnimation = getAnimation(songs);

        new Thread(() -> {
            try {
                if (fromMainController) {
                    Bootstrap.getMainController().getLatch().await();
                    Bootstrap.getMainController().resetLatch();
                } else {
                    loadedLatch.await();
                    loadedLatch = new CountDownLatch(1);
                }
            } catch (Exception e) {
                log.error("error", e);
            }
            songTableLoadAnimation.play();
        }).start();
    }

    private Animation getAnimation(ObservableList<Song> songs) {
        double height = (songs.size() + 1) * 50 + 2;
        Animation songTableLoadAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
                setInterpolator(Interpolator.EASE_BOTH);
            }

            protected void interpolate(double frac) {
                songTable.setMinHeight(frac * height);
                songTable.setPrefHeight(frac * height);
            }
        };

        songTableLoadAnimation.setOnFinished(x -> loadedLatch.countDown());
        return songTableLoadAnimation;
    }

    @Override
    public void play() {

        Song song = selectedSong;
        ArrayList<Song> songs = new ArrayList<>();

        if (selectedAlbum != null) {
            songs.addAll(selectedAlbum.getSongs());
        } else {
            for (Album album : selectedArtist.getAlbums()) {
                songs.addAll(album.getSongs());
            }
        }
        NewPlayer player = NewPlayer.getInstance();

        songs.sort((first, second) -> {
            Album firstAlbum = first.getAlbumObj();
            Album secondAlbum = second.getAlbumObj();
            if (firstAlbum.compareTo(secondAlbum) != 0) {
                return firstAlbum.compareTo(secondAlbum);
            } else {
                return first.compareTo(second);
            }
        });

        player.updatePlaylist(songs);
        player.play(song);
    }

    @Override
    public void scroll(char letter) {
        ObservableList<Artist> artistListItems = artistList.getItems();
        int selectedCell = 0;
        for (Artist artist : artistListItems) {
            // Removes article from artist title and compares it to selected letter.
            String artistTitle = artist.getTitle();
            char firstLetter = removeArticle(artistTitle).charAt(0);
            if (firstLetter < letter) {
                selectedCell++;
            }
        }

        double startVvalue = artistListScrollPane.getVvalue();
        List<Artist> artists = AppConfig.getInstance().getMusicSource().listArtists();
        double finalVvalue = (double) (selectedCell * 50) / (artists.size() * 50 - artistListScrollPane.getHeight());

        Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }

            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                artistListScrollPane.setVvalue(vValue);
            }
        };
        scrollAnimation.play();
    }

    private String removeArticle(String title) {
        String[] arr = title.split(" ", 2);
        if (arr.length < 2) {
            return title;
        } else {
            String firstWord = arr[0];
            String theRest = arr[1];
            return switch (firstWord) {
                case "A", "An", "The" -> theRest;
                default -> title;
            };
        }
    }

    private final Animation artistLoadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            subViewRoot.setTranslateY(expandedHeight - curHeight);
            subViewRoot.setOpacity(frac);
        }
    };

    private final Animation artistUnloadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            subViewRoot.setTranslateY(expandedHeight - curHeight);
            subViewRoot.setOpacity(1 - frac);
        }
    };

    private final Animation albumLoadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            songTable.setTranslateY(expandedHeight - curHeight);
            songTable.setOpacity(frac);
        }
    };

    private final Animation albumUnloadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            songTable.setTranslateY(expandedHeight - curHeight);
            songTable.setOpacity(1 - frac);
            songTable.setMinHeight(1 - frac);
            songTable.setPrefHeight(1 - frac);
        }
    };
}

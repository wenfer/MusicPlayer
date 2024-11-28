package app.fxplayer;

import javafx.scene.image.Image;

public interface Constants {
    String SOURCE_INFO = "source_info";

    String SOURCE_SERVER_URL = "source_server_url";
    String SOURCE_USERNAME = "source_username";
    String SOURCE_PASSWORD = "source_password";

    String FXML = "/app/fxplayer/views/";
    String IMG = "/app/fxplayer/img/";
    String CSS = "/app/fxplayer/css/";

    Image DEFAULT_ALBUM = new Image(IMG + "albumsIcon.png");
    Image DEFAULT_ARTISTS = new Image(IMG + "artistsIcon.png");
    Image DEFAULT_SONGS_ICON = new Image(IMG + "songsIcon.png");

}

package app.musicplayer.views;

import app.musicplayer.Config;
import app.musicplayer.DbConstants;
import app.musicplayer.model.Library;
import app.musicplayer.util.ImportMusicTask;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to import music library.
 */
public class ImportMusicDialogController {


    private static final Logger log = LoggerFactory.getLogger(ImportMusicDialogController.class);
    @Setter
    private Config config;


    @FXML
    public Label errorLabel;
    @FXML
    public TextField serverUrl;
    @FXML
    public TextField username;
    @FXML
    public TextField password;
    @FXML
    private Label label;
    @FXML
    private Button importMusicButton;
    @FXML
    private ProgressBar progressBar;


    @Setter
    private Stage dialogStage;

    @Getter
    private boolean musicImported = false;

    @FXML
    private void handleImport() {
        try {
            String serverUrlText = serverUrl.getText();
            String usernameText = username.getText();
            String passwordText = password.getText();
            // 清除之前的错误信息
            errorLabel.setText("");

            if (serverUrlText == null || serverUrlText.trim().isEmpty()) {
                errorLabel.setText("服务器地址不能为空！");
                return;
            }
            if (usernameText == null || usernameText.trim().isEmpty()) {
                errorLabel.setText("用户名不能为空！");
                return;
            }
            if (passwordText == null || passwordText.trim().isEmpty()) {
                errorLabel.setText("密码不能为空！");
                return;
            }
            config.set(DbConstants.SOURCE_SERVER_URL, serverUrlText);
            config.set(DbConstants.SOURCE_USERNAME, usernameText);
            config.set(DbConstants.SOURCE_PASSWORD, passwordText);
            // Makes the import music button invisible and the progress bar visible.
            // This happens as soon as the music import task is started.
            importMusicButton.setVisible(false);
            progressBar.setVisible(true);
        } catch (Exception e) {
            log.error("获取音乐源信息失败");
        }
    }

}

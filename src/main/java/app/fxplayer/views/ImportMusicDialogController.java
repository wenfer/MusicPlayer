package app.fxplayer.views;

import app.fxplayer.AppConfig;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static app.fxplayer.Constants.SOURCE_INFO;

/**
 * Dialog to import music library.
 */
public class ImportMusicDialogController implements Initializable {


    private static final Logger log = LoggerFactory.getLogger(ImportMusicDialogController.class);
    private final AppConfig appConfig = AppConfig.getInstance();


    @FXML
    public Label errorLabel;
    @FXML
    public TextField name;
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
            String nameText = name.getText();
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
            if (appConfig.createMusicSource(nameText, serverUrlText, usernameText, passwordText)) {
                importMusicButton.setVisible(false);
                progressBar.setVisible(true);
                musicImported = true;
                dialogStage.close();
            } else {
                errorLabel.setText("连接失败！");
            }
        } catch (Exception e) {
            log.error("获取音乐源信息失败");
            throw new RuntimeException("连接失败");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String sourceInfo = appConfig.get(SOURCE_INFO);
        if (!StrUtil.isBlank(sourceInfo)) {
            JSONObject jsonObject = new JSONObject();
            name.setText(jsonObject.getString("name"));
            serverUrl.setText(jsonObject.getString("serverUrl"));
            username.setText(jsonObject.getString("username"));
            password.setText(jsonObject.getString("password"));
        }

    }
}

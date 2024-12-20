package app.fxplayer.views;

import java.net.URL;
import java.util.ResourceBundle;

import app.fxplayer.Bootstrap;

import app.fxplayer.NewPlayer;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class VolumePopupController implements Initializable {

    @FXML
    private Slider volumeSlider;
    @FXML
    private Region frontVolumeTrack;
    @FXML
    private Label volumeLabel;
    @FXML
    private Pane muteButton;
    @FXML
    private Pane mutedButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            SliderSkin sliderSkin = new SliderSkin(volumeSlider);
            volumeSlider.setSkin(sliderSkin);
            frontVolumeTrack.prefWidthProperty().bind(volumeSlider.widthProperty().subtract(30).multiply(volumeSlider.valueProperty().divide(volumeSlider.maxProperty())));
            volumeSlider.valueProperty().addListener((x, y, z) -> {
                volumeLabel.setText(Integer.toString(z.intValue()));
            });
            volumeSlider.setOnMousePressed(x -> {
                if (mutedButton.isVisible()) {
                    muteClick();
                }
            });

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    Slider getSlider() {
        return volumeSlider;
    }

    @FXML
    private void volumeClick() {
        Bootstrap.getMainController().volumeClick();
    }

    @FXML
    private void muteClick() {

        PseudoClass muted = PseudoClass.getPseudoClass("muted");
        boolean isMuted = mutedButton.isVisible();
        muteButton.setVisible(isMuted);
        mutedButton.setVisible(!isMuted);
        volumeSlider.pseudoClassStateChanged(muted, !isMuted);
        frontVolumeTrack.pseudoClassStateChanged(muted, !isMuted);
        volumeLabel.pseudoClassStateChanged(muted, !isMuted);
        NewPlayer.getInstance().mute(isMuted);
    }
}

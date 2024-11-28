package app.fxplayer.model;

import app.fxplayer.AppConfig;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import lombok.Getter;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static app.fxplayer.Constants.DEFAULT_SONGS_ICON;

public final class Song implements Comparable<Song> {

    private static final Logger log = LoggerFactory.getLogger(Song.class);
    @Getter
    private final String id;
    @Getter
    private final Album albumObj;
    private final SimpleStringProperty title;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final SimpleStringProperty length;
    private final SimpleStringProperty format;
    @Getter
    private final long lengthInSeconds;
    private final int trackNumber;
    private final int discNumber;
    private final SimpleIntegerProperty playCount;
    private final SimpleBooleanProperty playing;
    private final SimpleBooleanProperty selected;
    @Getter
    private final long size;
    @Getter
    private final String artistId;
    private Image artwork;
    @Getter
    private final String firstPinyin;

    private static final HanyuPinyinOutputFormat hanyuPinyinOutputFormat = new HanyuPinyinOutputFormat();

    static {
        hanyuPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE); // 大写输出
        hanyuPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 无音标
    }

    public Song(String id, String title, String artist, String artistId, int length,
                int trackNumber, int discNumber, int playCount, long size, String coverArtId, Album album) {
        this.artistId = artistId;
        if (artist == null) {
            artist = "Unknown Artist";
        }
        this.format = new SimpleStringProperty("mp3");
        this.id = id;
        this.size = size;
        this.title = new SimpleStringProperty(title);
        this.firstPinyin = getFirstPinyin(title, hanyuPinyinOutputFormat);
        this.artist = new SimpleStringProperty(artist);
        if (album == null) {
            this.album = new SimpleStringProperty("Unknown Album");
        } else {
            this.album = new SimpleStringProperty(album.getTitle());
        }
        this.lengthInSeconds = length;
        Duration duration = Duration.of(length, ChronoUnit.SECONDS);
        long seconds = length % 60;
        this.length = new SimpleStringProperty(duration.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds));
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.playCount = new SimpleIntegerProperty(playCount);
        this.playing = new SimpleBooleanProperty(false);
        this.selected = new SimpleBooleanProperty(false);
        this.albumObj = album;
        CompletableFuture.supplyAsync(() ->
                        AppConfig.getInstance().getMusicSource().getCoverArt(coverArtId))
                .thenAccept((file) -> this.artwork = new Image(file.toURI().toString()));
    }


    // 获取字符串的拼音首字母
    private static String getFirstPinyin(String text, HanyuPinyinOutputFormat format) {
        char firstChar = text.charAt(0);

        // 判断是否为中文字符
        if (Character.toString(firstChar).matches("[\\u4E00-\\u9FA5]")) {
            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    return pinyinArray[0]; // 返回第一个拼音
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 如果不是中文字符，则直接返回自身的大写形式
        return Character.toString(firstChar).toUpperCase();
    }

    public String getTitle() {
        return this.title.get();
    }

    public StringProperty titleProperty() {
        return this.title;
    }

    public String getFormat() {
        return format.get();
    }

    public SimpleStringProperty formatProperty() {
        return format;
    }

    public String getArtist() {
        return this.artist.get();
    }

    public StringProperty artistProperty() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album.get();
    }

    public Image getArtwork() {
        return this.artwork == null ? DEFAULT_SONGS_ICON : this.artwork;
    }

    public StringProperty albumProperty() {
        return this.album;
    }

    public String getLength() {
        return this.length.get();
    }

    public StringProperty lengthProperty() {
        return this.length;
    }


    public int getPlayCount() {
        return this.playCount.get();
    }

    public IntegerProperty playCountProperty() {
        return this.playCount;
    }

    public BooleanProperty playingProperty() {
        return this.playing;
    }

    public boolean getPlaying() {
        return this.playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public BooleanProperty selectedProperty() {
        return this.selected;
    }

    public boolean getSelected() {
        return this.selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public void played() {
        this.playCount.set(this.playCount.get() + 1);
    }

    @Override
    public int compareTo(Song other) throws NullPointerException {
        int discComparison = Integer.compare(this.discNumber, other.discNumber);

        if (discComparison != 0) {
            return discComparison;
        } else {
            return Integer.compare(this.trackNumber, other.trackNumber);
        }
    }
}
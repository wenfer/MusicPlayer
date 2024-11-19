package app.fxplayer;

import app.fxplayer.model.Album;
import app.fxplayer.model.Artist;
import app.fxplayer.model.Settings;
import app.fxplayer.source.MusicSource;
import app.fxplayer.source.SonicClientSource;
import cn.hutool.core.util.StrUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static app.fxplayer.DbConstants.SOURCE_INFO;
import static app.fxplayer.DbConstants.SOURCE_SERVER_URL;


@Slf4j
public class AppConfig {

    @Setter
    @Getter
    private MusicSource musicSource;

    private final ConnectionSource connectionSource;

    private final Map<String, String> settingsCache = new HashMap<>();

    private final Dao<Settings, Integer> settingsDao;

    private static AppConfig instance;


    private AppConfig() {
        try {
            String databaseUrl = "jdbc:sqlite:music-player.db";
            connectionSource = new JdbcConnectionSource(databaseUrl);
            settingsDao = this.getDao(Settings.class);
            log.info("开始初始化数据库");
            TableUtils.createTableIfNotExists(connectionSource, Settings.class);
            //TableUtils.createTableIfNotExists(connectionSource, Album.class);
            //TableUtils.createTableIfNotExists(connectionSource, Artist.class);
            settingsDao.queryForAll().forEach(s -> settingsCache.put(s.getName(), s.getValue()));
            String sourceInfo = this.get(SOURCE_INFO);
            if (!StrUtil.isBlank(sourceInfo)) {
                JSONObject jsonObject = new JSONObject(sourceInfo);
                SonicClientSource musicSource = new SonicClientSource(
                        jsonObject.getString("name"),
                        jsonObject.getString("serverUrl"),
                        jsonObject.getString("username"),
                        jsonObject.getString("password"), 128);
                if (musicSource.ping()) {
                    this.musicSource = musicSource;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }


    public String get(String name) {
        return settingsCache.get(name);
    }


    public boolean createMusicSource(String name, String serverUrl, String username, String password) {
        this.musicSource = new SonicClientSource(name, serverUrl, username, password, 128);
        SonicClientSource musicSource = new SonicClientSource(
                name,
                serverUrl,
                username,
                password, 128);
        if (!musicSource.ping()) {
            return false;
        }
        this.musicSource  = musicSource;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("serverUrl",serverUrl);
        jsonObject.put("username",username);
        jsonObject.put("password",password);
        this.set(SOURCE_INFO,jsonObject.toString());
        return true;
    }


    public void set(String name, String value) {
        try {
            Settings setting = settingsDao.queryBuilder()
                    .where()
                    .eq("name", name)
                    .queryForFirst();
            if (setting == null) {
                setting = new Settings();
                setting.setName(name);
            }
            setting.setValue(value);
            settingsDao.createOrUpdate(setting);
            settingsCache.put(name, value);
        } catch (SQLException e) {
            log.error("set failed ", e);
        }
    }

    public <T> Dao<T, Integer> getDao(Class<T> clazz) {
        try {
            return DaoManager.createDao(connectionSource, clazz);
        } catch (SQLException e) {
            log.error("dao create failed ", e);
            throw new RuntimeException(e);
        }
    }

}

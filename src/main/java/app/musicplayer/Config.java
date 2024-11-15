package app.musicplayer;

import app.musicplayer.model.Settings;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Config {

    private final ConnectionSource connectionSource;

    private Map<String, String> settingsCache = new HashMap<>();

    private Dao<Settings, Integer> settingsDao = null;


    public Config() {
        try {
            String databaseUrl = "jdbc:sqlite:music-player.db";
            connectionSource = new JdbcConnectionSource(databaseUrl);
            settingsDao = this.getDao(Settings.class);

            settingsDao.queryForAll().forEach(s -> settingsCache.put(s.getName(), s.getValue()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public String get(String name) {
        return settingsCache.get(name);
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

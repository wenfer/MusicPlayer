package app.musicplayer.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "server")
@Setter
@Getter
public class Server {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String baseUrl;

}

package subude.gg.playerreporthelper.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private final File dbFile;

    private Connection connection;

    public DBManager(File folder) {
        this.dbFile = new File(folder, "reports.db");
    }

    public void connect() {
        try {
            if (!this.dbFile.getParentFile().exists())
                this.dbFile.getParentFile().mkdirs();
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        String reports = "CREATE TABLE IF NOT EXISTS reports (id INTEGER PRIMARY KEY AUTOINCREMENT,reporter TEXT,target TEXT,reason TEXT,date TEXT,status TEXT,handled_by TEXT,handled_date TEXT,response_time INTEGER);";
        String reputation = "CREATE TABLE IF NOT EXISTS reputation (player TEXT PRIMARY KEY,points INTEGER);";
        String moderatorStats = "CREATE TABLE IF NOT EXISTS moderator_stats (moderator TEXT PRIMARY KEY,accepted INTEGER,denied INTEGER,total_response_time INTEGER);";
        this.connection.createStatement().execute(moderatorStats);
        this.connection.createStatement().execute(reports);
        this.connection.createStatement().execute(reputation);
    }

    public Connection getConnection() {
        return this.connection;
    }
}

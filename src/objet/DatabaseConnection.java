package objet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private DatabaseConnection(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static DatabaseConnection getInstance(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection(dbUrl, dbUser, dbPassword);
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
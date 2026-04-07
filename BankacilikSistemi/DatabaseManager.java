package BankacilikSistemi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String url = "jdbc:mysql://localhost:3306/bankasistemi";
    private static final String user = System.getenv("DB_USER");
    private static final String dbPassword = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, dbPassword);
    }
}

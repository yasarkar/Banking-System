package BankacilikSistemi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String url = "jdbc:mysql://localhost:3306/bankasistemi";
    private static final String user = "yasar";
    private static final String dbPassword = "yakhhkay34";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, dbPassword);
    }
}

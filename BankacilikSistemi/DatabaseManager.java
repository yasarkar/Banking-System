package BankacilikSistemi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static final String PROPERTIES_FILE = "database.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();

        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Yapilandirma sinif yolundan yuklendi.");
                return;
            }
        } catch (IOException e) {
            System.err.println("Sinif yolundan yapilandirma okunamiyor: "
                    + e.getMessage());
        }

        try (InputStream is = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(is);
            System.out.println("Yapilandirma dosya sisteminden yuklendi.");
        } catch (IOException e) {
            System.err.println("Yapilandirma dosyasi bulunamadi: "
                    + PROPERTIES_FILE);
            System.err.println("Lutfen 'database.properties.example' dosyasini "
                    + "kopyalayip 'database.properties' olarak adlandirin "
                    + "ve bilgilerinizi girin.");
            throw new RuntimeException("Veritabani yapilandirmasi bulunamadi!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");

        if (url == null || user == null || dbPassword == null) {
            throw new SQLException(
                    "Veritabani yapilandirmasi eksik! "
                            + "Lutfen 'database.properties' dosyasini kontrol edin. "
                            + "Gerekli alanlar: db.url, db.user, db.password");
        }

        return DriverManager.getConnection(url, user, dbPassword);
    }
}

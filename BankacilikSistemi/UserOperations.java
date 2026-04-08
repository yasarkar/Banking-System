package BankacilikSistemi;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class UserOperations {

    private static final Scanner scanner = new Scanner(System.in);

    public static Scanner getScanner() {
        return scanner;
    }

    public static void register() {
        System.out.println("\n--- KAYIT OL ---");
        System.out.print("Kullanıcı Adı => ");
        String userName = scanner.nextLine();
        
        String email = validateEmailFormat();
        if (email == null) return; // İptal durumu

        String password;
        while (true) {
            System.out.print("Şifre => ");
            password = scanner.nextLine();
            System.out.print("Şifre (Tekrar) => ");
            String confirmPassword = scanner.nextLine();

            if (password.equals(confirmPassword)) {
                break;
            } else {
                System.out.println("Şifreler eşleşmiyor! Lütfen tekrar deneyiniz.");
            }
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String query = "INSERT INTO users(userName, mail, password) VALUES(?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, hashedPassword);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Kayıt başarıyla gerçekleştirildi.");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Kayıt gerçekleştirilemedi. Bu mail adresi zaten kayıtlı!");
            } else {
                System.out.println("Veri tabanı hatası: " + e.getMessage());
            }
        }
    }

    public static String login() {
        while (true) {
            System.out.println("\n--- GİRİŞ YAP ---");
            System.out.println("(İptal edip geri dönmek için mail kısmına '0' yazabilirsiniz)");
            
            System.out.print("E-posta => ");
            String inputEmail = scanner.nextLine();
            
            if (inputEmail.equals("0")) {
                System.out.println("Giriş işlemi iptal edildi.");
                return null;
            }

            System.out.print("Şifre => ");
            String password = scanner.nextLine();

            String query = "SELECT * FROM users WHERE mail = ?";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, inputEmail);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("password");
                        String username = resultSet.getString("userName");

                        if (BCrypt.checkpw(password, storedPassword)) {
                            System.out.println("Hoş geldin! " + username.toUpperCase());
                            return inputEmail;
                        } else {
                            System.out.println("Giriş Başarısız. Şifre hatalı!");
                        }
                    } else {
                        System.out.println("Girdiğiniz mail adresi sistemde kayıtlı değil.");
                    }
                    System.out.println("Lütfen tekrar deneyiniz.\n");
                }
            } catch (SQLException e) {
                System.out.println("Veri tabanı hatası: " + e.getMessage());
                return null;
            }
        }
    }

    public static void openCheckingAccount(String userEmail) {
        System.out.print("Hesap Adı (Örn: Maaş Hesabım) => ");
        String accountName = scanner.nextLine();

        String query = "INSERT INTO vadesizhesap(accountName, mail) VALUES(?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountName);
            preparedStatement.setString(2, userEmail);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vadesiz İşlem Hesabınız açıldı.");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Hesap Açılamadı. Bu mail adresine kayıtlı bir Vadesiz Hesabınız zaten bulunmakta!");
            } else {
                System.out.println("Veri tabanı hatası: " + e.getMessage());
            }
        }
    }

    public static void openSavingsAccount(String userEmail) {
        System.out.print("Hesap Adı (Örn: Birikim Hesabım) => ");
        String accountName = scanner.nextLine();

        String query = "INSERT INTO vadelihesap(accountName, mail) VALUES(?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountName);
            preparedStatement.setString(2, userEmail);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vadeli İşlem Hesabınız açıldı.");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Hesap Açılamadı. Bu mail adresine kayıtlı bir Vadeli Hesabınız zaten bulunmakta!");
            } else {
                System.out.println("Veri tabanı hatası: " + e.getMessage());
            }
        }
    }

    public static String validateEmailFormat() {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);

        while (true) {
            System.out.print("E-posta (İptal için 0) => ");
            String inputEmail = scanner.nextLine();
            
            if (inputEmail.equals("0")) return null;

            Matcher matcher = pattern.matcher(inputEmail);

            if (matcher.matches()) {
                return inputEmail;
            } else {
                System.out.println("Geçersiz e-posta formatı! Lütfen tekrar deneyiniz.");
            }
        }
    }

    public static int getSecureNumber(String message) {
        while (true) {
            try {
                System.out.print(message);
                String choice = scanner.nextLine();
                return Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                System.out.println("Hata: Lütfen geçerli bir sayı giriniz.");
            }
        }
    }
}

package BankacilikSistemi;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class KullaniciIslemleri {

    static Scanner scanner = new Scanner(System.in);

    private static String userName;
    private static String mail;
    private static String password;

    public static String getMail() {
        return mail;
    }

    public static void kayitOl() {

        System.out.print("Kullanıcı Adı => ");
        userName = scanner.nextLine();
        mailSyntaxKontrol();
        System.out.print("Şifre => ");
        password = scanner.nextLine();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection connection = DatabaseManager.getConnection()){

            String registerQuery = "INSERT INTO users(userName, mail, password) VALUES(?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(registerQuery);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, mail);
            preparedStatement.setString(3, hashedPassword);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Kayıt gerçekleştirildi.");
            }
            else {
                System.out.print("Kayıt gerçekleştirelemedi.\nMail adresi sistemde zaten kayıtlı!");
            }
        }
        catch (SQLException e){
            System.out.println("Veri tabanı hatası." + e);
        }
    }

    public static void girisYap() {

        boolean girisControl = false;

        while (!girisControl) {
            System.out.println("Lütfen sisteme kayıtlı mail adresinizi ve şifrenizi giriniz.");
            mailSyntaxKontrol();
            System.out.print("Şifre => ");
            password = scanner.nextLine();

            try (Connection connection = DatabaseManager.getConnection()) {
                String entryQuery = "SELECT * FROM users WHERE mail = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(entryQuery);
                preparedStatement.setString(1, mail);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    String storedPassword = resultSet.getString("password");
                    String username = resultSet.getString("userName");

                    if (BCrypt.checkpw(password, storedPassword)) {
                        System.out.println("Hoş geldin! " +  username.toUpperCase());
                        girisControl = true;
                    }
                    else {
                        System.out.println("Giriş Başarısız. Mail veya Şifre hatalı!");
                        System.out.println("Lütfen tekrar deneyiniz.\n");
                    }
                }
                else {
                    System.out.println("Girdiğiniz mail hatalı veya böyle bir hesap yok.");
                    System.out.println("Lütfen tekrar deneyiniz.\n");
                }
            }
            catch (SQLException e) {
                System.out.println("Veri tabanı hatası. " + e.getMessage());
                return;
            }
        }
    }

    public static void vadesizHesapAc(String aktifKullanici) {

        System.out.print("Kullanıcı Adı => ");
        userName = scanner.nextLine();
        System.out.print("Şifre => ");
        password = scanner.nextLine();

        try (Connection connection = DatabaseManager.getConnection()){

            String registerQuery = "INSERT INTO vadesizhesap(userName, mail) VALUES(?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(registerQuery);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, aktifKullanici);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Vadesiz İşlem Hesabınız açıldı.");
            }
            else {
                System.out.print("Hesap Açılamadı.\nMail adresine kayıtlı bir Vadesiz Hesabınız zaten bulunmakta!");
            }
        }
        catch (SQLException e){
            System.out.println("Veri tabanı hatası." + e);
        }
    }

    public static void vadeliHesapAc(String aktifKullanici) {

        System.out.print("Kullanıcı Adı => ");
        userName = scanner.nextLine();
        System.out.print("Şifre => ");
        password = scanner.nextLine();

        try (Connection connection = DatabaseManager.getConnection()){

            String registerQuery = "INSERT INTO vadelihesap(userName, mail) VALUES(?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(registerQuery);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, aktifKullanici);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Vadeli İşlem Hesabınız açıldı.");
            }
            else {
                System.out.print("Hesap Açılamadı.\nMail adresine kayıtlı bir Vadeli Hesabınız zaten bulunmakta!");
            }
        }
        catch (SQLException e){
            System.out.println("Veri tabanı hatası." + e);
        }
    }

    public static void mailSyntaxKontrol(){

        String desen = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(desen);

        while (true) {
            System.out.print("E-posta => ");
            mail = scanner.nextLine();

            Matcher matcher = pattern.matcher(mail);

            if (matcher.matches()) {
                break;
            }
            else {
                System.out.println("Geçersiz e-posta! Lütfen tekrar deneyiniz.");
            }
        }
    }

    public static int secureNumberGet(String message){
        while(true){
            try{
                System.out.print(message);
                String secim = scanner.nextLine();

                return Integer.parseInt(secim);
            }
            catch (NumberFormatException e){
                System.out.println(e + "\nHata lütfen bir sayı giriniz: ");
            }
        }
    }
}

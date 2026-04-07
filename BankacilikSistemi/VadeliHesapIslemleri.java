package BankacilikSistemi;
import java.sql.*;
import java.time.LocalDate;

public class VadeliHesapIslemleri extends HesapIslemleri {

    // Constructor
    public VadeliHesapIslemleri() {
        super("vadelihesap");
    }

    public static void vadeliParaYatir(String mail) {

        double amount = validateEnteredValue("Yatırmak istediğiniz miktarı giriniz: ");

        try (Connection connection = DatabaseManager.getConnection()) {

            String mailControlQuery = "SELECT balance FROM vadelihesap WHERE mail = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(mailControlQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");
                double faizOrani = resultSet.getDouble("faizOrani");
                double newBalance = currentBalance + amount;
                double faizTutari = amount * faizOrani;
                double vadeSonuBalance = currentBalance + amount + faizTutari;

                String updateQuery = "UPDATE vadelihesap SET balance = ? WHERE mail = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setDouble(1, vadeSonuBalance);
                updateStatement.setString(2, mail);

                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Para yatırma işlemi başarılı!");
                    System.out.println("Güncel bakiyeniz: " + newBalance + " TL");
                    System.out.println("Ay Sonu Bakiyeniz: " + vadeSonuBalance + " TL");
                    HesapIslemleri.saveLog(mail, "Para Yatırma", amount, "Vadelihesap");

                }
            }
            else {
                System.out.println("E-posta adresine ait hesap bulunamadı!");
            }
        }
        catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public static void vadeliParaCek(String mail) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "SELECT balance FROM vadelihesap WHERE mail = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                double currentBalance = resultSet.getDouble("balance");

                while (true){
                    double amount = validateEnteredValue("Çekmek istediğiniz miktarı giriniz: ");

                    if (amount > currentBalance) {
                        System.out.println("Yetersiz Bakiye! Lütfen tekrar deneyiniz.");
                        return;
                    }

                    LocalDate bugun = LocalDate.now();
                    LocalDate vadeGunu = LocalDate.now().withDayOfMonth(1);

                    double deductionFee = 0;
                    double newBalance;

                    if (bugun.isEqual(vadeGunu)) {
                        newBalance = currentBalance - amount;
                    }
                    else {
                        deductionFee = amount * 0.10;
                        newBalance = currentBalance - (amount + deductionFee);
                    }

                    String updateQuery = "UPDATE vadelihesap SET balance = ? WHERE mail = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, newBalance);
                    updateStatement.setString(2, mail);

                    int rowsAffected = updateStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Para çekme işlemi başarılı!");
                        if (deductionFee > 0) {
                            System.out.println("Kesinti Ücreti: " + deductionFee + " TL");
                            System.out.println("Güncel bakiyeniz: " + newBalance + " TL");
                            HesapIslemleri.saveLog(mail, "Para Çekme", amount, "Vadelihesap");
                        }
                        System.out.println("Para Çekme İşleminin Gerçekleştiği Tarih: " + bugun);
                    }
                }
            }
            else {
                System.out.println("E-posta adresine ait hesap bulunamadı!");
            }
        }
        catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}
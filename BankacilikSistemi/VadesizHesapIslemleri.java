package BankacilikSistemi;
import java.sql.*;
import java.util.Scanner;

public class VadesizHesapIslemleri extends HesapIslemleri {

    //Constructor
    public VadesizHesapIslemleri() {
        super("vadesizhesap");
    }

    public static void vadesizParaYatir(String mail) {

        double amount = validateEnteredValue("Yatırmak istediğiniz miktarı giriniz: ");

        try (Connection connection = DatabaseManager.getConnection()){

            String mailControlQuery = "SELECT balance FROM vadesizhesap WHERE mail = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(mailControlQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");
                double newBalance = currentBalance + amount;

                String updateQuery = "UPDATE vadesizhesap SET balance = ? WHERE mail = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setDouble(1, newBalance);
                updateStatement.setString(2, mail);

                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Para yatırma işlemi başarılı!");
                    System.out.println("Güncel bakiyeniz: " + newBalance + " TL");
                    HesapIslemleri.saveLog(mail, "Para Yatırma", amount, "Vadesizhesap");
                }
                else {
                    System.out.println("E-posta adresine ait hesap bulunamadı!");
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public static void vadesizParaCek(String mail) {
        try (Connection connection = DatabaseManager.getConnection()) {

            String mailControlQuery = "SELECT balance FROM vadesizhesap WHERE mail = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(mailControlQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");

                while(true){
                    double amount = validateEnteredValue("Çekmek istediğiniz miktarı giriniz: ");

                    if (amount > currentBalance) {
                        System.out.println("Yetersiz Bakiye! Lütfen tekrar deneyiniz.");
                    }

                    else {
                        double newBalance = currentBalance - amount;

                        String updateQuery = "UPDATE vadesizhesap SET balance = ? WHERE mail = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        updateStatement.setDouble(1, newBalance);
                        updateStatement.setString(2, mail);

                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            System.out.println("Para çekme işlemi başarılı!");
                            System.out.println("Güncel bakiyeniz: " + newBalance + " TL");
                            HesapIslemleri.saveLog(mail, "Para Çekme", amount, "Vadesizhesap");

                        }
                        break;
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

    public void paraTransferi(String senderMail) throws SQLException {

        Scanner input = new Scanner(System.in);
        Connection connection = null;

        try{
            connection = DatabaseManager.getConnection();
            connection.setAutoCommit(false);

            String senderQuery = "SELECT balance  FROM vadesizhesap WHERE mail = ?";
            PreparedStatement psSender = connection.prepareStatement(senderQuery);
            psSender.setString(1, senderMail);
            ResultSet rsSender = psSender.executeQuery();

            if (rsSender.next()) {
                double currentBalanceForSender = rsSender.getDouble("balance");
                double amount = validateEnteredValue("Göndermek istediğiniz miktarı giriniz: ");

                if (currentBalanceForSender < amount) {
                    System.out.println("Yetersiz Bakiye!");
                    return;
                }

                System.out.print("Lütfen alıcı mailini griiniz: ");
                String buyerMail = input.nextLine();

                String buyerQuery = "SELECT balance, username FROM vadesizhesap WHERE mail = ?";
                PreparedStatement psBuyer = connection.prepareStatement(buyerQuery);
                psBuyer.setString(1, buyerMail);
                ResultSet rsBuyer = psBuyer.executeQuery();

                if (rsBuyer.next()) {
                    String buyerUserName = rsBuyer.getString("username");
                    double currentBalanceForBuyer = rsBuyer.getDouble("balance");

                    double newBalanceForSender = currentBalanceForSender - amount;
                    double newBalanceForBuyer = currentBalanceForBuyer + amount;

                    String updateyForSender = "UPDATE vadesizHesap SET balance = ? WHERE mail = ?";
                    PreparedStatement ups = connection.prepareStatement(updateyForSender);
                    ups.setDouble(1, newBalanceForSender);
                    ups.setString(2, senderMail);
                    int rowsSender = ups.executeUpdate();

                    String updateForBuyer = "UPDATE vadesizHesap SET balance = ? WHERE mail = ?";
                    PreparedStatement upb = connection.prepareStatement(updateForBuyer);
                    upb.setDouble(1, newBalanceForBuyer);
                    upb.setString(2, buyerMail);
                    int rowsBuyer = upb.executeUpdate();

                    if (rowsBuyer > 0 && rowsSender > 0) {
                        connection.commit();
                        System.out.println("Para transferi başarıyla gerçekleştirildié");
                        System.out.println(buyerUserName + " adlı kişiye hesabınızdan " + amount + " TL para transferi gerçekleştirildi.");

                        HesapIslemleri.saveLog(senderMail, "Havale Gönderildi (Alıcı: " + buyerMail + ")", amount, "Vadesizhesap");
                        HesapIslemleri.saveLog(buyerMail, "Havale Geldi (Gönderen: " + senderMail + ")", amount, "Vadesizhesap");
                    } else {
                        connection.rollback();
                        System.out.println("Hata: İşlem sırasında bir sorun oluştu, transfer iptal edildi.");
                    }
                } else {
                    System.out.println("Kullanıcı bulunamadı!");
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}


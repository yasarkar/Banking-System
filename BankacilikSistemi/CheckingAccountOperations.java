package BankacilikSistemi;

import java.math.BigDecimal;
import java.sql.*;

public class CheckingAccountOperations extends AccountOperations {

    public CheckingAccountOperations() {
        super("vadesizhesap");
    }

    public void deposit(String email) {
        BigDecimal amount = validateEnteredValue("Yatırmak istediğiniz miktarı giriniz (İptal için 0): ");
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("İşlem iptal edildi.");
            return;
        }

        String query = "UPDATE vadesizhesap SET balance = balance + ? WHERE mail = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBigDecimal(1, amount);
            statement.setString(2, email);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Para yatırma işlemi başarılı!");
                AccountOperations.saveLog(email, "Para Yatırma", amount, "Vadesizhesap");
            } else {
                System.out.println("Hesap bulunamadı!");
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public void withdraw(String email) {
        String query = "SELECT balance FROM vadesizhesap WHERE mail = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    BigDecimal currentBalance = resultSet.getBigDecimal("balance");

                    while (true) {
                        BigDecimal amount = validateEnteredValue("Çekmek istediğiniz miktarı giriniz (İptal için 0): ");

                        if (amount.compareTo(BigDecimal.ZERO) == 0) {
                            System.out.println("İşlem iptal edildi.");
                            break;
                        }

                        if (amount.compareTo(currentBalance) > 0) {
                            System.out.println("Yetersiz Bakiye! Mevcut Bakiyeniz: " + currentBalance + " TL");
                        } else {
                            String updateQuery = "UPDATE vadesizhesap SET balance = balance - ? WHERE mail = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                updateStatement.setBigDecimal(1, amount);
                                updateStatement.setString(2, email);

                                int rowsAffected = updateStatement.executeUpdate();

                                if (rowsAffected > 0) {
                                    System.out.println("Para çekme işlemi başarılı!");
                                    AccountOperations.saveLog(email, "Para Çekme", amount, "Vadesizhesap");
                                }
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("Hesap bulunamadı!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public void transferMoney(String senderEmail) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            String senderQuery = "SELECT balance FROM vadesizhesap WHERE mail = ?";
            try (PreparedStatement psSender = connection.prepareStatement(senderQuery)) {
                psSender.setString(1, senderEmail);
                try (ResultSet rsSender = psSender.executeQuery()) {

                    if (rsSender.next()) {
                        BigDecimal currentBalanceForSender = rsSender.getBigDecimal("balance");
                        BigDecimal amount = validateEnteredValue("Göndermek istediğiniz miktarı giriniz (İptal için 0): ");

                        if (amount.compareTo(BigDecimal.ZERO) == 0) {
                            System.out.println("İşlem iptal edildi.");
                            connection.rollback();
                            return;
                        }

                        if (currentBalanceForSender.compareTo(amount) < 0) {
                            System.out.println("Yetersiz Bakiye!");
                            connection.rollback();
                            return;
                        }

                        System.out.print("Lütfen alıcı mailini giriniz: ");
                        String recipientEmail = input.nextLine();

                        if (senderEmail.equalsIgnoreCase(recipientEmail)) {
                            System.out.println("Hata: Kendi hesabınıza para gönderemezsiniz!");
                            connection.rollback();
                            return;
                        }

                        String recipientQuery = "SELECT accountName FROM vadesizhesap WHERE mail = ?";
                        try (PreparedStatement psRecipient = connection.prepareStatement(recipientQuery)) {
                            psRecipient.setString(1, recipientEmail);
                            try (ResultSet rsRecipient = psRecipient.executeQuery()) {

                                if (rsRecipient.next()) {
                                    String recipientName = rsRecipient.getString("accountName");

                                    String updateSender = "UPDATE vadesizhesap SET balance = balance - ? WHERE mail = ?";
                                    try (PreparedStatement ups = connection.prepareStatement(updateSender)) {
                                        ups.setBigDecimal(1, amount);
                                        ups.setString(2, senderEmail);
                                        ups.executeUpdate();
                                    }

                                    String updateRecipient = "UPDATE vadesizhesap SET balance = balance + ? WHERE mail = ?";
                                    try (PreparedStatement upr = connection.prepareStatement(updateRecipient)) {
                                        upr.setBigDecimal(1, amount);
                                        upr.setString(2, recipientEmail);
                                        upr.executeUpdate();
                                    }

                                    connection.commit();
                                    System.out.println("Para transferi başarıyla gerçekleştirildi.");
                                    System.out.println(recipientName + " adlı kişiye " + amount + " TL gönderildi.");

                                    AccountOperations.saveLog(senderEmail, "Transfer Gönderildi -> " + recipientEmail, amount, "Vadesizhesap");
                                    AccountOperations.saveLog(recipientEmail, "Transfer Geldi <- " + senderEmail, amount, "Vadesizhesap");
                                } else {
                                    System.out.println("Alıcı bulunamadı!");
                                    connection.rollback();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}

package BankacilikSistemi;

import java.sql.*;
import java.time.LocalDate;

public class SavingsAccountOperations extends AccountOperations {

    public SavingsAccountOperations() {
        super("vadelihesap");
    }

    public void deposit(String email) {
        double amount = validateEnteredValue("Yatırmak istediğiniz miktarı giriniz (İptal için 0): ");
        if (amount == 0) {
            System.out.println("İşlem iptal edildi.");
            return;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "UPDATE vadelihesap SET balance = balance + ? WHERE mail = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, amount);
            statement.setString(2, email);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Para yatırma işlemi başarılı!");
                AccountOperations.saveLog(email, "Para Yatırma", amount, "Vadelihesap");
            } else {
                System.out.println("Hesap bulunamadı!");
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public void withdraw(String email) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "SELECT balance FROM vadelihesap WHERE mail = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");

                while (true) {
                    double amount = validateEnteredValue("Çekmek istediğiniz miktarı giriniz (İptal için 0): ");
                    
                    if (amount == 0) {
                        System.out.println("İşlem iptal edildi.");
                        break;
                    }

                    if (amount > currentBalance) {
                        System.out.println("Yetersiz Bakiye! Mevcut Bakiyeniz: " + currentBalance + " TL");
                        continue;
                    }

                    LocalDate today = LocalDate.now();
                    LocalDate maturityDate = today.withDayOfMonth(today.lengthOfMonth());

                    double deductionFee = 0;
                    if (!today.isEqual(maturityDate)) {
                        deductionFee = amount * 0.10;
                    }

                    if (currentBalance < (amount + deductionFee)) {
                        System.out.println("Yetersiz Bakiye (Kesinti dahil)! Toplam gereken: " + (amount + deductionFee) + " TL");
                        continue;
                    }

                    double totalDeduction = amount + deductionFee;

                    String updateQuery = "UPDATE vadelihesap SET balance = balance - ? WHERE mail = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, totalDeduction);
                    updateStatement.setString(2, email);

                    int rowsAffected = updateStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Para çekme işlemi başarılı!");
                        if (deductionFee > 0) {
                            System.out.println("Erken çekim kesinti ücreti: " + deductionFee + " TL");
                        }
                        System.out.println("İşlem Tarihi: " + today);
                        AccountOperations.saveLog(email, "Para Çekme", totalDeduction, "Vadelihesap");
                        break;
                    }
                }
            } else {
                System.out.println("Hesap bulunamadı!");
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}

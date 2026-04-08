package BankacilikSistemi;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class SavingsAccountOperations extends AccountOperations {

    public SavingsAccountOperations() {
        super("vadelihesap");
    }

    public void deposit(String email) {
        BigDecimal amount = validateEnteredValue("Yatırmak istediğiniz miktarı giriniz (İptal için 0): ");
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("İşlem iptal edildi.");
            return;
        }

        String query = "UPDATE vadelihesap SET balance = balance + ? WHERE mail = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBigDecimal(1, amount);
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
        String query = "SELECT balance FROM vadelihesap WHERE mail = ?";
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
                            continue;
                        }

                        LocalDate today = LocalDate.now();
                        LocalDate maturityDate = today.withDayOfMonth(today.lengthOfMonth());

                        BigDecimal deductionFee = BigDecimal.ZERO;
                        if (!today.isEqual(maturityDate)) {
                            deductionFee = amount.multiply(new BigDecimal("0.10"));
                        }

                        BigDecimal totalDeduction = amount.add(deductionFee);

                        if (currentBalance.compareTo(totalDeduction) < 0) {
                            System.out.println("Yetersiz Bakiye (Kesinti dahil)! Toplam gereken: " + totalDeduction + " TL");
                            continue;
                        }

                        String updateQuery = "UPDATE vadelihesap SET balance = balance - ? WHERE mail = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setBigDecimal(1, totalDeduction);
                            updateStatement.setString(2, email);

                            int rowsAffected = updateStatement.executeUpdate();

                            if (rowsAffected > 0) {
                                System.out.println("Para çekme işlemi başarılı!");
                                if (deductionFee.compareTo(BigDecimal.ZERO) > 0) {
                                    System.out.println("Erken çekim kesinti ücreti: " + deductionFee + " TL");
                                }
                                System.out.println("İşlem Tarihi: " + today);
                                AccountOperations.saveLog(email, "Para Çekme", totalDeduction, "Vadelihesap");
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
}

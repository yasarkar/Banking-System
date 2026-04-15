package BankacilikSistemi;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class AccountOperations {

    protected static final Scanner input = UserOperations.getScanner();
    private final String accountType;

    public AccountOperations(String accountType) {
        this.accountType = accountType;
    }

    public static BigDecimal validateEnteredValue(String message) {
        while (true) {
            try {
                System.out.print(message);
                String inputStr = input.nextLine();
                BigDecimal amount = new BigDecimal(inputStr);

                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Geçersiz değer! Lütfen geçerli bir değer giriniz:");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Hata: Lütfen geçerli bir sayı giriniz.");
            }
        }
    }

    public boolean checkIsAccountExist(String email) {
        String accountControlQuery = "SELECT 1 FROM " + this.accountType + " WHERE mail = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(accountControlQuery)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("Hesap kontrolü sırasında hata: " + e.getMessage());
            return false;
        }
    }

    public void displayBalance(String email) {
        String query = "SELECT balance FROM " + this.accountType + " WHERE mail = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal currentBalance = resultSet.getBigDecimal("balance");
                    System.out.println("Güncel bakiyeniz: " + currentBalance + " TL");
                } else {
                    System.out.println("Hesabınız bulunmamaktadır!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public static void saveLog(String email, String transactionType, BigDecimal amount, String accountType) {
        String query = "INSERT INTO hesap_hareketleri(mail, islem_turu, miktar, hesap_turu) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, transactionType);
            preparedStatement.setBigDecimal(3, amount);
            preparedStatement.setString(4, accountType);
            preparedStatement.execute();
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public static void showTransactionHistory(String email) {
        String query = "SELECT islem_turu, miktar, hesap_turu, tarih FROM hesap_hareketleri WHERE mail = ? ORDER BY tarih DESC";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                System.out.println("\n==============================================================================");
                System.out.println("                      HESAP HAREKETLERİ DÖKÜMÜ                               ");
                System.out.println("==============================================================================");

                System.out.println(System.out.printf("%-22s | %-15s | %-12s | %-10s%n", "TARİH", "İŞLEM TÜRÜ", "MİKTAR", "HESAP"));

                boolean hasRecord = false;
                while (resultSet.next()) {
                    hasRecord = true;
                    String date = resultSet.getTimestamp("tarih") != null ? resultSet.getTimestamp("tarih").toString().substring(0, 19) : "N/A";
                    String type = resultSet.getString("islem_turu");
                    BigDecimal amount = resultSet.getBigDecimal("miktar");
                    String account = resultSet.getString("hesap_turu");

                    String sign = type.toLowerCase().contains("yatır") || type.toLowerCase().contains("geldi") ? "+" : "-";
                    String amountFormatted = String.format("%s %.2f TL", sign, amount);
                    System.out.printf("%-22s | %-15s | %-12s | %-10s%n", date, type, amountFormatted, account);
                }

                if (!hasRecord) {
                    System.out.println("Henüz bir hesap hareketiniz bulunmamaktadır.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Hata: Hesap hareketleri çekilemedi! " + e.getMessage());
        }
    }

    public static void downloadAccountSummary(String email) {
        String cleanMail = email.split("@")[0];
        String fileName = "hesap_ozeti_" + cleanMail + ".csv";
        String query = "SELECT tarih, hesap_turu, islem_turu, miktar FROM hesap_hareketleri WHERE mail = ? ORDER BY tarih DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                boolean hasRecord = false;

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                    writer.write('\ufeff');
                    writer.write("Tarih,Hesap Türü,İşlem Detayı,Tutar (TL)");
                    writer.newLine();

                    while (resultSet.next()) {
                        hasRecord = true;
                        String date = resultSet.getTimestamp("tarih") != null ? resultSet.getTimestamp("tarih").toString().substring(0, 19) : "N/A";
                        BigDecimal amount = resultSet.getBigDecimal("miktar");
                        String accountT = resultSet.getString("hesap_turu");
                        String type = resultSet.getString("islem_turu");

                        writer.write(date + "," + accountT + "," + type + "," + amount);
                        writer.newLine();
                    }

                    if (hasRecord) {
                        System.out.println("Hesap özeti başarıyla '" + fileName + "' dosyasına kaydedildi.");
                        saveLog(email, "Hesap Özeti İndirildi", BigDecimal.ZERO, "Sistem");
                    } else {
                        System.out.println("Hesap özetine eklenecek veri bulunamadı!");
                    }
                } catch (IOException error) {
                    System.out.println("Dosya yazılırken bir hata oluştu: " + error.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Veri tabanı hatası: " + e.getMessage());
        }
    }
}

package BankacilikSistemi;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HesapIslemleri {

    private static final Scanner input = new Scanner(System.in);
    private final String hesapTuru;

    public HesapIslemleri(String hesapTuru) {
        this.hesapTuru = hesapTuru;
    }

    public static double validateEnteredValue(String message){
        System.out.print(message);
        double amount = input.nextDouble();

        while (amount <= 0){

            System.out.print("Geçersiz miktar! Lütfen pozitif bir değer giriniz: ");
            amount = input.nextDouble();
        }
        return amount;
    }

    public boolean checkIsAccountExist(String mail){
        try (Connection connection = DatabaseManager.getConnection()){

            String accountControlQuery = "SELECT 1 FROM " + this.hesapTuru + " WHERE mail = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(accountControlQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        }
        catch (SQLException e) {
            System.out.println("Hesap kontrolü sırasında hata: " + e.getMessage());
            return false;
        }
    }

    public void bakiyeGoruntule(String mail){

        try(Connection connection = DatabaseManager.getConnection()){

            String mailControlQuery = "SELECT balance FROM " + this.hesapTuru + " WHERE mail = ?";
            PreparedStatement statement = connection.prepareStatement(mailControlQuery);
            statement.setString(1, mail);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()){
                double currentBalance = resultSet.getDouble("balance");
                System.out.println("Güncel bakiyeniz: " + currentBalance + " TL");
            }
            else {
                System.out.println("Vadeli İşlem hesabınız bulunmamaktadır!");
            }
        }

        catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    public static void saveLog(String mail, String islemTuru, double miktar, String hesapTuru){

        try(Connection connection = DatabaseManager.getConnection()){

            String saveLogQuery = "INSERT INTO hesap_hareketleri(mail, islem_turu, miktar, hesap_turu) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(saveLogQuery);
            preparedStatement.setString(1, mail);
            preparedStatement.setString(2, islemTuru);
            preparedStatement.setDouble(3, miktar);
            preparedStatement.setString(4, hesapTuru);
            preparedStatement.execute();

        }
        catch (SQLException e){
            System.out.println("Veritabanı hatası. " + e.getMessage());
        }
    }

    public static void showTransactionHistory(String mail){
        try(Connection connection = DatabaseManager.getConnection()){

            String transactionHistoryQuery =  "SELECT islem_turu, miktar, hesap_turu, tarih FROM hesap_hareketleri WHERE mail = ? ORDER BY tarih DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(transactionHistoryQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\n==============================================================================");
            System.out.println("                      HESAP HAREKETLERİ DÖKÜMÜ                               ");
            System.out.println("==============================================================================");

            // Tablo Başlıkları (Format: %-sol yasla, rakam genişlik, s string/f float)
            System.out.printf("%-22s | %-15s | %-12s | %-10s%n", "TARİH", "İŞLEM TÜRÜ", "MİKTAR", "HESAP");
            System.out.println("------------------------------------------------------------------------------");

            boolean isRecord = false;
            while(resultSet.next()){
                isRecord = true;
                String tarih = resultSet.getTimestamp("tarih").toString().substring(0, 19);
                String islem = resultSet.getString("islem_turu");
                double miktar = resultSet.getDouble("miktar");
                String hTuru = resultSet.getString("hesap_turu");

                // Verileri hizalı bir şekilde yazdırıyoruz
                String isaret = islem.toLowerCase().contains("yatır") ? "+" : "-";
                String miktarFormatli = String.format("%s %.2f TL", isaret, miktar);
                System.out.printf("%-22s | %-15s | %-12s | %-10s%n", tarih, islem, miktarFormatli, hTuru);
            }

            if (!isRecord){
                System.out.println("Henüz bir hesap hareketiniz bulunmamaktadır.");
            }
        }
        catch (SQLException e){
            System.out.println("Hata: Hesap hareketleri çekilemedi! " + e.getMessage());
        }
    }

    public static void hesapOzetiIndir(String mail){
        String fileName = "hesap_ozeti.csv";

        File file = new File(fileName);

        if (file.exists()){
            file.delete();
        }

        try(Connection connection = DatabaseManager.getConnection()){

            String accountActivityQuery = "SELECT tarih, hesap_turu, islem_turu, miktar FROM hesap_hareketleri WHERE mail = ? ORDER BY tarih DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(accountActivityQuery);
            preparedStatement.setString(1, mail);
            ResultSet resultSet = preparedStatement.executeQuery();

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                writer.write('\ufeff');
                writer.write("Tarih,Hesap Türü,İşlem Detayı,Tutar (TL)");
                writer.newLine();

                if (resultSet.next()){
                    while(resultSet.next()){
                        String tarih = resultSet.getTimestamp("tarih").toString().substring(0, 19);
                        double miktar = resultSet.getDouble("miktar");
                        String hesapTuru = resultSet.getString("hesap_turu");
                        String islemTuru = resultSet.getString("islem_turu");

                        writer.write(tarih + "," + hesapTuru + "," + islemTuru + "," + miktar);
                        writer.newLine();
                    }
                    System.out.println("Hesap özeti başarıyla '" + fileName + "' dosyasına kaydedildi.");
                    HesapIslemleri.saveLog(mail, "Hesap Özeti İndirildi", 0, "Sistem");
                }
                else {
                    System.out.println("Hesap özetine eklenecek veri bulunamadı!");
                }
            }
            catch (IOException error){
                System.out.println("Dosya yazılırken bir hata oluştu: " + error.getMessage());
            }

        }
        catch (Exception e){
            System.out.println("Veri tabanı hatası: " + e.getMessage());
        }
    }
}

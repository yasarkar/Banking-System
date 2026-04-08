package BankacilikSistemi;

import java.sql.SQLException;

public class BankingSystem {

    public static void main(String[] args) {
        MenuManager menuManager = new MenuManager();
        try {
            menuManager.startApp();
        } catch (SQLException e) {
            System.err.println("Sistem hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

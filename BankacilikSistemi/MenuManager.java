package BankacilikSistemi;

import java.sql.SQLException;

public class MenuManager {

    private final CheckingAccountOperations checkingOps = new CheckingAccountOperations();
    private final SavingsAccountOperations savingsOps = new SavingsAccountOperations();
    private String activeUser;

    public void startApp() throws SQLException {
        int choice;
        do {
            System.out.println("\n---BANKAMIZA HOŞGELDİNİZ---");
            System.out.println("1- Giriş Yap");
            System.out.println("2- Kayıt Ol");
            System.out.println("3- Çıkış");
            choice = UserOperations.getSecureNumber("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (choice) {
                case 1:
                    activeUser = UserOperations.login();
                    if (activeUser != null) {
                        mainMenu();
                    }
                    break;
                case 2:
                    UserOperations.register();
                    break;
                case 3:
                    System.out.println("İyi günler, sistemden çıkış yapılır...");
                    break;
                default:
                    System.out.println("Geçersiz bir değer girdiniz. Lütfen tekrar deneyiniz!");
                    break;
            }
        } while (choice != 3);
    }

    private void mainMenu() throws SQLException {
        int choice;
        do {
            System.out.println("\n--- ANA MENÜ ---");
            System.out.println("1- Yeni Hesap Aç");
            System.out.println("2- Vadesiz Hesap İşlemleri");
            System.out.println("3- Vadeli Hesap İşlemleri");
            System.out.println("4- Hesap Hareketlerini Görüntüle");
            System.out.println("5- Hesap Özeti İndir");
            System.out.println("6- Oturumu Kapat");
            choice = UserOperations.getSecureNumber("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (choice) {
                case 1:
                    openNewAccountMenu();
                    break;
                case 2:
                    if (checkingOps.checkIsAccountExist(activeUser)) {
                        checkingAccountMenu();
                    } else {
                        System.out.println("Vadesiz Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;
                case 3:
                    if (savingsOps.checkIsAccountExist(activeUser)) {
                        savingsAccountMenu();
                    } else {
                        System.out.println("Vadeli Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;
                case 4:
                    AccountOperations.showTransactionHistory(activeUser);
                    break;
                case 5:
                    AccountOperations.downloadAccountSummary(activeUser);
                    break;
                case 6:
                    System.out.println("Oturum kapatıldı.");
                    activeUser = null;
                    return;
            }
        } while (choice != 6);
    }

    private void openNewAccountMenu() throws SQLException {
        System.out.println("\n--- HESAP AÇMA MENÜSÜ ---");
        System.out.println("1- Vadesiz Hesap");
        System.out.println("2- Vadeli Hesap");
        System.out.println("3- Ana Menüye Dön");
        int choice = UserOperations.getSecureNumber("Lütfen açmak istediğiniz hesap türünü seçiniz: ");

        switch (choice) {
            case 1:
                UserOperations.openCheckingAccount(activeUser);
                break;
            case 2:
                UserOperations.openSavingsAccount(activeUser);
                break;
            case 3:
                return;
            default:
                System.out.println("Geçersiz bir değer girdiniz.");
                break;
        }
    }

    private void checkingAccountMenu() throws SQLException {
        int choice;
        do {
            System.out.println("\n---Vadesiz İşlem Hesabınıza Hoşgeldiniz---");
            System.out.println("1- Para Yatır");
            System.out.println("2- Para Çek");
            System.out.println("3- Havale/EFT İşlemleri");
            System.out.println("4- Bakiye Görüntüle");
            System.out.println("5- Ana Menüye Dön");
            choice = UserOperations.getSecureNumber("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (choice) {
                case 1:
                    checkingOps.deposit(activeUser);
                    break;
                case 2:
                    checkingOps.withdraw(activeUser);
                    break;
                case 3:
                    checkingOps.transferMoney(activeUser);
                    break;
                case 4:
                    checkingOps.displayBalance(activeUser);
                    break;
                case 5:
                    return;
            }
        } while (choice != 5);
    }

    private void savingsAccountMenu() throws SQLException {
        int choice;
        do {
            System.out.println("\n---Vadeli İşlem Hesabınıza Hoşgeldiniz---");
            System.out.println("1- Para Yatır");
            System.out.println("2- Para Çek");
            System.out.println("3- Bakiye Görüntüle");
            System.out.println("4- Ana Menüye Dön");
            choice = UserOperations.getSecureNumber("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (choice) {
                case 1:
                    savingsOps.deposit(activeUser);
                    break;
                case 2:
                    savingsOps.withdraw(activeUser);
                    break;
                case 3:
                    savingsOps.displayBalance(activeUser);
                    break;
                case 4:
                    return;
            }
        } while (choice != 4);
    }
}

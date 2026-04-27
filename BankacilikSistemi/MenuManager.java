package BankacilikSistemi;

import java.sql.SQLException;

public class MenuManager {

    private final CheckingAccountOperations checkingOps = new CheckingAccountOperations();
    private final SavingsAccountOperations savingsOps = new SavingsAccountOperations();
    /** Oturum açmış kullanıcının e-posta adresi (veritabanında mail ile eşleşir). */
    private String activeUserEmail;

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
                    activeUserEmail = UserOperations.login();
                    if (activeUserEmail != null) {
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
                    if (checkingOps.checkIsAccountExist(activeUserEmail)) {
                        checkingAccountMenu();
                    } else {
                        System.out.println("Vadesiz Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;
                case 3:
                    if (savingsOps.checkIsAccountExist(activeUserEmail)) {
                        savingsAccountMenu();
                    } else {
                        System.out.println("Vadeli Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;
                case 4:
                    AccountOperations.showTransactionHistory(activeUserEmail);
                    break;
                case 5:
                    AccountOperations.downloadAccountSummary(activeUserEmail);
                    break;
                case 6:
                    System.out.println("Oturum kapatıldı.");
                    activeUserEmail = null;
                    return;
                default:
                    System.out.println("Geçersiz bir değer girdiniz. Lütfen 1 ile 6 arasında seçim yapınız.");
                    break;
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
                UserOperations.openCheckingAccount(activeUserEmail);
                break;
            case 2:
                UserOperations.openSavingsAccount(activeUserEmail);
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
                    checkingOps.deposit(activeUserEmail);
                    break;
                case 2:
                    checkingOps.withdraw(activeUserEmail);
                    break;
                case 3:
                    checkingOps.transferMoney(activeUserEmail);
                    break;
                case 4:
                    checkingOps.displayBalance(activeUserEmail);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Geçersiz bir değer girdiniz. Lütfen 1 ile 5 arasında seçim yapınız.");
                    break;
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
                    savingsOps.deposit(activeUserEmail);
                    break;
                case 2:
                    savingsOps.withdraw(activeUserEmail);
                    break;
                case 3:
                    savingsOps.displayBalance(activeUserEmail);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Geçersiz bir değer girdiniz. Lütfen 1 ile 4 arasında seçim yapınız.");
                    break;
            }
        } while (choice != 4);
    }
}

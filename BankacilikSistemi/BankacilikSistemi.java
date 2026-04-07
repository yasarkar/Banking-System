package BankacilikSistemi;
import java.sql.*;

public class BankacilikSistemi {

    static VadesizHesapIslemleri vadesiz = new VadesizHesapIslemleri();
    static VadeliHesapIslemleri vadeli = new VadeliHesapIslemleri();

    static int secim;
    static String aktifKullanici;
    
    public static void main(String [] args) throws SQLException {

        do {
            System.out.println("\n---BANKAMIZA HOŞGELDİNİZ---");
            System.out.println("1- Giriş Yap");
            System.out.println("2- Kayıt Ol");
            System.out.println("3- Çıkış");
            secim = KullaniciIslemleri.secureNumberGet("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (secim){

                case 1:
                    KullaniciIslemleri.girisYap();
                    aktifKullanici = KullaniciIslemleri.getMail();
                    anaIslemMenusu(aktifKullanici);
                    break;

                case 2:
                    KullaniciIslemleri.kayitOl();
                    break;

                case 3:
                    System.out.println("İyi günler, sistemden çıkış yapılır...");
                    break;

                default:
                    System.out.println("Geçersiz bir değer girdiniz. Lütfen tekrar deneyiniz!");
                    break;
            }
        }
        while (secim != 3);
    }

    public static void yeniHesapAc() throws SQLException {
        System.out.println("1- Vadesiz Hesap");
        System.out.println("2- Vadeli Hesap");
        System.out.println("3- Ana Menüye Dön");
        secim = KullaniciIslemleri.secureNumberGet("Lütfen açmak istediğiniz hesap türünü seçiniz: ");

        switch (secim){

            case 1:
                KullaniciIslemleri.vadesizHesapAc(aktifKullanici);
                return;

            case 2:
                KullaniciIslemleri.vadeliHesapAc(aktifKullanici);
                return;

            case 3:
                anaIslemMenusu(aktifKullanici);
                return;

            default:
                System.out.println("Geçersiz bir değer girdiniz. Lütfen tekrar deneyiniz!");
                break;
        }
    }

    public static void vadesizHesapMenusu(String aktifKullanici) throws SQLException {

        do {
            System.out.println("\n---Vadesiz İşlem Hesabınıza Hoşgeldiniz---\n");
            System.out.println("1- Para Yatır");
            System.out.println("2- Para Çek");
            System.out.println("3- Havale/EFT İşlemleri");
            System.out.println("4- Bakiye Görüntüle");
            System.out.println("5- Ana Meniye Dön");
            secim = KullaniciIslemleri.secureNumberGet("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (secim){

                case 1:
                    vadesiz.vadesizParaYatir(aktifKullanici);
                    break;

                case 2:
                    vadesiz.vadesizParaCek(aktifKullanici);
                    break;

                case 3:
                    vadesiz.paraTransferi(aktifKullanici);
                    break;
                case 4:
                    vadesiz.bakiyeGoruntule(aktifKullanici);
                    break;
                case 5:
                    anaIslemMenusu(aktifKullanici);
                    break;
            }
        }
        while (secim != 5);
    }

    public static void vadeliHesapMenusu(String aktifKullanici) throws SQLException {

        do {
            System.out.println("\n---Vadeli İşlem Hesabınıza Goşgeldiniz---");
            System.out.println("1- Para Yatır");
            System.out.println("2- Para Çek");
            System.out.println("3- Bakiye Görüntüle");
            System.out.println("4- Ana Menüye Dön");
            secim = KullaniciIslemleri.secureNumberGet("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (secim){

                case 1:
                    vadeli.vadeliParaYatir(aktifKullanici);
                    break;

                case 2:
                    vadeli.vadeliParaCek(aktifKullanici);
                    break;

                case 3:
                    vadeli.bakiyeGoruntule(aktifKullanici);
                    break;

                case 4:
                    anaIslemMenusu(aktifKullanici);
                    return;
            }
        }
        while (secim != 4);
    }

    public static void anaIslemMenusu(String aktifKullanici) throws SQLException {
        do {
            System.out.println("\n--- ANA MENÜ ---");
            System.out.println("1- Yeni Hesap Aç");
            System.out.println("2- Vadesiz Hesap İşlemleri");
            System.out.println("3- Vadeli Hesap İşlemleri");
            System.out.println("4- Hesap Hareketlerini Görüntüle");
            System.out.println("5- Hesap Özeti İndir");
            System.out.println("6- Oturumu Kapat");
            secim = KullaniciIslemleri.secureNumberGet("Lütfen yapmak istediğiniz işlemi seçiniz => ");

            switch (secim){

                case 1:
                    yeniHesapAc();
                    break;

                case 2:
                    if (vadesiz.checkIsAccountExist(aktifKullanici)){
                        vadesizHesapMenusu(aktifKullanici);
                    }
                    else {
                        System.out.println("Vadesiz Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;

                case 3:
                    if (vadeli.checkIsAccountExist(aktifKullanici)){
                        vadeliHesapMenusu(aktifKullanici);
                    }
                    else {
                        System.out.println("Vadeli Hesabınız bulunmuyor.");
                        System.out.println("İşlem yapabilmek için lütfen 'Yeni Hesap Aç' menüsünü kullanın.");
                    }
                    break;

                case 4:
                    HesapIslemleri.showTransactionHistory(aktifKullanici);
                    break;

                case 5:
                    HesapIslemleri.hesapOzetiIndir(aktifKullanici);
                    break;

                case 6:
                    System.out.println("İyi günler, sistemden çıkış yapılır...");
                    System.exit(0);
                    break;
            }
        }
        while (secim != 5);
    }
}

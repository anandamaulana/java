# Prediksi Omzet The Play Zone

Aplikasi desktop Java + MySQL untuk memprediksi omzet transaksi Counter The Play Zone menggunakan **Regresi Linear** dengan pembanding musiman (Year-over-Year), sesuai rancangan pada `Rancangan_Aplikasi.md`.

## Struktur Proyek

```
pom.xml                  # Konfigurasi Maven (dependency & build)
docker-compose.yml       # Opsi menjalankan MySQL via Docker (alternatif XAMPP)
db/schema.sql            # Skema database + seed akun & contoh data
src/main/resources/      # db.properties (konfigurasi koneksi database)
src/main/java/com/theplayzone/prediksi/
├── Main.java             # Entry point aplikasi
├── koneksi/               # Koneksi JDBC
├── model/                 # POJO (User, TransaksiHarian, OmzetBulanan, HasilPrediksi, dll.)
├── dao/                   # Akses data (UserDAO, TransaksiDAO, PrediksiDAO)
├── service/                # Logika bisnis (RegresiLinearService, MapeEvaluator, ExcelImportService, AuthService)
├── ui/                     # Form Swing (Login, Dashboard, Import, Kelola Transaksi, Prediksi, Laporan)
└── util/                   # ChartHelper (JFreeChart)
docs/
├── 01_Manual_Book.md       # Gambaran umum aplikasi
├── 02_Manual_Aplikasi.md   # Panduan penggunaan tiap menu
├── 03_Manual_Setup.md      # Panduan instalasi (JDK, XAMPP/Docker, build & jalankan)
└── 04_Manual_NetBeans.md   # Panduan khusus membuka/Run/Debug proyek di NetBeans
```

## Mulai Cepat

1. Siapkan database — pilih salah satu:
   - **XAMPP**: start Apache+MySQL, import `db/schema.sql` via phpMyAdmin.
   - **Docker**: `docker compose up -d` (otomatis membuat database & seed data).
2. Buka project ini di NetBeans (**File > Open Project**, `pom.xml` terdeteksi otomatis) lalu **Run**.
   Atau lewat terminal: `mvn clean package && java -jar target/prediksi-theplayzone.jar`.
3. Login dengan `admin` / `admin123` (Kepala Divisi) atau `operasional` / `opr123` (Staf Operasional).

Panduan lengkap ada di folder `docs/`.

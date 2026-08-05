# Prediksi Omzet The Play Zone

Aplikasi desktop Java + MySQL untuk memprediksi omzet transaksi Counter The Play Zone menggunakan **Regresi Linear** dengan pembanding musiman (Year-over-Year), sesuai rancangan pada `Rancangan_Aplikasi.md`.

## Struktur Proyek

```
pom.xml                  # Konfigurasi Maven (dependency & build)
docker-compose.yml       # Opsi menjalankan MySQL via Docker (alternatif XAMPP)
db/schema.sql            # Skema database + seed akun & contoh data
db/migration_001_toko.sql  # Migrasi tambahan untuk database lama (toko/metode_bayar/rekap)
db/migration_002_admin_staff.sql  # Migrasi role users lama (operasional/kepala_divisi) ke (admin/staff)
src/main/resources/      # db.properties (konfigurasi koneksi database)
src/main/java/com/theplayzone/prediksi/
├── Main.java             # Entry point aplikasi
├── koneksi/               # Koneksi JDBC
├── model/                 # POJO (User, TransaksiHarian, OmzetBulanan, HasilPrediksi, Toko, MetodeBayar, dll.)
├── dao/                   # Akses data (UserDAO, TransaksiDAO, PrediksiDAO, TokoDAO, MetodeBayarDAO, RekapMetodeDAO)
├── service/                # Logika bisnis (RegresiLinearService, MapeEvaluator, ExcelImportService,
│                           #   MasterMetodeBayarImportService, RekapTokoImportService, PdfReportService, AuthService)
├── ui/                     # Form Swing (Login, Dashboard, Kelola User, Import, Kelola Transaksi, Import Rekap Toko, Prediksi, Laporan)
└── util/                   # ChartHelper (JFreeChart)
docs/
├── 01_Manual_Book.md       # Gambaran umum aplikasi
├── 02_Manual_Aplikasi.md   # Panduan penggunaan tiap menu
├── 03_Manual_Setup.md      # Panduan instalasi (JDK, XAMPP/Docker, build & jalankan)
├── 04_Manual_NetBeans.md   # Panduan khusus membuka/Run/Debug proyek di NetBeans
├── 05_Format_Excel_Import.md  # Spesifikasi format Excel (Master Metode Bayar, Rekap Toko Bulanan, Import Data Transaksi)
└── pemodelan-uml.html      # Use Case / Activity / Sequence / Class diagram (host via GitHub Pages)
```

## Mulai Cepat

1. Siapkan database — pilih salah satu:
   - **XAMPP**: start Apache+MySQL, import `db/schema.sql` via phpMyAdmin.
   - **Docker**: `docker compose up -d` (otomatis membuat database & seed data).
2. Buka project ini di NetBeans (**File > Open Project**, `pom.xml` terdeteksi otomatis) lalu **Run**.
   Atau lewat terminal: `mvn clean package && java -jar target/prediksi-theplayzone.jar`.
3. Login dengan `admin` / `admin123` (Admin) atau `staff` / `staff123` (Staff).

Panduan lengkap ada di folder `docs/`.

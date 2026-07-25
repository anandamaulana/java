# Rancangan Aplikasi
## Prediksi Transaksi The Play Zone (Regresi Linear) — Java Desktop + MySQL

Dokumen ini adalah rancangan teknis (setara materi Bab IV.C "Pemodelan Perangkat Lunak") berdasarkan Bab I–III skripsi *Penerapan Metode Regresi Linear dalam Analisis Prediksi Transaksi pada Counter The Play Zone*.

---

## 1. Ringkasan Kebutuhan (dari Bab I–III)

| Aspek | Ketentuan |
|---|---|
| Jenis aplikasi | Desktop, bukan POS/kasir — alat bantu analisis manajerial |
| Bahasa & IDE | Java, NetBeans |
| Database | MySQL (via XAMPP + phpMyAdmin) |
| Algoritma | Regresi Linear Sederhana (Least Square Method), pembanding data **musiman/Year-over-Year** |
| Evaluasi akurasi | MAPE (Mean Absolute Percentage Error) |
| Input data | Import dari file Excel (spreadsheet) + input/edit manual |
| Output | Angka rekomendasi prediksi omzet + grafik tren (bukan keputusan final — keputusan % kenaikan tetap wewenang Kepala Divisi) |
| Pengguna | Staf Operasional Pusat (input/proses), Kepala Divisi (lihat hasil/laporan) |

---

## 2. Arsitektur Sistem

Arsitektur 3 lapis (3-tier), berjalan lokal di satu mesin:

```
┌─────────────────────────────┐
│   UI Layer (Java Swing)     │  LoginForm, DashboardForm, ImportForm,
│                              │  KelolaTransaksiForm, PrediksiForm, LaporanForm
└───────────────┬─────────────┘
                │
┌───────────────▼─────────────┐
│  Service / Logic Layer      │  RegresiLinearService, MapeEvaluator,
│                              │  ExcelImportService, AuthService
└───────────────┬─────────────┘
                │
┌───────────────▼─────────────┐
│  DAO / Data Access Layer    │  TransaksiDAO, PrediksiDAO, UserDAO
│  (JDBC)                     │
└───────────────┬─────────────┘
                │
┌───────────────▼─────────────┐
│  MySQL (via XAMPP)          │  db_theplayzone
└─────────────────────────────┘
```

**Teknologi pendukung:**
- `mysql-connector-j` — koneksi JDBC ke MySQL
- Apache POI — baca file `.xlsx` untuk fitur import
- JFreeChart — render grafik tren omzet & garis prediksi di UI
- (opsional) JasperReports/iText — kalau nanti perlu export laporan ke PDF

---

## 3. Desain Database (MySQL)

### ERD (ringkas)

```
users (1) ────< import_log (N)
users (1) ────< hasil_prediksi (N)
transaksi_harian (N) ──agregasi──> (view/query) omzet bulanan ──> dipakai oleh hasil_prediksi
```

### DDL

```sql
CREATE DATABASE IF NOT EXISTS db_theplayzone;
USE db_theplayzone;

-- Pengguna aplikasi
CREATE TABLE users (
    id_user       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nama_lengkap  VARCHAR(100) NOT NULL,
    role          ENUM('operasional', 'kepala_divisi') NOT NULL DEFAULT 'operasional',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data mentah hasil import Excel / input manual (transaksi harian)
CREATE TABLE transaksi_harian (
    id_transaksi   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tanggal        DATE NOT NULL,
    nominal        DECIMAL(15,2) NOT NULL,
    metode_bayar   ENUM('cash','e_wallet','kartu_debit','kartu_kredit') DEFAULT 'cash',
    id_import      INT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_import) REFERENCES import_log(id_import)
);

-- Rekap agregat bulanan (hasil preprocessing dari transaksi_harian)
CREATE TABLE omzet_bulanan (
    id_omzet     INT AUTO_INCREMENT PRIMARY KEY,
    tahun        SMALLINT NOT NULL,
    bulan        TINYINT NOT NULL,          -- 1..12
    total_omzet  DECIMAL(18,2) NOT NULL,
    UNIQUE KEY uq_periode (tahun, bulan)
);

-- Log setiap kali file Excel diimport
CREATE TABLE import_log (
    id_import      INT AUTO_INCREMENT PRIMARY KEY,
    nama_file      VARCHAR(255) NOT NULL,
    jumlah_baris   INT NOT NULL,
    status         ENUM('sukses','gagal') NOT NULL,
    id_user        INT NOT NULL,
    tanggal_import TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

-- Hasil proses prediksi (output algoritma Regresi Linear)
CREATE TABLE hasil_prediksi (
    id_prediksi     INT AUTO_INCREMENT PRIMARY KEY,
    bulan_target    TINYINT NOT NULL,
    tahun_target    SMALLINT NOT NULL,
    jumlah_data_n   INT NOT NULL,           -- n data historis YoY yang dipakai
    konstanta_a     DECIMAL(18,4) NOT NULL,
    koefisien_b     DECIMAL(18,4) NOT NULL,
    nilai_prediksi  DECIMAL(18,2) NOT NULL, -- Y hasil a + bX
    mape_persen     DECIMAL(6,2) NOT NULL,
    id_user         INT NOT NULL,
    tanggal_proses  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);
```

> Catatan: `transaksi_harian.id_import` mereferensikan `import_log`, jadi urutan pembuatan tabel di atas perlu disesuaikan (`import_log` dibuat sebelum `transaksi_harian`) saat dieksekusi — sudah diperhitungkan dalam skrip final di `src/main/resources/schema.sql` nanti.

---

## 4. Use Case

**Aktor:** Staf Operasional Pusat, Kepala Divisi

| Use Case | Staf Operasional | Kepala Divisi |
|---|---|---|
| Login | ✔ | ✔ |
| Import data transaksi (Excel) | ✔ | – |
| Kelola data transaksi manual (CRUD) | ✔ | – |
| Jalankan proses prediksi (pilih bulan/tahun target) | ✔ | – |
| Lihat hasil prediksi & grafik tren | ✔ | ✔ |
| Lihat riwayat/log prediksi sebelumnya | ✔ | ✔ |
| Export/cetak laporan prediksi | ✔ | ✔ |

`Jalankan proses prediksi` **include** `Hitung Regresi Linear (a, b)` dan `Hitung MAPE`.

---

## 5. Alur Algoritma (RegresiLinearService)

Sesuai Bab III.C:

1. **Preprocessing** — agregasi `transaksi_harian` → `omzet_bulanan` (SUM per tahun+bulan).
2. **Bentuk variabel** — ambil omzet bulan yang sama di tahun-tahun sebelumnya (YoY) sebagai pasangan (X = urutan tahun ke-n, Y = omzet).
3. **Hitung komponen dasar**: ΣX, ΣY, ΣXY, ΣX².
4. **Least Square Method**:
   - `b = (nΣXY − ΣXΣY) / (nΣX² − (ΣX)²)`
   - `a = (ΣY − bΣX) / n`
5. **Prediksi**: substitusi X target baru ke `Y = a + bX`.
6. **Evaluasi**: hitung `MAPE = (1/n) Σ |(Xt − Ft)/Xt| × 100%` dari data historis vs fitting.
7. **Persist & render** — simpan ke `hasil_prediksi`, tampilkan angka + grafik (JFreeChart) di `PrediksiForm`.

---

## 6. Struktur Kelas Java (usulan package)

```
com.theplayzone.prediksi
├── Main.java
├── koneksi/
│   └── DatabaseConnection.java      // JDBC singleton
├── model/
│   ├── User.java
│   ├── TransaksiHarian.java
│   ├── OmzetBulanan.java
│   └── HasilPrediksi.java
├── dao/
│   ├── UserDAO.java
│   ├── TransaksiDAO.java
│   └── PrediksiDAO.java
├── service/
│   ├── AuthService.java
│   ├── RegresiLinearService.java     // langkah 3-6 di atas
│   ├── MapeEvaluator.java
│   └── ExcelImportService.java       // Apache POI
├── ui/
│   ├── LoginForm.java
│   ├── MainDashboard.java
│   ├── ImportDataForm.java
│   ├── KelolaTransaksiForm.java
│   ├── PrediksiForm.java             // input target + tombol proses + grafik
│   └── LaporanForm.java
└── util/
    └── ChartHelper.java              // wrapper JFreeChart
```

---

## 7. Langkah Implementasi Selanjutnya

1. Buat project NetBeans + tambahkan dependency (`mysql-connector-j`, `poi-ooxml`, `jfreechart`) via Maven.
2. Jalankan `schema.sql` di phpMyAdmin/MySQL.
3. Implementasi layer DAO + koneksi JDBC.
4. Implementasi `RegresiLinearService` & `MapeEvaluator` (bisa di-unit-test terpisah dari UI).
5. Bangun UI Swing per form di atas.
6. Uji dengan dataset riwayat transaksi Jan 2023 – Des 2025 (sesuai disebutkan di Abstrak).

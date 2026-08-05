-- Skema database: Prediksi Omzet The Play Zone
-- Import file ini melalui phpMyAdmin (XAMPP) atau: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS db_theplayzone CHARACTER SET utf8mb4;
USE db_theplayzone;

CREATE TABLE users (
    id_user       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,          -- SHA-256 hex
    nama_lengkap  VARCHAR(100) NOT NULL,
    role          ENUM('admin', 'staff') NOT NULL DEFAULT 'staff',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE import_log (
    id_import      INT AUTO_INCREMENT PRIMARY KEY,
    nama_file      VARCHAR(255) NOT NULL,
    jumlah_baris   INT NOT NULL DEFAULT 0,
    status         ENUM('sukses','gagal') NOT NULL,
    id_user        INT NOT NULL,
    tanggal_import TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

CREATE TABLE toko (
    id_toko     INT AUTO_INCREMENT PRIMARY KEY,
    kode_toko   VARCHAR(10) NOT NULL UNIQUE,
    nama_toko   VARCHAR(100) NOT NULL,
    lokasi_toko VARCHAR(100)
);

CREATE TABLE metode_bayar (
    id_metode   INT AUTO_INCREMENT PRIMARY KEY,
    kode_metode INT NOT NULL,
    nama_metode VARCHAR(50) NOT NULL UNIQUE,
    kategori    VARCHAR(50),
    urutan      INT NOT NULL,
    aktif       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Rekap jumlah transaksi per toko x metode bayar x bulan, hasil import Rekap_Omzet_Per_Toko_Bulanan.xlsx.
-- Total bulanan per toko (dipakai regresi) dihitung on-the-fly via SUM(jumlah_transaksi).
CREATE TABLE rekap_metode_bulanan (
    id_rekap         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_toko          INT NOT NULL,
    id_metode        INT NOT NULL,
    tahun            SMALLINT NOT NULL,
    bulan            TINYINT NOT NULL,          -- 1..12
    jumlah_transaksi INT NOT NULL DEFAULT 0,
    id_import        INT NULL,
    UNIQUE KEY uq_rekap (id_toko, id_metode, tahun, bulan),
    FOREIGN KEY (id_toko) REFERENCES toko(id_toko),
    FOREIGN KEY (id_metode) REFERENCES metode_bayar(id_metode),
    FOREIGN KEY (id_import) REFERENCES import_log(id_import)
);

CREATE TABLE hasil_prediksi (
    id_prediksi     INT AUTO_INCREMENT PRIMARY KEY,
    bulan_target    TINYINT NOT NULL,
    tahun_target    SMALLINT NOT NULL,
    id_toko         INT NULL,                   -- NULL = agregat Semua Toko
    jumlah_data_n   INT NOT NULL,               -- n data historis YoY yang dipakai
    konstanta_a     DECIMAL(18,4) NOT NULL,
    koefisien_b     DECIMAL(18,4) NOT NULL,
    nilai_prediksi  DECIMAL(18,2) NOT NULL,      -- Y hasil a + bX
    mape_persen     DECIMAL(6,2) NOT NULL,
    id_user         INT NOT NULL,
    tanggal_proses  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES users(id_user),
    FOREIGN KEY (id_toko) REFERENCES toko(id_toko)
);

-- Seed user default
-- admin / admin123  (role: admin)
-- staff / staff123  (role: staff)
INSERT INTO users (username, password_hash, nama_lengkap, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'admin'),
('staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'Staff Operasional', 'staff');

-- Master metode bayar (15 metode riil, sesuai format Master_Metode_Bayar.xlsx). Bisa diimpor ulang lewat menu Kelola Master Metode Bayar.
INSERT INTO metode_bayar (kode_metode, nama_metode, kategori, urutan, aktif) VALUES
(1, 'Gopay', 'E-Wallet', 8, TRUE),
(2, 'OVO', 'E-Wallet', 9, TRUE),
(3, 'Dana', 'E-Wallet', 10, TRUE),
(4, 'Dana Deals', 'E-Wallet', 11, TRUE),
(5, 'Traveloka', 'E-Wallet', 12, TRUE),
(6, 'Tiket.com', 'E-Wallet', 13, TRUE),
(7, 'JD.Id', 'E-Wallet', 14, TRUE),
(8, 'K. Debit BCA', 'Kartu Debit', 15, TRUE),
(9, 'K. Kredit BCA', 'Kartu Kredit', 16, TRUE),
(10, 'QR BCA', 'QR', 17, TRUE),
(11, 'Flazz BCA', 'E-Money', 18, TRUE),
(12, 'K. Debit BRI', 'Kartu Debit', 19, TRUE),
(13, 'K. Kredit BRI', 'Kartu Kredit', 20, TRUE),
(14, 'QR BRI', 'QR', 21, TRUE),
(15, 'Cash', 'Cash', 22, TRUE);

-- Contoh 3 toko (sesuai sheet DAFTAR TOKO di Rekap_Omzet_Per_Toko_Bulanan.xlsx). Bisa diimpor ulang/ditambah lewat menu Kelola Daftar Toko.
INSERT INTO toko (kode_toko, nama_toko, lokasi_toko) VALUES
('R 14', 'PS. KOPRO', 'JAKARTA'),
('R 20', 'CIPUTAT', 'TANGERANG SELATAN'),
('R 21', 'BEKASI', 'BEKASI');

-- Contoh rekap transaksi bulanan (Januari, 3 tahun terakhir, metode Cash & Gopay) agar menu Prediksi bisa langsung dicoba.
-- Ganti/lengkapi dengan data riil via menu Kelola Rekap Transaksi Toko.
INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, jumlah_transaksi) VALUES
(1, 15, 2023, 1, 520), (1, 1, 2023, 1, 180),
(1, 15, 2024, 1, 560), (1, 1, 2024, 1, 210),
(1, 15, 2025, 1, 605), (1, 1, 2025, 1, 250),
(2, 15, 2023, 1, 610), (2, 1, 2023, 1, 200),
(2, 15, 2024, 1, 645), (2, 1, 2024, 1, 235),
(2, 15, 2025, 1, 690), (2, 1, 2025, 1, 268),
(3, 15, 2023, 1, 470), (3, 1, 2023, 1, 150),
(3, 15, 2024, 1, 505), (3, 1, 2024, 1, 178),
(3, 15, 2025, 1, 540), (3, 1, 2025, 1, 205);

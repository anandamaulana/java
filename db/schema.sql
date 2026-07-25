-- Skema database: Prediksi Omzet The Play Zone
-- Import file ini melalui phpMyAdmin (XAMPP) atau: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS db_theplayzone CHARACTER SET utf8mb4;
USE db_theplayzone;

CREATE TABLE users (
    id_user       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,          -- SHA-256 hex
    nama_lengkap  VARCHAR(100) NOT NULL,
    role          ENUM('operasional', 'kepala_divisi') NOT NULL DEFAULT 'operasional',
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

CREATE TABLE transaksi_harian (
    id_transaksi   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tanggal        DATE NOT NULL,
    nominal        DECIMAL(15,2) NOT NULL,
    metode_bayar   ENUM('cash','e_wallet','kartu_debit','kartu_kredit') DEFAULT 'cash',
    id_import      INT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_import) REFERENCES import_log(id_import)
);

CREATE TABLE omzet_bulanan (
    id_omzet     INT AUTO_INCREMENT PRIMARY KEY,
    tahun        SMALLINT NOT NULL,
    bulan        TINYINT NOT NULL,             -- 1..12
    total_omzet  DECIMAL(18,2) NOT NULL,
    UNIQUE KEY uq_periode (tahun, bulan)
);

CREATE TABLE hasil_prediksi (
    id_prediksi     INT AUTO_INCREMENT PRIMARY KEY,
    bulan_target    TINYINT NOT NULL,
    tahun_target    SMALLINT NOT NULL,
    jumlah_data_n   INT NOT NULL,               -- n data historis YoY yang dipakai
    konstanta_a     DECIMAL(18,4) NOT NULL,
    koefisien_b     DECIMAL(18,4) NOT NULL,
    nilai_prediksi  DECIMAL(18,2) NOT NULL,      -- Y hasil a + bX
    mape_persen     DECIMAL(6,2) NOT NULL,
    id_user         INT NOT NULL,
    tanggal_proses  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

-- Seed user default
-- admin / admin123   (role: kepala_divisi)
-- operasional / opr123  (role: operasional)
INSERT INTO users (username, password_hash, nama_lengkap, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Kepala Divisi The Play Zone', 'kepala_divisi'),
('operasional', 'd2c8205cbb870f54a95c3183349a30a4e29520faaf2dd5fc3366cd7c91ce0437', 'Staf Operasional Pusat', 'operasional');

-- Contoh data historis omzet bulanan (opsional, boleh dihapus lalu diisi data riil via fitur Import Excel + Rekap)
INSERT INTO omzet_bulanan (tahun, bulan, total_omzet) VALUES
(2023, 1, 85000000), (2024, 1, 92500000), (2025, 1, 101000000),
(2023, 2, 79000000), (2024, 2, 88250000), (2025, 2, 95400000);

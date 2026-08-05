-- Migrasi untuk database db_theplayzone yang SUDAH ada (dibuat sebelum fitur multi-toko).
-- Jalankan sekali via phpMyAdmin (tab SQL) atau: mysql -u root -p db_theplayzone < migration_001_toko.sql
-- Database BARU tidak perlu menjalankan file ini -- schema.sql sudah mencakup semuanya.
USE db_theplayzone;

CREATE TABLE IF NOT EXISTS toko (
    id_toko     INT AUTO_INCREMENT PRIMARY KEY,
    kode_toko   VARCHAR(10) NOT NULL UNIQUE,
    nama_toko   VARCHAR(100) NOT NULL,
    lokasi_toko VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS metode_bayar (
    id_metode   INT AUTO_INCREMENT PRIMARY KEY,
    kode_metode INT NOT NULL,
    nama_metode VARCHAR(50) NOT NULL UNIQUE,
    kategori    VARCHAR(50),
    urutan      INT NOT NULL,
    aktif       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rekap_metode_bulanan (
    id_rekap         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_toko          INT NOT NULL,
    id_metode        INT NOT NULL,
    tahun            SMALLINT NOT NULL,
    bulan            TINYINT NOT NULL,
    jumlah_transaksi INT NOT NULL DEFAULT 0,
    id_import        INT NULL,
    UNIQUE KEY uq_rekap (id_toko, id_metode, tahun, bulan),
    FOREIGN KEY (id_toko) REFERENCES toko(id_toko),
    FOREIGN KEY (id_metode) REFERENCES metode_bayar(id_metode),
    FOREIGN KEY (id_import) REFERENCES import_log(id_import)
);

ALTER TABLE hasil_prediksi
    ADD COLUMN IF NOT EXISTS id_toko INT NULL AFTER tahun_target,
    ADD CONSTRAINT fk_hasil_prediksi_toko FOREIGN KEY (id_toko) REFERENCES toko(id_toko);

INSERT IGNORE INTO metode_bayar (kode_metode, nama_metode, kategori, urutan, aktif) VALUES
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

INSERT IGNORE INTO toko (kode_toko, nama_toko, lokasi_toko) VALUES
('R 14', 'PS. KOPRO', 'JAKARTA'),
('R 20', 'CIPUTAT', 'TANGERANG SELATAN'),
('R 21', 'BEKASI', 'BEKASI');

INSERT IGNORE INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, jumlah_transaksi) VALUES
(1, 15, 2023, 1, 520), (1, 1, 2023, 1, 180),
(1, 15, 2024, 1, 560), (1, 1, 2024, 1, 210),
(1, 15, 2025, 1, 605), (1, 1, 2025, 1, 250),
(2, 15, 2023, 1, 610), (2, 1, 2023, 1, 200),
(2, 15, 2024, 1, 645), (2, 1, 2024, 1, 235),
(2, 15, 2025, 1, 690), (2, 1, 2025, 1, 268),
(3, 15, 2023, 1, 470), (3, 1, 2023, 1, 150),
(3, 15, 2024, 1, 505), (3, 1, 2024, 1, 178),
(3, 15, 2025, 1, 540), (3, 1, 2025, 1, 205);

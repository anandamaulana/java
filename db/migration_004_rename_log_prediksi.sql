-- Ganti nama tabel hasil_prediksi -> log_prediksi (mengikuti istilah pada skripsi Bab IV).
-- Jalankan sekali via phpMyAdmin (tab SQL) atau: mysql -u root -p db_theplayzone < migration_004_rename_log_prediksi.sql
-- Database BARU tidak perlu menjalankan file ini -- schema.sql sudah memakai nama log_prediksi.
USE db_theplayzone;

RENAME TABLE hasil_prediksi TO log_prediksi;

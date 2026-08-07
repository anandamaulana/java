-- Migration 005:
-- 1) Mengizinkan akun user dihapus (menu Kelola Data Pengguna) walau akun itu sudah pernah melakukan
--    import (import_log) atau proses prediksi (log_prediksi). Sebelumnya penghapusan gagal karena
--    FOREIGN KEY id_user NOT NULL tanpa ON DELETE, sehingga MySQL menolak (error 1451: "Cannot delete
--    or update a parent row: a foreign key constraint fails"). Riwayat lama TETAP tersimpan, hanya
--    id_user pada baris lama menjadi NULL (ditampilkan sebagai "Pengguna Dihapus" di aplikasi).
-- 2) Menambah kolom total_omzet (Rupiah) pada rekap_metode_bulanan untuk fitur upload grid
--    Rekap_Transaksi_Toko_<Tahun>.xlsx / Rekap_Transaksi_Toko_<Bulan>_<Tahun>.xlsx (nominal omzet per
--    toko x metode x bulan, terpisah dari jumlah_transaksi yang sudah ada).
--
-- Catatan: nama FOREIGN KEY di bawah (import_log_ibfk_1, log_prediksi_ibfk_1) mengikuti penamaan
-- otomatis default MySQL/InnoDB sesuai urutan FOREIGN KEY pada db/schema.sql. Jika database Anda
-- ternyata memakai nama constraint yang berbeda, cek dulu lewat:
--   SHOW CREATE TABLE import_log;
--   SHOW CREATE TABLE log_prediksi;
-- lalu sesuaikan nama pada baris DROP FOREIGN KEY di bawah ini.

ALTER TABLE import_log MODIFY id_user INT NULL;
ALTER TABLE import_log DROP FOREIGN KEY import_log_ibfk_1;
ALTER TABLE import_log ADD CONSTRAINT import_log_ibfk_1 FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE SET NULL;

ALTER TABLE log_prediksi MODIFY id_user INT NULL;
ALTER TABLE log_prediksi DROP FOREIGN KEY log_prediksi_ibfk_1;
ALTER TABLE log_prediksi ADD CONSTRAINT log_prediksi_ibfk_1 FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE SET NULL;

ALTER TABLE rekap_metode_bulanan ADD COLUMN total_omzet DECIMAL(18,2) NOT NULL DEFAULT 0 AFTER jumlah_transaksi;

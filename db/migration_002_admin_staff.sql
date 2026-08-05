-- Migrasi role users dari (operasional, kepala_divisi) ke (admin, staff).
-- Jalankan sekali via phpMyAdmin (tab SQL) atau: mysql -u root -p db_theplayzone < migration_002_admin_staff.sql
-- Database BARU tidak perlu menjalankan file ini -- schema.sql sudah mencakup semuanya.
USE db_theplayzone;

-- Perluas dulu ENUM supaya nilai lama & baru bisa hidup berdampingan sesaat.
ALTER TABLE users MODIFY COLUMN role ENUM('operasional', 'kepala_divisi', 'admin', 'staff') NOT NULL DEFAULT 'staff';

UPDATE users SET role = 'admin' WHERE role = 'kepala_divisi';
UPDATE users SET role = 'staff' WHERE role = 'operasional';

-- Persempit kembali ENUM ke nilai final.
ALTER TABLE users MODIFY COLUMN role ENUM('admin', 'staff') NOT NULL DEFAULT 'staff';

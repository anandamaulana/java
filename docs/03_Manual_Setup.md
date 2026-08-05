# Manual Setup & Instalasi
## Aplikasi Prediksi Omzet The Play Zone

Panduan ini mencakup dua jalur database yang bisa dipilih salah satu: **XAMPP** (sesuai batasan masalah pada skripsi) atau **Docker** (alternatif lebih cepat/terisolasi). Kedua jalur menghasilkan MySQL yang bisa diakses di `localhost:3306` dengan konfigurasi identik, sehingga **tidak perlu mengubah kode aplikasi**.

---

## 0. Prasyarat

| Komponen | Kebutuhan | Cek versi |
|---|---|---|
| JDK | 17 atau lebih baru | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| MySQL | via XAMPP **atau** Docker (pilih salah satu) | — |
| Docker Desktop | hanya jika memilih jalur Docker | `docker -version` |

**Instal JDK & Maven** (jika belum ada):
- Windows: unduh [Eclipse Temurin JDK 17](https://adoptium.net/) dan [Apache Maven](https://maven.apache.org/download.cgi), atau via `winget install EclipseAdoptium.Temurin.17.JDK` dan `winget install Apache.Maven`.
- Tambahkan `JAVA_HOME` dan folder `bin` Maven ke environment variable `PATH`.

---

## 1A. Setup Database via XAMPP

1. Instal [XAMPP](https://www.apachefriends.org/) lalu jalankan **XAMPP Control Panel**.
2. Start modul **Apache** dan **MySQL**.
3. Buka `http://localhost/phpmyadmin`.
4. Klik tab **Import** → pilih file `db/schema.sql` dari folder proyek ini → klik **Go**.
   - Ini otomatis membuat database `db_theplayzone`, seluruh tabel, dan 2 akun contoh.
   - **Database yang sudah ada dari versi lama**: jangan import ulang `schema.sql` (akan gagal karena tabel sudah ada). Import migrasi yang sesuai lewat cara yang sama, urut: `db/migration_001_toko.sql` (menambah tabel `toko`/`metode_bayar`/`rekap_metode_bulanan`), lalu `db/migration_002_admin_staff.sql` (mengubah role `operasional`/`kepala_divisi` menjadi `staff`/`admin`) — keduanya tanpa menghapus data yang sudah ada.
5. Konfigurasi default di `src/main/resources/db.properties` sudah cocok dengan XAMPP (`root` tanpa password). Tidak perlu diubah kecuali konfigurasi MySQL di komputer client berbeda.

## 1B. Setup Database via Docker (Alternatif)

1. Instal [Docker Desktop](https://www.docker.com/products/docker-desktop/) dan pastikan sudah berjalan.
2. Dari folder root proyek ini, jalankan:

   ```
   docker compose up -d
   ```

3. Ini akan membuat:
   - Container **MySQL 8** di port `3306`, otomatis menjalankan `db/schema.sql` saat pertama kali dibuat (database, tabel, dan seed akun).
   - Container **phpMyAdmin** di `http://localhost:8081` (opsional, untuk melihat/mengelola data lewat browser — user `root`, password kosong).
4. Cek status: `docker compose ps` (pastikan status `healthy`/`running`).
5. Untuk menghentikan: `docker compose down` (data tetap tersimpan di volume `db_data`). Untuk menghapus total termasuk data: `docker compose down -v`.
6. Konfigurasi `db.properties` **tidak perlu diubah** — password root sengaja dikosongkan di `docker-compose.yml` agar sama seperti default XAMPP.

---

## 2. Menjalankan Aplikasi

### Opsi A — Lewat NetBeans (sesuai skripsi)

1. Buka NetBeans → **File > Open Project** → pilih folder root proyek ini (`pom.xml` akan terdeteksi otomatis sebagai Maven project).
2. Tunggu NetBeans mengunduh dependency (mysql-connector-j, Apache POI, JFreeChart) — butuh koneksi internet saat pertama kali.
3. Klik kanan project → **Run** (atau tombol ▶), pastikan Main Class = `com.theplayzone.prediksi.Main`.
4. Jendela Login akan muncul.

### Opsi B — Lewat Terminal / Maven CLI

```
mvn clean package
java -jar target/prediksi-theplayzone.jar
```

Perintah `mvn clean package` menghasilkan **fat jar** (sudah berisi semua dependency) di folder `target/`, sehingga file jar ini bisa dipindahkan/dijalankan langsung di laptop client lain yang sudah memiliki JDK 17+ tanpa perlu instal Maven/NetBeans di sana.

---

## 3. Login Awal

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `staff` | `staff123` | Staff |

Lihat `02_Manual_Aplikasi.md` untuk panduan pemakaian tiap menu.

---

## 4. Troubleshooting

| Gejala | Penyebab Umum | Solusi |
|---|---|---|
| `Gagal terhubung ke database` saat login | MySQL belum aktif | Pastikan modul MySQL di XAMPP hijau/running, atau `docker compose ps` menunjukkan container `mysql` sehat |
| `Unknown database 'db_theplayzone'` | `schema.sql` belum diimport | Ulangi langkah 1A poin 4, atau pastikan `docker-entrypoint-initdb.d` memuat file saat container **pertama kali** dibuat (jika container lama sudah ada tanpa data, jalankan `docker compose down -v` lalu `docker compose up -d` untuk membuat ulang dari awal) |
| Port 3306 sudah dipakai (bentrok XAMPP vs Docker) | Kedua jalur database aktif bersamaan | Gunakan **salah satu saja** — matikan MySQL di XAMPP jika memakai Docker, atau `docker compose down` jika memakai XAMPP |
| `Data historis ... belum cukup untuk regresi` saat proses prediksi | Rekap transaksi toko untuk bulan/toko tsb kurang dari 2 tahun sebelumnya | Lengkapi data lewat menu **Import Rekap Toko Bulanan** |
| `Table 'db_theplayzone.metode_bayar' doesn't exist` (atau `toko`/`rekap_metode_bulanan`) | Database dibuat sebelum fitur multi-toko ditambahkan | Import `db/migration_001_toko.sql` (lihat langkah 1A poin 4) |
| `Data truncated for column 'role'` saat login akun lama, atau akun `operasional`/`kepala_divisi` tidak bisa login | Database dibuat sebelum role diubah menjadi Admin/Staff | Import `db/migration_002_admin_staff.sql` |
| Karakter Rupiah/format aneh di hasil prediksi | Locale sistem operasi non-Indonesia | Tidak mempengaruhi perhitungan, hanya tampilan format mata uang |

---

## 5. Keamanan (Sebelum Dipakai Produksi)

- Ganti password akun `admin` dan `staff` bawaan — update kolom `password_hash` di tabel `users` dengan hash SHA-256 dari password baru (atau buat akun Staff baru lewat menu **Kelola Data Pengguna**, lalu hapus akun `staff` bawaan).
- Jika memakai XAMPP di jaringan bersama, set password root MySQL (jangan biarkan kosong) dan sesuaikan `db.properties`.
- Batasi akses folder `db.properties` karena berisi kredensial database.

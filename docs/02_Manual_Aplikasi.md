# Manual Aplikasi (Panduan Penggunaan)
## Aplikasi Prediksi Omzet The Play Zone

Dokumen ini menjelaskan cara memakai setiap menu aplikasi. Pastikan environment sudah disiapkan mengikuti `03_Manual_Setup.md` sebelum mengikuti panduan ini.

---

## 1. Login

1. Jalankan aplikasi (lihat `03_Manual_Setup.md` untuk cara menjalankan).
2. Isi **Username** dan **Password**.
3. Akun bawaan (seed) dari `db/schema.sql`:

   | Username | Password | Role |
   |---|---|---|
   | `admin` | `admin123` | Admin |
   | `staff` | `staff123` | Staff |

4. Klik **Login**. Jika berhasil, jendela **Dashboard** akan terbuka sesuai peran akun.

> Ganti password default ini sebelum digunakan secara produksi — lihat bagian *Keamanan* di `03_Manual_Setup.md`.

## 2. Dashboard

Ada dua peran pengguna. **Admin bukan Kepala Divisi** — Kepala Divisi adalah pihak eksternal yang menerima laporan PDF (lihat bagian 7), bukan pengguna sistem.

- **Admin**: bisa akses **semua menu**, termasuk yang khusus Admin: **Kelola Data Pengguna** (membuat/menghapus akun Staff), serta menu legaci Import Data Transaksi & Kelola Data Transaksi.
- **Staff**: Login, Import Rekap Toko Bulanan, Proses & Lihat Prediksi, Riwayat/Laporan Prediksi.

Tombol **Logout** di pojok kanan atas kembali ke layar Login.

## 3. Kelola Data Pengguna (khusus Admin)

1. Buka menu **Kelola Data Pengguna** — tabel menampilkan seluruh akun (Admin & Staff).
2. **Tambah akun Staff**: isi Username, Password, Nama Lengkap, klik **Tambah Akun Staff**. Akun baru otomatis berperan Staff (menu ini tidak membuat akun Admin lain).
3. **Hapus akun**: pilih baris, klik **Hapus Terpilih**. Akun yang sedang login tidak bisa menghapus dirinya sendiri. Akun yang sudah pernah memproses/menyimpan prediksi atau import tidak bisa dihapus (dilindungi relasi data) — hapus riwayatnya dulu jika benar-benar perlu.
4. Klik **Refresh** untuk memuat ulang data terbaru.

## 4. Import Rekap Toko Bulanan (sumber data Prediksi)

Ini jalur import utama — data dari sinilah yang dipakai algoritma Regresi Linear di menu Prediksi. Detail lengkap format kedua file ada di `05_Format_Excel_Import.md`.

1. Buka menu **Import Rekap Toko Bulanan**.
2. Klik **Pilih Master_Metode_Bayar.xlsx**, pilih file master metode bayar, lalu klik **Import Metode Bayar**. Lakukan ini **sebelum** langkah berikutnya — nama metode di file ini dipakai untuk mencocokkan kolom di file rekap.
3. Klik **Pilih Rekap_Omzet_Per_Toko_Bulanan.xlsx**, pilih file rekap toko. Isi **Tahun** data (satu tahun berlaku untuk seluruh 12 sheet bulan di file tersebut).
4. Klik **Import Daftar Toko + Rekap**. Aplikasi akan mengimpor sheet DAFTAR TOKO (daftar cabang) lalu 12 sheet bulanan (Januari–Desember) sekaligus. Ringkasan hasil (jumlah toko, baris rekap berhasil/gagal) tampil di log.
5. Import ulang dengan file yang sama (mis. setelah data direvisi) aman dilakukan — data lama akan ditimpa, bukan diduplikasi.

> Untuk tahun berikutnya, siapkan workbook rekap baru (boleh salinan template dengan nilai diperbarui) dan ulangi langkah 3–4 dengan Tahun yang sesuai.

## 5. Import Data Transaksi (Excel) — pencatatan harian (opsional, khusus Admin)

Digunakan untuk memasukkan arsip transaksi harian dari file spreadsheet accounting.

1. Buka menu **Import Data Transaksi (Excel)**.
2. Klik **Unduh Template Excel** — muncul dialog Save, pilih lokasi penyimpanan (mis. Desktop), klik Save. Aplikasi akan menyalin file template `.xlsx` siap pakai ke lokasi tersebut (template ini dibundel di dalam aplikasi, tidak perlu file terpisah/internet).
3. Buka file template yang baru diunduh, hapus baris contoh, isi dengan data transaksi asli dari accounting, lalu simpan.
4. Kembali ke aplikasi, klik **Pilih File Excel (.xlsx)**, pilih file yang sudah diisi tadi.
5. Format kolom file Excel (baris 1 = header, data mulai baris 2) — sudah sesuai bawaan template:

   | Kolom | Isi | Contoh |
   |---|---|---|
   | A | Tanggal (format `yyyy-MM-dd`) | 2025-01-15 |
   | B | Nominal | 1500000 |
   | C | Metode Bayar *(opsional)* | cash / e_wallet / kartu_debit / kartu_kredit |

6. Klik **Import ke Database** — hasil (jumlah baris berhasil/gagal) tampil di area log.
7. Klik **Rekap ke Omzet Bulanan** — mengagregasi seluruh transaksi harian menjadi total omzet per bulan.

> **Catatan**: menu ini (dan hasil rekapnya) **tidak lagi dipakai oleh fitur Proses Prediksi** — sumber data prediksi sekarang dari menu *Import Rekap Toko Bulanan* (bagian 4). Menu ini tetap tersedia untuk pencatatan/arsip transaksi harian manual.

## 6. Kelola Data Transaksi (khusus Admin)

Alternatif input manual (satu per satu) tanpa file Excel, atau untuk mengoreksi/menghapus data harian (lihat catatan di bagian 5 — tidak memengaruhi hasil Prediksi).

1. Buka menu **Kelola Data Transaksi** — tabel menampilkan seluruh transaksi harian tersimpan.
2. **Tambah data**: isi Tanggal (`yyyy-MM-dd`), Nominal, pilih Metode, klik **Tambah**.
3. **Hapus data**: pilih baris pada tabel, klik **Hapus Terpilih**.
4. Klik **Refresh** untuk memuat ulang data terbaru.

## 7. Proses & Lihat Prediksi

Menu inti — menjalankan algoritma Regresi Linear atas data yang sudah diimport lewat **Import Rekap Toko Bulanan** (bagian 4). Bisa diakses Admin maupun Staff.

1. Pilih **Toko** (toko tertentu, atau "Semua Toko" untuk agregat seluruh cabang), **Bulan Target**, dan **Tahun Target**.
2. Klik **Proses Prediksi**. Sistem mengambil total transaksi bulan yang sama dari tahun-tahun sebelumnya (Year-over-Year) untuk toko/agregat terpilih, lalu menghitung:
   - Konstanta (`a`) dan koefisien (`b`)
   - Persamaan garis tren `Y = a + bX`
   - Nilai prediksi jumlah transaksi
   - Tingkat error (**MAPE**)
3. Hasil angka tampil di panel atas; grafik tren (data aktual, garis regresi, titik prediksi) tampil di panel bawah.
4. Klik **Simpan Hasil Prediksi** untuk mencatatnya ke riwayat.
5. Klik **Export PDF** untuk mengunduh laporan hasil prediksi ini sebagai file PDF (kop logo The Play Zone, toko target, rincian angka, grafik tren, dan area tanda tangan "Dibuat oleh, Staff" / "Mengetahui, Kepala Divisi The Play Zone" untuk ditandatangani manual setelah dicetak).

**Catatan**: minimal dibutuhkan 2 tahun data historis untuk bulan & toko yang sama sebelum tahun target. Jika data belum cukup, sistem menampilkan peringatan — lengkapi data lewat menu **Import Rekap Toko Bulanan** terlebih dahulu.

## 8. Riwayat / Laporan Prediksi

1. Buka menu **Riwayat / Laporan Prediksi**.
2. Tabel menampilkan seluruh hasil prediksi yang pernah disimpan: waktu diproses, toko, periode target, jumlah data (n), nilai `a`/`b`, hasil prediksi, MAPE, dan siapa yang memprosesnya.
3. Klik **Refresh** untuk memuat data terbaru.

Dipakai Admin/Staff untuk meninjau riwayat prediksi sebelum dicetak (Export PDF) dan diserahkan ke **Kepala Divisi** — pihak eksternal (bukan pengguna sistem) yang memakainya sebagai bahan evaluasi dan pengambilan keputusan target operasional.

---

## Alur Kerja yang Disarankan (End-to-End)

```
Login (Admin)
   -> Kelola Data Pengguna: buat akun Staff
Login (Staff)
   -> Import Rekap Toko Bulanan: Master Metode Bayar, lalu Daftar Toko + Rekap 12 bulan
   -> Proses & Lihat Prediksi (pilih Toko, bulan/tahun target)
   -> Simpan Hasil Prediksi
   -> Export PDF -> cetak & serahkan ke Kepala Divisi untuk ditandatangani
   -> Riwayat / Laporan Prediksi -> tinjau riwayat sebelum periode berikutnya
```

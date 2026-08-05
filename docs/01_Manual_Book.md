# Buku Manual (Manual Book)
## Aplikasi Prediksi Omzet The Play Zone

| | |
|---|---|
| **Nama Aplikasi** | Aplikasi Prediksi Transaksi & Omzet The Play Zone |
| **Metode** | Regresi Linear Sederhana (Least Square Method), pembanding musiman (Year-over-Year) |
| **Platform** | Desktop (Java Swing) |
| **Basis Data** | MySQL 8 |
| **Studi Kasus** | Jaringan toko The Play Zone — PT. Ramayana Makmur Sentosa |
| **Pengguna** | Admin, Staff (pengguna sistem) — Kepala Divisi sebagai penerima laporan (bukan pengguna sistem) |

---

## 1. Tentang Aplikasi

Aplikasi ini dibangun untuk mendigitalkan proses penyusunan proyeksi jumlah transaksi bulanan per toko yang sebelumnya dikerjakan manual menggunakan spreadsheet. Aplikasi menghitung angka rekomendasi prediksi secara otomatis menggunakan algoritma **Regresi Linear**, dengan membandingkan data historis jumlah transaksi pada bulan yang sama di tahun-tahun sebelumnya (**Year-over-Year**), per toko maupun agregat seluruh toko.

Output aplikasi berupa **angka rekomendasi prediksi (baseline)** beserta **grafik tren**, **tingkat akurasi (MAPE)**, dan **laporan PDF siap cetak** — bukan keputusan final. Laporan PDF diserahkan ke **Kepala Divisi** untuk ditandatangani dan menjadi dasar keputusan target operasional.

> Aplikasi ini adalah alat bantu analisis manajerial, **bukan** sistem kasir/Point of Sales (POS).

## 2. Ruang Lingkup Fitur

| Fitur | Deskripsi | Hak Akses |
|---|---|---|
| Login | Autentikasi pengguna berbasis username & password | Semua pengguna |
| Kelola Data Pengguna | Membuat/menghapus akun Staff | Admin |
| Import Rekap Toko Bulanan | Import Master Metode Bayar + Daftar Toko & rekap transaksi 12 bulan (sumber data Prediksi) | Admin, Staff |
| Import Data Excel (legaci) | Mengimpor data transaksi harian dari file `.xlsx`, tidak dipakai Prediksi | Admin |
| Kelola Data Transaksi (legaci) | Tambah/lihat/hapus data transaksi harian secara manual | Admin |
| Proses Prediksi | Menjalankan algoritma Regresi Linear per toko/semua toko untuk bulan & tahun target | Admin, Staff |
| Lihat Grafik Tren & Prediksi | Visualisasi data aktual, garis tren, dan titik prediksi | Admin, Staff |
| Simpan Hasil Prediksi | Menyimpan hasil proses ke riwayat basis data | Admin, Staff |
| Export Laporan PDF | Membuat file PDF siap cetak & ditandatangani | Admin, Staff |
| Riwayat / Laporan Prediksi | Melihat seluruh riwayat hasil prediksi yang pernah diproses | Admin, Staff |

## 3. Peran Pengguna (Role)

- **Admin** (`admin`) — mengelola akun Staff, dan bisa mengakses seluruh fitur (termasuk semua yang bisa dilakukan Staff serta menu legaci).
- **Staff** (`staff`) — login, mengelola Master Metode Bayar & Rekap Transaksi Toko (Import Rekap Toko Bulanan), menjalankan & menyimpan proses prediksi, export laporan PDF.
- **Kepala Divisi** — **bukan** peran login di sistem ini. Merupakan pihak eksternal yang menerima laporan PDF hasil prediksi (dicetak & ditandatangani secara fisik) sebagai dasar pengambilan keputusan strategis.

## 4. Struktur Dokumen Pendukung

Dokumentasi aplikasi ini terdiri atas tiga bagian:

1. **`01_Manual_Book.md`** (dokumen ini) — gambaran umum aplikasi.
2. **`02_Manual_Aplikasi.md`** — panduan langkah demi langkah menggunakan setiap menu.
3. **`03_Manual_Setup.md`** — panduan instalasi & konfigurasi environment (JDK, MySQL via XAMPP atau Docker, build & jalankan aplikasi).

## 5. Landasan Algoritma (Ringkas)

Persamaan regresi: **Y = a + bX**

- `X` = urutan waktu historis (tahun ke-1, ke-2, dst. untuk bulan yang sama)
- `Y` = total jumlah transaksi aktual pada periode tersebut (per toko atau agregat semua toko)
- `a`, `b` dihitung dengan metode kuadrat terkecil (*Least Square Method*)
- Akurasi diukur dengan **MAPE** (*Mean Absolute Percentage Error*) — semakin kecil nilainya, semakin andal model prediksinya.

Detail teknis lengkap ada di `Rancangan_Aplikasi.md` pada root proyek.

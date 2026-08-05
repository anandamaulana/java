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
| Kelola Data Pengguna | Membuat, mengedit, menghapus akun Staff | Admin |
| Kelola Daftar Toko | Data master cabang/toko, manual atau import Excel massal | Admin |
| Kelola Master Metode Bayar | Data master jenis pembayaran, manual atau import Excel massal | Admin |
| Kelola Rekap Transaksi Toko | Input/edit/hapus rekap transaksi bulanan per toko x metode, manual atau import Excel massal (sumber data Prediksi) | Admin, Staff |
| Proses Prediksi | Menjalankan algoritma Regresi Linear per toko/semua toko untuk bulan & tahun target | Admin, Staff |
| Lihat Grafik Tren & Prediksi | Visualisasi data aktual, garis tren, dan titik prediksi | Admin, Staff |
| Simpan Hasil Prediksi | Menyimpan hasil proses ke riwayat basis data | Admin, Staff |
| Export Laporan PDF | Membuat file PDF siap cetak & ditandatangani (per hasil prediksi) | Admin, Staff |
| Riwayat / Laporan Prediksi | Melihat, filter, hapus, dan export PDF riwayat hasil prediksi | Admin, Staff |
| Visualisasi Grafik Prediksi Omzet | Menampilkan ulang grafik tren dari hasil prediksi tersimpan + export PDF | Admin, Staff |
| Laporan Gabungan | Export satu PDF berisi Daftar Toko + Master Metode Bayar + Rekap Transaksi + Riwayat Prediksi | Admin, Staff |
| Laporan Daftar Toko / Master Metode Bayar / Rekap Transaksi | Export PDF tabel data masing-masing (dari menu Kelola terkait) | Admin (Toko/Metode), Admin+Staff (Rekap) |

## 3. Peran Pengguna (Role)

- **Admin** (`admin`) — superuser: mengelola akun Staff dan data master (Daftar Toko, Master Metode Bayar), plus bisa mengakses semua yang bisa dilakukan Staff.
- **Staff** (`staff`) — login, mengelola Rekap Transaksi Toko, menjalankan & menyimpan proses prediksi, export laporan PDF.
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

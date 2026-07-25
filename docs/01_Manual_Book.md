# Buku Manual (Manual Book)
## Aplikasi Prediksi Omzet The Play Zone

| | |
|---|---|
| **Nama Aplikasi** | Aplikasi Prediksi Transaksi & Omzet The Play Zone |
| **Metode** | Regresi Linear Sederhana (Least Square Method), pembanding musiman (Year-over-Year) |
| **Platform** | Desktop (Java Swing) |
| **Basis Data** | MySQL 8 |
| **Studi Kasus** | Counter The Play Zone — PT. Ramayana Makmur Sentosa |
| **Pengguna** | Staf Operasional Pusat, Kepala Divisi The Play Zone |

---

## 1. Tentang Aplikasi

Aplikasi ini dibangun untuk mendigitalkan proses penyusunan proyeksi omzet bulanan yang sebelumnya dikerjakan manual menggunakan spreadsheet oleh Bagian Operasional Pusat The Play Zone. Aplikasi menghitung angka rekomendasi prediksi omzet secara otomatis menggunakan algoritma **Regresi Linear**, dengan membandingkan data historis omzet pada bulan yang sama di tahun-tahun sebelumnya (**Year-over-Year**).

Output aplikasi berupa **angka rekomendasi prediksi (baseline)** beserta **grafik tren** dan **tingkat akurasi (MAPE)** — bukan keputusan final. Keputusan persentase kenaikan target operasional tetap menjadi wewenang Kepala Divisi.

> Aplikasi ini adalah alat bantu analisis manajerial, **bukan** sistem kasir/Point of Sales (POS).

## 2. Ruang Lingkup Fitur

| Fitur | Deskripsi | Hak Akses |
|---|---|---|
| Login | Autentikasi pengguna berbasis username & password | Semua pengguna |
| Import Data Excel | Mengimpor data transaksi harian dari file `.xlsx` | Staf Operasional |
| Kelola Data Transaksi | Tambah/lihat/hapus data transaksi harian secara manual | Staf Operasional |
| Rekap Omzet Bulanan | Agregasi transaksi harian menjadi total omzet per bulan | Staf Operasional |
| Proses Prediksi | Menjalankan algoritma Regresi Linear untuk bulan & tahun target tertentu | Staf Operasional |
| Lihat Grafik Tren & Prediksi | Visualisasi data aktual, garis tren, dan titik prediksi | Semua pengguna |
| Simpan Hasil Prediksi | Menyimpan hasil proses ke riwayat basis data | Staf Operasional |
| Riwayat / Laporan Prediksi | Melihat seluruh riwayat hasil prediksi yang pernah diproses | Semua pengguna |

## 3. Peran Pengguna (Role)

- **Staf Operasional Pusat** (`operasional`) — mengelola data (import/CRUD), menjalankan & menyimpan proses prediksi.
- **Kepala Divisi** (`kepala_divisi`) — meninjau hasil prediksi dan laporan riwayat sebagai dasar pengambilan keputusan strategis (read-only, tidak mengelola data mentah).

## 4. Struktur Dokumen Pendukung

Dokumentasi aplikasi ini terdiri atas tiga bagian:

1. **`01_Manual_Book.md`** (dokumen ini) — gambaran umum aplikasi.
2. **`02_Manual_Aplikasi.md`** — panduan langkah demi langkah menggunakan setiap menu.
3. **`03_Manual_Setup.md`** — panduan instalasi & konfigurasi environment (JDK, MySQL via XAMPP atau Docker, build & jalankan aplikasi).

## 5. Landasan Algoritma (Ringkas)

Persamaan regresi: **Y = a + bX**

- `X` = urutan waktu historis (tahun ke-1, ke-2, dst. untuk bulan yang sama)
- `Y` = total omzet aktual pada periode tersebut
- `a`, `b` dihitung dengan metode kuadrat terkecil (*Least Square Method*)
- Akurasi diukur dengan **MAPE** (*Mean Absolute Percentage Error*) — semakin kecil nilainya, semakin andal model prediksinya.

Detail teknis lengkap ada di `Rancangan_Aplikasi.md` pada root proyek.

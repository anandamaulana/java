# Format Excel untuk Import Data

Ada dua file Excel yang dipakai untuk mengisi data master & rekap yang menjadi sumber fitur **Proses Prediksi**: `Master_Metode_Bayar.xlsx` (menu **Kelola Master Metode Bayar**) dan `Rekap_Omzet_Per_Toko_Bulanan.xlsx` (dibaca sebagian di **Kelola Daftar Toko**, sebagian lagi di **Kelola Rekap Transaksi Toko**).

## 1. Master_Metode_Bayar.xlsx

Dibaca oleh menu **Kelola Master Metode Bayar** (`MasterMetodeBayarImportService.java`). Sheet bernama **MASTER METODE BAYAR**. Header di baris ke-4, data mulai baris ke-5.

| Kolom | Field | Format | Contoh |
|---|---|---|---|
| A | Kode Metode | Angka | `1` |
| B | Nama Metode | Teks (harus unik, jadi acuan pencocokan kolom di sheet rekap) | `Gopay` |
| C | Kategori | Teks bebas | `E-Wallet` |
| D | Urutan | Angka | `8` |
| E | Aktif | `TRUE` atau `FALSE` | `TRUE` |

Import file ini **lebih dulu**, sebelum mengisi Kelola Daftar Toko dan Kelola Rekap Transaksi Toko — nama metode di sini dipakai untuk mencocokkan kolom pada sheet rekap bulanan.

## 2. Rekap_Omzet_Per_Toko_Bulanan.xlsx

Satu workbook berisi 13 sheet yang relevan untuk import (sheet ke-14 "TOTAL OMZET PER BULAN" hanya rollup formula, tidak dibaca aplikasi):

- **DAFTAR TOKO** — daftar cabang/toko. Header baris ke-4, data mulai baris ke-5: A = Kode Toko, B = Nama Toko, C = Lokasi Toko (opsional). Dibaca oleh menu **Kelola Daftar Toko** (`TokoImportService.java`) — hanya sheet ini yang dipakai saat import di menu tersebut.
- **Januari** s.d. **Desember** (12 sheet) — rekap transaksi bulan tersebut. Header baris ke-4: kolom A = Kode Toko, kolom B = Nama Toko (formula lookup, diabaikan saat import), kolom berikutnya = **jumlah transaksi per metode bayar** dengan header persis nama metode di Master Metode Bayar (mis. `Gopay`, `OVO`, `Cash`, dst). Data mulai baris ke-5. Dibaca oleh menu **Kelola Rekap Transaksi Toko** (`RekapTokoImportService.java`).

Saat import di **Kelola Rekap Transaksi Toko**, aplikasi meminta input **Tahun** (satu tahun berlaku untuk semua 12 sheet bulan dalam file tersebut — workbook ini didesain untuk diisi ulang setiap tahun).

Urutan proses (dua menu terpisah, dua langkah):
1. **Kelola Daftar Toko** → Import Excel → sheet DAFTAR TOKO diimport, mengisi/memperbarui tabel `toko`.
2. **Kelola Rekap Transaksi Toko** → Import Excel (12 sheet bulan) → toko dicocokkan via Kode Toko (harus **sudah ada** dari langkah 1 — menu ini tidak lagi otomatis membuat toko baru), kolom dicocokkan ke `metode_bayar` berdasarkan **nama header** (bukan posisi kolom). Tiap baris (toko) × tiap kolom metode yang cocok diupsert ke tabel `rekap_metode_bulanan` (kunci: toko + metode + tahun + bulan) — import ulang dengan file yang sama akan menimpa (bukan menduplikasi) data.
3. Kode Toko yang belum terdaftar di `toko` dicatat sebagai gagal (ditampilkan di log, tidak menghentikan proses baris lain) — lengkapi Kelola Daftar Toko dahulu.

**Catatan penting**: nilai yang direkap adalah **jumlah transaksi** (angka bulat), bukan nominal Rupiah — sesuai judul skripsi "Prediksi Transaksi". Total per toko per bulan dihitung otomatis (SUM seluruh kolom metode) saat menjalankan Regresi Linear.

## 3. Input Manual (tanpa Excel)

Ketiga menu (Kelola Daftar Toko, Kelola Master Metode Bayar, Kelola Rekap Transaksi Toko) juga punya form input manual satu-baris di bawah tabelnya, untuk menambah atau memperbaiki data satu per satu tanpa perlu file Excel — lihat `02_Manual_Aplikasi.md` bagian 4–6.

## 4. Kesalahan Umum

- **Import Rekap Transaksi Toko sebelum Kelola Master Metode Bayar diisi** — gagal total dengan pesan "Master Metode Bayar masih kosong". Isi Master Metode Bayar dulu.
- **Import Rekap Transaksi Toko sebelum Kelola Daftar Toko diisi** — gagal total dengan pesan "Daftar Toko masih kosong". Isi Daftar Toko dulu (lewat menunya sendiri, bukan otomatis dari file rekap).
- **Nama sheet bulan diubah** (mis. "Jan" bukan "Januari") — sheet itu akan dilewati begitu saja (tidak dianggap error, tapi datanya tidak masuk). Gunakan nama sheet persis seperti template asli.
- **Header kolom metode di sheet bulan tidak sama persis dengan Nama Metode di Master** (mis. beda spasi/kapitalisasi signifikan) — kolom itu diabaikan diam-diam karena tidak cocok. Samakan penulisan nama metode di kedua file.
- **Kode Toko di sheet bulan tidak ada di tabel `toko`** — baris tersebut gagal dan dicatat di log; tambahkan dulu toko itu lewat Kelola Daftar Toko.

# Format Excel untuk Import Data

Aplikasi punya **dua** jalur import Excel yang independen. Menu **Import Rekap Toko Bulanan** adalah sumber data yang dipakai fitur **Proses Prediksi** — pastikan data master & rekap toko sudah diimport lewat menu ini sebelum menjalankan prediksi.

## 1. Import Rekap Toko Bulanan (dipakai fitur Prediksi)

Menu **Import Rekap Toko Bulanan** menerima dua file terpisah. Logika pembacaan ada di `MasterMetodeBayarImportService.java` dan `RekapTokoImportService.java`.

### 1.1 Master_Metode_Bayar.xlsx

Sheet bernama **MASTER METODE BAYAR**. Header di baris ke-4, data mulai baris ke-5.

| Kolom | Field | Format | Contoh |
|---|---|---|---|
| A | Kode Metode | Angka | `1` |
| B | Nama Metode | Teks (harus unik, jadi acuan pencocokan kolom di sheet rekap) | `Gopay` |
| C | Kategori | Teks bebas | `E-Wallet` |
| D | Urutan | Angka | `8` |
| E | Aktif | `TRUE` atau `FALSE` | `TRUE` |

Import file ini **lebih dulu** sebelum import Rekap Toko Bulanan — nama metode di sini dipakai untuk mencocokkan kolom pada sheet bulanan.

### 1.2 Rekap_Omzet_Per_Toko_Bulanan.xlsx

Satu workbook berisi 14 sheet:

- **DAFTAR TOKO** — daftar cabang/toko. Header baris ke-4, data mulai baris ke-5: A = Kode Toko, B = Nama Toko, C = Lokasi Toko (opsional).
- **Januari** s.d. **Desember** (12 sheet) — rekap transaksi bulan tersebut. Header baris ke-4: kolom A = Kode Toko, kolom B = Nama Toko (formula lookup, diabaikan saat import), kolom berikutnya = **jumlah transaksi per metode bayar** dengan header persis nama metode di Master Metode Bayar (mis. `Gopay`, `OVO`, `Cash`, dst). Data mulai baris ke-5.

Saat import, aplikasi meminta input **Tahun** (satu tahun berlaku untuk semua 12 sheet bulan dalam file tersebut — workbook ini didesain untuk diisi ulang setiap tahun).

Urutan proses:
1. Sheet **DAFTAR TOKO** diimport dulu → mengisi/memperbarui tabel `toko`.
2. Untuk tiap sheet bulan yang ada di workbook, kolom dicocokkan ke `metode_bayar` berdasarkan **nama header** (bukan posisi kolom) — jadi kolom boleh ditambah/diurutkan ulang selama namanya cocok dengan Master Metode Bayar.
3. Tiap baris (toko) × tiap kolom metode yang cocok diupsert ke tabel `rekap_metode_bulanan` (kunci: toko + metode + tahun + bulan) — import ulang dengan file yang sama akan menimpa (bukan menduplikasi) data.
4. Baris dengan Kode Toko yang tidak ada di sheet DAFTAR TOKO dicatat sebagai gagal (ditampilkan di log, tidak menghentikan proses baris lain).

**Catatan penting**: nilai yang direkap adalah **jumlah transaksi** (angka bulat), bukan nominal Rupiah — sesuai judul skripsi "Prediksi Transaksi". Total per toko per bulan dihitung otomatis (SUM seluruh kolom metode) saat menjalankan Regresi Linear.

## 2. Import Data Transaksi (legaci, harian per baris)

Menu terpisah **Import Data Transaksi** (lihat tombol *Unduh Template Excel* di menu tersebut) menerima file flat sederhana: kolom A = Tanggal (`yyyy-MM-dd`), B = Nominal, C = Metode Bayar (`cash`/`e_wallet`/`kartu_debit`/`kartu_kredit`), satu baris per transaksi harian. Data ini masuk ke `transaksi_harian` → `omzet_bulanan` (lewat tombol *Rekap ke Omzet Bulanan*).

**Fitur Proses Prediksi saat ini TIDAK lagi membaca `omzet_bulanan`** — sumber datanya sudah dipindah ke `rekap_metode_bulanan` (per toko, lihat bagian 1). Menu ini masih berfungsi untuk pencatatan transaksi manual/harian, tapi tidak memengaruhi hasil prediksi. Detail parsing (aturan tanggal/nominal, penanganan baris gagal) ada di `ExcelImportService.java`.

## 3. Kesalahan Umum

- **Import Rekap Toko sebelum Master Metode Bayar** — akan gagal total dengan pesan "Master Metode Bayar masih kosong". Selalu import metode bayar dulu.
- **Nama sheet bulan diubah** (mis. "Jan" bukan "Januari") — sheet itu akan dilewati begitu saja (tidak dianggap error, tapi datanya tidak masuk). Gunakan nama sheet persis seperti template asli.
- **Header kolom metode di sheet bulan tidak sama persis dengan Nama Metode di Master** (mis. beda spasi/kapitalisasi signifikan) — kolom itu diabaikan diam-diam karena tidak cocok. Samakan penulisan nama metode di kedua file.
- **Kode Toko di sheet bulan tidak ada di DAFTAR TOKO** — baris tersebut gagal dan dicatat di log; tambahkan dulu toko itu ke sheet DAFTAR TOKO.

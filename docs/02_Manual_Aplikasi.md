# Manual Aplikasi (Panduan Penggunaan)
## Aplikasi Prediksi Omzet The Play Zone

Dokumen ini menjelaskan cara memakai setiap menu aplikasi, mengikuti alur kerja operasional sistem (Login → Data Master → Rekap Transaksi → Prediksi → Laporan → Logout). Pastikan environment sudah disiapkan mengikuti `03_Manual_Setup.md` sebelum mengikuti panduan ini.

---

## 1. Login

1. Jalankan aplikasi (lihat `03_Manual_Setup.md` untuk cara menjalankan).
2. Isi **Username** dan **Password**. Sistem memvalidasi ke database dan mendistribusikan hak akses (Admin atau Staff) sebelum masuk ke Dashboard.
3. Akun bawaan (seed) dari `db/schema.sql`:

   | Username | Password | Role |
   |---|---|---|
   | `admin` | `admin123` | Admin |
   | `staff` | `staff123` | Staff |

4. Klik **Login**. Jika berhasil, jendela **Dashboard** akan terbuka sesuai peran akun.

> Ganti password default ini sebelum digunakan secara produksi — lihat bagian *Keamanan* di `03_Manual_Setup.md`.

## 2. Dashboard & Peran Pengguna

**Admin bukan Kepala Divisi.** Kepala Divisi adalah pihak eksternal yang menerima laporan PDF (lihat bagian 6), bukan pengguna sistem.

- **Admin** — superuser yang menyiapkan akses & data dasar: **Kelola Data Pengguna**, **Kelola Daftar Toko**, **Kelola Master Metode Bayar**, plus semua menu yang bisa diakses Staff.
- **Staff** — operasional harian: **Kelola Rekap Transaksi Toko**, **Proses & Lihat Prediksi**, **Riwayat / Laporan Prediksi**, **Visualisasi Grafik Prediksi Omzet**, **Laporan Gabungan**.

Tombol **Logout** di pojok kanan atas menutup sesi dan kembali ke layar Login.

## 3. Kelola Data Pengguna (khusus Admin)

Admin bertindak sebagai superuser yang menyiapkan akses operator, sehingga operasional sistem dapat didelegasikan dengan aman.

1. Buka menu **Kelola Data Pengguna** — tabel menampilkan seluruh akun (Admin & Staff).
2. **Tambah akun Staff**: isi Username, Password, Nama Lengkap, klik **Tambah Akun Staff**. Akun baru otomatis berperan Staff (menu ini tidak membuat akun Admin lain).
3. **Edit akun**: klik baris di tabel — form terisi otomatis, tombol berubah jadi **Simpan Perubahan**. Ubah Nama Lengkap dan/atau isi Password baru (kosongkan Password jika tidak ingin menggantinya), lalu klik **Simpan Perubahan**. Klik **Form Baru** untuk batal edit.
4. **Hapus akun**: pilih baris, klik **Hapus Terpilih**. Akun yang sedang login tidak bisa menghapus dirinya sendiri. Akun yang sudah pernah memproses/menyimpan prediksi atau import tidak bisa dihapus (dilindungi relasi data).
5. Klik **Refresh** untuk memuat ulang data terbaru.

## 4. Kelola Daftar Toko (khusus Admin)

Data master cabang/toko — variabel esensial untuk pelaporan dan pemisahan target prediksi per toko.

1. Buka menu **Kelola Daftar Toko** — tabel menampilkan seluruh toko tersimpan.
2. **Tambah/Edit manual**: isi Kode Toko, Nama Toko, Lokasi, klik **Simpan (Tambah/Update)**. Kode Toko yang sudah ada akan memperbarui data toko tsb (bukan duplikat) — klik baris di tabel untuk memuat datanya ke form lebih dulu.
3. **Import massal**: klik **Pilih File Excel**, pilih `Rekap_Omzet_Per_Toko_Bulanan.xlsx` (hanya sheet DAFTAR TOKO yang dibaca), klik **Import Excel**.
4. **Hapus**: pilih baris, klik **Hapus Terpilih**. Toko yang sudah punya data rekap/prediksi tidak bisa dihapus.
5. **Laporan Daftar Toko**: klik **Export PDF** untuk mengunduh daftar seluruh toko sebagai PDF siap cetak.

## 5. Kelola Master Metode Bayar (khusus Admin)

Data jenis pembayaran yang sah (Gopay, OVO, Kartu Debit, Cash, dll), dipakai untuk mengkategorikan rekap transaksi berdasarkan metodenya.

1. Buka menu **Kelola Master Metode Bayar** — tabel menampilkan seluruh metode tersimpan.
2. **Tambah/Edit manual**: isi Kode, Nama Metode, Kategori, Urutan, centang Aktif, klik **Simpan (Tambah/Update)**. Nama Metode yang sudah ada akan memperbarui data metode tsb — klik baris di tabel untuk memuat datanya ke form lebih dulu.
3. **Import massal**: klik **Pilih Master_Metode_Bayar.xlsx**, klik **Import Excel**.
4. **Hapus**: pilih baris, klik **Hapus Terpilih**. Metode yang sudah punya data rekap tidak bisa dihapus.
5. **Laporan Master Metode Bayar**: klik **Export PDF** untuk mengunduh daftar seluruh metode sebagai PDF siap cetak.

> Lakukan langkah ini **sebelum** mengisi Kelola Rekap Transaksi Toko — nama metode di sini dipakai untuk mencocokkan kolom pada file rekap Excel.

## 6. Kelola Rekap Transaksi Toko (Admin & Staff)

Di sinilah sistem mulai menyerap data transaksional — sumber data yang dipakai algoritma Regresi Linear di menu Prediksi. Detail lengkap format file Excel ada di `05_Format_Excel_Import.md`.

**Cara Kerja (import massal)**:
1. Pilih file `Rekap_Omzet_Per_Toko_Bulanan.xlsx`, isi **Tahun data** (satu tahun berlaku untuk seluruh 12 sheet bulan di file tersebut), klik **Import Excel (12 sheet bulan)**.
2. Sistem membaca tiap sheet bulan (Januari–Desember yang ada di file), mencocokkan kolom ke Master Metode Bayar, lalu **mengagregasi** jumlah transaksi per toko per metode per bulan ke database. Import ulang dengan file yang sama aman dilakukan — data lama ditimpa, bukan diduplikasi.
3. Kode Toko yang belum terdaftar di Kelola Daftar Toko akan gagal (dicatat di log) — lengkapi Daftar Toko dahulu.

**Cara Kerja (input/edit manual satu baris)**:
1. Pilih Toko, Metode, Tahun, Bulan, isi Jumlah Transaksi, klik **Simpan (Tambah/Update)**. Kombinasi Toko+Metode+Tahun+Bulan yang sudah ada akan diperbarui — klik baris di tabel untuk memuat datanya ke form lebih dulu.
2. **Hapus**: pilih baris, klik **Hapus Terpilih**.

**Melihat data & Laporan**: tabel utama di menu ini menampilkan seluruh rekap tersimpan, dengan filter **Toko** dan **Tahun** (atau centang "Semua Tahun") + tombol **Tampilkan**. Klik **Export PDF** untuk mengunduh data yang sedang tampil (sesuai filter aktif) sebagai laporan PDF.

## 7. Proses & Lihat Prediksi

Fase inti/otak dari sistem — sangat bergantung pada selesainya pengisian data di bagian 4–6. Bisa diakses Admin maupun Staff.

1. Pilih **Toko** (toko tertentu, atau "Semua Toko" untuk agregat seluruh cabang sekaligus), **Bulan Target**, dan **Tahun Target**.
2. Klik **Proses Prediksi**. Sistem menarik data "Total Transaksi Bulanan" historis (hasil bagian 6) untuk bulan yang sama di tahun-tahun sebelumnya (Year-over-Year), lalu menjalankan persamaan Regresi Linear untuk menghitung:
   - Konstanta (`a`) dan koefisien (`b`)
   - Persamaan garis tren `Y = a + bX`
   - Nilai prediksi jumlah transaksi
   - Tingkat error (**MAPE**)
3. Hasil angka tampil di panel atas; grafik tren (data aktual, garis regresi, titik prediksi) tampil di panel bawah.
4. Klik **Simpan Hasil Prediksi** untuk mengarsipkannya ke riwayat.
5. Klik **Export PDF** untuk mengunduh laporan siap cetak (kop logo The Play Zone, toko target, rincian angka, grafik tren, dan area tanda tangan "Dibuat oleh, Staff" / "Mengetahui, Kepala Divisi The Play Zone").

**Catatan**: minimal dibutuhkan 2 tahun data historis untuk bulan & toko yang sama sebelum tahun target. Jika data belum cukup, sistem menampilkan peringatan — lengkapi data lewat menu **Kelola Rekap Transaksi Toko** terlebih dahulu.

## 8. Riwayat / Laporan Prediksi & Penyerahan ke Kepala Divisi

1. Buka menu **Riwayat / Laporan Prediksi**.
2. Tabel menampilkan seluruh hasil prediksi yang pernah disimpan: waktu diproses, toko, periode target, jumlah data (n), nilai `a`/`b`, hasil prediksi, MAPE, dan siapa yang memprosesnya. Gunakan filter **Toko** dan **Tahun Target** + tombol **Tampilkan** untuk mempersempit daftar.
3. **Hapus**: pilih baris, klik **Hapus Terpilih** (ada konfirmasi) untuk membuang hasil prediksi yang salah/tidak relevan.
4. **Laporan Riwayat**: klik **Export PDF** untuk mengunduh tabel yang sedang tampil (sesuai filter aktif) sebagai PDF.
5. Klik **Refresh** untuk memuat data terbaru.

Dipakai Admin/Staff untuk meninjau riwayat prediksi sebelum dicetak (**Export PDF** di menu Prediksi, bagian 7, atau laporan tabel di bagian ini). Laporan PDF/cetak fisik kemudian diserahkan kepada **Kepala Divisi** — pihak eksternal (bukan pengguna sistem) — untuk kebutuhan pengambilan keputusan bisnis.

## 9. Visualisasi Grafik Prediksi Omzet

Menu ini menampilkan ulang grafik tren dari hasil prediksi yang sudah tersimpan di Riwayat — berguna untuk meninjau kembali sebuah hasil prediksi tanpa harus menjalankan ulang secara manual di menu Prediksi.

1. Buka menu **Visualisasi Grafik Prediksi Omzet** — tabel menampilkan seluruh riwayat prediksi tersimpan.
2. Klik salah satu baris — grafik trennya akan dibangun ulang (dihitung ulang dari data rekap saat ini, menggunakan toko/bulan/tahun yang tersimpan) dan ditampilkan di panel bawah.
3. Klik **Export PDF** untuk mengunduh grafik + rincian hasil tersebut sebagai laporan PDF (format sama seperti Export PDF di menu Prediksi).

> Jika data rekap historis untuk toko/bulan tersebut sudah berubah sejak prediksi ini disimpan (mis. dihapus/direvisi di Kelola Rekap Transaksi Toko), grafik mungkin tidak bisa dibangun ulang — sistem akan menampilkan peringatan.

## 10. Laporan Gabungan

Satu file PDF berisi 4 bagian sekaligus: Daftar Toko, Master Metode Bayar, Rekap Transaksi Toko, dan Riwayat Hasil Prediksi.

1. Buka menu **Laporan Gabungan**.
2. Pilih filter **Toko** dan **Tahun** (atau centang "Semua Tahun") — filter ini hanya berlaku untuk bagian Rekap Transaksi dan Riwayat Prediksi; bagian Daftar Toko dan Master Metode Bayar selalu ditampilkan lengkap.
3. Klik **Export Laporan Gabungan (PDF)** → pilih lokasi simpan.

## 11. Logout

Setelah pekerjaan selesai, klik **Logout** di pojok kanan atas Dashboard. Jendela Dashboard tertutup dan kembali ke layar Login.

---

## Alur Kerja yang Disarankan (End-to-End)

```
1. Login (Admin) -> Kelola Data Pengguna: buat akun Staff
2. Login (Admin) -> Kelola Daftar Toko & Kelola Master Metode Bayar: siapkan data master
3. Login (Staff) -> Kelola Rekap Transaksi Toko: import/isi rekap bulanan per toko
4. Login (Staff) -> Proses & Lihat Prediksi: pilih Toko + bulan/tahun target -> Simpan Hasil Prediksi
5. Login (Staff) -> Export PDF -> cetak & serahkan ke Kepala Divisi untuk ditandatangani
6. Riwayat / Laporan Prediksi, Visualisasi Grafik, atau Laporan Gabungan -> tinjau sebelum periode berikutnya -> Logout
```

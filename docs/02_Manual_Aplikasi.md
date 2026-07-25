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
   | `admin` | `admin123` | Kepala Divisi |
   | `operasional` | `opr123` | Staf Operasional Pusat |

4. Klik **Login**. Jika berhasil, jendela **Dashboard** akan terbuka sesuai peran akun.

> Ganti password default ini sebelum digunakan secara produksi — lihat bagian *Keamanan* di `03_Manual_Setup.md`.

## 2. Dashboard

Menu yang tampil menyesuaikan peran:

- **Staf Operasional**: Import Data Transaksi, Kelola Data Transaksi, Proses & Lihat Prediksi Omzet, Riwayat/Laporan Prediksi.
- **Kepala Divisi**: Proses & Lihat Prediksi Omzet (mode lihat saja, tanpa tombol simpan), Riwayat/Laporan Prediksi.

Tombol **Logout** di pojok kanan atas kembali ke layar Login.

## 3. Import Data Transaksi (Excel)

Digunakan Staf Operasional untuk memasukkan arsip transaksi harian dari file spreadsheet accounting.

1. Buka menu **Import Data Transaksi (Excel)**.
2. Klik **Pilih File Excel (.xlsx)**, pilih file sumber.
3. Format kolom file Excel (baris 1 = header, data mulai baris 2):

   | Kolom | Isi | Contoh |
   |---|---|---|
   | A | Tanggal | 2025-01-15 |
   | B | Nominal | 1500000 |
   | C | Metode Bayar *(opsional)* | cash / e_wallet / kartu_debit / kartu_kredit |

4. Klik **Import ke Database** — hasil (jumlah baris berhasil/gagal) tampil di area log.
5. Klik **Rekap ke Omzet Bulanan** — mengagregasi seluruh transaksi harian menjadi total omzet per bulan (dipakai algoritma prediksi). **Wajib dijalankan setiap kali ada data baru** sebelum memproses prediksi.

## 4. Kelola Data Transaksi

Alternatif input manual (satu per satu) tanpa file Excel, atau untuk mengoreksi/menghapus data.

1. Buka menu **Kelola Data Transaksi** — tabel menampilkan seluruh transaksi harian tersimpan.
2. **Tambah data**: isi Tanggal (`yyyy-MM-dd`), Nominal, pilih Metode, klik **Tambah**.
3. **Hapus data**: pilih baris pada tabel, klik **Hapus Terpilih**.
4. Klik **Refresh** untuk memuat ulang data terbaru.

> Setelah menambah/menghapus data di sini, jalankan kembali **Rekap ke Omzet Bulanan** dari menu Import agar prediksi memakai data terbaru.

## 5. Proses & Lihat Prediksi Omzet

Menu inti — menjalankan algoritma Regresi Linear.

1. Pilih **Bulan Target** dan **Tahun Target** yang ingin diprediksi.
2. Klik **Proses Prediksi**. Sistem mengambil omzet bulan yang sama dari tahun-tahun sebelumnya (Year-over-Year), lalu menghitung:
   - Konstanta (`a`) dan koefisien (`b`)
   - Persamaan garis tren `Y = a + bX`
   - Nilai prediksi omzet
   - Tingkat error (**MAPE**)
3. Hasil angka tampil di panel atas; grafik tren (data aktual, garis regresi, titik prediksi) tampil di panel bawah.
4. *(Khusus Staf Operasional)* klik **Simpan Hasil Prediksi** untuk mencatatnya ke riwayat.

**Catatan**: minimal dibutuhkan 2 tahun data historis untuk bulan yang sama sebelum tahun target. Jika data belum cukup, sistem menampilkan peringatan — lengkapi data lewat menu Import/Kelola Data Transaksi terlebih dahulu.

## 6. Riwayat / Laporan Prediksi

1. Buka menu **Riwayat / Laporan Prediksi**.
2. Tabel menampilkan seluruh hasil prediksi yang pernah disimpan: waktu diproses, periode target, jumlah data (n), nilai `a`/`b`, hasil prediksi, MAPE, dan siapa yang memprosesnya.
3. Klik **Refresh** untuk memuat data terbaru.

Digunakan Kepala Divisi untuk meninjau riwayat rekomendasi sebagai bahan evaluasi dan pengambilan keputusan target operasional.

---

## Alur Kerja yang Disarankan (End-to-End)

```
Login (Staf Operasional)
   -> Import Data Transaksi (Excel) atau input manual di Kelola Data Transaksi
   -> Rekap ke Omzet Bulanan
   -> Proses & Lihat Prediksi Omzet (pilih bulan/tahun target)
   -> Simpan Hasil Prediksi
   -> (Kepala Divisi login) -> Riwayat / Laporan Prediksi -> tinjau & putuskan target
```

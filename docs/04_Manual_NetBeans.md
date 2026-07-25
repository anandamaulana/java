# Manual Editor NetBeans

Panduan khusus membuka, menjalankan, dan men-debug proyek ini di **Apache NetBeans**, sesuai batasan masalah pada skripsi (IDE = NetBeans).

---

## 1. Instal NetBeans

1. Pastikan **JDK 17+** sudah terinstal terlebih dahulu (lihat `03_Manual_Setup.md` bagian Prasyarat).
2. Unduh **Apache NetBeans** versi 17 ke atas (mendukung Java 17) dari [netbeans.apache.org](https://netbeans.apache.org/) — pilih paket **Java SE** atau **Java EE** (yang sudah termasuk dukungan Maven bawaan).
3. Instal seperti aplikasi Windows biasa (Next-next-finish).

## 2. Membuka Proyek

1. Buka NetBeans → **File > Open Project...**
2. Arahkan ke folder root proyek ini (folder yang berisi `pom.xml`) → pilih folder tersebut → **Open Project**.
3. NetBeans otomatis mengenali ini sebagai **Maven Project** (ikon proyek bergambar kubus Maven), bukan perlu dibuat manual lewat *New Project*.
4. Klik kanan proyek di panel **Projects** → **Clean and Build**. Pada proses ini NetBeans/Maven akan **mengunduh dependency** (`mysql-connector-j`, Apache POI, JFreeChart) dari internet — pastikan komputer terkoneksi internet saat langkah ini, hanya perlu sekali (tersimpan di local Maven repository `~/.m2`).

> Jika muncul banyak garis merah "cannot find symbol" saat pertama kali dibuka, itu normal — akan hilang setelah proses **Clean and Build** selesai mengunduh dependency.

## 3. Menyesuaikan JDK Platform (jika perlu)

Jika NetBeans memakai JDK versi lain sebagai default:

1. Klik kanan proyek → **Properties** → kategori **Sources**.
2. Pastikan **Source/Binary Format** = `17` (atau lebih tinggi).
3. Kategori **Libraries** (Maven) → pastikan platform Java yang dipakai untuk kompilasi mengarah ke JDK 17+. Jika belum ada, tambahkan lewat **Tools > Java Platforms > Add Platform...**.

## 4. Menyiapkan Database Sebelum Run

Jalankan MySQL terlebih dahulu (XAMPP **atau** Docker — lihat `03_Manual_Setup.md`). Aplikasi akan gagal konek saat Login kalau database belum aktif.

## 5. Menjalankan Aplikasi (Run)

1. Klik kanan proyek → **Run** (atau tombol ▶ / tekan **F6**).
2. `pom.xml` proyek ini sudah dikonfigurasi dengan property `exec.mainClass` mengarah ke `com.theplayzone.prediksi.Main`, jadi NetBeans **langsung tahu** kelas mana yang harus dijalankan — tidak perlu memilih Main Class secara manual.
3. Jendela **Login** aplikasi akan muncul dalam beberapa detik.

Jika NetBeans tetap menampilkan dialog "Select Main Class": pilih `com.theplayzone.prediksi.Main` lalu centang **Remember permanently**.

## 6. Debugging

- Buka file kelas yang ingin diperiksa (misalnya `RegresiLinearService.java`), klik di margin kiri nomor baris untuk memasang **breakpoint**.
- Klik kanan proyek → **Debug** (atau **Ctrl+F5**). Eksekusi akan berhenti saat mencapai breakpoint, gunakan panel **Variables**/**Call Stack** di NetBeans untuk memeriksa nilai (misalnya nilai `a`, `b`, `sumX`, `sumY` saat proses regresi).

## 7. Catatan Penting Soal Form (UI)

Seluruh form (`LoginForm`, `MainDashboard`, `ImportDataForm`, `KelolaTransaksiForm`, `PrediksiForm`, `LaporanForm` di folder `ui/`) ditulis sebagai **kode Swing biasa** (bukan file `.form` hasil GUI Builder/Matisse bawaan NetBeans).

- Saat file-file ini dibuka, tab **Design** / **Source** khas GUI Builder NetBeans **tidak akan muncul** (karena tidak ada file `.form` pasangannya) — ini bukan error. Edit tampilan langsung lewat kode Java di tab **Source** seperti biasa.
- Alasan desain ini: kode Swing manual lebih portabel (bisa dibuka & dikompilasi di IDE apa pun, termasuk `mvn` tanpa NetBeans), tidak terikat file `.form` milik NetBeans.
- Jika ke depannya ingin memakai **GUI Builder NetBeans** (drag-drop komponen), bisa dibuat form `.form` baru secara terpisah dan isi logikanya memanggil kelas-kelas `service`/`dao` yang sudah ada — tidak perlu menulis ulang logika bisnisnya.

## 8. Build Jar Siap Distribusi (dari NetBeans)

Klik kanan proyek → **Clean and Build** akan otomatis menjalankan seluruh lifecycle Maven termasuk `maven-shade-plugin`, menghasilkan **fat jar** di folder `target/prediksi-theplayzone.jar` — file inilah yang dikirim/dipindahkan ke laptop client untuk dijalankan lewat `java -jar prediksi-theplayzone.jar` (lihat `03_Manual_Setup.md` Opsi B).

## 9. Troubleshooting Khusus NetBeans

| Gejala | Solusi |
|---|---|
| Proyek muncul dengan tanda seru merah (!) setelah dibuka | Klik kanan proyek → **Reload POM**, lalu **Clean and Build** ulang |
| "cannot find symbol" pada class dari POI/JFreeChart/MySQL Connector | Dependency belum terunduh — sambungkan internet lalu **Clean and Build** |
| Tombol Run tidak aktif / abu-abu | Tunggu proses indexing NetBeans selesai (lihat progress bar di kanan bawah) |
| Error `Access denied for user 'root'` saat Login | Sesuaikan `src/main/resources/db.properties` dengan kredensial MySQL yang aktif (XAMPP default kosong, Docker compose di proyek ini juga sengaja dikosongkan) |

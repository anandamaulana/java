package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.Toko;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Import rekap transaksi bulanan dari grid "Rekap_Transaksi_Toko" -- baris = toko (kolom A = Kode Toko,
 * kolom B = Nama Toko, diabaikan), kolom = metode bayar (header baris pertama, mis. Gopay/OVO/Dana/Cash),
 * nilai sel = nominal omzet Rupiah toko itu lewat metode itu bulan itu. Toko (via Kode Toko) dan Metode
 * Bayar (via nama kolom) harus SUDAH ADA -- lengkapi lewat menu Kelola Daftar Toko / Kelola Master Metode
 * Bayar dahulu.
 *
 * Dua varian file:
 *  - importTahunan: Rekap_Transaksi_Toko_&lt;Tahun&gt;.xlsx, 12 sheet (satu per bulan, nama sheet memuat
 *    nama bulan mis. "Januari 2023" / "September_2023" -- dicocokkan dengan "startsWith" nama bulan agar
 *    tahan terhadap variasi spasi/underscore/tahun pada nama sheet).
 *  - importBulanan: Rekap_Transaksi_Toko_&lt;Bulan&gt;_&lt;Tahun&gt;.xlsx, 1 sheet, bulan &amp; tahun target
 *    dipilih manual di form (nama/isi sheet tidak perlu cocok apa pun).
 *
 * Nilai omzet DIAKUMULASI (ditambahkan ke yang sudah tercatat, bukan ditimpa) mengikuti istilah
 * "mengakumulasi total pendapatan (omzet) bulanan" pada skripsi Bab IV Tabel 4.8. Baris dengan kode toko
 * yang tidak dikenal dicatat gagal tanpa menghentikan baris lain, tapi jika koneksi database terputus di
 * tengah proses, seluruh perubahan pada import ini di-rollback.
 */
public class RekapTokoImportService {

    private static final int BARIS_HEADER = 0; // 0-based, baris pertama (header: Kode_Toko, Nama_Toko, <metode...>)
    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();

    public static class HasilImport {
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    /** Import file tahunan (12 sheet bulan sekaligus). */
    public HasilImport importTahunan(File file, int tahun, int idUser) throws Exception {
        int idImport = buatLogImport(file.getName(), idUser);
        HasilImport hasil = new HasilImport();
        Map<String, Integer> metodeByNama = muatMetodeByNama();
        Map<String, Integer> tokoByKode = muatTokoByKode();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis);
             Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (int bulan = 1; bulan <= 12; bulan++) {
                    Sheet sheet = cariSheetBulan(workbook, bulan);
                    if (sheet == null) {
                        continue;
                    }
                    importSheetGrid(conn, sheet, bulan, tahun, tokoByKode, metodeByNama, idImport, hasil);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                updateLogImport(idImport, 0, "gagal");
                throw new Exception("Import dibatalkan (rollback) karena kesalahan koneksi/database: " + ex.getMessage(), ex);
            }
        }

        updateLogImport(idImport, hasil.jumlahBaris, hasil.jumlahBaris > 0 ? "sukses" : "gagal");
        return hasil;
    }

    /** Import file satu bulan (1 sheet, bulan/tahun target ditentukan manual di form). */
    public HasilImport importBulanan(File file, int bulan, int tahun, int idUser) throws Exception {
        int idImport = buatLogImport(file.getName(), idUser);
        HasilImport hasil = new HasilImport();
        Map<String, Integer> metodeByNama = muatMetodeByNama();
        Map<String, Integer> tokoByKode = muatTokoByKode();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis);
             Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null) {
                    throw new IllegalStateException("File Excel tidak memiliki sheet.");
                }
                importSheetGrid(conn, sheet, bulan, tahun, tokoByKode, metodeByNama, idImport, hasil);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                updateLogImport(idImport, 0, "gagal");
                throw new Exception("Import dibatalkan (rollback) karena kesalahan koneksi/database: " + ex.getMessage(), ex);
            }
        }

        updateLogImport(idImport, hasil.jumlahBaris, hasil.jumlahBaris > 0 ? "sukses" : "gagal");
        return hasil;
    }

    private Map<String, Integer> muatMetodeByNama() throws SQLException {
        Map<String, Integer> metodeByNama = new HashMap<>();
        for (MetodeBayar m : metodeBayarDAO.findAll()) {
            metodeByNama.put(m.getNamaMetode().trim().toLowerCase(), m.getIdMetode());
        }
        if (metodeByNama.isEmpty()) {
            throw new IllegalStateException("Master Metode Bayar masih kosong. Import lewat menu Kelola Master Metode Bayar dahulu.");
        }
        return metodeByNama;
    }

    private Map<String, Integer> muatTokoByKode() throws SQLException {
        Map<String, Integer> tokoByKode = new HashMap<>();
        for (Toko t : tokoDAO.findAll()) {
            tokoByKode.put(t.getKodeToko(), t.getIdToko());
        }
        if (tokoByKode.isEmpty()) {
            throw new IllegalStateException("Daftar Toko masih kosong. Import lewat menu Kelola Daftar Toko dahulu.");
        }
        return tokoByKode;
    }

    /** Cari sheet yang namanya diawali nama bulan (case-insensitive), tahan variasi "Januari 2023" / "Januari_2023". */
    private Sheet cariSheetBulan(Workbook workbook, int bulan) {
        String target = NAMA_BULAN[bulan - 1].toLowerCase();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().trim().toLowerCase().startsWith(target)) {
                return sheet;
            }
        }
        return null;
    }

    private void importSheetGrid(Connection conn, Sheet sheet, int bulan, int tahun, Map<String, Integer> tokoByKode,
                                  Map<String, Integer> metodeByNama, int idImport, HasilImport hasil) throws SQLException {
        Row headerRow = sheet.getRow(BARIS_HEADER);
        if (headerRow == null) {
            return;
        }
        Map<Integer, Integer> kolomKeMetode = new HashMap<>();
        for (int col = 2; col < headerRow.getLastCellNum(); col++) {
            String namaHeader = teksSel(headerRow.getCell(col)).trim().toLowerCase();
            Integer idMetode = metodeByNama.get(namaHeader);
            if (idMetode != null) {
                kolomKeMetode.put(col, idMetode);
            }
        }

        for (int i = BARIS_HEADER + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell kodeCell = row == null ? null : row.getCell(0);
            if (kodeCell == null || kodeCell.toString().trim().isEmpty()) {
                continue;
            }
            String kodeToko = kodeCell.toString().trim();
            Integer idToko = tokoByKode.get(kodeToko);
            if (idToko == null) {
                hasil.jumlahGagal++;
                hasil.pesanGagal.add(NAMA_BULAN[bulan - 1] + " baris " + (row.getRowNum() + 1) +
                        ": kode toko '" + kodeToko + "' belum terdaftar di Kelola Daftar Toko");
                continue;
            }
            // Kegagalan dari titik ini (mis. koneksi terputus) dianggap fatal -> rollback seluruh import.
            for (Map.Entry<Integer, Integer> entry : kolomKeMetode.entrySet()) {
                BigDecimal nominal = nominalSel(row.getCell(entry.getKey()));
                if (nominal.signum() == 0) {
                    continue;
                }
                rekapMetodeDAO.tambahOmzet(conn, idToko, entry.getValue(), tahun, bulan, nominal, idImport);
            }
            hasil.jumlahBaris++;
        }
    }

    private String teksSel(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }

    private BigDecimal nominalSel(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String raw = cell.toString().trim();
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private int buatLogImport(String namaFile, int idUser) throws SQLException {
        String sql = "INSERT INTO import_log (nama_file, jumlah_baris, status, id_user) VALUES (?, 0, 'gagal', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, namaFile);
            ps.setInt(2, idUser);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updateLogImport(int idImport, int jumlahBaris, String status) throws SQLException {
        String sql = "UPDATE import_log SET jumlah_baris = ?, status = ? WHERE id_import = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlahBaris);
            ps.setString(2, status);
            ps.setInt(3, idImport);
            ps.executeUpdate();
        }
    }
}

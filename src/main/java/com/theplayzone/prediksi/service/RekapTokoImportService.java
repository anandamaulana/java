package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.MetodeBayar;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
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
 * Import daftar toko + rekap transaksi bulanan dari file Rekap_Omzet_Per_Toko_Bulanan.xlsx.
 * Struktur workbook:
 *   - sheet "DAFTAR TOKO": header baris 4, data mulai baris 5 (A=Kode Toko, B=Nama Toko, C=Lokasi Toko)
 *   - sheet per bulan ("Januari".."Desember"): header baris 4 (nama metode bayar per kolom mulai C),
 *     data mulai baris 5 (A=Kode Toko, kolom metode = jumlah transaksi bulan itu)
 * Metode bayar dicocokkan berdasarkan NAMA (harus sudah ada di tabel metode_bayar -- import Master Metode Bayar dulu).
 */
public class RekapTokoImportService {

    private static final int BARIS_HEADER = 3; // 0-based, baris ke-4
    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();

    public static class HasilImport {
        public int jumlahToko;
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    public HasilImport importFile(File file, int tahun, int idUser) throws Exception {
        int idImport = buatLogImport(file.getName(), idUser);
        HasilImport hasil = new HasilImport();

        Map<String, Integer> metodeByNama = new HashMap<>();
        for (MetodeBayar m : metodeBayarDAO.findAll()) {
            metodeByNama.put(m.getNamaMetode().trim().toLowerCase(), m.getIdMetode());
        }
        if (metodeByNama.isEmpty()) {
            throw new IllegalStateException("Master Metode Bayar masih kosong. Import Master Metode Bayar terlebih dahulu.");
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Map<String, Integer> tokoByKode = importDaftarToko(workbook, hasil);

            for (int bulan = 1; bulan <= 12; bulan++) {
                Sheet sheet = workbook.getSheet(NAMA_BULAN[bulan - 1]);
                if (sheet == null) {
                    continue;
                }
                importSheetBulan(sheet, bulan, tahun, tokoByKode, metodeByNama, idImport, hasil);
            }
        }

        updateLogImport(idImport, hasil.jumlahBaris, hasil.jumlahBaris > 0 ? "sukses" : "gagal");
        return hasil;
    }

    private Map<String, Integer> importDaftarToko(Workbook workbook, HasilImport hasil) throws SQLException {
        Map<String, Integer> tokoByKode = new HashMap<>();
        Sheet sheet = workbook.getSheet("DAFTAR TOKO");
        if (sheet == null) {
            throw new IllegalStateException("Sheet 'DAFTAR TOKO' tidak ditemukan di file.");
        }
        for (int i = BARIS_HEADER + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell kodeCell = row == null ? null : row.getCell(0);
            if (kodeCell == null || kodeCell.toString().trim().isEmpty()) {
                continue;
            }
            try {
                String kodeToko = kodeCell.toString().trim();
                String namaToko = teksSel(row.getCell(1));
                String lokasiToko = teksSel(row.getCell(2));
                if (namaToko.isEmpty()) {
                    throw new IllegalArgumentException("Nama Toko kosong");
                }
                int idToko = tokoDAO.upsert(kodeToko, namaToko, lokasiToko.isEmpty() ? null : lokasiToko);
                tokoByKode.put(kodeToko, idToko);
                hasil.jumlahToko++;
            } catch (Exception ex) {
                hasil.jumlahGagal++;
                hasil.pesanGagal.add("DAFTAR TOKO baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
            }
        }
        return tokoByKode;
    }

    private void importSheetBulan(Sheet sheet, int bulan, int tahun, Map<String, Integer> tokoByKode,
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
                        ": kode toko '" + kodeToko + "' tidak ada di sheet DAFTAR TOKO");
                continue;
            }
            try {
                for (Map.Entry<Integer, Integer> entry : kolomKeMetode.entrySet()) {
                    int jumlah = jumlahSel(row.getCell(entry.getKey()));
                    rekapMetodeDAO.upsertJumlah(idToko, entry.getValue(), tahun, bulan, jumlah, idImport);
                }
                hasil.jumlahBaris++;
            } catch (Exception ex) {
                hasil.jumlahGagal++;
                hasil.pesanGagal.add(NAMA_BULAN[bulan - 1] + " baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
            }
        }
    }

    private String teksSel(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }

    private int jumlahSel(Cell cell) {
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String raw = cell.toString().trim();
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return 0;
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

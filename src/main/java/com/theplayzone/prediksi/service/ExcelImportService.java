package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.TransaksiDAO;
import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.TransaksiHarian;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Import data transaksi harian dari file Excel (.xlsx).
 * Format kolom yang diharapkan (baris 1 = header, data mulai baris 2):
 *   A = Tanggal, B = Nominal, C = Metode Bayar (opsional: cash/e_wallet/kartu_debit/kartu_kredit)
 */
public class ExcelImportService {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    public static class HasilImport {
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    public HasilImport importFile(File file, int idUser) throws Exception {
        int idImport = buatLogImport(file.getName(), idUser);
        HasilImport hasil = new HasilImport();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    TransaksiHarian t = parseRow(row);
                    t.setIdImport(idImport);
                    transaksiDAO.insertTransaksi(t);
                    hasil.jumlahBaris++;
                } catch (Exception ex) {
                    hasil.jumlahGagal++;
                    hasil.pesanGagal.add("Baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                }
            }
        }

        updateLogImport(idImport, hasil.jumlahBaris, hasil.jumlahBaris > 0 ? "sukses" : "gagal");
        return hasil;
    }

    private TransaksiHarian parseRow(Row row) {
        Cell tanggalCell = row.getCell(0);
        Cell nominalCell = row.getCell(1);
        Cell metodeCell = row.getCell(2);

        if (tanggalCell == null || nominalCell == null) {
            throw new IllegalArgumentException("Kolom tanggal/nominal kosong pada baris " + (row.getRowNum() + 1));
        }

        LocalDate tanggal = parseTanggal(tanggalCell);
        double nominalD = parseNominal(nominalCell);

        String metode = "cash";
        if (metodeCell != null) {
            String raw = metodeCell.getStringCellValue().trim().toLowerCase().replace(" ", "_");
            if (raw.equals("cash") || raw.equals("e_wallet") || raw.equals("kartu_debit") || raw.equals("kartu_kredit")) {
                metode = raw;
            }
        }

        TransaksiHarian t = new TransaksiHarian();
        t.setTanggal(tanggal);
        t.setNominal(BigDecimal.valueOf(nominalD));
        t.setMetodeBayar(metode);
        return t;
    }

    private LocalDate parseTanggal(Cell cell) {
        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
            // Sel numeric di kolom tanggal hampir pasti angka serial tanggal Excel,
            // walau formatnya tidak terdeteksi isCellDateFormatted() (mis. hasil paste-value).
            return DateUtil.getLocalDateTime(cell.getNumericCellValue()).toLocalDate();
        }
        if (type == CellType.STRING) {
            String raw = cell.getStringCellValue().trim();
            try {
                return LocalDate.parse(raw); // format ISO: yyyy-MM-dd
            } catch (Exception ex) {
                throw new IllegalArgumentException("Format tanggal '" + raw + "' harus yyyy-MM-dd (mis. 2025-01-15)");
            }
        }
        throw new IllegalArgumentException("Kolom tanggal kosong/tidak dikenali");
    }

    private double parseNominal(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            String raw = cell.getStringCellValue().trim();
            String cleaned = raw.replaceAll("[^0-9,.\\-]", ""); // buang "Rp", spasi, dsb.
            if (cleaned.contains(",")) {
                // Format Indonesia: "." pemisah ribuan, "," pemisah desimal -> 1.500.000,50
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                // Tanpa koma: anggap semua "." adalah pemisah ribuan -> 1.500.000
                cleaned = cleaned.replace(".", "");
            }
            if (cleaned.isEmpty()) {
                throw new IllegalArgumentException("Nominal '" + raw + "' bukan angka yang valid");
            }
            return Double.parseDouble(cleaned);
        }
        return cell.getNumericCellValue();
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

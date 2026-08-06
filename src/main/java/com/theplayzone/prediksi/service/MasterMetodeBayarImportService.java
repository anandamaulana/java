package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Import data master metode bayar dari file Master_Metode_Bayar.xlsx.
 * Format: sheet "MASTER METODE BAYAR", header di baris 4, data mulai baris 5:
 *   A = Kode Metode, B = Nama Metode, C = Kategori, D = Urutan, E = Aktif (TRUE/FALSE)
 *
 * Seluruh baris diproses dalam SATU transaksi database: baris dengan data tidak valid dicatat
 * sebagai gagal tanpa menghentikan baris lain, tapi jika koneksi database terputus di tengah
 * proses, seluruh perubahan pada import ini di-rollback.
 */
public class MasterMetodeBayarImportService {

    private static final int BARIS_HEADER = 3; // 0-based, baris ke-4
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();

    public static class HasilImport {
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    private static class BarisMetode {
        int kodeMetode;
        String namaMetode;
        String kategori;
        int urutan;
        boolean aktif;
    }

    public HasilImport importFile(File file) throws Exception {
        HasilImport hasil = new HasilImport();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis);
             Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Sheet sheet = workbook.getSheet("MASTER METODE BAYAR");
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                }
                for (int i = BARIS_HEADER + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) {
                        continue;
                    }
                    BarisMetode b;
                    try {
                        b = new BarisMetode();
                        b.kodeMetode = (int) row.getCell(0).getNumericCellValue();
                        b.namaMetode = teksSel(row.getCell(1));
                        b.kategori = teksSel(row.getCell(2));
                        b.urutan = (int) row.getCell(3).getNumericCellValue();
                        b.aktif = "TRUE".equalsIgnoreCase(teksSel(row.getCell(4)));
                        if (b.namaMetode.isEmpty()) {
                            throw new IllegalArgumentException("Nama Metode kosong");
                        }
                    } catch (Exception ex) {
                        hasil.jumlahGagal++;
                        hasil.pesanGagal.add("Baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                        continue;
                    }
                    // Kegagalan dari titik ini (mis. koneksi terputus) dianggap fatal -> rollback seluruh import.
                    metodeBayarDAO.upsert(conn, b.kodeMetode, b.namaMetode, b.kategori, b.urutan, b.aktif);
                    hasil.jumlahBaris++;
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new Exception("Import dibatalkan (rollback) karena kesalahan koneksi/database: " + ex.getMessage(), ex);
            }
        }
        return hasil;
    }

    private String teksSel(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }
}

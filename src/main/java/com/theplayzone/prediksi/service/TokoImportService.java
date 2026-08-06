package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.TokoDAO;
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
 * Import daftar toko dari sheet "DAFTAR TOKO" pada file Rekap_Omzet_Per_Toko_Bulanan.xlsx
 * (atau file lain dengan sheet bernama sama). Header baris 4, data mulai baris 5:
 *   A = Kode Toko, B = Nama Toko, C = Lokasi Toko (opsional)
 *
 * Seluruh baris diproses dalam SATU transaksi database: baris yang datanya tidak valid (mis. nama
 * kosong) dicatat sebagai gagal tanpa menghentikan baris lain, tapi jika koneksi database terputus
 * di tengah proses, seluruh perubahan pada import ini di-rollback (tidak ada data setengah jalan).
 */
public class TokoImportService {

    private static final int BARIS_HEADER = 3; // 0-based, baris ke-4
    private final TokoDAO tokoDAO = new TokoDAO();

    public static class HasilImport {
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    public HasilImport importFile(File file) throws Exception {
        HasilImport hasil = new HasilImport();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis);
             Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Sheet sheet = workbook.getSheet("DAFTAR TOKO");
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                }
                for (int i = BARIS_HEADER + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    Cell kodeCell = row == null ? null : row.getCell(0);
                    if (kodeCell == null || kodeCell.toString().trim().isEmpty()) {
                        continue;
                    }
                    String kodeToko;
                    String namaToko;
                    String lokasiToko;
                    try {
                        kodeToko = kodeCell.toString().trim();
                        namaToko = teksSel(row.getCell(1));
                        lokasiToko = teksSel(row.getCell(2));
                        if (namaToko.isEmpty()) {
                            throw new IllegalArgumentException("Nama Toko kosong");
                        }
                    } catch (IllegalArgumentException ex) {
                        hasil.jumlahGagal++;
                        hasil.pesanGagal.add("Baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                        continue;
                    }
                    // Kegagalan dari titik ini (mis. koneksi terputus) dianggap fatal -> rollback seluruh import.
                    tokoDAO.upsert(conn, kodeToko, namaToko, lokasiToko.isEmpty() ? null : lokasiToko);
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

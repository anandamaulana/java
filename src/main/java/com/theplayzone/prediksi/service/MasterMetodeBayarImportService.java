package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Import data master metode bayar dari file Master_Metode_Bayar.xlsx.
 * Format: sheet "MASTER METODE BAYAR", header di baris 4, data mulai baris 5:
 *   A = Kode Metode, B = Nama Metode, C = Kategori, D = Urutan, E = Aktif (TRUE/FALSE)
 */
public class MasterMetodeBayarImportService {

    private static final int BARIS_HEADER = 3; // 0-based, baris ke-4
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();

    public static class HasilImport {
        public int jumlahBaris;
        public int jumlahGagal;
        public final List<String> pesanGagal = new ArrayList<>();
    }

    public HasilImport importFile(File file) throws Exception {
        HasilImport hasil = new HasilImport();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet("MASTER METODE BAYAR");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }
            for (int i = BARIS_HEADER + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    continue;
                }
                try {
                    int kodeMetode = (int) row.getCell(0).getNumericCellValue();
                    String namaMetode = teksSel(row.getCell(1));
                    String kategori = teksSel(row.getCell(2));
                    int urutan = (int) row.getCell(3).getNumericCellValue();
                    boolean aktif = "TRUE".equalsIgnoreCase(teksSel(row.getCell(4)));

                    if (namaMetode.isEmpty()) {
                        throw new IllegalArgumentException("Nama Metode kosong");
                    }
                    metodeBayarDAO.upsert(kodeMetode, namaMetode, kategori, urutan, aktif);
                    hasil.jumlahBaris++;
                } catch (Exception ex) {
                    hasil.jumlahGagal++;
                    hasil.pesanGagal.add("Baris " + (row.getRowNum() + 1) + ": " + ex.getMessage());
                }
            }
        }
        return hasil;
    }

    private String teksSel(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }
}

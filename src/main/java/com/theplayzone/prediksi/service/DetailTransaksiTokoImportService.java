package com.theplayzone.prediksi.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * Menghitung jumlah baris transaksi mentah pada file Excel arsip eceran (satu file = satu toko x
 * satu metode bayar x satu bulan), sesuai skenario use case "Mengelola Rekap Transaksi Toko":
 * sistem mengekstraksi seluruh baris data dan mengakumulasi (menjumlahkan) total transaksinya.
 * Format: sheet pertama, baris 1 = header (bebas), data mulai baris 2 -- satu baris = satu transaksi.
 * Nilai kolom tidak diperiksa isinya (aplikasi ini hanya mencatat JUMLAH transaksi, bukan nominal
 * per transaksi) -- baris dihitung selama kolom A tidak kosong.
 */
public class DetailTransaksiTokoImportService {

    public int hitungJumlahTransaksi(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int jumlah = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null || row.getCell(0).toString().trim().isEmpty()) {
                    continue;
                }
                jumlah++;
            }
            return jumlah;
        }
    }
}

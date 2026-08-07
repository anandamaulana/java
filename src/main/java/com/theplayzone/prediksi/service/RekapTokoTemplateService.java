package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.Toko;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Membuat file Excel contoh/template untuk upload grid Rekap_Transaksi_Toko, mengikuti persis format
 * yang dibaca RekapTokoImportService: baris pertama = header (Kode_Toko, Nama_Toko, lalu nama tiap metode
 * bayar aktif), baris berikutnya = satu baris per toko (kode &amp; nama sudah terisi dari Daftar Toko,
 * kolom metode dikosongkan untuk diisi nominal omzet Rupiah oleh Staff/Admin).
 */
public class RekapTokoTemplateService {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    /** Template 1 sheet, untuk import bulanan (Rekap_Transaksi_Toko_&lt;Bulan&gt;_&lt;Tahun&gt;.xlsx). */
    public void buatTemplateBulanan(List<Toko> daftarToko, List<MetodeBayar> daftarMetode, int bulan, int tahun, File target) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String namaSheet = NAMA_BULAN[bulan - 1] + "_" + tahun;
            tulisSheet(workbook, workbook.createSheet(namaSheet), daftarToko, daftarMetode);
            simpan(workbook, target);
        }
    }

    /** Template 12 sheet (satu per bulan), untuk import tahunan (Rekap_Transaksi_Toko_&lt;Tahun&gt;.xlsx). */
    public void buatTemplateTahunan(List<Toko> daftarToko, List<MetodeBayar> daftarMetode, int tahun, File target) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (String bulan : NAMA_BULAN) {
                tulisSheet(workbook, workbook.createSheet(bulan + "_" + tahun), daftarToko, daftarMetode);
            }
            simpan(workbook, target);
        }
    }

    private void tulisSheet(XSSFWorkbook workbook, Sheet sheet, List<Toko> daftarToko, List<MetodeBayar> daftarMetode) {
        CellStyle gayaHeader = workbook.createCellStyle();
        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setColor(IndexedColors.WHITE.getIndex());
        gayaHeader.setFont(fontHeader);
        gayaHeader.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        gayaHeader.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        gayaHeader.setAlignment(HorizontalAlignment.CENTER);

        Row header = sheet.createRow(0);
        buatSel(header, 0, "Kode_Toko", gayaHeader);
        buatSel(header, 1, "Nama_Toko", gayaHeader);
        int kolom = 2;
        for (MetodeBayar m : daftarMetode) {
            buatSel(header, kolom++, m.getNamaMetode(), gayaHeader);
        }

        int baris = 1;
        for (Toko t : daftarToko) {
            Row row = sheet.createRow(baris++);
            row.createCell(0).setCellValue(t.getKodeToko());
            row.createCell(1).setCellValue(t.getNamaToko());
            // Kolom metode sengaja dikosongkan -- diisi nominal omzet Rupiah per metode oleh pengguna.
        }

        sheet.setColumnWidth(0, 12 * 256);
        sheet.setColumnWidth(1, 24 * 256);
        for (int i = 2; i < kolom; i++) {
            sheet.setColumnWidth(i, 16 * 256);
        }
        sheet.createFreezePane(2, 1);
    }

    private void buatSel(Row row, int kolom, String nilai, CellStyle gaya) {
        Cell cell = row.createCell(kolom);
        cell.setCellValue(nilai);
        cell.setCellStyle(gaya);
    }

    private void simpan(XSSFWorkbook workbook, File target) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(target)) {
            workbook.write(fos);
        }
    }
}

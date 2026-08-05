package com.theplayzone.prediksi.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.User;
import org.jfree.chart.JFreeChart;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Membuat laporan hasil prediksi omzet dalam format PDF (kop logo + area tanda tangan). */
public class PdfReportService {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    private static final String LOGO_PATH = "/Image/theplayzone-logo-putih.png";
    private static final Color KUNING = new Color(0xF7, 0xB1, 0x18);
    private static final Color HITAM = new Color(0x1A, 0x1A, 0x1A);

    public void exportHasilPrediksi(PrediksiResult hasil, User user, File target) throws IOException {
        Document document = new Document(PageSize.A4, 42, 42, 36, 42);
        try (FileOutputStream fos = new FileOutputStream(target)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            tambahKop(document);
            tambahJudul(document, hasil);
            tambahInfoProses(document, user);
            tambahDetailHasil(document, hasil);
            tambahGrafik(document, hasil);
            tambahTandaTangan(document);

            document.close();
        } catch (Exception ex) {
            throw new IOException("Gagal membuat PDF: " + ex.getMessage(), ex);
        }
    }

    private void tambahKop(Document document) throws Exception {
        PdfPTable kop = new PdfPTable(2);
        kop.setWidthPercentage(100);
        kop.setWidths(new float[]{1.2f, 3f});

        PdfPCell selLogo;
        try (InputStream in = getClass().getResourceAsStream(LOGO_PATH)) {
            if (in != null) {
                Image logo = Image.getInstance(in.readAllBytes());
                logo.scaleToFit(90, 90);
                selLogo = new PdfPCell(logo, false);
            } else {
                selLogo = new PdfPCell(new Phrase(""));
            }
        }
        selLogo.setBorder(0);
        selLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        kop.addCell(selLogo);

        Font fontNama = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HITAM);
        Font fontAlamat = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Paragraph teks = new Paragraph();
        teks.add(new Chunk("THE PLAY ZONE\n", fontNama));
        teks.add(new Chunk("Sistem Prediksi Omzet - Metode Regresi Linear\n", fontAlamat));
        PdfPCell selTeks = new PdfPCell(teks);
        selTeks.setBorder(0);
        selTeks.setVerticalAlignment(Element.ALIGN_MIDDLE);
        kop.addCell(selTeks);

        document.add(kop);

        PdfPTable garis = new PdfPTable(1);
        garis.setWidthPercentage(100);
        PdfPCell selGaris = new PdfPCell();
        selGaris.setFixedHeight(2f);
        selGaris.setBackgroundColor(KUNING);
        selGaris.setBorder(0);
        garis.addCell(selGaris);
        garis.setSpacingAfter(14f);
        document.add(garis);
    }

    private void tambahJudul(Document document, PrediksiResult hasil) throws Exception {
        Font fontJudul = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, HITAM);
        Paragraph judul = new Paragraph("LAPORAN HASIL PREDIKSI TRANSAKSI", fontJudul);
        judul.setAlignment(Element.ALIGN_CENTER);
        document.add(judul);

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        String toko = hasil.getNamaToko() == null ? "Semua Toko" : hasil.getNamaToko();
        Paragraph sub = new Paragraph(
                toko + " — Periode Target: " + NAMA_BULAN[hasil.getBulanTarget() - 1] + " " + hasil.getTahunTarget(), fontSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(16f);
        document.add(sub);
    }

    private void tambahInfoProses(Document document, User user) throws Exception {
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("id", "ID")));
        Paragraph info = new Paragraph();
        info.setFont(fontLabel);
        info.add("Dicetak oleh  : " + user.getNamaLengkap() + " (" + user.getRole() + ")\n");
        info.add("Tanggal cetak : " + tanggal + "\n");
        info.setSpacingAfter(14f);
        document.add(info);
    }

    private void tambahDetailHasil(Document document, PrediksiResult hasil) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 3f});
        table.setSpacingAfter(16f);

        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, HITAM);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10, HITAM);

        baris(table, "Toko", hasil.getNamaToko() == null ? "Semua Toko" : hasil.getNamaToko(), fontLabel, fontValue);
        baris(table, "Jumlah Data Historis (n)", hasil.getJumlahDataN() + " tahun (Year-over-Year)", fontLabel, fontValue);
        baris(table, "Konstanta (a)", String.format(Locale.US, "%.4f", hasil.getKonstantaA()), fontLabel, fontValue);
        baris(table, "Koefisien (b)", String.format(Locale.US, "%.4f", hasil.getKoefisienB()), fontLabel, fontValue);
        baris(table, "Persamaan Regresi", "Y = " + String.format(Locale.US, "%.2f", hasil.getKonstantaA())
                + " + " + String.format(Locale.US, "%.2f", hasil.getKoefisienB()) + " * X", fontLabel, fontValue);
        baris(table, "Prediksi Jumlah Transaksi", String.format(Locale.US, "%,.0f", hasil.getNilaiPrediksi()), fontLabel, fontValue);
        baris(table, "MAPE (Tingkat Error)", String.format(Locale.US, "%.2f%%", hasil.getMapePersen()), fontLabel, fontValue);

        document.add(table);
    }

    private void baris(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell selLabel = new PdfPCell(new Phrase(label, fontLabel));
        selLabel.setBorder(Rectangle.BOTTOM);
        selLabel.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        selLabel.setPadding(5f);

        PdfPCell selValue = new PdfPCell(new Phrase(value, fontValue));
        selValue.setBorder(Rectangle.BOTTOM);
        selValue.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        selValue.setPadding(5f);

        table.addCell(selLabel);
        table.addCell(selValue);
    }

    private void tambahGrafik(Document document, PrediksiResult hasil) throws Exception {
        JFreeChart chart = com.theplayzone.prediksi.util.ChartHelper.buatChart(hasil);
        BufferedImage bufferedImage = chart.createBufferedImage(480, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        Image chartImage = Image.getInstance(baos.toByteArray());
        chartImage.scaleToFit(480, 300);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        chartImage.setSpacingAfter(24f);
        document.add(chartImage);
    }

    private void tambahTandaTangan(Document document) throws Exception {
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID")));
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, HITAM);
        Font fontJabatan = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, HITAM);

        PdfPTable ttd = new PdfPTable(2);
        ttd.setWidthPercentage(100);
        ttd.setWidths(new float[]{1f, 1f});

        Paragraph kiri = new Paragraph();
        kiri.setFont(fontNormal);
        kiri.setAlignment(Element.ALIGN_CENTER);
        kiri.add("Dibuat oleh,\n\n\n\n\n");
        kiri.add(new Chunk("( .............................. )\n", fontJabatan));
        kiri.add("Staff\n");

        Paragraph kanan = new Paragraph();
        kanan.setFont(fontNormal);
        kanan.setAlignment(Element.ALIGN_CENTER);
        kanan.add("Jakarta, " + tanggal + "\nMengetahui,\n\n\n\n");
        kanan.add(new Chunk("( .............................. )\n", fontJabatan));
        kanan.add("Kepala Divisi The Play Zone\n");

        PdfPCell selKiri = new PdfPCell(kiri);
        selKiri.setBorder(0);
        selKiri.setPaddingTop(10f);
        PdfPCell selKanan = new PdfPCell(kanan);
        selKanan.setBorder(0);
        selKanan.setPaddingTop(10f);

        ttd.addCell(selKiri);
        ttd.addCell(selKanan);

        document.add(ttd);
    }
}

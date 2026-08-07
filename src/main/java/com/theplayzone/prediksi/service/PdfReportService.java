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
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.RekapTokoBulanan;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import org.jfree.chart.JFreeChart;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Membuat laporan hasil prediksi omzet dalam format PDF (kop surat resmi The Play Zone + area tanda tangan). */
public class PdfReportService {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    private static final String KOP_SURAT_PATH = "/Image/kop-surat-theplayzone.jpeg";
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

    public void exportTabelToko(List<Toko> data, User user, File target) throws IOException {
        buatDokumenTabel(target, "LAPORAN DAFTAR TOKO", data.size() + " toko terdaftar", user,
                new String[]{"Kode Toko", "Nama Toko", "Lokasi"}, () -> {
                    List<String[]> rows = new ArrayList<>();
                    for (Toko t : data) {
                        rows.add(new String[]{t.getKodeToko(), t.getNamaToko(), t.getLokasiToko() == null ? "-" : t.getLokasiToko()});
                    }
                    return rows;
                });
    }

    public void exportTabelMetodeBayar(List<MetodeBayar> data, User user, File target) throws IOException {
        buatDokumenTabel(target, "LAPORAN MASTER METODE BAYAR", data.size() + " metode terdaftar", user,
                new String[]{"Kode", "Nama Metode", "Kategori", "Urutan", "Aktif"}, () -> {
                    List<String[]> rows = new ArrayList<>();
                    for (MetodeBayar m : data) {
                        rows.add(new String[]{String.valueOf(m.getKodeMetode()), m.getNamaMetode(),
                                m.getKategori() == null ? "-" : m.getKategori(), String.valueOf(m.getUrutan()),
                                m.isAktif() ? "Ya" : "Tidak"});
                    }
                    return rows;
                });
    }

    public void exportRingkasanRekap(List<RekapTokoBulanan> data, String filterLabel, User user, File target) throws IOException {
        buatDokumenTabel(target, "LAPORAN REKAP TRANSAKSI TOKO", filterLabel + " — " + data.size() + " baris", user,
                new String[]{"Kode Toko", "Nama Toko", "Lokasi Toko", "Tahun", "Bulan", "Jumlah Transaksi", "Total Omzet (Rp)"}, () -> {
                    List<String[]> rows = new ArrayList<>();
                    for (RekapTokoBulanan b : data) {
                        rows.add(new String[]{b.getKodeToko(), b.getNamaToko(), b.getLokasiToko() == null ? "-" : b.getLokasiToko(),
                                String.valueOf(b.getTahun()), NAMA_BULAN[b.getBulan() - 1],
                                String.format(Locale.US, "%,d", b.getJumlahTransaksi()), formatRupiah(b.getTotalOmzet())});
                    }
                    return rows;
                });
    }

    private static String formatRupiah(BigDecimal nilai) {
        return "Rp " + String.format(Locale.US, "%,.0f", nilai == null ? BigDecimal.ZERO : nilai);
    }

    public void exportTabelRiwayat(List<HasilPrediksi> data, String filterLabel, User user, File target) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        buatDokumenTabel(target, "LAPORAN HASIL PREDIKSI TRANSAKSI OMZET", filterLabel + " — " + data.size() + " baris", user,
                new String[]{"Diproses", "Toko", "Target", "n", "a", "b", "Prediksi Omzet (Rp)", "MAPE (%)", "Oleh"}, () -> {
                    List<String[]> rows = new ArrayList<>();
                    for (HasilPrediksi h : data) {
                        rows.add(new String[]{
                                h.getTanggalProses().format(fmt),
                                h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                                NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                                String.valueOf(h.getJumlahDataN()),
                                String.format(Locale.US, "%.2f", h.getKonstantaA()),
                                String.format(Locale.US, "%.2f", h.getKoefisienB()),
                                formatRupiah(BigDecimal.valueOf(h.getNilaiPrediksi())),
                                String.format(Locale.US, "%.2f", h.getMapePersen()),
                                h.getNamaUser()
                        });
                    }
                    return rows;
                });
    }

    /** Laporan gabungan: Daftar Toko + Master Metode Bayar + Rekap Transaksi + Riwayat Prediksi dalam satu file. */
    public void exportGabungan(List<Toko> toko, List<MetodeBayar> metode, List<RekapTokoBulanan> rekap,
                                List<HasilPrediksi> riwayat, String filterLabel, User user, File target) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Document document = new Document(PageSize.A4, 42, 42, 36, 42);
        try (FileOutputStream fos = new FileOutputStream(target)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            tambahKop(document);
            tambahJudulGenerik(document, "LAPORAN GABUNGAN", filterLabel);
            tambahInfoProses(document, user);

            tambahSubJudul(document, "1. Daftar Toko (" + toko.size() + ")");
            List<String[]> baris1 = new ArrayList<>();
            for (Toko t : toko) {
                baris1.add(new String[]{t.getKodeToko(), t.getNamaToko(), t.getLokasiToko() == null ? "-" : t.getLokasiToko()});
            }
            tambahTabelData(document, new String[]{"Kode Toko", "Nama Toko", "Lokasi"}, baris1);

            tambahSubJudul(document, "2. Master Metode Bayar (" + metode.size() + ")");
            List<String[]> baris2 = new ArrayList<>();
            for (MetodeBayar m : metode) {
                baris2.add(new String[]{String.valueOf(m.getKodeMetode()), m.getNamaMetode(),
                        m.getKategori() == null ? "-" : m.getKategori(), m.isAktif() ? "Ya" : "Tidak"});
            }
            tambahTabelData(document, new String[]{"Kode", "Nama Metode", "Kategori", "Aktif"}, baris2);

            tambahSubJudul(document, "3. Rekap Transaksi Toko (" + rekap.size() + " baris)");
            List<String[]> baris3 = new ArrayList<>();
            for (RekapTokoBulanan b : rekap) {
                baris3.add(new String[]{b.getKodeToko(), b.getNamaToko(), String.valueOf(b.getTahun()),
                        NAMA_BULAN[b.getBulan() - 1], String.format(Locale.US, "%,d", b.getJumlahTransaksi()), formatRupiah(b.getTotalOmzet())});
            }
            tambahTabelData(document, new String[]{"Kode Toko", "Nama Toko", "Tahun", "Bulan", "Jumlah Transaksi", "Total Omzet (Rp)"}, baris3);

            tambahSubJudul(document, "4. Riwayat Hasil Prediksi Omzet (" + riwayat.size() + " baris)");
            List<String[]> baris4 = new ArrayList<>();
            for (HasilPrediksi h : riwayat) {
                baris4.add(new String[]{
                        h.getTanggalProses().format(fmt),
                        h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                        NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                        formatRupiah(BigDecimal.valueOf(h.getNilaiPrediksi())),
                        String.format(Locale.US, "%.2f%%", h.getMapePersen())
                });
            }
            tambahTabelData(document, new String[]{"Diproses", "Toko", "Target", "Prediksi Omzet (Rp)", "MAPE"}, baris4);

            tambahTandaTangan(document);
            document.close();
        } catch (Exception ex) {
            throw new IOException("Gagal membuat PDF: " + ex.getMessage(), ex);
        }
    }

    /** Rekapitulasi Prediksi Serentak: tabel semua toko + grafik batang perbandingan, dalam satu file. */
    public void exportRekapitulasiSerentak(List<PrediksiResult> hasilPerToko, int bulanTarget, int tahunTarget,
                                            User user, File target) throws IOException {
        Document document = new Document(PageSize.A4, 42, 42, 36, 42);
        try (FileOutputStream fos = new FileOutputStream(target)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            tambahKop(document);
            String labelPeriode = NAMA_BULAN[bulanTarget - 1] + " " + tahunTarget;
            tambahJudulGenerik(document, "LAPORAN REKAPITULASI PREDIKSI SERENTAK",
                    "Periode Target: " + labelPeriode + " -- " + hasilPerToko.size() + " toko");
            tambahInfoProses(document, user);

            List<String[]> baris = new ArrayList<>();
            for (PrediksiResult r : hasilPerToko) {
                baris.add(new String[]{
                        r.getNamaToko(),
                        String.valueOf(r.getJumlahDataN()),
                        String.format(Locale.US, "%.2f", r.getKonstantaA()),
                        String.format(Locale.US, "%.2f", r.getKoefisienB()),
                        formatRupiah(BigDecimal.valueOf(r.getNilaiPrediksi())),
                        String.format(Locale.US, "%.2f%%", r.getMapePersen())
                });
            }
            tambahTabelData(document, new String[]{"Toko", "n", "a", "b", "Prediksi Omzet (Rp)", "MAPE"}, baris);

            if (!hasilPerToko.isEmpty()) {
                JFreeChart chart = com.theplayzone.prediksi.util.ChartHelper.buatChartPerbandingan(hasilPerToko, labelPeriode);
                BufferedImage bufferedImage = chart.createBufferedImage(480, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                Image chartImage = Image.getInstance(baos.toByteArray());
                chartImage.scaleToFit(480, 300);
                chartImage.setAlignment(Element.ALIGN_CENTER);
                chartImage.setSpacingAfter(24f);
                document.add(chartImage);
            }

            tambahTandaTangan(document);
            document.close();
        } catch (Exception ex) {
            throw new IOException("Gagal membuat PDF: " + ex.getMessage(), ex);
        }
    }

    private void buatDokumenTabel(File target, String judul, String subJudul, User user, String[] headers,
                                   java.util.function.Supplier<List<String[]>> penyedia) throws IOException {
        Document document = new Document(PageSize.A4, 42, 42, 36, 42);
        try (FileOutputStream fos = new FileOutputStream(target)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            tambahKop(document);
            tambahJudulGenerik(document, judul, subJudul);
            tambahInfoProses(document, user);
            tambahTabelData(document, headers, penyedia.get());
            tambahTandaTangan(document);

            document.close();
        } catch (Exception ex) {
            throw new IOException("Gagal membuat PDF: " + ex.getMessage(), ex);
        }
    }

    private void tambahJudulGenerik(Document document, String judulUtama, String subJudul) throws Exception {
        Font fontJudul = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, HITAM);
        Paragraph judul = new Paragraph(judulUtama, fontJudul);
        judul.setAlignment(Element.ALIGN_CENTER);
        document.add(judul);

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph sub = new Paragraph(subJudul, fontSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(16f);
        document.add(sub);
    }

    private void tambahSubJudul(Document document, String teks) throws Exception {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11.5f, HITAM);
        Paragraph p = new Paragraph(teks, font);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(6f);
        document.add(p);
    }

    private void tambahTabelData(Document document, String[] headers, List<String[]> rows) throws Exception {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingAfter(16f);

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 9, HITAM);

        for (String h : headers) {
            PdfPCell sel = new PdfPCell(new Phrase(h, fontHeader));
            sel.setBackgroundColor(HITAM);
            sel.setPadding(5f);
            table.addCell(sel);
        }

        if (rows.isEmpty()) {
            PdfPCell kosong = new PdfPCell(new Phrase("Tidak ada data.", fontCell));
            kosong.setColspan(headers.length);
            kosong.setPadding(8f);
            kosong.setBorder(Rectangle.BOTTOM);
            kosong.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
            table.addCell(kosong);
        } else {
            for (String[] row : rows) {
                for (String val : row) {
                    PdfPCell sel = new PdfPCell(new Phrase(val == null ? "-" : val, fontCell));
                    sel.setPadding(4f);
                    sel.setBorder(Rectangle.BOTTOM);
                    sel.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
                    table.addCell(sel);
                }
            }
        }

        document.add(table);
    }

    private void tambahKop(Document document) throws Exception {
        boolean adaKopResmi = false;
        try (InputStream in = getClass().getResourceAsStream(KOP_SURAT_PATH)) {
            if (in != null) {
                Image kopSurat = Image.getInstance(in.readAllBytes());
                float lebar = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                kopSurat.scaleToFit(lebar, 100f);
                kopSurat.setAlignment(Element.ALIGN_LEFT);
                document.add(kopSurat);
                adaKopResmi = true;
            }
        }

        if (!adaKopResmi) {
            // Fallback jika gambar kop surat resmi tidak ditemukan di resources.
            Font fontNama = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HITAM);
            document.add(new Paragraph("THE PLAY ZONE", fontNama));
        }

        Font fontSistem = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8.5f, Color.DARK_GRAY);
        Paragraph sistem = new Paragraph("Sistem Prediksi Omzet - Metode Regresi Linear", fontSistem);
        sistem.setSpacingBefore(4f);
        document.add(sistem);

        PdfPTable garis = new PdfPTable(1);
        garis.setWidthPercentage(100);
        PdfPCell selGaris = new PdfPCell();
        selGaris.setFixedHeight(2f);
        selGaris.setBackgroundColor(KUNING);
        selGaris.setBorder(0);
        garis.addCell(selGaris);
        garis.setSpacingBefore(6f);
        garis.setSpacingAfter(14f);
        document.add(garis);
    }

    private void tambahJudul(Document document, PrediksiResult hasil) throws Exception {
        Font fontJudul = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, HITAM);
        Paragraph judul = new Paragraph("LAPORAN HASIL PREDIKSI TRANSAKSI OMZET", fontJudul);
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
        baris(table, "Prediksi Omzet (Rupiah)", formatRupiah(BigDecimal.valueOf(hasil.getNilaiPrediksi())), fontLabel, fontValue);
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

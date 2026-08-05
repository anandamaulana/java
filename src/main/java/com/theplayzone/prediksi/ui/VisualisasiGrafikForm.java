package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PdfReportService;
import com.theplayzone.prediksi.service.RegresiLinearService;
import com.theplayzone.prediksi.util.ChartHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.util.List;

/** Visualisasi Grafik Prediksi Omzet -- pilih hasil prediksi tersimpan, tampilkan ulang grafik trennya. */
public class VisualisasiGrafikForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final RegresiLinearService regresiService = new RegresiLinearService();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Diproses", "Toko", "Target", "Prediksi Transaksi"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JButton btnExportPdf = new JButton("Export PDF");

    private List<HasilPrediksi> riwayat;
    private PrediksiResult hasilAktif;

    public VisualisasiGrafikForm(User user) {
        super("Visualisasi Grafik Prediksi Omzet");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 640);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        initUI();
        muatData();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);
        panel.add(nav, BorderLayout.NORTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                tampilkanGrafik(table.getSelectedRow());
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), chartContainer);
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);

        btnExportPdf.setEnabled(false);
        btnExportPdf.addActionListener(e -> exportPdf());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnExportPdf);
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);

        add(panel);
    }

    private void muatData() {
        tableModel.setRowCount(0);
        chartContainer.removeAll();
        chartContainer.revalidate();
        chartContainer.repaint();
        btnExportPdf.setEnabled(false);
        hasilAktif = null;
        try {
            riwayat = prediksiDAO.findRiwayat();
            for (HasilPrediksi h : riwayat) {
                tableModel.addRow(new Object[]{
                        h.getIdPrediksi(),
                        h.getTanggalProses(),
                        h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                        NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                        String.format("%,.0f", h.getNilaiPrediksi())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanGrafik(int row) {
        HasilPrediksi h = riwayat.get(row);
        try {
            PrediksiResult hasil = regresiService.prediksi(h.getBulanTarget(), h.getTahunTarget(), h.getIdToko());
            hasil.setNamaToko(h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko());
            hasilAktif = hasil;
            chartContainer.removeAll();
            chartContainer.add(ChartHelper.buatPanelTren(hasil), BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
            btnExportPdf.setEnabled(true);
        } catch (IllegalStateException ex) {
            chartContainer.removeAll();
            chartContainer.revalidate();
            chartContainer.repaint();
            btnExportPdf.setEnabled(false);
            hasilAktif = null;
            JOptionPane.showMessageDialog(this,
                    "Grafik tidak bisa dibangun ulang: " + ex.getMessage() +
                            "\n(Data rekap historis untuk toko/bulan ini mungkin sudah berubah sejak prediksi ini disimpan.)",
                    "Data Tidak Cukup", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membangun grafik: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        if (hasilAktif == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        String namaDefault = "Grafik_Prediksi_" + NAMA_BULAN[hasilAktif.getBulanTarget() - 1] + "_" + hasilAktif.getTahunTarget() + ".pdf";
        chooser.setSelectedFile(new File(namaDefault));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        try {
            pdfReportService.exportHasilPrediksi(hasilAktif, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

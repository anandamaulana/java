package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PdfReportService;
import com.theplayzone.prediksi.service.RegresiLinearService;
import com.theplayzone.prediksi.util.ChartHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;

public class PrediksiForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final RegresiLinearService regresiService = new RegresiLinearService();
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final TokoDAO tokoDAO = new TokoDAO();

    private final JComboBox<String> cmbBulan = new JComboBox<>(NAMA_BULAN);
    private final JComboBox<Object> cmbToko = new JComboBox<>();
    private final JSpinner spnTahun;
    private final JTextArea txtHasil = new JTextArea(6, 40);
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JButton btnSimpan = new JButton("Simpan Hasil Prediksi");
    private final JButton btnExportPdf = new JButton("Export PDF");
    private final PdfReportService pdfReportService = new PdfReportService();

    private PrediksiResult hasilTerakhir;

    public PrediksiForm(User user) {
        super("Proses Prediksi Omzet - Regresi Linear");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 620);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }

        int tahunSekarang = LocalDate.now().getYear();
        spnTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));

        initUI();
        muatDaftarToko();
    }

    private void muatDaftarToko() {
        cmbToko.addItem("Semua Toko");
        try {
            for (Toko t : tokoDAO.findAll()) {
                cmbToko.addItem(t);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Toko:"));
        form.add(cmbToko);
        form.add(new JLabel("Bulan Target:"));
        form.add(cmbBulan);
        form.add(new JLabel("Tahun Target:"));
        form.add(spnTahun);

        JButton btnProses = new JButton("Proses Prediksi");
        AppTheme.terapkanTombolUtama(btnProses);
        btnProses.addActionListener(e -> prosesPrediksi());
        form.add(btnProses);

        btnSimpan.setEnabled(false);
        btnSimpan.addActionListener(e -> simpanHasil());
        form.add(btnSimpan);

        btnExportPdf.setEnabled(false);
        btnExportPdf.addActionListener(e -> exportPdf());
        form.add(btnExportPdf);

        txtHasil.setEditable(false);
        txtHasil.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(txtHasil), chartContainer);
        split.setResizeWeight(0.35);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(nav, BorderLayout.NORTH);
        northWrap.add(form, BorderLayout.CENTER);

        root.add(northWrap, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);

        add(root);
    }

    private void prosesPrediksi() {
        int bulan = cmbBulan.getSelectedIndex() + 1;
        int tahun = (Integer) spnTahun.getValue();
        Object tokoTerpilih = cmbToko.getSelectedItem();
        Integer idToko = (tokoTerpilih instanceof Toko) ? ((Toko) tokoTerpilih).getIdToko() : null;
        String namaTokoLabel = (tokoTerpilih instanceof Toko)
                ? ((Toko) tokoTerpilih).getKodeToko() + " - " + ((Toko) tokoTerpilih).getNamaToko()
                : "Semua Toko";

        try {
            PrediksiResult hasil = regresiService.prediksi(bulan, tahun, idToko);
            hasil.setNamaToko(namaTokoLabel);
            hasilTerakhir = hasil;
            tampilkanHasil(hasil);
            chartContainer.removeAll();
            chartContainer.add(ChartHelper.buatPanelTren(hasil), BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
            btnSimpan.setEnabled(true);
            btnExportPdf.setEnabled(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Data Tidak Cukup", JOptionPane.WARNING_MESSAGE);
            btnSimpan.setEnabled(false);
            btnExportPdf.setEnabled(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses prediksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanHasil(PrediksiResult hasil) {
        StringBuilder sb = new StringBuilder();
        sb.append("Toko                    : ").append(hasil.getNamaToko()).append("\n");
        sb.append("Target periode          : ").append(NAMA_BULAN[hasil.getBulanTarget() - 1]).append(" ").append(hasil.getTahunTarget()).append("\n");
        sb.append("Jumlah data historis (n) : ").append(hasil.getJumlahDataN()).append(" tahun (Year-over-Year)\n");
        sb.append(String.format("Konstanta (a)           : %.4f%n", hasil.getKonstantaA()));
        sb.append(String.format("Koefisien (b)           : %.4f%n", hasil.getKoefisienB()));
        sb.append("Persamaan                : Y = ").append(String.format("%.2f", hasil.getKonstantaA()))
                .append(" + ").append(String.format("%.2f", hasil.getKoefisienB())).append(" * X\n");
        sb.append("Prediksi Jumlah Transaksi: ").append(String.format("%,.0f", hasil.getNilaiPrediksi())).append("\n");
        sb.append(String.format("MAPE (tingkat error)    : %.2f%%%n", hasil.getMapePersen()));
        txtHasil.setText(sb.toString());
    }

    private void exportPdf() {
        if (hasilTerakhir == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        String namaDefault = "Laporan_Prediksi_" + NAMA_BULAN[hasilTerakhir.getBulanTarget() - 1]
                + "_" + hasilTerakhir.getTahunTarget() + ".pdf";
        chooser.setSelectedFile(new File(namaDefault));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }

        try {
            pdfReportService.exportHasilPrediksi(hasilTerakhir, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanHasil() {
        if (hasilTerakhir == null) {
            return;
        }
        try {
            prediksiDAO.simpan(hasilTerakhir, user.getIdUser());
            JOptionPane.showMessageDialog(this, "Hasil prediksi berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

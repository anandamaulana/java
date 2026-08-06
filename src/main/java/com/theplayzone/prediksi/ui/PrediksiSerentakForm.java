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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Proses Prediksi Serentak -- menjalankan algoritma Regresi Linear ke SEMUA toko sekaligus
 * (looping otomatis), hasilnya direkapitulasi dalam satu tabel + grafik perbandingan.
 */
public class PrediksiSerentakForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final RegresiLinearService regresiService = new RegresiLinearService();
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final JComboBox<String> cmbBulan = new JComboBox<>(NAMA_BULAN);
    private final JSpinner spnTahun;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Toko", "n", "a", "b", "Prediksi Transaksi", "MAPE (%)", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JTextArea logArea = new JTextArea(4, 60);

    private final JButton btnSimpanSemua = new JButton("Simpan Semua Hasil");
    private final JButton btnExportPdf = new JButton("Export PDF Rekapitulasi");

    private List<PrediksiResult> hasilValid = new ArrayList<>();
    private int bulanTerakhir;
    private int tahunTerakhir;

    public PrediksiSerentakForm(User user) {
        super("Proses Prediksi Serentak (Semua Toko)");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Bulan Target:"));
        form.add(cmbBulan);
        form.add(new JLabel("Tahun Target:"));
        form.add(spnTahun);
        JButton btnProses = new JButton("Proses Semua Toko (Serentak)");
        AppTheme.terapkanTombolUtama(btnProses);
        btnProses.addActionListener(e -> prosesSerentak());
        form.add(btnProses);

        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.add(nav, BorderLayout.NORTH);
        navWrap.add(form, BorderLayout.CENTER);
        panel.add(navWrap, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), chartContainer);
        split.setResizeWeight(0.45);
        panel.add(split, BorderLayout.CENTER);

        btnSimpanSemua.setEnabled(false);
        btnSimpanSemua.addActionListener(e -> simpanSemua());
        btnExportPdf.setEnabled(false);
        btnExportPdf.addActionListener(e -> exportPdf());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpanSemua);
        actions.add(btnExportPdf);

        logArea.setEditable(false);
        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.add(actions, BorderLayout.NORTH);
        south.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        add(panel);
    }

    private void prosesSerentak() {
        int bulan = cmbBulan.getSelectedIndex() + 1;
        int tahun = (Integer) spnTahun.getValue();
        bulanTerakhir = bulan;
        tahunTerakhir = tahun;

        tableModel.setRowCount(0);
        logArea.setText("");
        hasilValid = new ArrayList<>();
        chartContainer.removeAll();
        chartContainer.revalidate();
        chartContainer.repaint();

        List<Toko> daftarToko;
        try {
            daftarToko = tokoDAO.findAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Daftar Toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (daftarToko.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Daftar Toko masih kosong. Lengkapi lewat menu Kelola Daftar Toko.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int gagal = 0;
        for (Toko toko : daftarToko) {
            try {
                PrediksiResult hasil = regresiService.prediksi(bulan, tahun, toko.getIdToko());
                hasil.setNamaToko(toko.getKodeToko() + " - " + toko.getNamaToko());
                hasilValid.add(hasil);
                tableModel.addRow(new Object[]{
                        hasil.getNamaToko(),
                        hasil.getJumlahDataN(),
                        String.format("%.2f", hasil.getKonstantaA()),
                        String.format("%.2f", hasil.getKoefisienB()),
                        String.format("%,.0f", hasil.getNilaiPrediksi()),
                        String.format("%.2f", hasil.getMapePersen()),
                        "OK"
                });
            } catch (IllegalStateException ex) {
                gagal++;
                tableModel.addRow(new Object[]{toko.getKodeToko() + " - " + toko.getNamaToko(), "-", "-", "-", "-", "-", "Data Kurang"});
                logArea.append(toko.getKodeToko() + ": " + ex.getMessage() + "\n");
            } catch (Exception ex) {
                gagal++;
                tableModel.addRow(new Object[]{toko.getKodeToko() + " - " + toko.getNamaToko(), "-", "-", "-", "-", "-", "Error"});
                logArea.append(toko.getKodeToko() + ": " + ex.getMessage() + "\n");
            }
        }

        logArea.append("\nSelesai: " + hasilValid.size() + " toko berhasil diprediksi, " + gagal + " toko dilewati.\n");

        if (!hasilValid.isEmpty()) {
            chartContainer.add(new org.jfree.chart.ChartPanel(
                    ChartHelper.buatChartPerbandingan(hasilValid, NAMA_BULAN[bulan - 1] + " " + tahun)), BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
        }
        btnSimpanSemua.setEnabled(!hasilValid.isEmpty());
        btnExportPdf.setEnabled(!hasilValid.isEmpty());
    }

    private void simpanSemua() {
        if (hasilValid.isEmpty()) {
            return;
        }
        int berhasil = 0;
        for (PrediksiResult hasil : hasilValid) {
            try {
                prediksiDAO.simpan(hasil, user.getIdUser());
                berhasil++;
            } catch (Exception ex) {
                logArea.append("Gagal menyimpan " + hasil.getNamaToko() + ": " + ex.getMessage() + "\n");
            }
        }
        JOptionPane.showMessageDialog(this, berhasil + " dari " + hasilValid.size() + " hasil berhasil disimpan ke riwayat.",
                "Selesai", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportPdf() {
        if (hasilValid.isEmpty()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Rekapitulasi_Prediksi_Serentak_" + NAMA_BULAN[bulanTerakhir - 1] + "_" + tahunTerakhir + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        try {
            pdfReportService.exportRekapitulasiSerentak(hasilValid, bulanTerakhir, tahunTerakhir, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

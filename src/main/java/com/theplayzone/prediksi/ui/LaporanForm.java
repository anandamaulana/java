package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PdfReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class LaporanForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final TokoDAO tokoDAO = new TokoDAO();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Diproses", "Toko", "Target", "n", "a", "b", "Prediksi Transaksi", "MAPE (%)", "Oleh"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JComboBox<Object> cmbToko = new JComboBox<>();
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahunDari;
    private final JSpinner spnTahunSampai;

    public LaporanForm(User user) {
        super("Riwayat Hasil Prediksi");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 520);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahunDari = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunSampai = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
        muatPilihanToko();
        muatData();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.add(new JLabel("Toko:"));
        filter.add(cmbToko);
        chkSemuaTahun.addActionListener(e -> {
            spnTahunDari.setEnabled(!chkSemuaTahun.isSelected());
            spnTahunSampai.setEnabled(!chkSemuaTahun.isSelected());
        });
        spnTahunDari.setEnabled(false);
        spnTahunSampai.setEnabled(false);
        filter.add(chkSemuaTahun);
        filter.add(new JLabel("Tahun Target Dari:"));
        filter.add(spnTahunDari);
        filter.add(new JLabel("Sampai:"));
        filter.add(spnTahunSampai);
        JButton btnTampilkan = new JButton("Tampilkan");
        btnTampilkan.addActionListener(e -> muatData());
        filter.add(btnTampilkan);

        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.add(nav, BorderLayout.NORTH);
        navWrap.add(filter, BorderLayout.CENTER);

        panel.add(navWrap, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusRiwayat());
        JButton btnExportPdf = new JButton("Export PDF");
        AppTheme.terapkanTombolUtama(btnExportPdf);
        btnExportPdf.addActionListener(e -> exportPdf());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnHapus);
        south.add(btnExportPdf);
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);

        add(panel);
    }

    private void muatPilihanToko() {
        cmbToko.addItem("Semua Toko");
        try {
            for (Toko t : tokoDAO.findAll()) {
                cmbToko.addItem(t);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer idTokoTerpilih() {
        Object sel = cmbToko.getSelectedItem();
        return (sel instanceof Toko) ? ((Toko) sel).getIdToko() : null;
    }

    private Integer tahunDariTerpilih() {
        return chkSemuaTahun.isSelected() ? null : (Integer) spnTahunDari.getValue();
    }

    private Integer tahunSampaiTerpilih() {
        return chkSemuaTahun.isSelected() ? null : (Integer) spnTahunSampai.getValue();
    }

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            List<HasilPrediksi> list = prediksiDAO.findRiwayat(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
            for (HasilPrediksi h : list) {
                tableModel.addRow(new Object[]{
                        h.getIdPrediksi(),
                        h.getTanggalProses(),
                        h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                        NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                        h.getJumlahDataN(),
                        String.format("%.2f", h.getKonstantaA()),
                        String.format("%.2f", h.getKoefisienB()),
                        String.format("%,.0f", h.getNilaiPrediksi()),
                        String.format("%.2f", h.getMapePersen()),
                        h.getNamaUser()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusRiwayat() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idPrediksi = (int) tableModel.getValueAt(row, 0);
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Hapus hasil prediksi ini dari riwayat?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            prediksiDAO.delete(idPrediksi);
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Laporan_Riwayat_Prediksi.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        Object tokoSel = cmbToko.getSelectedItem();
        String labelToko = (tokoSel instanceof Toko) ? ((Toko) tokoSel).getKodeToko() + " - " + ((Toko) tokoSel).getNamaToko() : "Semua Toko";
        String labelPeriode = chkSemuaTahun.isSelected() ? "Semua Tahun" : (spnTahunDari.getValue() + " s.d. " + spnTahunSampai.getValue());
        String filterLabel = "Toko: " + labelToko + " | Periode Target: " + labelPeriode;

        try {
            List<HasilPrediksi> list = prediksiDAO.findRiwayat(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
            pdfReportService.exportTabelRiwayat(list, filterLabel, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

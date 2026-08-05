package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.RekapMetodeBaris;
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
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;

/** Laporan Gabungan: Daftar Toko + Master Metode Bayar + Rekap Transaksi + Riwayat Prediksi dalam satu file PDF. */
public class LaporanGabunganForm extends JFrame {

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final JComboBox<Object> cmbToko = new JComboBox<>();
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahun;
    private final JTextArea infoArea = new JTextArea(8, 60);

    public LaporanGabunganForm(User user) {
        super("Laporan Gabungan");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
        muatPilihanToko();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);
        panel.add(nav, BorderLayout.NORTH);

        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText("Laporan Gabungan berisi 4 bagian dalam satu file PDF:\n" +
                "1. Daftar Toko (seluruh data)\n" +
                "2. Master Metode Bayar (seluruh data)\n" +
                "3. Rekap Transaksi Toko (sesuai filter Toko/Tahun di bawah)\n" +
                "4. Riwayat Hasil Prediksi (sesuai filter Toko/Tahun di bawah)\n\n" +
                "Filter Toko/Tahun hanya berlaku untuk bagian 3 dan 4 -- Daftar Toko dan Master Metode Bayar selalu ditampilkan lengkap.");
        panel.add(infoArea, BorderLayout.CENTER);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.add(new JLabel("Toko:"));
        filter.add(cmbToko);
        chkSemuaTahun.addActionListener(e -> spnTahun.setEnabled(!chkSemuaTahun.isSelected()));
        spnTahun.setEnabled(false);
        filter.add(chkSemuaTahun);
        filter.add(new JLabel("Tahun:"));
        filter.add(spnTahun);

        JButton btnExport = new JButton("Export Laporan Gabungan (PDF)");
        AppTheme.terapkanTombolUtama(btnExport);
        btnExport.addActionListener(e -> exportPdf());

        JPanel south = new JPanel(new BorderLayout());
        south.add(filter, BorderLayout.NORTH);
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionRow.add(btnExport);
        south.add(actionRow, BorderLayout.CENTER);

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

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Laporan_Gabungan.pdf"));
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
        String labelTahun = chkSemuaTahun.isSelected() ? "Semua Tahun" : String.valueOf(spnTahun.getValue());
        String filterLabel = "Filter bagian Rekap Transaksi & Riwayat Prediksi -- Toko: " + labelToko + " | Tahun: " + labelTahun;
        Integer idToko = idTokoTerpilih();
        Integer tahun = chkSemuaTahun.isSelected() ? null : (Integer) spnTahun.getValue();

        try {
            java.util.List<Toko> daftarToko = tokoDAO.findAll();
            java.util.List<MetodeBayar> daftarMetode = metodeBayarDAO.findAll();
            java.util.List<RekapMetodeBaris> rekap = rekapMetodeDAO.findAll(idToko, tahun);
            java.util.List<HasilPrediksi> riwayat = prediksiDAO.findRiwayat(idToko, tahun);

            pdfReportService.exportGabungan(daftarToko, daftarMetode, rekap, riwayat, filterLabel, user, target);
            JOptionPane.showMessageDialog(this, "Laporan Gabungan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

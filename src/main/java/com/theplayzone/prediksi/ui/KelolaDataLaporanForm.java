package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.RekapMetodeBaris;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PdfReportService;
import com.theplayzone.prediksi.service.RegresiLinearService;

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
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

/**
 * Kelola Data Laporan -- satu menu terpusat untuk memilih kategori laporan, menerapkan filter
 * (toko/periode), lalu melihat preview (dibuka via aplikasi PDF bawaan sistem operasi) sebelum
 * mengekspor/mencetak. Mengikuti skenario use case "Kelola Data Laporan" pada skripsi Bab IV.
 */
public class KelolaDataLaporanForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    private static final String[] KATEGORI = {
            "Daftar Toko", "Master Metode Bayar", "Rekap Transaksi Toko",
            "Laporan Prediksi (Riwayat)", "Visualisasi Grafik Prediksi", "Laporan Gabungan"
    };
    private static final String KARTU_RENTANG = "rentang";
    private static final String KARTU_GRAFIK = "grafik";

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final RegresiLinearService regresiService = new RegresiLinearService();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final JComboBox<String> cmbKategori = new JComboBox<>(KATEGORI);
    private final JComboBox<Object> cmbToko = new JComboBox<>();

    // Kartu filter rentang periode (Daftar Toko/Metode Bayar/Rekap/Riwayat/Gabungan)
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahunDari;
    private final JSpinner spnTahunSampai;

    // Kartu filter Visualisasi Grafik (satu periode target spesifik)
    private final JComboBox<String> cmbBulanTarget = new JComboBox<>(NAMA_BULAN);
    private final JSpinner spnTahunTarget;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JTextArea infoArea = new JTextArea(6, 60);

    public KelolaDataLaporanForm(User user) {
        super("Kelola Data Laporan");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahunDari = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunSampai = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunTarget = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
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
        panel.add(infoArea, BorderLayout.CENTER);

        JPanel kategoriPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kategoriPanel.add(new JLabel("Kategori Laporan:"));
        cmbKategori.addActionListener(e -> gantiKategori());
        kategoriPanel.add(cmbKategori);

        JPanel kartuRentang = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kartuRentang.add(new JLabel("Toko:"));
        kartuRentang.add(cmbToko);
        chkSemuaTahun.addActionListener(e -> {
            spnTahunDari.setEnabled(!chkSemuaTahun.isSelected());
            spnTahunSampai.setEnabled(!chkSemuaTahun.isSelected());
        });
        spnTahunDari.setEnabled(false);
        spnTahunSampai.setEnabled(false);
        kartuRentang.add(chkSemuaTahun);
        kartuRentang.add(new JLabel("Tahun Dari:"));
        kartuRentang.add(spnTahunDari);
        kartuRentang.add(new JLabel("Sampai:"));
        kartuRentang.add(spnTahunSampai);

        JPanel kartuGrafik = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kartuGrafik.add(new JLabel("Toko: (gunakan pilihan Toko yang sama di atas)"));
        kartuGrafik.add(new JLabel("Bulan Target:"));
        kartuGrafik.add(cmbBulanTarget);
        kartuGrafik.add(new JLabel("Tahun Target:"));
        kartuGrafik.add(spnTahunTarget);

        cardPanel.add(kartuRentang, KARTU_RENTANG);
        cardPanel.add(kartuGrafik, KARTU_GRAFIK);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.add(kategoriPanel, BorderLayout.NORTH);
        filterWrap.add(cardPanel, BorderLayout.CENTER);

        JButton btnPreview = new JButton("Preview");
        btnPreview.addActionListener(e -> preview());
        JButton btnExport = new JButton("Export PDF");
        AppTheme.terapkanTombolUtama(btnExport);
        btnExport.addActionListener(e -> exportPdf());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnPreview);
        actions.add(btnExport);

        JPanel south = new JPanel(new BorderLayout());
        south.add(filterWrap, BorderLayout.NORTH);
        south.add(actions, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        add(panel);
        gantiKategori();
    }

    private void gantiKategori() {
        String kategori = (String) cmbKategori.getSelectedItem();
        boolean grafik = "Visualisasi Grafik Prediksi".equals(kategori);
        cardLayout.show(cardPanel, grafik ? KARTU_GRAFIK : KARTU_RENTANG);

        String penjelasan;
        switch (kategori) {
            case "Daftar Toko":
                penjelasan = "Menampilkan seluruh data toko (filter Toko/Tahun tidak berlaku untuk kategori ini).";
                break;
            case "Master Metode Bayar":
                penjelasan = "Menampilkan seluruh data master metode bayar (filter Toko/Tahun tidak berlaku untuk kategori ini).";
                break;
            case "Rekap Transaksi Toko":
                penjelasan = "Menampilkan rekap transaksi sesuai filter Toko dan rentang Tahun di bawah.";
                break;
            case "Laporan Prediksi (Riwayat)":
                penjelasan = "Menampilkan riwayat hasil prediksi sesuai filter Toko dan rentang Tahun Target di bawah.";
                break;
            case "Visualisasi Grafik Prediksi":
                penjelasan = "Menjalankan ulang Regresi Linear untuk Toko + Bulan/Tahun Target yang dipilih, lalu menyajikan grafik trennya.";
                break;
            default:
                penjelasan = "Satu file PDF berisi Daftar Toko + Master Metode Bayar (lengkap) + Rekap Transaksi + Laporan Prediksi (sesuai filter Toko/rentang Tahun di bawah).";
        }
        infoArea.setText("Kategori: " + kategori + "\n\n" + penjelasan +
                "\n\nKlik Preview untuk membuka pratinjau PDF di aplikasi pembaca PDF bawaan sistem, atau Export PDF untuk langsung menyimpan ke lokasi pilihan.");
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

    private String labelFilterRentang() {
        Object tokoSel = cmbToko.getSelectedItem();
        String labelToko = (tokoSel instanceof Toko) ? ((Toko) tokoSel).getKodeToko() + " - " + ((Toko) tokoSel).getNamaToko() : "Semua Toko";
        String labelPeriode = chkSemuaTahun.isSelected() ? "Semua Tahun" : (spnTahunDari.getValue() + " s.d. " + spnTahunSampai.getValue());
        return "Toko: " + labelToko + " | Periode: " + labelPeriode;
    }

    /** Membuat file PDF sesuai kategori & filter yang aktif, mengembalikan file yang sudah dibuat. */
    private File buatLaporan(File target) throws Exception {
        String kategori = (String) cmbKategori.getSelectedItem();
        switch (kategori) {
            case "Daftar Toko":
                pdfReportService.exportTabelToko(tokoDAO.findAll(), user, target);
                break;
            case "Master Metode Bayar":
                pdfReportService.exportTabelMetodeBayar(metodeBayarDAO.findAll(), user, target);
                break;
            case "Rekap Transaksi Toko": {
                List<RekapMetodeBaris> data = rekapMetodeDAO.findAll(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
                pdfReportService.exportTabelRekap(data, labelFilterRentang(), user, target);
                break;
            }
            case "Laporan Prediksi (Riwayat)": {
                List<HasilPrediksi> data = prediksiDAO.findRiwayat(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
                pdfReportService.exportTabelRiwayat(data, labelFilterRentang(), user, target);
                break;
            }
            case "Visualisasi Grafik Prediksi": {
                int bulan = cmbBulanTarget.getSelectedIndex() + 1;
                int tahun = (Integer) spnTahunTarget.getValue();
                Object tokoSel = cmbToko.getSelectedItem();
                String labelToko = (tokoSel instanceof Toko) ? ((Toko) tokoSel).getKodeToko() + " - " + ((Toko) tokoSel).getNamaToko() : "Semua Toko";
                PrediksiResult hasil = regresiService.prediksi(bulan, tahun, idTokoTerpilih());
                hasil.setNamaToko(labelToko);
                pdfReportService.exportHasilPrediksi(hasil, user, target);
                break;
            }
            default: {
                List<Toko> daftarToko = tokoDAO.findAll();
                List<MetodeBayar> daftarMetode = metodeBayarDAO.findAll();
                List<RekapMetodeBaris> rekap = rekapMetodeDAO.findAll(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
                List<HasilPrediksi> riwayat = prediksiDAO.findRiwayat(idTokoTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
                pdfReportService.exportGabungan(daftarToko, daftarMetode, rekap, riwayat, labelFilterRentang(), user, target);
            }
        }
        return target;
    }

    private void preview() {
        try {
            File temp = Files.createTempFile("preview_laporan_", ".pdf").toFile();
            temp.deleteOnExit();
            buatLaporan(temp);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(temp);
            } else {
                JOptionPane.showMessageDialog(this, "Preview dibuat di: " + temp.getAbsolutePath() +
                        "\n(Tidak bisa membuka otomatis di komputer ini -- buka file tsb secara manual.)", "Preview", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Data Tidak Cukup", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat preview: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Laporan_" + ((String) cmbKategori.getSelectedItem()).replaceAll("[^A-Za-z0-9]+", "_") + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        try {
            buatLaporan(target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Data Tidak Cukup", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

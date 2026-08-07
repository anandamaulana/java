package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.RegresiLinearService;
import com.theplayzone.prediksi.util.ChartHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Proses Prediksi Transaksi Omzet -- satu form yang menggabungkan proses regresi per-toko/agregat
 * (dulu "Proses & Lihat Prediksi Omzet") dengan mode looping otomatis ke semua toko sekaligus
 * (dulu "Proses Prediksi Serentak"), keduanya memakai mesin regresi yang sama
 * (RegresiLinearService.prediksi). Tab kedua menampilkan riwayat hasil yang sudah disimpan (CRUD:
 * lihat + hapus). Export PDF TIDAK ada di sini -- gunakan menu "Kelola Data Laporan".
 */
public class ProsesPrediksiOmzetForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    private static final String OPSI_AGREGAT = "Semua Toko (Agregat)";
    private static final String OPSI_SERENTAK = "Semua Toko (Serentak per Toko)";
    private static final String KARTU_SATU = "satu";
    private static final String KARTU_SERENTAK = "serentak";

    private final User user;
    private final RegresiLinearService regresiService = new RegresiLinearService();
    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final TokoDAO tokoDAO = new TokoDAO();

    // --- Tab 1: Proses Prediksi ---
    private final JComboBox<Object> cmbToko = new JComboBox<>();
    private final JComboBox<String> cmbBulan = new JComboBox<>(NAMA_BULAN);
    private final JSpinner spnTahun;

    private final CardLayout hasilCardLayout = new CardLayout();
    private final JPanel hasilCardPanel = new JPanel(hasilCardLayout);

    private final JTextArea txtHasil = new JTextArea(8, 40);
    private final JPanel chartContainerSatu = new JPanel(new BorderLayout());
    private final JButton btnSimpanSatu = new JButton("Simpan Hasil Prediksi");

    private final DefaultTableModel tableModelSerentak = new DefaultTableModel(
            new Object[]{"Toko", "n", "a", "b", "Prediksi Omzet (Rp)", "MAPE (%)", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tableSerentak = new JTable(tableModelSerentak);
    private final JPanel chartContainerSerentak = new JPanel(new BorderLayout());
    private final JButton btnSimpanSemua = new JButton("Simpan Semua Hasil");
    private final JTextArea logAreaSerentak = new JTextArea(3, 60);

    private PrediksiResult hasilTerakhir;
    private List<PrediksiResult> hasilSerentak = new ArrayList<>();

    // --- Tab 2: Riwayat Prediksi Tersimpan ---
    private final JComboBox<Object> cmbTokoRiwayat = new JComboBox<>();
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahunDari;
    private final JSpinner spnTahunSampai;
    private final DefaultTableModel tableModelRiwayat = new DefaultTableModel(
            new Object[]{"ID", "Diproses", "Toko", "Target", "n", "a", "b", "Prediksi Omzet (Rp)", "MAPE (%)", "Oleh"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tableRiwayat = new JTable(tableModelRiwayat);

    public ProsesPrediksiOmzetForm(User user) {
        super("Proses Prediksi Transaksi Omzet");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 760);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunDari = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunSampai = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
        muatDaftarToko();
        muatRiwayat();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);
        root.add(nav, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Proses Prediksi", buildTabProses());
        tabs.addTab("Riwayat Prediksi Tersimpan", buildTabRiwayat());
        root.add(tabs, BorderLayout.CENTER);

        add(root);
    }

    private JPanel buildTabProses() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

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
        JLabel lblHint = new JLabel("(Untuk cetak PDF, gunakan menu Kelola Data Laporan)");
        lblHint.setFont(lblHint.getFont().deriveFont(Font.ITALIC, 11f));
        form.add(lblHint);
        panel.add(form, BorderLayout.NORTH);

        // Kartu hasil tunggal/agregat
        txtHasil.setEditable(false);
        txtHasil.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JSplitPane splitSatu = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(txtHasil), chartContainerSatu);
        splitSatu.setResizeWeight(0.35);
        btnSimpanSatu.setEnabled(false);
        btnSimpanSatu.addActionListener(e -> simpanSatu());
        JPanel kartuSatu = new JPanel(new BorderLayout());
        kartuSatu.add(splitSatu, BorderLayout.CENTER);
        JPanel aksiSatu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aksiSatu.add(btnSimpanSatu);
        kartuSatu.add(aksiSatu, BorderLayout.SOUTH);

        // Kartu hasil serentak (semua toko)
        tableSerentak.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JSplitPane splitSerentak = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tableSerentak), chartContainerSerentak);
        splitSerentak.setResizeWeight(0.4);
        btnSimpanSemua.setEnabled(false);
        btnSimpanSemua.addActionListener(e -> simpanSemua());
        logAreaSerentak.setEditable(false);
        JPanel kartuSerentak = new JPanel(new BorderLayout());
        kartuSerentak.add(splitSerentak, BorderLayout.CENTER);
        JPanel southSerentak = new JPanel(new BorderLayout(0, 4));
        JPanel aksiSerentak = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aksiSerentak.add(btnSimpanSemua);
        southSerentak.add(aksiSerentak, BorderLayout.NORTH);
        southSerentak.add(new JScrollPane(logAreaSerentak), BorderLayout.CENTER);
        kartuSerentak.add(southSerentak, BorderLayout.SOUTH);

        hasilCardPanel.add(kartuSatu, KARTU_SATU);
        hasilCardPanel.add(kartuSerentak, KARTU_SERENTAK);
        panel.add(hasilCardPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTabRiwayat() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.add(new JLabel("Toko:"));
        filter.add(cmbTokoRiwayat);
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
        btnTampilkan.addActionListener(e -> muatRiwayat());
        filter.add(btnTampilkan);
        panel.add(filter, BorderLayout.NORTH);

        tableRiwayat.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panel.add(new JScrollPane(tableRiwayat), BorderLayout.CENTER);

        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusRiwayat());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatRiwayat());
        JLabel lblHint = new JLabel("(Untuk cetak PDF, gunakan menu Kelola Data Laporan)");
        lblHint.setFont(lblHint.getFont().deriveFont(Font.ITALIC, 11f));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(btnHapus);
        south.add(btnRefresh);
        south.add(lblHint);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void muatDaftarToko() {
        cmbToko.addItem(OPSI_AGREGAT);
        cmbToko.addItem(OPSI_SERENTAK);
        cmbTokoRiwayat.addItem("Semua Toko");
        try {
            for (Toko t : tokoDAO.findAll()) {
                cmbToko.addItem(t);
                cmbTokoRiwayat.addItem(t);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prosesPrediksi() {
        Object sel = cmbToko.getSelectedItem();
        int bulan = cmbBulan.getSelectedIndex() + 1;
        int tahun = (Integer) spnTahun.getValue();

        if (OPSI_SERENTAK.equals(sel)) {
            hasilCardLayout.show(hasilCardPanel, KARTU_SERENTAK);
            prosesSerentak(bulan, tahun);
        } else {
            hasilCardLayout.show(hasilCardPanel, KARTU_SATU);
            Integer idToko = (sel instanceof Toko) ? ((Toko) sel).getIdToko() : null;
            String label = (sel instanceof Toko) ? ((Toko) sel).getKodeToko() + " - " + ((Toko) sel).getNamaToko() : OPSI_AGREGAT;
            prosesSatu(bulan, tahun, idToko, label);
        }
    }

    private void prosesSatu(int bulan, int tahun, Integer idToko, String namaTokoLabel) {
        try {
            PrediksiResult hasil = regresiService.prediksi(bulan, tahun, idToko);
            hasil.setNamaToko(namaTokoLabel);
            hasilTerakhir = hasil;
            tampilkanHasilSatu(hasil);
            chartContainerSatu.removeAll();
            chartContainerSatu.add(ChartHelper.buatPanelTren(hasil), BorderLayout.CENTER);
            chartContainerSatu.revalidate();
            chartContainerSatu.repaint();
            btnSimpanSatu.setEnabled(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Data Tidak Cukup", JOptionPane.WARNING_MESSAGE);
            hasilTerakhir = null;
            btnSimpanSatu.setEnabled(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses prediksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanHasilSatu(PrediksiResult hasil) {
        StringBuilder sb = new StringBuilder();
        sb.append("Toko                     : ").append(hasil.getNamaToko()).append("\n");
        sb.append("Target periode           : ").append(NAMA_BULAN[hasil.getBulanTarget() - 1]).append(" ").append(hasil.getTahunTarget()).append("\n");
        sb.append("Jumlah data historis (n) : ").append(hasil.getJumlahDataN()).append(" tahun (Year-over-Year)\n");
        sb.append(String.format("Konstanta (a)            : %.4f%n", hasil.getKonstantaA()));
        sb.append(String.format("Koefisien (b)            : %.4f%n", hasil.getKoefisienB()));
        sb.append("Persamaan                : Y = ").append(String.format("%.2f", hasil.getKonstantaA()))
                .append(" + ").append(String.format("%.2f", hasil.getKoefisienB())).append(" * X\n");
        sb.append("Prediksi Omzet (Rupiah)  : Rp ").append(String.format("%,.0f", hasil.getNilaiPrediksi())).append("\n");
        sb.append(String.format("MAPE (tingkat error)     : %.2f%%%n", hasil.getMapePersen()));
        txtHasil.setText(sb.toString());
    }

    private void simpanSatu() {
        if (hasilTerakhir == null) {
            return;
        }
        try {
            prediksiDAO.simpan(hasilTerakhir, user.getIdUser());
            JOptionPane.showMessageDialog(this, "Hasil prediksi berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatRiwayat();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prosesSerentak(int bulan, int tahun) {
        tableModelSerentak.setRowCount(0);
        logAreaSerentak.setText("");
        hasilSerentak = new ArrayList<>();
        chartContainerSerentak.removeAll();
        chartContainerSerentak.revalidate();
        chartContainerSerentak.repaint();
        btnSimpanSemua.setEnabled(false);

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
                hasilSerentak.add(hasil);
                tableModelSerentak.addRow(new Object[]{
                        hasil.getNamaToko(),
                        hasil.getJumlahDataN(),
                        String.format("%.2f", hasil.getKonstantaA()),
                        String.format("%.2f", hasil.getKoefisienB()),
                        "Rp " + String.format("%,.0f", hasil.getNilaiPrediksi()),
                        String.format("%.2f", hasil.getMapePersen()),
                        "OK"
                });
            } catch (IllegalStateException ex) {
                gagal++;
                tableModelSerentak.addRow(new Object[]{toko.getKodeToko() + " - " + toko.getNamaToko(), "-", "-", "-", "-", "-", "Data Kurang"});
                logAreaSerentak.append(toko.getKodeToko() + ": " + ex.getMessage() + "\n");
            } catch (Exception ex) {
                gagal++;
                tableModelSerentak.addRow(new Object[]{toko.getKodeToko() + " - " + toko.getNamaToko(), "-", "-", "-", "-", "-", "Error"});
                logAreaSerentak.append(toko.getKodeToko() + ": " + ex.getMessage() + "\n");
            }
        }

        logAreaSerentak.append("\nSelesai: " + hasilSerentak.size() + " toko berhasil diprediksi, " + gagal + " toko dilewati.\n");

        if (!hasilSerentak.isEmpty()) {
            chartContainerSerentak.add(new org.jfree.chart.ChartPanel(
                    ChartHelper.buatChartPerbandingan(hasilSerentak, NAMA_BULAN[bulan - 1] + " " + tahun)), BorderLayout.CENTER);
            chartContainerSerentak.revalidate();
            chartContainerSerentak.repaint();
        }
        btnSimpanSemua.setEnabled(!hasilSerentak.isEmpty());
    }

    private void simpanSemua() {
        if (hasilSerentak.isEmpty()) {
            return;
        }
        int berhasil = 0;
        for (PrediksiResult hasil : hasilSerentak) {
            try {
                prediksiDAO.simpan(hasil, user.getIdUser());
                berhasil++;
            } catch (Exception ex) {
                logAreaSerentak.append("Gagal menyimpan " + hasil.getNamaToko() + ": " + ex.getMessage() + "\n");
            }
        }
        JOptionPane.showMessageDialog(this, berhasil + " dari " + hasilSerentak.size() + " hasil berhasil disimpan ke riwayat.",
                "Selesai", JOptionPane.INFORMATION_MESSAGE);
        muatRiwayat();
    }

    private Integer idTokoRiwayatTerpilih() {
        Object sel = cmbTokoRiwayat.getSelectedItem();
        return (sel instanceof Toko) ? ((Toko) sel).getIdToko() : null;
    }

    private Integer tahunDariTerpilih() {
        return chkSemuaTahun.isSelected() ? null : (Integer) spnTahunDari.getValue();
    }

    private Integer tahunSampaiTerpilih() {
        return chkSemuaTahun.isSelected() ? null : (Integer) spnTahunSampai.getValue();
    }

    private void muatRiwayat() {
        tableModelRiwayat.setRowCount(0);
        try {
            List<HasilPrediksi> list = prediksiDAO.findRiwayat(idTokoRiwayatTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
            for (HasilPrediksi h : list) {
                tableModelRiwayat.addRow(new Object[]{
                        h.getIdPrediksi(),
                        h.getTanggalProses(),
                        h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                        NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                        h.getJumlahDataN(),
                        String.format("%.2f", h.getKonstantaA()),
                        String.format("%.2f", h.getKoefisienB()),
                        "Rp " + String.format("%,.0f", h.getNilaiPrediksi()),
                        String.format("%.2f", h.getMapePersen()),
                        h.getNamaUser()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusRiwayat() {
        int[] rows = tableRiwayat.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih satu atau lebih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Hapus " + rows.length + " hasil prediksi terpilih dari riwayat?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        int gagal = 0;
        for (int row : rows) {
            int idPrediksi = (int) tableModelRiwayat.getValueAt(row, 0);
            try {
                prediksiDAO.delete(idPrediksi);
            } catch (Exception ex) {
                gagal++;
            }
        }
        if (gagal > 0) {
            JOptionPane.showMessageDialog(this, gagal + " dari " + rows.length + " gagal dihapus.", "Sebagian Gagal", JOptionPane.WARNING_MESSAGE);
        }
        muatRiwayat();
    }
}

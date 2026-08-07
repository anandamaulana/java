package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.RekapTokoBulanan;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.DetailTransaksiTokoImportService;
import com.theplayzone.prediksi.service.PdfReportService;
import com.theplayzone.prediksi.service.RekapTokoImportService;

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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Kelola Rekap Transaksi Toko -- entri manual, upload grid omzet (tahunan/bulanan), atau upload detail ledger. */
public class KelolaRekapTransaksiTokoForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();
    private final PdfReportService pdfReportService = new PdfReportService();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode Toko", "Nama Toko", "Lokasi Toko", "Tahun", "Bulan", "Jumlah Transaksi", "Total Omzet (Rp)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JComboBox<Toko> cmbToko = new JComboBox<>();
    private final JComboBox<MetodeBayar> cmbMetode = new JComboBox<>();
    private final JSpinner spnTahun;
    private final JComboBox<String> cmbBulan = new JComboBox<>(NAMA_BULAN);
    private final JSpinner spnJumlah = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
    private final JSpinner spnOmzet = new JSpinner(new SpinnerNumberModel(0.0d, 0.0d, 999999999999.0d, 1000.0d));

    private final JComboBox<Object> cmbTokoFilter = new JComboBox<>();
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahunDari;
    private final JSpinner spnTahunSampai;

    private final JSpinner spnTahunImportTahunan;
    private File fileImportTahunan;
    private final JLabel lblFileTahunan = new JLabel("Belum ada file dipilih.");

    private final JComboBox<String> cmbBulanImportBulanan = new JComboBox<>(NAMA_BULAN);
    private final JSpinner spnTahunImportBulanan;
    private File fileImportBulanan;
    private final JLabel lblFileBulanan = new JLabel("Belum ada file dipilih.");

    private File fileDetail;
    private final JLabel lblFileDetail = new JLabel("Belum ada file dipilih.");

    private final JTextArea logArea = new JTextArea(5, 60);

    public KelolaRekapTransaksiTokoForm(User user) {
        super("Kelola Rekap Transaksi Toko");
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
        spnTahunImportTahunan = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunImportBulanan = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
        muatPilihan();
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
        filter.add(cmbTokoFilter);
        chkSemuaTahun.addActionListener(e -> {
            spnTahunDari.setEnabled(!chkSemuaTahun.isSelected());
            spnTahunSampai.setEnabled(!chkSemuaTahun.isSelected());
        });
        spnTahunDari.setEnabled(false);
        spnTahunSampai.setEnabled(false);
        filter.add(chkSemuaTahun);
        filter.add(new JLabel("Tahun Dari:"));
        filter.add(spnTahunDari);
        filter.add(new JLabel("Sampai:"));
        filter.add(spnTahunSampai);
        JButton btnTampilkan = new JButton("Tampilkan");
        btnTampilkan.addActionListener(e -> muatData());
        filter.add(btnTampilkan);
        JButton btnExportPdf = new JButton("Export PDF");
        btnExportPdf.addActionListener(e -> exportPdf());
        filter.add(btnExportPdf);

        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.add(nav, BorderLayout.NORTH);
        navWrap.add(filter, BorderLayout.CENTER);
        panel.add(navWrap, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Entri Manual", buildTabManual());
        tabs.addTab("Import Grid Omzet (Tahunan/Bulanan)", buildTabImportGrid());
        tabs.addTab("Upload Detail Transaksi (Jumlah)", buildTabDetail());

        logArea.setEditable(false);
        JPanel southWrap = new JPanel(new BorderLayout(0, 8));
        southWrap.add(tabs, BorderLayout.NORTH);
        southWrap.add(new JScrollPane(logArea), BorderLayout.CENTER);
        southWrap.setPreferredSize(new java.awt.Dimension(10, 320));

        panel.add(southWrap, BorderLayout.SOUTH);
        add(panel);
    }

    private JPanel buildTabManual() {
        JPanel wrap = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Toko:"));
        form.add(cmbToko);
        form.add(new JLabel("Metode:"));
        form.add(cmbMetode);
        form.add(new JLabel("Tahun:"));
        form.add(spnTahun);
        form.add(new JLabel("Bulan:"));
        form.add(cmbBulan);
        form.add(new JLabel("Jumlah Transaksi:"));
        form.add(spnJumlah);
        form.add(new JLabel("Total Omzet (Rp):"));
        form.add(spnOmzet);

        JButton btnSimpan = new JButton("Simpan (Tambah/Update)");
        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpanRekap());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusRekap());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpan);
        actions.add(btnHapus);
        actions.add(btnRefresh);

        wrap.add(form, BorderLayout.NORTH);
        wrap.add(actions, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildTabImportGrid() {
        JPanel wrap = new JPanel(new java.awt.GridLayout(2, 1));

        JPanel tahunan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPilihTahunan = new JButton("Pilih Rekap_Transaksi_Toko_<Tahun>.xlsx");
        btnPilihTahunan.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileImportTahunan = chooser.getSelectedFile();
                lblFileTahunan.setText(fileImportTahunan.getName());
            }
        });
        tahunan.add(new JLabel("Import Tahunan (12 sheet bulan):"));
        tahunan.add(btnPilihTahunan);
        tahunan.add(lblFileTahunan);
        tahunan.add(new JLabel("Tahun data:"));
        tahunan.add(spnTahunImportTahunan);
        JButton btnImportTahunan = new JButton("Import Tahunan");
        btnImportTahunan.addActionListener(e -> doImportTahunan());
        tahunan.add(btnImportTahunan);

        JPanel bulanan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPilihBulanan = new JButton("Pilih Rekap_Transaksi_Toko_<Bulan>_<Tahun>.xlsx");
        btnPilihBulanan.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileImportBulanan = chooser.getSelectedFile();
                lblFileBulanan.setText(fileImportBulanan.getName());
            }
        });
        bulanan.add(new JLabel("Import Bulanan (1 sheet):"));
        bulanan.add(btnPilihBulanan);
        bulanan.add(lblFileBulanan);
        bulanan.add(new JLabel("Bulan:"));
        bulanan.add(cmbBulanImportBulanan);
        bulanan.add(new JLabel("Tahun:"));
        bulanan.add(spnTahunImportBulanan);
        JButton btnImportBulanan = new JButton("Import Bulanan");
        btnImportBulanan.addActionListener(e -> doImportBulanan());
        bulanan.add(btnImportBulanan);

        wrap.add(tahunan);
        wrap.add(bulanan);
        return wrap;
    }

    private JPanel buildTabDetail() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrap.add(new JLabel("Upload Detail Transaksi (pakai Toko/Metode/Tahun/Bulan pada tab Entri Manual):"));
        JButton btnPilihDetail = new JButton("Pilih File Detail");
        btnPilihDetail.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileDetail = chooser.getSelectedFile();
                lblFileDetail.setText(fileDetail.getName());
            }
        });
        wrap.add(btnPilihDetail);
        wrap.add(lblFileDetail);
        JButton btnUploadDetail = new JButton("Upload & Akumulasi Jumlah Transaksi");
        btnUploadDetail.addActionListener(e -> uploadDetail());
        wrap.add(btnUploadDetail);
        return wrap;
    }

    private void muatPilihan() {
        cmbToko.removeAllItems();
        cmbMetode.removeAllItems();
        cmbTokoFilter.removeAllItems();
        cmbTokoFilter.addItem("Semua Toko");
        try {
            for (Toko t : tokoDAO.findAll()) {
                cmbToko.addItem(t);
                cmbTokoFilter.addItem(t);
            }
            for (MetodeBayar m : metodeBayarDAO.findAll()) {
                cmbMetode.addItem(m);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar toko/metode: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (cmbToko.getItemCount() == 0 || cmbMetode.getItemCount() == 0) {
            logArea.append("Peringatan: Daftar Toko atau Master Metode Bayar masih kosong -- lengkapi dulu lewat menu masing-masing.\n");
        }
    }

    private Integer idTokoFilterTerpilih() {
        Object sel = cmbTokoFilter.getSelectedItem();
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
            List<RekapTokoBulanan> list = rekapMetodeDAO.findRingkasanBulanan(idTokoFilterTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
            for (RekapTokoBulanan b : list) {
                tableModel.addRow(new Object[]{b.getKodeToko(), b.getNamaToko(), b.getLokasiToko() == null ? "-" : b.getLokasiToko(),
                        b.getTahun(), NAMA_BULAN[b.getBulan() - 1], b.getJumlahTransaksi(), formatRupiah(b.getTotalOmzet())});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatRupiah(BigDecimal nilai) {
        return "Rp " + String.format(Locale.US, "%,.0f", nilai == null ? BigDecimal.ZERO : nilai);
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Laporan_Rekap_Transaksi_Toko.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        Object tokoSel = cmbTokoFilter.getSelectedItem();
        String labelToko = (tokoSel instanceof Toko) ? ((Toko) tokoSel).getKodeToko() + " - " + ((Toko) tokoSel).getNamaToko() : "Semua Toko";
        String labelPeriode = chkSemuaTahun.isSelected() ? "Semua Tahun" : (spnTahunDari.getValue() + " s.d. " + spnTahunSampai.getValue());
        String filterLabel = "Toko: " + labelToko + " | Periode: " + labelPeriode;

        try {
            List<RekapTokoBulanan> list = rekapMetodeDAO.findRingkasanBulanan(idTokoFilterTerpilih(), tahunDariTerpilih(), tahunSampaiTerpilih());
            pdfReportService.exportRingkasanRekap(list, filterLabel, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanRekap() {
        Toko toko = (Toko) cmbToko.getSelectedItem();
        MetodeBayar metode = (MetodeBayar) cmbMetode.getSelectedItem();
        if (toko == null || metode == null) {
            JOptionPane.showMessageDialog(this, "Toko dan Metode wajib dipilih (lengkapi Daftar Toko/Master Metode Bayar dulu jika masih kosong).", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun = (Integer) spnTahun.getValue();
        int bulan = cmbBulan.getSelectedIndex() + 1;
        int jumlah = (Integer) spnJumlah.getValue();
        BigDecimal omzet = BigDecimal.valueOf((Double) spnOmzet.getValue());
        try {
            // upsert: kombinasi toko+metode+tahun+bulan baru -> insert, yang sudah ada -> update (timpa)
            rekapMetodeDAO.upsertJumlah(toko.getIdToko(), metode.getIdMetode(), tahun, bulan, jumlah, null);
            rekapMetodeDAO.upsertOmzet(toko.getIdToko(), metode.getIdMetode(), tahun, bulan, omzet, null);
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan rekap: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusRekap() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih satu atau lebih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Hapus " + rows.length + " periode terpilih (SEMUA metode pada periode itu ikut terhapus)?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        int gagal = 0;
        for (int row : rows) {
            String kodeToko = (String) tableModel.getValueAt(row, 0);
            int tahun = (int) tableModel.getValueAt(row, 3);
            String namaBulan = (String) tableModel.getValueAt(row, 4);
            int bulan = java.util.Arrays.asList(NAMA_BULAN).indexOf(namaBulan) + 1;
            try {
                rekapMetodeDAO.deleteByTokoPeriode(kodeToko, tahun, bulan);
            } catch (Exception ex) {
                gagal++;
                logArea.append("Gagal menghapus " + kodeToko + " " + namaBulan + " " + tahun + ": " + ex.getMessage() + "\n");
            }
        }
        if (gagal > 0) {
            JOptionPane.showMessageDialog(this, gagal + " dari " + rows.length + " gagal dihapus (lihat log).", "Sebagian Gagal", JOptionPane.WARNING_MESSAGE);
        }
        muatData();
    }

    private void doImportTahunan() {
        if (fileImportTahunan == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun = (Integer) spnTahunImportTahunan.getValue();
        try {
            RekapTokoImportService service = new RekapTokoImportService();
            RekapTokoImportService.HasilImport hasil = service.importTahunan(fileImportTahunan, tahun, user.getIdUser());
            tampilkanHasilImport("Import Tahunan " + tahun, hasil);
            muatData();
        } catch (Exception ex) {
            logArea.append("Gagal import tahunan: " + ex.getMessage() + "\n");
        }
    }

    private void doImportBulanan() {
        if (fileImportBulanan == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bulan = cmbBulanImportBulanan.getSelectedIndex() + 1;
        int tahun = (Integer) spnTahunImportBulanan.getValue();
        try {
            RekapTokoImportService service = new RekapTokoImportService();
            RekapTokoImportService.HasilImport hasil = service.importBulanan(fileImportBulanan, bulan, tahun, user.getIdUser());
            tampilkanHasilImport("Import Bulanan " + NAMA_BULAN[bulan - 1] + " " + tahun, hasil);
            muatData();
        } catch (Exception ex) {
            logArea.append("Gagal import bulanan: " + ex.getMessage() + "\n");
        }
    }

    private void tampilkanHasilImport(String label, RekapTokoImportService.HasilImport hasil) {
        logArea.append(label + " selesai: " + hasil.jumlahBaris + " baris toko berhasil, " + hasil.jumlahGagal + " gagal.\n");
        int tampil = Math.min(hasil.pesanGagal.size(), 10);
        for (int i = 0; i < tampil; i++) {
            logArea.append("  - " + hasil.pesanGagal.get(i) + "\n");
        }
        if (hasil.pesanGagal.size() > tampil) {
            logArea.append("  - ... dan " + (hasil.pesanGagal.size() - tampil) + " baris gagal lainnya\n");
        }
    }

    private void uploadDetail() {
        if (fileDetail == null) {
            JOptionPane.showMessageDialog(this, "Pilih file detail transaksi terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Toko toko = (Toko) cmbToko.getSelectedItem();
        MetodeBayar metode = (MetodeBayar) cmbMetode.getSelectedItem();
        if (toko == null || metode == null) {
            JOptionPane.showMessageDialog(this, "Toko dan Metode (di tab Entri Manual) wajib dipilih dulu sebelum upload detail.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun = (Integer) spnTahun.getValue();
        int bulan = cmbBulan.getSelectedIndex() + 1;
        try {
            DetailTransaksiTokoImportService service = new DetailTransaksiTokoImportService();
            int jumlahBaris = service.hitungJumlahTransaksi(fileDetail);
            rekapMetodeDAO.tambahJumlah(toko.getIdToko(), metode.getIdMetode(), tahun, bulan, jumlahBaris, null);
            logArea.append("Upload detail berhasil: " + jumlahBaris + " transaksi diakumulasikan ke " +
                    toko.getKodeToko() + " / " + metode.getNamaMetode() + " / " + NAMA_BULAN[bulan - 1] + " " + tahun + ".\n");
            muatData();
        } catch (Exception ex) {
            logArea.append("Gagal upload detail: " + ex.getMessage() + "\n");
        }
    }
}

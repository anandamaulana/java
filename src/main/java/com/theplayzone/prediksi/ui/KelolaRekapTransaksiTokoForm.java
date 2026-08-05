package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.RekapMetodeBaris;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

/** Kelola Rekap Transaksi Toko -- tambah/edit/hapus manual per toko x metode x bulan, atau import massal dari Excel. */
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
            new Object[]{"Kode Toko", "Nama Toko", "Metode", "Tahun", "Bulan", "Jumlah Transaksi"}, 0) {
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

    private final JComboBox<Object> cmbTokoFilter = new JComboBox<>();
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);
    private final JSpinner spnTahunFilter;

    private final JSpinner spnTahunImport;
    private final JTextArea logArea = new JTextArea(4, 50);
    private File fileImport;

    public KelolaRekapTransaksiTokoForm(User user) {
        super("Kelola Rekap Transaksi Toko");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 660);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        int tahunSekarang = LocalDate.now().getYear();
        spnTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunFilter = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        spnTahunImport = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
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
        chkSemuaTahun.addActionListener(e -> spnTahunFilter.setEnabled(!chkSemuaTahun.isSelected()));
        spnTahunFilter.setEnabled(false);
        filter.add(chkSemuaTahun);
        filter.add(new JLabel("Tahun:"));
        filter.add(spnTahunFilter);
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
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formManual = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formManual.add(new JLabel("Toko:"));
        formManual.add(cmbToko);
        formManual.add(new JLabel("Metode:"));
        formManual.add(cmbMetode);
        formManual.add(new JLabel("Tahun:"));
        formManual.add(spnTahun);
        formManual.add(new JLabel("Bulan:"));
        formManual.add(cmbBulan);
        formManual.add(new JLabel("Jumlah:"));
        formManual.add(spnJumlah);

        JButton btnSimpan = new JButton("Simpan (Tambah/Update)");
        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpanRekap());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusRekap());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                isiFormDariBaris(table.getSelectedRow());
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpan);
        actions.add(btnHapus);
        actions.add(btnRefresh);

        JPanel formImport = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFile = new JLabel("Belum ada file dipilih.");
        JButton btnPilihFile = new JButton("Pilih Rekap_Omzet_Per_Toko_Bulanan.xlsx");
        btnPilihFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileImport = chooser.getSelectedFile();
                lblFile.setText(fileImport.getName());
            }
        });
        formImport.add(btnPilihFile);
        formImport.add(lblFile);
        formImport.add(new JLabel("Tahun data:"));
        formImport.add(spnTahunImport);
        JButton btnImport = new JButton("Import Excel (12 sheet bulan)");
        btnImport.addActionListener(e -> doImport());
        formImport.add(btnImport);

        logArea.setEditable(false);

        JPanel south = new JPanel(new GridLayout(3, 1));
        south.add(formManual);
        south.add(actions);
        south.add(formImport);

        JPanel southWrap = new JPanel(new BorderLayout(0, 8));
        southWrap.add(south, BorderLayout.NORTH);
        southWrap.add(new JScrollPane(logArea), BorderLayout.CENTER);

        panel.add(southWrap, BorderLayout.SOUTH);
        add(panel);
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

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            Integer tahun = chkSemuaTahun.isSelected() ? null : (Integer) spnTahunFilter.getValue();
            List<RekapMetodeBaris> list = rekapMetodeDAO.findAll(idTokoFilterTerpilih(), tahun);
            for (RekapMetodeBaris b : list) {
                tableModel.addRow(new Object[]{b.getKodeToko(), b.getNamaToko(), b.getNamaMetode(),
                        b.getTahun(), NAMA_BULAN[b.getBulan() - 1], b.getJumlahTransaksi()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        String labelTahun = chkSemuaTahun.isSelected() ? "Semua Tahun" : String.valueOf(spnTahunFilter.getValue());
        String filterLabel = "Toko: " + labelToko + " | Tahun: " + labelTahun;

        try {
            Integer tahun = chkSemuaTahun.isSelected() ? null : (Integer) spnTahunFilter.getValue();
            List<RekapMetodeBaris> list = rekapMetodeDAO.findAll(idTokoFilterTerpilih(), tahun);
            pdfReportService.exportTabelRekap(list, filterLabel, user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void isiFormDariBaris(int row) {
        String kodeToko = (String) tableModel.getValueAt(row, 0);
        String namaMetode = (String) tableModel.getValueAt(row, 2);
        int tahun = (int) tableModel.getValueAt(row, 3);
        String namaBulan = (String) tableModel.getValueAt(row, 4);
        int jumlah = (int) tableModel.getValueAt(row, 5);

        for (int i = 0; i < cmbToko.getItemCount(); i++) {
            if (cmbToko.getItemAt(i).getKodeToko().equals(kodeToko)) {
                cmbToko.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cmbMetode.getItemCount(); i++) {
            if (cmbMetode.getItemAt(i).getNamaMetode().equals(namaMetode)) {
                cmbMetode.setSelectedIndex(i);
                break;
            }
        }
        spnTahun.setValue(tahun);
        cmbBulan.setSelectedItem(namaBulan);
        spnJumlah.setValue(jumlah);
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
        try {
            // upsert: kombinasi toko+metode+tahun+bulan baru -> insert, yang sudah ada -> update jumlah
            rekapMetodeDAO.upsertJumlah(toko.getIdToko(), metode.getIdMetode(), tahun, bulan, jumlah, null);
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan rekap: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusRekap() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String kodeToko = (String) tableModel.getValueAt(row, 0);
        String namaMetode = (String) tableModel.getValueAt(row, 2);
        int tahun = (int) tableModel.getValueAt(row, 3);
        String namaBulan = (String) tableModel.getValueAt(row, 4);
        int bulan = java.util.Arrays.asList(NAMA_BULAN).indexOf(namaBulan) + 1;
        try {
            rekapMetodeDAO.delete(kodeToko, namaMetode, tahun, bulan);
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport() {
        if (fileImport == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun = (Integer) spnTahunImport.getValue();
        try {
            RekapTokoImportService service = new RekapTokoImportService();
            RekapTokoImportService.HasilImport hasil = service.importFile(fileImport, tahun, user.getIdUser());
            logArea.append("Import Rekap " + tahun + " selesai: " + hasil.jumlahBaris + " baris berhasil, " + hasil.jumlahGagal + " gagal.\n");
            int tampil = Math.min(hasil.pesanGagal.size(), 10);
            for (int i = 0; i < tampil; i++) {
                logArea.append("  - " + hasil.pesanGagal.get(i) + "\n");
            }
            if (hasil.pesanGagal.size() > tampil) {
                logArea.append("  - ... dan " + (hasil.pesanGagal.size() - tampil) + " baris gagal lainnya\n");
            }
            muatData();
        } catch (Exception ex) {
            logArea.append("Gagal import: " + ex.getMessage() + "\n");
        }
    }
}

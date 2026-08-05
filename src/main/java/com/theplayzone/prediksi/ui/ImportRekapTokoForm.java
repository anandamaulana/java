package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.model.RekapMetodeBaris;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.MasterMetodeBayarImportService;
import com.theplayzone.prediksi.service.RekapTokoImportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

/** Import data master (Metode Bayar, Daftar Toko) dan rekap transaksi bulanan per toko, plus lihat isi tabelnya. */
public class ImportRekapTokoForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();

    private final JLabel lblFileMetode = new JLabel("Belum ada file dipilih.");
    private final JLabel lblFileRekap = new JLabel("Belum ada file dipilih.");
    private final JSpinner spnTahun;
    private final JTextArea logArea = new JTextArea(8, 50);
    private File fileMetode;
    private File fileRekap;

    private final DefaultTableModel modelToko = tabelReadOnly("Kode Toko", "Nama Toko", "Lokasi Toko");
    private final DefaultTableModel modelMetode = tabelReadOnly("Kode", "Nama Metode", "Kategori", "Urutan", "Aktif");
    private final DefaultTableModel modelRekap = tabelReadOnly("Kode Toko", "Nama Toko", "Metode", "Tahun", "Bulan", "Jumlah Transaksi");
    private final JSpinner spnTahunLihat;
    private final JCheckBox chkSemuaTahun = new JCheckBox("Semua Tahun", true);

    public ImportRekapTokoForm(User user) {
        super("Import Master Metode Bayar & Rekap Toko Bulanan");
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
        spnTahunLihat = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
        initUI();
        muatSemuaTabel();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);

        JPanel bagianMetode = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPilihMetode = new JButton("1. Pilih Master_Metode_Bayar.xlsx");
        btnPilihMetode.addActionListener(e -> pilihFileMetode());
        JButton btnImportMetode = new JButton("Import Metode Bayar");
        AppTheme.terapkanTombolUtama(btnImportMetode);
        btnImportMetode.addActionListener(e -> doImportMetode());
        bagianMetode.add(btnPilihMetode);
        bagianMetode.add(lblFileMetode);
        bagianMetode.add(btnImportMetode);

        JPanel bagianRekap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPilihRekap = new JButton("2. Pilih Rekap_Omzet_Per_Toko_Bulanan.xlsx");
        btnPilihRekap.addActionListener(e -> pilihFileRekap());
        bagianRekap.add(btnPilihRekap);
        bagianRekap.add(lblFileRekap);

        JPanel bagianRekap2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bagianRekap2.add(new JLabel("Tahun data:"));
        bagianRekap2.add(spnTahun);
        JButton btnImportRekap = new JButton("Import Daftar Toko + Rekap");
        AppTheme.terapkanTombolUtama(btnImportRekap);
        btnImportRekap.addActionListener(e -> doImportRekap());
        bagianRekap2.add(btnImportRekap);

        JLabel hint = new JLabel("<html>Import Metode Bayar dulu sebelum Import Rekap (nama metode dicocokkan ke data master). " +
                "Tahun data dipakai untuk semua 12 sheet bulan (Januari-Desember) di file Rekap.</html>");

        JPanel north = new JPanel(new GridLayout(4, 1));
        north.add(bagianMetode);
        north.add(bagianRekap);
        north.add(bagianRekap2);
        north.add(hint);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(nav, BorderLayout.NORTH);
        northWrap.add(north, BorderLayout.CENTER);

        logArea.setEditable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Log Import", new JScrollPane(logArea));
        tabs.addTab("Daftar Toko", buatTabToko());
        tabs.addTab("Master Metode Bayar", buatTabMetode());
        tabs.addTab("Rekap Transaksi", buatTabRekap());

        panel.add(northWrap, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);

        add(panel);
    }

    private JPanel buatTabToko() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatToko());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modelToko)), BorderLayout.CENTER);
        return p;
    }

    private JPanel buatTabMetode() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatMetode());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modelMetode)), BorderLayout.CENTER);
        return p;
    }

    private JPanel buatTabRekap() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(chkSemuaTahun);
        top.add(new JLabel("Tahun:"));
        top.add(spnTahunLihat);
        chkSemuaTahun.addActionListener(e -> spnTahunLihat.setEnabled(!chkSemuaTahun.isSelected()));
        spnTahunLihat.setEnabled(false);
        JButton btnTampilkan = new JButton("Tampilkan");
        btnTampilkan.addActionListener(e -> muatRekap());
        top.add(btnTampilkan);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modelRekap)), BorderLayout.CENTER);
        return p;
    }

    private void muatSemuaTabel() {
        muatToko();
        muatMetode();
        muatRekap();
    }

    private void muatToko() {
        modelToko.setRowCount(0);
        try {
            List<Toko> list = tokoDAO.findAll();
            for (Toko t : list) {
                modelToko.addRow(new Object[]{t.getKodeToko(), t.getNamaToko(), t.getLokasiToko()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Daftar Toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void muatMetode() {
        modelMetode.setRowCount(0);
        try {
            List<MetodeBayar> list = metodeBayarDAO.findAll();
            for (MetodeBayar m : list) {
                modelMetode.addRow(new Object[]{m.getKodeMetode(), m.getNamaMetode(), m.getKategori(), m.getUrutan(), m.isAktif()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Master Metode Bayar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void muatRekap() {
        modelRekap.setRowCount(0);
        try {
            Integer tahun = chkSemuaTahun.isSelected() ? null : (Integer) spnTahunLihat.getValue();
            List<RekapMetodeBaris> list = rekapMetodeDAO.findAll(tahun);
            for (RekapMetodeBaris b : list) {
                modelRekap.addRow(new Object[]{b.getKodeToko(), b.getNamaToko(), b.getNamaMetode(),
                        b.getTahun(), NAMA_BULAN[b.getBulan() - 1], b.getJumlahTransaksi()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Rekap Transaksi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pilihFileMetode() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileMetode = chooser.getSelectedFile();
            lblFileMetode.setText(fileMetode.getName());
        }
    }

    private void pilihFileRekap() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileRekap = chooser.getSelectedFile();
            lblFileRekap.setText(fileRekap.getName());
        }
    }

    private void doImportMetode() {
        if (fileMetode == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Master_Metode_Bayar.xlsx terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MasterMetodeBayarImportService service = new MasterMetodeBayarImportService();
            MasterMetodeBayarImportService.HasilImport hasil = service.importFile(fileMetode);
            logArea.append("Import Metode Bayar selesai: " + hasil.jumlahBaris + " berhasil, " + hasil.jumlahGagal + " gagal.\n");
            tampilkanGagal(hasil.pesanGagal);
            muatMetode();
        } catch (Exception ex) {
            logArea.append("Gagal import metode bayar: " + ex.getMessage() + "\n");
        }
    }

    private void doImportRekap() {
        if (fileRekap == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Rekap_Omzet_Per_Toko_Bulanan.xlsx terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun = (Integer) spnTahun.getValue();
        try {
            RekapTokoImportService service = new RekapTokoImportService();
            RekapTokoImportService.HasilImport hasil = service.importFile(fileRekap, tahun, user.getIdUser());
            logArea.append("Import Rekap Toko " + tahun + " selesai: " + hasil.jumlahToko + " toko, " +
                    hasil.jumlahBaris + " baris rekap berhasil, " + hasil.jumlahGagal + " gagal.\n");
            tampilkanGagal(hasil.pesanGagal);
            muatToko();
            muatRekap();
        } catch (Exception ex) {
            logArea.append("Gagal import rekap toko: " + ex.getMessage() + "\n");
        }
    }

    private static DefaultTableModel tabelReadOnly(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void tampilkanGagal(java.util.List<String> pesanGagal) {
        int tampil = Math.min(pesanGagal.size(), 10);
        for (int i = 0; i < tampil; i++) {
            logArea.append("  - " + pesanGagal.get(i) + "\n");
        }
        if (pesanGagal.size() > tampil) {
            logArea.append("  - ... dan " + (pesanGagal.size() - tampil) + " baris gagal lainnya\n");
        }
    }
}

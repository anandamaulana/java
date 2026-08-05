package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.MasterMetodeBayarImportService;
import com.theplayzone.prediksi.service.RekapTokoImportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.time.LocalDate;

/** Import data master (Metode Bayar, Daftar Toko) dan rekap transaksi bulanan per toko. */
public class ImportRekapTokoForm extends JFrame {

    private final User user;
    private final JLabel lblFileMetode = new JLabel("Belum ada file dipilih.");
    private final JLabel lblFileRekap = new JLabel("Belum ada file dipilih.");
    private final JSpinner spnTahun;
    private final JTextArea logArea = new JTextArea(10, 50);
    private File fileMetode;
    private File fileRekap;

    public ImportRekapTokoForm(User user) {
        super("Import Master Metode Bayar & Rekap Toko Bulanan");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 520);
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

        panel.add(northWrap, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(panel);
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
        } catch (Exception ex) {
            logArea.append("Gagal import rekap toko: " + ex.getMessage() + "\n");
        }
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

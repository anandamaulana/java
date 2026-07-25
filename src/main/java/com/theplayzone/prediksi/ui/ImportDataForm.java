package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.TransaksiDAO;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.ExcelImportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImportDataForm extends JFrame {

    private final User user;
    private final JLabel lblFile = new JLabel("Belum ada file dipilih.");
    private final JTextArea logArea = new JTextArea(8, 40);
    private File selectedFile;

    public ImportDataForm(User user) {
        super("Import Data Transaksi");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(580, 460);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);

        JPanel template = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTemplate = new JButton("Unduh Template Excel");
        btnTemplate.addActionListener(e -> unduhTemplate());
        template.add(btnTemplate);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPilih = new JButton("Pilih File Excel (.xlsx)");
        btnPilih.addActionListener(e -> pilihFile());
        top.add(btnPilih);
        top.add(lblFile);

        JButton btnImport = new JButton("Import ke Database");
        AppTheme.terapkanTombolUtama(btnImport);
        btnImport.addActionListener(e -> doImport());

        JButton btnRekap = new JButton("Rekap ke Omzet Bulanan");
        btnRekap.addActionListener(e -> doRekap());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnImport);
        actions.add(btnRekap);

        logArea.setEditable(false);

        JPanel north = new JPanel(new GridLayout(3, 1));
        north.add(template);
        north.add(top);
        north.add(actions);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(nav, BorderLayout.NORTH);
        northWrap.add(north, BorderLayout.CENTER);

        panel.add(northWrap, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JLabel hint = new JLabel("<html>Format kolom: A=Tanggal, B=Nominal, C=Metode Bayar (opsional: cash/e_wallet/kartu_debit/kartu_kredit). Baris 1 = header.</html>");
        panel.add(hint, BorderLayout.SOUTH);

        add(panel);
    }

    private void unduhTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Template_Import_Transaksi.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".xlsx")) {
            target = new File(target.getParentFile(), target.getName() + ".xlsx");
        }

        try (InputStream in = getClass().getResourceAsStream("/templates/Template_Import_Transaksi.xlsx");
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) {
                throw new IllegalStateException("Template tidak ditemukan di dalam aplikasi.");
            }
            in.transferTo(out);
            logArea.append("Template berhasil diunduh ke: " + target.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            logArea.append("Gagal mengunduh template: " + ex.getMessage() + "\n");
        }
    }

    private void pilihFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            lblFile.setText(selectedFile.getName());
        }
    }

    private void doImport() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            ExcelImportService service = new ExcelImportService();
            ExcelImportService.HasilImport hasil = service.importFile(selectedFile, user.getIdUser());
            logArea.append("Import selesai: " + hasil.jumlahBaris + " baris berhasil, " + hasil.jumlahGagal + " baris gagal.\n");
            int tampil = Math.min(hasil.pesanGagal.size(), 10);
            for (int i = 0; i < tampil; i++) {
                logArea.append("  - " + hasil.pesanGagal.get(i) + "\n");
            }
            if (hasil.pesanGagal.size() > tampil) {
                logArea.append("  - ... dan " + (hasil.pesanGagal.size() - tampil) + " baris gagal lainnya\n");
            }
        } catch (Exception ex) {
            logArea.append("Gagal import: " + ex.getMessage() + "\n");
        }
    }

    private void doRekap() {
        try {
            new TransaksiDAO().rekapBulanan();
            logArea.append("Rekap omzet bulanan berhasil diperbarui.\n");
        } catch (Exception ex) {
            logArea.append("Gagal rekap: " + ex.getMessage() + "\n");
        }
    }
}

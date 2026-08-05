package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.TokoDAO;
import com.theplayzone.prediksi.model.Toko;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PdfReportService;
import com.theplayzone.prediksi.service.TokoImportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.util.List;

/** Kelola Daftar Toko -- tambah/hapus manual, atau import massal dari file Excel (sheet DAFTAR TOKO). */
public class KelolaDaftarTokoForm extends JFrame {

    private final User user;
    private final TokoDAO tokoDAO = new TokoDAO();
    private final PdfReportService pdfReportService = new PdfReportService();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode Toko", "Nama Toko", "Lokasi Toko"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtKode = new JTextField(8);
    private final JTextField txtNama = new JTextField(16);
    private final JTextField txtLokasi = new JTextField(16);
    private final JTextArea logArea = new JTextArea(4, 50);
    private File fileImport;

    public KelolaDaftarTokoForm(User user) {
        super("Kelola Daftar Toko");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        initUI();
        muatData();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnKembali = new JButton("← Kembali ke Dashboard");
        btnKembali.addActionListener(e -> dispose());
        nav.add(btnKembali);
        panel.add(nav, BorderLayout.NORTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formManual = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formManual.add(new JLabel("Kode Toko:"));
        formManual.add(txtKode);
        formManual.add(new JLabel("Nama Toko:"));
        formManual.add(txtNama);
        formManual.add(new JLabel("Lokasi:"));
        formManual.add(txtLokasi);

        JButton btnSimpan = new JButton("Simpan (Tambah/Update)");
        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpanToko());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusToko());
        JButton btnBersihkan = new JButton("Form Baru");
        btnBersihkan.addActionListener(e -> bersihkanForm());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                txtKode.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                txtNama.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                Object lokasi = tableModel.getValueAt(row, 2);
                txtLokasi.setText(lokasi == null ? "" : lokasi.toString());
            }
        });

        JButton btnExportPdf = new JButton("Export PDF");
        btnExportPdf.addActionListener(e -> exportPdf());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpan);
        actions.add(btnHapus);
        actions.add(btnBersihkan);
        actions.add(btnRefresh);
        actions.add(btnExportPdf);

        JPanel formImport = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFile = new JLabel("Belum ada file dipilih.");
        JButton btnPilihFile = new JButton("Pilih File Excel (sheet DAFTAR TOKO)");
        btnPilihFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileImport = chooser.getSelectedFile();
                lblFile.setText(fileImport.getName());
            }
        });
        JButton btnImport = new JButton("Import Excel");
        btnImport.addActionListener(e -> doImport());
        formImport.add(btnPilihFile);
        formImport.add(lblFile);
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

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            List<Toko> list = tokoDAO.findAll();
            for (Toko t : list) {
                tableModel.addRow(new Object[]{t.getKodeToko(), t.getNamaToko(), t.getLokasiToko()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanToko() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String lokasi = txtLokasi.getText().trim();
        if (kode.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode Toko dan Nama Toko wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // upsert: kode toko baru -> insert, kode toko yang sudah ada -> update nama/lokasi
            tokoDAO.upsert(kode, nama, lokasi.isEmpty() ? null : lokasi);
            bersihkanForm();
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan toko: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bersihkanForm() {
        txtKode.setText("");
        txtNama.setText("");
        txtLokasi.setText("");
        table.clearSelection();
    }

    private void hapusToko() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String kode = (String) tableModel.getValueAt(row, 0);
        try {
            Integer idToko = tokoDAO.findIdByKode(kode);
            if (idToko != null) {
                tokoDAO.delete(idToko);
            }
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus (kemungkinan toko sudah punya data rekap/prediksi): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Laporan_Daftar_Toko.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }
        try {
            pdfReportService.exportTabelToko(tokoDAO.findAll(), user, target);
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat:\n" + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport() {
        if (fileImport == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            TokoImportService service = new TokoImportService();
            TokoImportService.HasilImport hasil = service.importFile(fileImport);
            logArea.append("Import selesai: " + hasil.jumlahBaris + " berhasil, " + hasil.jumlahGagal + " gagal.\n");
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

package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.MetodeBayarDAO;
import com.theplayzone.prediksi.model.MetodeBayar;
import com.theplayzone.prediksi.service.MasterMetodeBayarImportService;

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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.util.List;

/** Kelola Master Metode Bayar -- tambah/hapus manual, atau import massal dari file Excel. */
public class KelolaMasterMetodeBayarForm extends JFrame {

    private final MetodeBayarDAO metodeBayarDAO = new MetodeBayarDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode", "Nama Metode", "Kategori", "Urutan", "Aktif"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JSpinner spnKode = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField txtNama = new JTextField(14);
    private final JTextField txtKategori = new JTextField(12);
    private final JSpinner spnUrutan = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JCheckBox chkAktif = new JCheckBox("Aktif", true);
    private final JTextArea logArea = new JTextArea(4, 50);
    private File fileImport;

    public KelolaMasterMetodeBayarForm() {
        super("Kelola Master Metode Bayar");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 580);
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
        formManual.add(new JLabel("Kode:"));
        formManual.add(spnKode);
        formManual.add(new JLabel("Nama Metode:"));
        formManual.add(txtNama);
        formManual.add(new JLabel("Kategori:"));
        formManual.add(txtKategori);
        formManual.add(new JLabel("Urutan:"));
        formManual.add(spnUrutan);
        formManual.add(chkAktif);

        JButton btnSimpan = new JButton("Simpan (Tambah/Update)");
        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpanMetode());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusMetode());
        JButton btnBersihkan = new JButton("Form Baru");
        btnBersihkan.addActionListener(e -> bersihkanForm());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                spnKode.setValue(tableModel.getValueAt(row, 0));
                txtNama.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                Object kategori = tableModel.getValueAt(row, 2);
                txtKategori.setText(kategori == null ? "" : kategori.toString());
                spnUrutan.setValue(tableModel.getValueAt(row, 3));
                chkAktif.setSelected((Boolean) tableModel.getValueAt(row, 4));
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpan);
        actions.add(btnHapus);
        actions.add(btnBersihkan);
        actions.add(btnRefresh);

        JPanel formImport = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFile = new JLabel("Belum ada file dipilih.");
        JButton btnPilihFile = new JButton("Pilih Master_Metode_Bayar.xlsx");
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
            List<MetodeBayar> list = metodeBayarDAO.findAll();
            for (MetodeBayar m : list) {
                tableModel.addRow(new Object[]{m.getKodeMetode(), m.getNamaMetode(), m.getKategori(), m.getUrutan(), m.isAktif()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanMetode() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Metode wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // upsert: nama metode baru -> insert, nama metode yang sudah ada -> update
            metodeBayarDAO.upsert((Integer) spnKode.getValue(), nama, txtKategori.getText().trim(),
                    (Integer) spnUrutan.getValue(), chkAktif.isSelected());
            bersihkanForm();
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan metode: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bersihkanForm() {
        spnKode.setValue(1);
        txtNama.setText("");
        txtKategori.setText("");
        spnUrutan.setValue(1);
        chkAktif.setSelected(true);
        table.clearSelection();
    }

    private void hapusMetode() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nama = (String) tableModel.getValueAt(row, 1);
        try {
            Integer idMetode = metodeBayarDAO.findIdByNama(nama);
            if (idMetode != null) {
                metodeBayarDAO.delete(idMetode);
            }
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus (kemungkinan metode sudah punya data rekap): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport() {
        if (fileImport == null) {
            JOptionPane.showMessageDialog(this, "Pilih file Excel terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MasterMetodeBayarImportService service = new MasterMetodeBayarImportService();
            MasterMetodeBayarImportService.HasilImport hasil = service.importFile(fileImport);
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

package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.TransaksiDAO;
import com.theplayzone.prediksi.model.TransaksiHarian;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class KelolaTransaksiForm extends JFrame {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Tanggal", "Nominal", "Metode Bayar"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtTanggal = new JTextField(10);
    private final JTextField txtNominal = new JTextField(10);
    private final JComboBox<String> cmbMetode =
            new JComboBox<>(new String[]{"cash", "e_wallet", "kartu_debit", "kartu_kredit"});

    public KelolaTransaksiForm() {
        super("Kelola Data Transaksi Harian");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
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

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Tanggal (yyyy-MM-dd):"));
        form.add(txtTanggal);
        form.add(new JLabel("Nominal:"));
        form.add(txtNominal);
        form.add(new JLabel("Metode:"));
        form.add(cmbMetode);

        JButton btnTambah = new JButton("Tambah");
        btnTambah.addActionListener(e -> tambahData());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusData());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnTambah);
        actions.add(btnHapus);
        actions.add(btnRefresh);

        JPanel south = new JPanel(new GridLayout(2, 1));
        south.add(form);
        south.add(actions);

        panel.add(south, BorderLayout.SOUTH);
        add(panel);
    }

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            List<TransaksiHarian> list = transaksiDAO.findAll();
            for (TransaksiHarian t : list) {
                tableModel.addRow(new Object[]{t.getIdTransaksi(), t.getTanggal(), t.getNominal(), t.getMetodeBayar()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tambahData() {
        try {
            LocalDate tanggal = LocalDate.parse(txtTanggal.getText().trim());
            BigDecimal nominal = new BigDecimal(txtNominal.getText().trim());
            String metode = (String) cmbMetode.getSelectedItem();

            TransaksiHarian t = new TransaksiHarian();
            t.setTanggal(tanggal);
            t.setNominal(nominal);
            t.setMetodeBayar(metode);
            transaksiDAO.insertTransaksi(t);

            txtTanggal.setText("");
            txtNominal.setText("");
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data tidak valid: " + ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void hapusData() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long id = (long) tableModel.getValueAt(row, 0);
        try {
            transaksiDAO.deleteTransaksi(id);
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.PrediksiDAO;
import com.theplayzone.prediksi.model.HasilPrediksi;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.List;

public class LaporanForm extends JFrame {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final PrediksiDAO prediksiDAO = new PrediksiDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Diproses", "Toko", "Target", "n", "a", "b", "Prediksi Transaksi", "MAPE (%)", "Oleh"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public LaporanForm() {
        super("Riwayat Hasil Prediksi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 480);
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

        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);

        add(panel);
    }

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            List<HasilPrediksi> list = prediksiDAO.findRiwayat();
            for (HasilPrediksi h : list) {
                tableModel.addRow(new Object[]{
                        h.getTanggalProses(),
                        h.getNamaToko() == null ? "Semua Toko" : h.getNamaToko(),
                        NAMA_BULAN[h.getBulanTarget() - 1] + " " + h.getTahunTarget(),
                        h.getJumlahDataN(),
                        String.format("%.2f", h.getKonstantaA()),
                        String.format("%.2f", h.getKoefisienB()),
                        String.format("%,.0f", h.getNilaiPrediksi()),
                        String.format("%.2f", h.getMapePersen()),
                        h.getNamaUser()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class MainDashboard extends JFrame {

    private final User user;

    public MainDashboard(User user) {
        super("Dashboard - Prediksi Omzet The Play Zone");
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 16, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel welcome = new JLabel("Selamat datang, " + user.getNamaLengkap() + " (" + labelRole() + ")");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 14f));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        top.add(welcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
        top.add(btnLogout, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        boolean isOperasional = "operasional".equals(user.getRole());

        if (isOperasional) {
            panel.add(menuButton("Import Data Transaksi (Excel)", () -> new ImportDataForm(user).setVisible(true)));
            panel.add(menuButton("Kelola Data Transaksi", () -> new KelolaTransaksiForm().setVisible(true)));
        }
        panel.add(menuButton("Proses & Lihat Prediksi Omzet", () -> new PrediksiForm(user).setVisible(true)));
        panel.add(menuButton("Riwayat / Laporan Prediksi", () -> new LaporanForm().setVisible(true)));
    }

    private String labelRole() {
        return "kepala_divisi".equals(user.getRole()) ? "Kepala Divisi" : "Staf Operasional Pusat";
    }

    private JButton menuButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(btn.getFont().deriveFont(13f));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}

package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.model.User;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

public class MainDashboard extends JFrame {

    private final User user;

    public MainDashboard(User user) {
        super("Dashboard - Prediksi Omzet The Play Zone");
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 640);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        getContentPane().setBackground(AppTheme.PUTIH);
        initUI();
    }

    private void initUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMenuGrid(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(AppTheme.PUTIH);
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.ABU_GARIS));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setBackground(AppTheme.PUTIH);
        left.setBorder(new EmptyBorder(12, 16, 12, 0));

        ImageIcon logoIcon = AppIcon.logoScaled(140);
        if (logoIcon != null) {
            left.add(new JLabel(logoIcon), BorderLayout.WEST);
        }

        JLabel welcome = new JLabel("<html>Selamat datang, <b>" + user.getNamaLengkap() + "</b><br>"
                + "<span style='color:#666666;font-size:9px'>" + labelRole() + "</span></html>");
        welcome.setFont(welcome.getFont().deriveFont(13f));
        left.add(welcome, BorderLayout.CENTER);

        header.add(left, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
        JPanel right = new JPanel();
        right.setBackground(AppTheme.PUTIH);
        right.setBorder(new EmptyBorder(0, 0, 0, 16));
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildMenuGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.PUTIH);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
        grid.setBackground(AppTheme.PUTIH);
        wrapper.add(grid, BorderLayout.NORTH);

        boolean isAdmin = "admin".equals(user.getRole());

        if (isAdmin) {
            grid.add(menuCard("Kelola Data Pengguna", "Buat, edit, hapus akun Staff (khusus Admin)", () -> new KelolaUserForm(user).setVisible(true)));
            grid.add(menuCard("Kelola Daftar Toko", "Data master cabang/toko (khusus Admin)", () -> new KelolaDaftarTokoForm(user).setVisible(true)));
            grid.add(menuCard("Kelola Master Metode Bayar", "Data master jenis pembayaran (khusus Admin)", () -> new KelolaMasterMetodeBayarForm(user).setVisible(true)));
        }
        grid.add(menuCard("Kelola Rekap Transaksi Toko", "Input/import rekap transaksi bulanan per toko x metode bayar", () -> new KelolaRekapTransaksiTokoForm(user).setVisible(true)));
        grid.add(menuCard("Proses & Lihat Prediksi Omzet", "Jalankan Regresi Linear untuk bulan/tahun target", () -> new PrediksiForm(user).setVisible(true)));
        grid.add(menuCard("Riwayat / Laporan Prediksi", "Lihat seluruh hasil prediksi yang tersimpan", () -> new LaporanForm(user).setVisible(true)));
        grid.add(menuCard("Visualisasi Grafik Prediksi Omzet", "Lihat ulang grafik tren dari hasil prediksi tersimpan", () -> new VisualisasiGrafikForm(user).setVisible(true)));
        grid.add(menuCard("Laporan Gabungan", "Export PDF gabungan Toko + Metode Bayar + Rekap + Riwayat", () -> new LaporanGabunganForm(user).setVisible(true)));

        return wrapper;
    }

    private String labelRole() {
        return "admin".equals(user.getRole()) ? "Admin" : "Staff";
    }

    private JPanel menuCard(String title, String desc, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(AppTheme.ABU_TERANG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, AppTheme.ABU_GARIS),
                new EmptyBorder(16, 18, 16, 18)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
        titleLabel.setForeground(AppTheme.HITAM);

        JLabel descLabel = new JLabel("<html>" + desc + "</html>");
        descLabel.setFont(descLabel.getFont().deriveFont(11f));
        descLabel.setForeground(new Color(0x66, 0x66, 0x66));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(AppTheme.KUNING);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(AppTheme.ABU_TERANG);
            }
        });

        return card;
    }
}

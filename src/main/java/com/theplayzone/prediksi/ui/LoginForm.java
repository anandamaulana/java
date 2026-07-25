package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.AuthService;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.SQLException;

public class LoginForm extends JFrame {

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final AuthService authService = new AuthService();

    public LoginForm() {
        super("Login - Prediksi Omzet The Play Zone");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        initUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        getContentPane().setBackground(AppTheme.PUTIH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.PUTIH);
        card.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.ABU_GARIS, 1, true),
                new EmptyBorder(28, 32, 28, 32)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 2;

        ImageIcon logoIcon = AppIcon.logoScaled(260);
        int row = 0;
        if (logoIcon != null) {
            JLabel logo = new JLabel(logoIcon);
            gbc.gridx = 0;
            gbc.gridy = row++;
            card.add(logo, gbc);
        }

        JLabel subtitle = new JLabel("Sistem Prediksi Omzet");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD, 15f));
        subtitle.setForeground(AppTheme.HITAM);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 6, 18, 6);
        card.add(subtitle, gbc);

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = row;
        gbc.gridx = 0;
        card.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        card.add(txtUsername, gbc);

        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        card.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        card.add(txtPassword, gbc);

        JButton btnLogin = new JButton("Login");
        AppTheme.terapkanTombolUtama(btnLogin);
        btnLogin.addActionListener(e -> doLogin());
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 6, 6);
        card.add(btnLogin, gbc);

        JLabel hint = new JLabel("<html><i>Akun contoh: admin/admin123 (Kepala Divisi), operasional/opr123 (Staf Operasional)</i></html>", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(10f));
        row++;
        gbc.gridy = row;
        gbc.insets = new Insets(10, 6, 0, 6);
        card.add(hint, gbc);

        getRootPane().setDefaultButton(btnLogin);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.PUTIH);
        outer.setBorder(new EmptyBorder(30, 30, 30, 30));
        outer.add(card, BorderLayout.CENTER);
        add(outer);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = authService.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new MainDashboard(user).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database:\n" + ex.getMessage(),
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

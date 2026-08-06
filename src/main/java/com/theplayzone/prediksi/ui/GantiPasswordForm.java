package com.theplayzone.prediksi.ui;

import com.theplayzone.prediksi.dao.UserDAO;
import com.theplayzone.prediksi.model.User;
import com.theplayzone.prediksi.service.PasswordUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;

/** Ganti password akun sendiri -- tersedia untuk Admin maupun Staff yang sedang login. */
public class GantiPasswordForm extends JFrame {

    private final User user;
    private final UserDAO userDAO = new UserDAO();

    private final JPasswordField txtLama = new JPasswordField(16);
    private final JPasswordField txtBaru = new JPasswordField(16);
    private final JPasswordField txtKonfirmasi = new JPasswordField(16);

    public GantiPasswordForm(User user) {
        super("Ganti Password");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        Image icon = AppIcon.windowIcon();
        if (icon != null) {
            setIconImage(icon);
        }
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel info = new JLabel("<html>Akun: <b>" + user.getUsername() + "</b> (" + user.getNamaLengkap() + ")</html>");
        panel.add(info, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));
        form.add(new JLabel("Password Lama:"));
        form.add(txtLama);
        form.add(new JLabel("Password Baru:"));
        form.add(txtBaru);
        form.add(new JLabel("Konfirmasi Password Baru:"));
        form.add(txtKonfirmasi);
        panel.add(form, BorderLayout.CENTER);

        JButton btnSimpan = new JButton("Simpan Password Baru");
        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpan());
        JButton btnBatal = new JButton("Batal");
        btnBatal.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnBatal);
        actions.add(btnSimpan);
        panel.add(actions, BorderLayout.SOUTH);

        add(panel);
    }

    private void simpan() {
        String lama = new String(txtLama.getPassword());
        String baru = new String(txtBaru.getPassword());
        String konfirmasi = new String(txtKonfirmasi.getPassword());

        if (lama.isEmpty() || baru.isEmpty() || konfirmasi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!baru.equals(konfirmasi)) {
            JOptionPane.showMessageDialog(this, "Konfirmasi Password Baru tidak cocok.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User terbaru = userDAO.findByUsername(user.getUsername());
            if (terbaru == null || !terbaru.getPasswordHash().equals(PasswordUtil.sha256(lama))) {
                JOptionPane.showMessageDialog(this, "Password Lama salah.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            userDAO.update(user.getIdUser(), user.getNamaLengkap(), PasswordUtil.sha256(baru));
            JOptionPane.showMessageDialog(this, "Password berhasil diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengganti password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

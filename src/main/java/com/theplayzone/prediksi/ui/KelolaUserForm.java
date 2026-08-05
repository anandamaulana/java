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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;

/** Kelola Data Pengguna -- khusus Admin. Membuat, mengedit, & menghapus akun Staff. */
public class KelolaUserForm extends JFrame {

    private final User adminAktif;
    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Nama Lengkap", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtUsername = new JTextField(14);
    private final JTextField txtNamaLengkap = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(14);
    private final JButton btnSimpan = new JButton("Tambah Akun Staff");
    private final JLabel lblHintPassword = new JLabel("(wajib diisi)");

    private Integer idUserEdit;

    public KelolaUserForm(User adminAktif) {
        super("Kelola Data Pengguna");
        this.adminAktif = adminAktif;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(680, 520);
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

        JPanel formAkun = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formAkun.add(new JLabel("Username:"));
        formAkun.add(txtUsername);
        formAkun.add(new JLabel("Password:"));
        formAkun.add(txtPassword);
        formAkun.add(lblHintPassword);

        JPanel formNama = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formNama.add(new JLabel("Nama Lengkap:"));
        formNama.add(txtNamaLengkap);

        AppTheme.terapkanTombolUtama(btnSimpan);
        btnSimpan.addActionListener(e -> simpanUser());
        JButton btnHapus = new JButton("Hapus Terpilih");
        btnHapus.addActionListener(e -> hapusUser());
        JButton btnBersihkan = new JButton("Form Baru");
        btnBersihkan.addActionListener(e -> bersihkanForm());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                isiFormDariBaris(table.getSelectedRow());
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnSimpan);
        actions.add(btnHapus);
        actions.add(btnBersihkan);
        actions.add(btnRefresh);

        JPanel south = new JPanel(new GridLayout(3, 1));
        south.add(formAkun);
        south.add(formNama);
        south.add(actions);

        panel.add(south, BorderLayout.SOUTH);
        add(panel);
    }

    private void muatData() {
        tableModel.setRowCount(0);
        try {
            List<User> list = userDAO.findAll();
            for (User u : list) {
                tableModel.addRow(new Object[]{u.getIdUser(), u.getUsername(), u.getNamaLengkap(), u.getRole()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void isiFormDariBaris(int row) {
        idUserEdit = (int) tableModel.getValueAt(row, 0);
        txtUsername.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtUsername.setEnabled(false);
        txtNamaLengkap.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtPassword.setText("");
        lblHintPassword.setText("(kosongkan jika password tidak diubah)");
        btnSimpan.setText("Simpan Perubahan");
    }

    private void bersihkanForm() {
        idUserEdit = null;
        txtUsername.setText("");
        txtUsername.setEnabled(true);
        txtNamaLengkap.setText("");
        txtPassword.setText("");
        lblHintPassword.setText("(wajib diisi)");
        btnSimpan.setText("Tambah Akun Staff");
        table.clearSelection();
    }

    private void simpanUser() {
        if (idUserEdit != null) {
            editUser();
        } else {
            tambahStaff();
        }
    }

    private void tambahStaff() {
        String username = txtUsername.getText().trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || namaLengkap.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, Password, dan Nama Lengkap wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            userDAO.insert(username, PasswordUtil.sha256(password), namaLengkap, "staff");
            bersihkanForm();
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menambah akun: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editUser() {
        String namaLengkap = txtNamaLengkap.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (namaLengkap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            userDAO.update(idUserEdit, namaLengkap, password.isEmpty() ? null : PasswordUtil.sha256(password));
            bersihkanForm();
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idUser = (int) tableModel.getValueAt(row, 0);
        if (idUser == adminAktif.getIdUser()) {
            JOptionPane.showMessageDialog(this, "Tidak bisa menghapus akun yang sedang login.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            userDAO.delete(idUser);
            bersihkanForm();
            muatData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

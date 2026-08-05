package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id_user, username, password_hash, nama_lengkap, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id_user, username, password_hash, nama_lengkap, role FROM users ORDER BY role, username";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public long insert(String username, String passwordHash, String namaLengkap, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, nama_lengkap, role) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, namaLengkap);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    /** Update nama_lengkap; password_hash hanya diperbarui jika passwordHashBaru tidak null. */
    public void update(int idUser, String namaLengkap, String passwordHashBaru) throws SQLException {
        String sql = passwordHashBaru != null
                ? "UPDATE users SET nama_lengkap = ?, password_hash = ? WHERE id_user = ?"
                : "UPDATE users SET nama_lengkap = ? WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaLengkap);
            if (passwordHashBaru != null) {
                ps.setString(2, passwordHashBaru);
                ps.setInt(3, idUser);
            } else {
                ps.setInt(2, idUser);
            }
            ps.executeUpdate();
        }
    }

    public void delete(int idUser) throws SQLException {
        String sql = "DELETE FROM users WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("id_user"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNamaLengkap(rs.getString("nama_lengkap"));
        u.setRole(rs.getString("role"));
        return u;
    }
}

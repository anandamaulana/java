package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.Toko;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TokoDAO {

    /** Insert toko baru, atau perbarui nama/lokasi jika kode_toko sudah ada. Mengembalikan id_toko. */
    public int upsert(String kodeToko, String namaToko, String lokasiToko) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return upsert(conn, kodeToko, namaToko, lokasiToko);
        }
    }

    /** Sama seperti {@link #upsert(String, String, String)}, memakai Connection eksternal (untuk transaksi atomik). */
    public int upsert(Connection conn, String kodeToko, String namaToko, String lokasiToko) throws SQLException {
        String sql = "INSERT INTO toko (kode_toko, nama_toko, lokasi_toko) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nama_toko = VALUES(nama_toko), lokasi_toko = VALUES(lokasi_toko), " +
                "id_toko = LAST_INSERT_ID(id_toko)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kodeToko);
            ps.setString(2, namaToko);
            ps.setString(3, lokasiToko);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<Toko> findAll() throws SQLException {
        String sql = "SELECT id_toko, kode_toko, nama_toko, lokasi_toko FROM toko ORDER BY kode_toko";
        List<Toko> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Toko t = new Toko();
                t.setIdToko(rs.getInt("id_toko"));
                t.setKodeToko(rs.getString("kode_toko"));
                t.setNamaToko(rs.getString("nama_toko"));
                t.setLokasiToko(rs.getString("lokasi_toko"));
                list.add(t);
            }
        }
        return list;
    }

    public void delete(int idToko) throws SQLException {
        String sql = "DELETE FROM toko WHERE id_toko = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.executeUpdate();
        }
    }

    public Integer findIdByKode(String kodeToko) throws SQLException {
        String sql = "SELECT id_toko FROM toko WHERE kode_toko = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeToko);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}

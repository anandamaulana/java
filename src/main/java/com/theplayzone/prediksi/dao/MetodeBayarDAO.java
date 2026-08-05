package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.MetodeBayar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MetodeBayarDAO {

    /** Insert metode baru, atau perbarui kategori/urutan/aktif jika nama_metode sudah ada. Mengembalikan id_metode. */
    public int upsert(int kodeMetode, String namaMetode, String kategori, int urutan, boolean aktif) throws SQLException {
        String sql = "INSERT INTO metode_bayar (kode_metode, nama_metode, kategori, urutan, aktif) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE kode_metode = VALUES(kode_metode), kategori = VALUES(kategori), " +
                "urutan = VALUES(urutan), aktif = VALUES(aktif), id_metode = LAST_INSERT_ID(id_metode)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, kodeMetode);
            ps.setString(2, namaMetode);
            ps.setString(3, kategori);
            ps.setInt(4, urutan);
            ps.setBoolean(5, aktif);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<MetodeBayar> findAll() throws SQLException {
        String sql = "SELECT id_metode, kode_metode, nama_metode, kategori, urutan, aktif " +
                "FROM metode_bayar ORDER BY urutan";
        List<MetodeBayar> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MetodeBayar m = new MetodeBayar();
                m.setIdMetode(rs.getInt("id_metode"));
                m.setKodeMetode(rs.getInt("kode_metode"));
                m.setNamaMetode(rs.getString("nama_metode"));
                m.setKategori(rs.getString("kategori"));
                m.setUrutan(rs.getInt("urutan"));
                m.setAktif(rs.getBoolean("aktif"));
                list.add(m);
            }
        }
        return list;
    }
}

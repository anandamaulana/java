package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.OmzetBulanan;
import com.theplayzone.prediksi.model.TransaksiHarian;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    public long insertTransaksi(TransaksiHarian t) throws SQLException {
        String sql = "INSERT INTO transaksi_harian (tanggal, nominal, metode_bayar, id_import) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(t.getTanggal()));
            ps.setBigDecimal(2, t.getNominal());
            ps.setString(3, t.getMetodeBayar());
            if (t.getIdImport() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, t.getIdImport());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    public void deleteTransaksi(long id) throws SQLException {
        String sql = "DELETE FROM transaksi_harian WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<TransaksiHarian> findAll() throws SQLException {
        String sql = "SELECT id_transaksi, tanggal, nominal, metode_bayar, id_import " +
                "FROM transaksi_harian ORDER BY tanggal DESC";
        List<TransaksiHarian> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private TransaksiHarian mapRow(ResultSet rs) throws SQLException {
        TransaksiHarian t = new TransaksiHarian();
        t.setIdTransaksi(rs.getLong("id_transaksi"));
        t.setTanggal(rs.getDate("tanggal").toLocalDate());
        t.setNominal(rs.getBigDecimal("nominal"));
        t.setMetodeBayar(rs.getString("metode_bayar"));
        int idImport = rs.getInt("id_import");
        t.setIdImport(rs.wasNull() ? null : idImport);
        return t;
    }

    /** Agregasi ulang transaksi_harian menjadi rekap omzet_bulanan (SUM per tahun & bulan). */
    public void rekapBulanan() throws SQLException {
        String sql = "INSERT INTO omzet_bulanan (tahun, bulan, total_omzet) " +
                "SELECT YEAR(tanggal), MONTH(tanggal), SUM(nominal) FROM transaksi_harian " +
                "GROUP BY YEAR(tanggal), MONTH(tanggal) " +
                "ON DUPLICATE KEY UPDATE total_omzet = VALUES(total_omzet)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    /** Data historis omzet bulan tertentu di semua tahun sebelum tahunBatas, terurut ASC (untuk Year-over-Year). */
    public List<OmzetBulanan> getOmzetByBulan(int bulan, int tahunBatas) throws SQLException {
        String sql = "SELECT id_omzet, tahun, bulan, total_omzet FROM omzet_bulanan " +
                "WHERE bulan = ? AND tahun < ? ORDER BY tahun ASC";
        List<OmzetBulanan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bulan);
            ps.setInt(2, tahunBatas);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOmzet(rs));
                }
            }
        }
        return list;
    }

    public List<OmzetBulanan> findAllOmzet() throws SQLException {
        String sql = "SELECT id_omzet, tahun, bulan, total_omzet FROM omzet_bulanan ORDER BY tahun, bulan";
        List<OmzetBulanan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOmzet(rs));
            }
        }
        return list;
    }

    private OmzetBulanan mapOmzet(ResultSet rs) throws SQLException {
        OmzetBulanan o = new OmzetBulanan();
        o.setIdOmzet(rs.getInt("id_omzet"));
        o.setTahun(rs.getInt("tahun"));
        o.setBulan(rs.getInt("bulan"));
        o.setTotalOmzet(rs.getBigDecimal("total_omzet"));
        return o;
    }
}

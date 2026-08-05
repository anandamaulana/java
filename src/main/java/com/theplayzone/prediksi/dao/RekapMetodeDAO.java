package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.OmzetBulanan;
import com.theplayzone.prediksi.model.RekapMetodeBaris;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Akses data rekap_metode_bulanan (hasil import Rekap_Omzet_Per_Toko_Bulanan.xlsx). */
public class RekapMetodeDAO {

    /** Insert/update jumlah transaksi satu toko x metode x periode. */
    public void upsertJumlah(int idToko, int idMetode, int tahun, int bulan, int jumlahTransaksi, Integer idImport) throws SQLException {
        String sql = "INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, jumlah_transaksi, id_import) " +
                "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE jumlah_transaksi = VALUES(jumlah_transaksi), id_import = VALUES(id_import)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.setInt(2, idMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            ps.setInt(5, jumlahTransaksi);
            if (idImport == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idImport);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Total transaksi (semua metode dijumlahkan) per tahun, untuk bulan tertentu, sebelum tahunBatas.
     * idToko = null berarti agregat Semua Toko. Dipakai sebagai data historis Year-over-Year oleh RegresiLinearService
     * (nilai dititipkan di field totalOmzet milik OmzetBulanan, walau isinya jumlah transaksi -- bukan nominal Rupiah).
     */
    public List<OmzetBulanan> getTotalTransaksiByBulan(Integer idToko, int bulan, int tahunBatas) throws SQLException {
        String sql = "SELECT tahun, SUM(jumlah_transaksi) AS total FROM rekap_metode_bulanan " +
                "WHERE bulan = ? AND tahun < ?" + (idToko != null ? " AND id_toko = ?" : "") +
                " GROUP BY tahun ORDER BY tahun ASC";
        List<OmzetBulanan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bulan);
            ps.setInt(2, tahunBatas);
            if (idToko != null) {
                ps.setInt(3, idToko);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OmzetBulanan o = new OmzetBulanan();
                    o.setTahun(rs.getInt("tahun"));
                    o.setBulan(bulan);
                    o.setTotalOmzet(BigDecimal.valueOf(rs.getLong("total")));
                    list.add(o);
                }
            }
        }
        return list;
    }

    /** Semua baris rekap (join nama toko & metode), untuk ditampilkan di tabel. tahun = null berarti semua tahun. */
    public List<RekapMetodeBaris> findAll(Integer tahun) throws SQLException {
        String sql = "SELECT t.kode_toko, t.nama_toko, m.nama_metode, r.tahun, r.bulan, r.jumlah_transaksi " +
                "FROM rekap_metode_bulanan r " +
                "JOIN toko t ON t.id_toko = r.id_toko " +
                "JOIN metode_bayar m ON m.id_metode = r.id_metode " +
                (tahun != null ? "WHERE r.tahun = ? " : "") +
                "ORDER BY t.kode_toko, r.tahun, r.bulan, m.urutan";
        List<RekapMetodeBaris> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (tahun != null) {
                ps.setInt(1, tahun);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RekapMetodeBaris b = new RekapMetodeBaris();
                    b.setKodeToko(rs.getString("kode_toko"));
                    b.setNamaToko(rs.getString("nama_toko"));
                    b.setNamaMetode(rs.getString("nama_metode"));
                    b.setTahun(rs.getInt("tahun"));
                    b.setBulan(rs.getInt("bulan"));
                    b.setJumlahTransaksi(rs.getInt("jumlah_transaksi"));
                    list.add(b);
                }
            }
        }
        return list;
    }

    /** Hapus satu baris rekap berdasarkan kode toko + nama metode + periode (dipakai form Kelola manual). */
    public void delete(String kodeToko, String namaMetode, int tahun, int bulan) throws SQLException {
        String sql = "DELETE r FROM rekap_metode_bulanan r " +
                "JOIN toko t ON t.id_toko = r.id_toko " +
                "JOIN metode_bayar m ON m.id_metode = r.id_metode " +
                "WHERE t.kode_toko = ? AND m.nama_metode = ? AND r.tahun = ? AND r.bulan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeToko);
            ps.setString(2, namaMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            ps.executeUpdate();
        }
    }
}

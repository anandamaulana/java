package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.OmzetBulanan;
import com.theplayzone.prediksi.model.RekapTokoBulanan;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Akses data rekap_metode_bulanan: jumlah_transaksi (hitungan) & total_omzet (Rupiah) per toko x metode x bulan. */
public class RekapMetodeDAO {

    /** Insert/update (timpa) jumlah transaksi satu toko x metode x periode -- dipakai import rekap konsolidasi. */
    public void upsertJumlah(int idToko, int idMetode, int tahun, int bulan, int jumlahTransaksi, Integer idImport) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            upsertJumlah(conn, idToko, idMetode, tahun, bulan, jumlahTransaksi, idImport);
        }
    }

    /** Sama seperti {@link #upsertJumlah(int, int, int, int, int, Integer)}, memakai Connection eksternal (untuk transaksi atomik). */
    public void upsertJumlah(Connection conn, int idToko, int idMetode, int tahun, int bulan, int jumlahTransaksi, Integer idImport) throws SQLException {
        String sql = "INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, jumlah_transaksi, id_import) " +
                "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE jumlah_transaksi = VALUES(jumlah_transaksi), id_import = VALUES(id_import)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
     * Insert, atau AKUMULASI (tambahkan ke nilai yang sudah ada) jika kombinasi toko x metode x periode
     * sudah ada -- dipakai upload Detail Transaksi per Toko & Metode (menjumlahkan baris transaksi baru
     * ke total yang sudah tercatat, bukan menimpanya).
     */
    public void tambahJumlah(int idToko, int idMetode, int tahun, int bulan, int tambahan, Integer idImport) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            tambahJumlah(conn, idToko, idMetode, tahun, bulan, tambahan, idImport);
        }
    }

    /** Sama seperti {@link #tambahJumlah(int, int, int, int, int, Integer)}, memakai Connection eksternal. */
    public void tambahJumlah(Connection conn, int idToko, int idMetode, int tahun, int bulan, int tambahan, Integer idImport) throws SQLException {
        String sql = "INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, jumlah_transaksi, id_import) " +
                "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE jumlah_transaksi = jumlah_transaksi + VALUES(jumlah_transaksi), id_import = VALUES(id_import)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.setInt(2, idMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            ps.setInt(5, tambahan);
            if (idImport == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idImport);
            }
            ps.executeUpdate();
        }
    }

    /** Insert/update (timpa) nominal omzet Rupiah satu toko x metode x periode -- dipakai form entri manual. */
    public void upsertOmzet(int idToko, int idMetode, int tahun, int bulan, BigDecimal nominal, Integer idImport) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            upsertOmzet(conn, idToko, idMetode, tahun, bulan, nominal, idImport);
        }
    }

    /** Sama seperti {@link #upsertOmzet(int, int, int, int, BigDecimal, Integer)}, memakai Connection eksternal. */
    public void upsertOmzet(Connection conn, int idToko, int idMetode, int tahun, int bulan, BigDecimal nominal, Integer idImport) throws SQLException {
        String sql = "INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, total_omzet, id_import) " +
                "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE total_omzet = VALUES(total_omzet), id_import = VALUES(id_import)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.setInt(2, idMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            ps.setBigDecimal(5, nominal);
            if (idImport == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idImport);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Insert, atau AKUMULASI (tambahkan ke nilai yang sudah ada) nominal omzet Rupiah jika kombinasi
     * toko x metode x periode sudah ada -- dipakai upload grid Rekap_Transaksi_Toko (mengikuti istilah
     * "mengakumulasi total pendapatan (omzet) bulanan" pada skripsi Bab IV Tabel 4.8).
     */
    public void tambahOmzet(Connection conn, int idToko, int idMetode, int tahun, int bulan, BigDecimal nominal, Integer idImport) throws SQLException {
        String sql = "INSERT INTO rekap_metode_bulanan (id_toko, id_metode, tahun, bulan, total_omzet, id_import) " +
                "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE total_omzet = total_omzet + VALUES(total_omzet), id_import = VALUES(id_import)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.setInt(2, idMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            ps.setBigDecimal(5, nominal);
            if (idImport == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idImport);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Total omzet Rupiah (semua metode dijumlahkan) per tahun, untuk bulan tertentu, sebelum tahunBatas.
     * idToko = null berarti agregat Semua Toko. Dipakai sebagai data historis Year-over-Year oleh
     * RegresiLinearService untuk memprediksi omzet (bukan jumlah transaksi).
     */
    public List<OmzetBulanan> getTotalOmzetByBulan(Integer idToko, int bulan, int tahunBatas) throws SQLException {
        String sql = "SELECT tahun, SUM(total_omzet) AS total FROM rekap_metode_bulanan " +
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
                    BigDecimal total = rs.getBigDecimal("total");
                    o.setTotalOmzet(total == null ? BigDecimal.ZERO : total);
                    list.add(o);
                }
            }
        }
        return list;
    }

    /**
     * Ringkasan per toko x bulan (semua metode dijumlahkan): kode/nama/lokasi toko, periode,
     * total jumlah transaksi, dan total omzet Rupiah. Dipakai tabel & laporan PDF Rekap Transaksi Toko.
     * idToko = null berarti semua toko. tahunDari/tahunSampai = null berarti tanpa batas ke arah itu.
     */
    public List<RekapTokoBulanan> findRingkasanBulanan(Integer idToko, Integer tahunDari, Integer tahunSampai) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.kode_toko, t.nama_toko, t.lokasi_toko, r.tahun, r.bulan, " +
                "SUM(r.jumlah_transaksi) AS total_jumlah, SUM(r.total_omzet) AS total_omzet " +
                "FROM rekap_metode_bulanan r " +
                "JOIN toko t ON t.id_toko = r.id_toko WHERE 1=1");
        if (idToko != null) {
            sql.append(" AND r.id_toko = ?");
        }
        if (tahunDari != null) {
            sql.append(" AND r.tahun >= ?");
        }
        if (tahunSampai != null) {
            sql.append(" AND r.tahun <= ?");
        }
        sql.append(" GROUP BY t.kode_toko, t.nama_toko, t.lokasi_toko, r.tahun, r.bulan ORDER BY t.kode_toko, r.tahun, r.bulan");

        List<RekapTokoBulanan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (idToko != null) {
                ps.setInt(idx++, idToko);
            }
            if (tahunDari != null) {
                ps.setInt(idx++, tahunDari);
            }
            if (tahunSampai != null) {
                ps.setInt(idx, tahunSampai);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RekapTokoBulanan b = new RekapTokoBulanan();
                    b.setKodeToko(rs.getString("kode_toko"));
                    b.setNamaToko(rs.getString("nama_toko"));
                    b.setLokasiToko(rs.getString("lokasi_toko"));
                    b.setTahun(rs.getInt("tahun"));
                    b.setBulan(rs.getInt("bulan"));
                    b.setJumlahTransaksi(rs.getInt("total_jumlah"));
                    BigDecimal totalOmzet = rs.getBigDecimal("total_omzet");
                    b.setTotalOmzet(totalOmzet == null ? BigDecimal.ZERO : totalOmzet);
                    list.add(b);
                }
            }
        }
        return list;
    }

    /** Jumlah transaksi saat ini untuk satu kombinasi toko x metode x periode (0 jika belum ada baris). */
    public int getJumlah(int idToko, int idMetode, int tahun, int bulan) throws SQLException {
        String sql = "SELECT jumlah_transaksi FROM rekap_metode_bulanan WHERE id_toko=? AND id_metode=? AND tahun=? AND bulan=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToko);
            ps.setInt(2, idMetode);
            ps.setInt(3, tahun);
            ps.setInt(4, bulan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Hapus SEMUA baris rekap (semua metode) milik satu toko x periode -- dipakai "Hapus Terpilih" pada tabel ringkasan. */
    public void deleteByTokoPeriode(String kodeToko, int tahun, int bulan) throws SQLException {
        String sql = "DELETE r FROM rekap_metode_bulanan r " +
                "JOIN toko t ON t.id_toko = r.id_toko " +
                "WHERE t.kode_toko = ? AND r.tahun = ? AND r.bulan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeToko);
            ps.setInt(2, tahun);
            ps.setInt(3, bulan);
            ps.executeUpdate();
        }
    }
}

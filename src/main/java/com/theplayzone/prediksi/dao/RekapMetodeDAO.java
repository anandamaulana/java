package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.OmzetBulanan;

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
}

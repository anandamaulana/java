package com.theplayzone.prediksi.dao;

import com.theplayzone.prediksi.koneksi.DatabaseConnection;
import com.theplayzone.prediksi.model.HasilPrediksi;
import com.theplayzone.prediksi.model.PrediksiResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrediksiDAO {

    public long simpan(PrediksiResult r, int idUser) throws SQLException {
        String sql = "INSERT INTO log_prediksi (bulan_target, tahun_target, id_toko, jumlah_data_n, konstanta_a, " +
                "koefisien_b, nilai_prediksi, mape_persen, id_user) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBulanTarget());
            ps.setInt(2, r.getTahunTarget());
            if (r.getIdToko() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, r.getIdToko());
            }
            ps.setInt(4, r.getJumlahDataN());
            ps.setDouble(5, r.getKonstantaA());
            ps.setDouble(6, r.getKoefisienB());
            ps.setDouble(7, r.getNilaiPrediksi());
            ps.setDouble(8, r.getMapePersen());
            ps.setInt(9, idUser);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    public List<HasilPrediksi> findRiwayat() throws SQLException {
        return findRiwayat(null, null, null);
    }

    /**
     * idToko = null berarti semua toko (termasuk hasil agregat Semua Toko).
     * tahunDari/tahunSampai = null berarti tanpa batas ke arah itu (mis. tahunDari=null & tahunSampai=2024 berarti "s.d. 2024").
     */
    public List<HasilPrediksi> findRiwayat(Integer idToko, Integer tahunDari, Integer tahunSampai) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT hp.id_prediksi, hp.bulan_target, hp.tahun_target, hp.id_toko, hp.jumlah_data_n, " +
                "hp.konstanta_a, hp.koefisien_b, hp.nilai_prediksi, hp.mape_persen, hp.id_user, " +
                "hp.tanggal_proses, u.nama_lengkap, t.nama_toko FROM log_prediksi hp " +
                "LEFT JOIN users u ON u.id_user = hp.id_user " +
                "LEFT JOIN toko t ON t.id_toko = hp.id_toko WHERE 1=1");
        if (idToko != null) {
            sql.append(" AND hp.id_toko = ?");
        }
        if (tahunDari != null) {
            sql.append(" AND hp.tahun_target >= ?");
        }
        if (tahunSampai != null) {
            sql.append(" AND hp.tahun_target <= ?");
        }
        sql.append(" ORDER BY hp.tanggal_proses DESC");

        List<HasilPrediksi> list = new ArrayList<>();
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
                    HasilPrediksi h = new HasilPrediksi();
                    h.setIdPrediksi(rs.getInt("id_prediksi"));
                    h.setBulanTarget(rs.getInt("bulan_target"));
                    h.setTahunTarget(rs.getInt("tahun_target"));
                    int idTokoRow = rs.getInt("id_toko");
                    h.setIdToko(rs.wasNull() ? null : idTokoRow);
                    h.setNamaToko(rs.getString("nama_toko"));
                    h.setJumlahDataN(rs.getInt("jumlah_data_n"));
                    h.setKonstantaA(rs.getDouble("konstanta_a"));
                    h.setKoefisienB(rs.getDouble("koefisien_b"));
                    h.setNilaiPrediksi(rs.getDouble("nilai_prediksi"));
                    h.setMapePersen(rs.getDouble("mape_persen"));
                    h.setIdUser(rs.getInt("id_user"));
                    h.setTanggalProses(rs.getTimestamp("tanggal_proses").toLocalDateTime());
                    String namaUser = rs.getString("nama_lengkap");
                    h.setNamaUser(namaUser == null ? "(Pengguna Dihapus)" : namaUser);
                    list.add(h);
                }
            }
        }
        return list;
    }

    public void delete(int idPrediksi) throws SQLException {
        String sql = "DELETE FROM log_prediksi WHERE id_prediksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrediksi);
            ps.executeUpdate();
        }
    }
}

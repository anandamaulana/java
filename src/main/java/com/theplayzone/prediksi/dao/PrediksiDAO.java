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
        String sql = "INSERT INTO hasil_prediksi (bulan_target, tahun_target, jumlah_data_n, konstanta_a, " +
                "koefisien_b, nilai_prediksi, mape_persen, id_user) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBulanTarget());
            ps.setInt(2, r.getTahunTarget());
            ps.setInt(3, r.getJumlahDataN());
            ps.setDouble(4, r.getKonstantaA());
            ps.setDouble(5, r.getKoefisienB());
            ps.setDouble(6, r.getNilaiPrediksi());
            ps.setDouble(7, r.getMapePersen());
            ps.setInt(8, idUser);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    public List<HasilPrediksi> findRiwayat() throws SQLException {
        String sql = "SELECT hp.id_prediksi, hp.bulan_target, hp.tahun_target, hp.jumlah_data_n, " +
                "hp.konstanta_a, hp.koefisien_b, hp.nilai_prediksi, hp.mape_persen, hp.id_user, " +
                "hp.tanggal_proses, u.nama_lengkap FROM hasil_prediksi hp " +
                "JOIN users u ON u.id_user = hp.id_user ORDER BY hp.tanggal_proses DESC";
        List<HasilPrediksi> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HasilPrediksi h = new HasilPrediksi();
                h.setIdPrediksi(rs.getInt("id_prediksi"));
                h.setBulanTarget(rs.getInt("bulan_target"));
                h.setTahunTarget(rs.getInt("tahun_target"));
                h.setJumlahDataN(rs.getInt("jumlah_data_n"));
                h.setKonstantaA(rs.getDouble("konstanta_a"));
                h.setKoefisienB(rs.getDouble("koefisien_b"));
                h.setNilaiPrediksi(rs.getDouble("nilai_prediksi"));
                h.setMapePersen(rs.getDouble("mape_persen"));
                h.setIdUser(rs.getInt("id_user"));
                h.setTanggalProses(rs.getTimestamp("tanggal_proses").toLocalDateTime());
                h.setNamaUser(rs.getString("nama_lengkap"));
                list.add(h);
            }
        }
        return list;
    }
}

package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.RekapMetodeDAO;
import com.theplayzone.prediksi.model.OmzetBulanan;
import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.TitikRegresi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi Regresi Linear Sederhana (Least Square Method) dengan pembanding data musiman
 * (Year-over-Year): X = urutan tahun ke-n untuk bulan yang sama, Y = total transaksi bulan tersebut
 * (per toko, atau agregat Semua Toko jika idToko null). Y = a + bX, dengan:
 *   b = (n.SUM(XY) - SUM(X).SUM(Y)) / (n.SUM(X^2) - SUM(X)^2)
 *   a = (SUM(Y) - b.SUM(X)) / n
 */
public class RegresiLinearService {

    private final RekapMetodeDAO rekapMetodeDAO = new RekapMetodeDAO();

    public PrediksiResult prediksi(int bulanTarget, int tahunTarget, Integer idToko) throws SQLException {
        List<OmzetBulanan> historis = rekapMetodeDAO.getTotalTransaksiByBulan(idToko, bulanTarget, tahunTarget);
        int n = historis.size();
        if (n < 2) {
            throw new IllegalStateException("Data historis bulan ini sebelum tahun " + tahunTarget +
                    " belum cukup untuk regresi (minimal 2 tahun, tersedia: " + n + "). " +
                    "Lengkapi data lewat menu Import Rekap Toko Bulanan.");
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int x = 1;
        for (OmzetBulanan o : historis) {
            double y = o.getTotalOmzet().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += (double) x * x;
            x++;
        }

        double b = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double a = (sumY - b * sumX) / n;

        int xTarget = n + 1;
        double nilaiPrediksi = a + b * xTarget;

        List<TitikRegresi> titikList = new ArrayList<>();
        double[] aktual = new double[n];
        double[] fitting = new double[n];
        x = 1;
        for (OmzetBulanan o : historis) {
            double yAktual = o.getTotalOmzet().doubleValue();
            double yFitting = a + b * x;
            titikList.add(new TitikRegresi(x, o.getTahun(), yAktual, yFitting));
            aktual[x - 1] = yAktual;
            fitting[x - 1] = yFitting;
            x++;
        }

        double mape = MapeEvaluator.hitung(aktual, fitting);

        PrediksiResult result = new PrediksiResult();
        result.setBulanTarget(bulanTarget);
        result.setTahunTarget(tahunTarget);
        result.setIdToko(idToko);
        result.setJumlahDataN(n);
        result.setKonstantaA(a);
        result.setKoefisienB(b);
        result.setNilaiPrediksi(nilaiPrediksi);
        result.setMapePersen(mape);
        result.setTitikList(titikList);
        return result;
    }
}

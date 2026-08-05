package com.theplayzone.prediksi.util;

import com.theplayzone.prediksi.model.PrediksiResult;
import com.theplayzone.prediksi.model.TitikRegresi;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public final class ChartHelper {

    private ChartHelper() {
    }

    public static ChartPanel buatPanelTren(PrediksiResult hasil) {
        return new ChartPanel(buatChart(hasil));
    }

    public static JFreeChart buatChart(PrediksiResult hasil) {
        XYSeries dataAktual = new XYSeries("Transaksi Aktual (YoY)");
        XYSeries garisTren = new XYSeries("Garis Tren Regresi");

        for (TitikRegresi titik : hasil.getTitikList()) {
            dataAktual.add(titik.getX(), titik.getYAktual());
            garisTren.add(titik.getX(), titik.getYFitting());
        }

        int xTarget = hasil.getJumlahDataN() + 1;
        garisTren.add(xTarget, hasil.getNilaiPrediksi());

        XYSeries titikPrediksi = new XYSeries("Prediksi " + hasil.getTahunTarget());
        titikPrediksi.add(xTarget, hasil.getNilaiPrediksi());

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dataAktual);
        dataset.addSeries(garisTren);
        dataset.addSeries(titikPrediksi);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Tren & Prediksi Jumlah Transaksi (Regresi Linear)",
                "Urutan Periode (Tahun ke-n)",
                "Jumlah Transaksi",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        return chart;
    }
}

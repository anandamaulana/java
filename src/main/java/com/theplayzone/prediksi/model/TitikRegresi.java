package com.theplayzone.prediksi.model;

/** Satu titik data pada grafik tren: posisi urutan waktu (x), nilai aktual, dan nilai hasil fitting garis regresi. */
public class TitikRegresi {
    private final int x;
    private final int tahun;
    private final double yAktual;
    private final double yFitting;

    public TitikRegresi(int x, int tahun, double yAktual, double yFitting) {
        this.x = x;
        this.tahun = tahun;
        this.yAktual = yAktual;
        this.yFitting = yFitting;
    }

    public int getX() { return x; }
    public int getTahun() { return tahun; }
    public double getYAktual() { return yAktual; }
    public double getYFitting() { return yFitting; }
}

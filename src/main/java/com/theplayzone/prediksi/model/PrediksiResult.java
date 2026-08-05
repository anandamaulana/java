package com.theplayzone.prediksi.model;

import java.util.List;

/** Hasil satu kali proses Regresi Linear: konstanta, koefisien, angka prediksi, MAPE, dan titik-titik untuk grafik. */
public class PrediksiResult {
    private int bulanTarget;
    private int tahunTarget;
    private Integer idToko;
    private String namaToko;
    private int jumlahDataN;
    private double konstantaA;
    private double koefisienB;
    private double nilaiPrediksi;
    private double mapePersen;
    private List<TitikRegresi> titikList;

    public int getBulanTarget() { return bulanTarget; }
    public void setBulanTarget(int bulanTarget) { this.bulanTarget = bulanTarget; }

    public int getTahunTarget() { return tahunTarget; }
    public void setTahunTarget(int tahunTarget) { this.tahunTarget = tahunTarget; }

    public Integer getIdToko() { return idToko; }
    public void setIdToko(Integer idToko) { this.idToko = idToko; }

    public String getNamaToko() { return namaToko; }
    public void setNamaToko(String namaToko) { this.namaToko = namaToko; }

    public int getJumlahDataN() { return jumlahDataN; }
    public void setJumlahDataN(int jumlahDataN) { this.jumlahDataN = jumlahDataN; }

    public double getKonstantaA() { return konstantaA; }
    public void setKonstantaA(double konstantaA) { this.konstantaA = konstantaA; }

    public double getKoefisienB() { return koefisienB; }
    public void setKoefisienB(double koefisienB) { this.koefisienB = koefisienB; }

    public double getNilaiPrediksi() { return nilaiPrediksi; }
    public void setNilaiPrediksi(double nilaiPrediksi) { this.nilaiPrediksi = nilaiPrediksi; }

    public double getMapePersen() { return mapePersen; }
    public void setMapePersen(double mapePersen) { this.mapePersen = mapePersen; }

    public List<TitikRegresi> getTitikList() { return titikList; }
    public void setTitikList(List<TitikRegresi> titikList) { this.titikList = titikList; }
}

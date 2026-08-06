package com.theplayzone.prediksi.model;

import java.time.LocalDateTime;

/** Representasi baris tabel log_prediksi, dilengkapi nama pengguna (join) untuk keperluan tampilan laporan. */
public class HasilPrediksi {
    private int idPrediksi;
    private int bulanTarget;
    private int tahunTarget;
    private int jumlahDataN;
    private double konstantaA;
    private double koefisienB;
    private double nilaiPrediksi;
    private double mapePersen;
    private int idUser;
    private LocalDateTime tanggalProses;
    private String namaUser;
    private Integer idToko;
    private String namaToko;

    public int getIdPrediksi() { return idPrediksi; }
    public void setIdPrediksi(int idPrediksi) { this.idPrediksi = idPrediksi; }

    public int getBulanTarget() { return bulanTarget; }
    public void setBulanTarget(int bulanTarget) { this.bulanTarget = bulanTarget; }

    public int getTahunTarget() { return tahunTarget; }
    public void setTahunTarget(int tahunTarget) { this.tahunTarget = tahunTarget; }

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

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public LocalDateTime getTanggalProses() { return tanggalProses; }
    public void setTanggalProses(LocalDateTime tanggalProses) { this.tanggalProses = tanggalProses; }

    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }

    public Integer getIdToko() { return idToko; }
    public void setIdToko(Integer idToko) { this.idToko = idToko; }

    public String getNamaToko() { return namaToko; }
    public void setNamaToko(String namaToko) { this.namaToko = namaToko; }
}

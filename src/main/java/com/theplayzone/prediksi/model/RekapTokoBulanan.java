package com.theplayzone.prediksi.model;

import java.math.BigDecimal;

/** Ringkasan rekap_metode_bulanan per toko x bulan (semua metode dijumlahkan) untuk tabel/laporan. */
public class RekapTokoBulanan {
    private String kodeToko;
    private String namaToko;
    private String lokasiToko;
    private int tahun;
    private int bulan;
    private int jumlahTransaksi;
    private BigDecimal totalOmzet;

    public String getKodeToko() { return kodeToko; }
    public void setKodeToko(String kodeToko) { this.kodeToko = kodeToko; }

    public String getNamaToko() { return namaToko; }
    public void setNamaToko(String namaToko) { this.namaToko = namaToko; }

    public String getLokasiToko() { return lokasiToko; }
    public void setLokasiToko(String lokasiToko) { this.lokasiToko = lokasiToko; }

    public int getTahun() { return tahun; }
    public void setTahun(int tahun) { this.tahun = tahun; }

    public int getBulan() { return bulan; }
    public void setBulan(int bulan) { this.bulan = bulan; }

    public int getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(int jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }

    public BigDecimal getTotalOmzet() { return totalOmzet; }
    public void setTotalOmzet(BigDecimal totalOmzet) { this.totalOmzet = totalOmzet; }
}

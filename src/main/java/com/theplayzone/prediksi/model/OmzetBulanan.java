package com.theplayzone.prediksi.model;

import java.math.BigDecimal;

public class OmzetBulanan {
    private int idOmzet;
    private int tahun;
    private int bulan;
    private BigDecimal totalOmzet;

    public int getIdOmzet() { return idOmzet; }
    public void setIdOmzet(int idOmzet) { this.idOmzet = idOmzet; }

    public int getTahun() { return tahun; }
    public void setTahun(int tahun) { this.tahun = tahun; }

    public int getBulan() { return bulan; }
    public void setBulan(int bulan) { this.bulan = bulan; }

    public BigDecimal getTotalOmzet() { return totalOmzet; }
    public void setTotalOmzet(BigDecimal totalOmzet) { this.totalOmzet = totalOmzet; }
}

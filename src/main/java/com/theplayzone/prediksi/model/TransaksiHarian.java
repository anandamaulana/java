package com.theplayzone.prediksi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaksiHarian {
    private long idTransaksi;
    private LocalDate tanggal;
    private BigDecimal nominal;
    private String metodeBayar;
    private Integer idImport; // nullable, null jika input manual

    public long getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(long idTransaksi) { this.idTransaksi = idTransaksi; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public BigDecimal getNominal() { return nominal; }
    public void setNominal(BigDecimal nominal) { this.nominal = nominal; }

    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }

    public Integer getIdImport() { return idImport; }
    public void setIdImport(Integer idImport) { this.idImport = idImport; }
}

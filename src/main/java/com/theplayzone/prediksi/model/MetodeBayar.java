package com.theplayzone.prediksi.model;

public class MetodeBayar {
    private int idMetode;
    private int kodeMetode;
    private String namaMetode;
    private String kategori;
    private int urutan;
    private boolean aktif;

    public int getIdMetode() { return idMetode; }
    public void setIdMetode(int idMetode) { this.idMetode = idMetode; }

    public int getKodeMetode() { return kodeMetode; }
    public void setKodeMetode(int kodeMetode) { this.kodeMetode = kodeMetode; }

    public String getNamaMetode() { return namaMetode; }
    public void setNamaMetode(String namaMetode) { this.namaMetode = namaMetode; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getUrutan() { return urutan; }
    public void setUrutan(int urutan) { this.urutan = urutan; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
}

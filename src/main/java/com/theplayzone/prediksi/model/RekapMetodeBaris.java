package com.theplayzone.prediksi.model;

/** Satu baris rekap_metode_bulanan untuk ditampilkan di tabel (denormalisasi nama toko/metode). */
public class RekapMetodeBaris {
    private String kodeToko;
    private String namaToko;
    private String namaMetode;
    private int tahun;
    private int bulan;
    private int jumlahTransaksi;

    public String getKodeToko() { return kodeToko; }
    public void setKodeToko(String kodeToko) { this.kodeToko = kodeToko; }

    public String getNamaToko() { return namaToko; }
    public void setNamaToko(String namaToko) { this.namaToko = namaToko; }

    public String getNamaMetode() { return namaMetode; }
    public void setNamaMetode(String namaMetode) { this.namaMetode = namaMetode; }

    public int getTahun() { return tahun; }
    public void setTahun(int tahun) { this.tahun = tahun; }

    public int getBulan() { return bulan; }
    public void setBulan(int bulan) { this.bulan = bulan; }

    public int getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(int jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }
}

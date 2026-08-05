package com.theplayzone.prediksi.model;

public class Toko {
    private int idToko;
    private String kodeToko;
    private String namaToko;
    private String lokasiToko;

    public int getIdToko() { return idToko; }
    public void setIdToko(int idToko) { this.idToko = idToko; }

    public String getKodeToko() { return kodeToko; }
    public void setKodeToko(String kodeToko) { this.kodeToko = kodeToko; }

    public String getNamaToko() { return namaToko; }
    public void setNamaToko(String namaToko) { this.namaToko = namaToko; }

    public String getLokasiToko() { return lokasiToko; }
    public void setLokasiToko(String lokasiToko) { this.lokasiToko = lokasiToko; }

    @Override
    public String toString() {
        return kodeToko + " - " + namaToko;
    }
}

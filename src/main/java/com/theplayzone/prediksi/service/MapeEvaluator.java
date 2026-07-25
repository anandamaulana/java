package com.theplayzone.prediksi.service;

/** Mean Absolute Percentage Error: MAPE = (1/n) * SUM(|Xt - Ft| / Xt) * 100% */
public final class MapeEvaluator {

    private MapeEvaluator() {
    }

    public static double hitung(double[] aktual, double[] prediksi) {
        if (aktual.length != prediksi.length || aktual.length == 0) {
            throw new IllegalArgumentException("Panjang data aktual dan prediksi harus sama dan tidak boleh kosong");
        }
        double totalPersen = 0;
        int jumlahValid = 0;
        for (int i = 0; i < aktual.length; i++) {
            if (aktual[i] == 0) {
                continue; // hindari pembagian dengan nol
            }
            totalPersen += Math.abs((aktual[i] - prediksi[i]) / aktual[i]);
            jumlahValid++;
        }
        return jumlahValid == 0 ? 0 : (totalPersen / jumlahValid) * 100.0;
    }
}

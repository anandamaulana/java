package com.theplayzone.prediksi.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;

/** Palet warna & helper styling bertema The Play Zone (kuning-hitam). */
public final class AppTheme {

    public static final Color KUNING = new Color(0xF7, 0xB1, 0x18);
    public static final Color KUNING_HOVER = new Color(0xE0, 0x9E, 0x0A);
    public static final Color HITAM = new Color(0x1A, 0x1A, 0x1A);
    public static final Color PUTIH = Color.WHITE;
    public static final Color ABU_TERANG = new Color(0xF6, 0xF6, 0xF6);
    public static final Color ABU_GARIS = new Color(0xE0, 0xE0, 0xE0);

    private AppTheme() {
    }

    /** Tombol aksi utama (mis. Login, Proses Prediksi) dengan aksen kuning brand. */
    public static void terapkanTombolUtama(JButton button) {
        button.setBackground(KUNING);
        button.setForeground(HITAM);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
}

package com.theplayzone.prediksi.ui;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

/** Memuat logo & icon aplikasi dari classpath (src/main/resources/Image). */
public final class AppIcon {

    private static final String ICON_PATH = "/Image/app-icon.png";
    private static final String LOGO_PATH = "/Image/theplayzone-logo-putih.png";

    private AppIcon() {
    }

    /** Icon jendela/taskbar (lingkaran kuning "O" dari logo). */
    public static Image windowIcon() {
        ImageIcon icon = load(ICON_PATH);
        return icon == null ? null : icon.getImage();
    }

    /** Logo penuh "the play ZONE" (background putih, latar belakang menyatu di panel putih). */
    public static ImageIcon logo() {
        return load(LOGO_PATH);
    }

    /** Logo penuh, discale proporsional ke lebar tertentu (px). */
    public static ImageIcon logoScaled(int width) {
        ImageIcon original = logo();
        if (original == null) {
            return null;
        }
        int height = Math.round(width * (float) original.getIconHeight() / original.getIconWidth());
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static ImageIcon load(String path) {
        URL url = AppIcon.class.getResource(path);
        return url == null ? null : new ImageIcon(url);
    }
}

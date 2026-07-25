package com.theplayzone.prediksi;

import com.theplayzone.prediksi.ui.LoginForm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // biarkan tampilan default jika look and feel sistem tidak tersedia
            }
            new LoginForm().setVisible(true);
        });
    }
}

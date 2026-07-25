package com.theplayzone.prediksi.koneksi;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Membuka koneksi JDBC baru ke MySQL berdasarkan konfigurasi di db.properties (classpath).
 * Cocok untuk skala aplikasi desktop single-user; setiap operasi DAO membuka & menutup koneksinya sendiri.
 */
public final class DatabaseConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties tidak ditemukan di classpath");
            }
            PROPS.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Gagal memuat konfigurasi database", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = PROPS.getProperty("db.url");
        String user = PROPS.getProperty("db.user");
        String password = PROPS.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}

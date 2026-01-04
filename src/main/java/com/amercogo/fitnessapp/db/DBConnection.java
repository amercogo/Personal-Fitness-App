package com.amercogo.fitnessapp.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfig();
    }

    private DBConnection() {
        // utility class
    }

    private static void loadConfig() {
        Properties props = new Properties();

        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new RuntimeException("Ne mogu pronaći " + CONFIG_FILE + " u src/main/resources.");
            }

            props.load(in);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Nedostaju db.url / db.user / db.password u " + CONFIG_FILE);
            }

        } catch (IOException e) {
            throw new RuntimeException("Greška pri čitanju " + CONFIG_FILE, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

package com.myapp.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class H2DB {
    private static final String PROPS = "/database.properties";
    private static String URL;
    private static String USER;
    private static String PASS;

    static {
        Properties p = new Properties();
        try (InputStream in = H2DB.class.getResourceAsStream(PROPS)) {
            if (in == null) throw new IllegalStateException("No se encontró " + PROPS);
            p.load(in);
            URL  = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASS = p.getProperty("db.password");
            Class.forName(p.getProperty("db.driver")); // carga el driver
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private H2DB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

package com.myApp.logicaDeNegocio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class H2DB {

    private static final String URL  = "jdbc:h2:~/laboratorio"; // BD en disco (persistente)
    private static final String USER = "sa";                     // usuario H2 por defecto
    private static final String PASS = "";                       // contraseña por defecto
    private static final String DRIVER = "org.h2.Driver";

    static {
        try {
            Class.forName(DRIVER); // carga el driver
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private H2DB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

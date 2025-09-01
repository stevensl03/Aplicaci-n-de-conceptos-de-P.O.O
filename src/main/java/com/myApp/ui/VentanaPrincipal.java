package com.myApp.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class VentanaPrincipal {
    private VBox root;
    private Button btnRegistrar;

    public VentanaPrincipal() {
        Label titulo = new Label("Registro de Estudiantes");
        btnRegistrar = new Button("Abrir Formulario");

        root = new VBox(10, titulo, btnRegistrar);
    }

    public VBox getRoot() {
        return root;
    }

    public Button getBtnRegistrar() {
        return btnRegistrar;
    }
}

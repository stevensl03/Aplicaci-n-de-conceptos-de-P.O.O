package com.myApp.ui;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FormRegistro {
    private VBox root;
    private TextField txtNombre;
    private TextField txtEmail;
    private Button btnGuardar;

    public FormRegistro() {
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        txtEmail = new TextField();
        txtEmail.setPromptText("Email");

        btnGuardar = new Button("Guardar");

        root = new VBox(10, txtNombre, txtEmail, btnGuardar);
    }

    public VBox getRoot() { return root; }
    public TextField getTxtNombre() { return txtNombre; }
    public TextField getTxtEmail() { return txtEmail; }
    public Button getBtnGuardar() { return btnGuardar; }
}

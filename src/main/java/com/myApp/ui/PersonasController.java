package com.myApp.ui;

import com.myApp.logicaDeNegocio.InscripcionesPersonas;
import com.myApp.logicaDB.Schema;
import com.myApp.logicaDB.dao.ImpPersonaDAO;
import com.myApp.logicaDB.dao.PersonaDAO;
import com.myApp.logicaDB.repository.PersonaRepository;
import com.myApp.modelos.Persona;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class PersonasController {

    // Caso de uso (sin tocar la capa no modificable)
    private final InscripcionesPersonas useCase = new InscripcionesPersonas();

    // Tabla y backing data
    @FXML private TableView<Persona> tlbCursoProfesor; // fx:id original
    @FXML private TableColumn<Persona, Double> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, String> colEmail;
    private final ObservableList<Persona> dataPersonas = FXCollections.observableArrayList();

    // Formulario
    @FXML private TextField txtid;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;

    // Regex email
    private static final Pattern EMAIL_RX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @FXML
    void initialize() {
        initSchemaIfNeeded();
        bindTable();
        bindSelection();
        reload();
    }

    // ================= Handlers =================

    /** Crear o actualizar (upsert) */
    @FXML
    void crearPersona(ActionEvent event) {
        saveOrUpdate();
    }

    /** Compatibilidad con tu FXML (nombre con typo) */
    @FXML
    void actulizarPersona(ActionEvent event) {
        saveOrUpdate();
    }

    /** Eliminar seleccionado (persiste en BD y refresca) */
    @FXML
    void EliminarPersona(ActionEvent event) {
        Persona sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alertError("Selecciona una persona en la tabla.");
            return;
        }
        if (!confirm("¿Eliminar a " + sel.getNombres() + " " + sel.getApellidos() + "?")) return;

        try {
            // Persistir eliminación en BD usando las clases no modificables
            PersonaDAO dao = new ImpPersonaDAO();
            PersonaRepository repo = new PersonaRepository(dao);
            repo.eliminar(sel.getId());

            // Sincronizar y refrescar UI
            reload();
            clearForm();
            alertInfo("Persona eliminada.");
        } catch (Exception e) {
            alertError("No fue posible eliminar: " + e.getMessage());
        }
    }

    /** Volver al menú */
    @FXML
    void volverAtras(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            alertError("No fue posible regresar al menú.");
        }
    }

    // =============== Orquestación ===============

    private void saveOrUpdate() {
        // Validaciones
        String idTxt = trim(txtid.getText());
        if (!isNumeric(idTxt)) { alertError("El ID debe ser numérico."); return; }
        double id = Double.parseDouble(idTxt);

        String nombres = trim(txtNombre.getText());
        String apellidos = trim(txtApellido.getText());
        String email = trim(txtEmail.getText());

        if (nombres.isEmpty())   { alertError("El nombre es requerido."); return; }
        if (apellidos.isEmpty()) { alertError("El apellido es requerido."); return; }
        if (!EMAIL_RX.matcher(email).matches()) { alertError("Email inválido."); return; }

        // Colisión de id (si se intenta cambiar a uno ya existente distinto al seleccionado)
        Persona selected = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (existsDifferentPersonaWithId(id, selected)) {
            alertError("Ya existe una persona con el ID " + id + ".");
            return;
        }

        try {
            // Persistir con la sobrecarga correcta para evitar el bug de upsert(Persona)
            PersonaDAO dao = new ImpPersonaDAO();
            PersonaRepository repo = new PersonaRepository(dao);
            repo.upsert(id, nombres, apellidos, email); // <-- usa la versión sana

            // Mantener coherencia de la lista en memoria y refrescar
            reload();
            clearForm();

            alertInfo((selected == null) ? "Persona creada." : "Persona actualizada.");
        } catch (Exception e) {
            alertError("No fue posible guardar: " + e.getMessage());
        }
    }

    private void reload() {
        useCase.cargarDatosDesdeBD();
        List<Persona> personas = useCase.getListaPersonas();
        dataPersonas.setAll(personas);
        tlbCursoProfesor.refresh();
    }

    // =============== UI binding ===============

    private void bindTable() {
        tlbCursoProfesor.setItems(dataPersonas);
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colNombre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNombres()));
        colApellido.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getApellidos()));
        colEmail.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEmail()));
    }

    private void bindSelection() {
        tlbCursoProfesor.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, p) -> fillForm(p));
    }

    private void fillForm(Persona p) {
        if (p == null) {
            clearForm();
            return;
        }
        txtid.setText(String.valueOf(p.getId()));
        txtNombre.setText(p.getNombres());
        txtApellido.setText(p.getApellidos());
        txtEmail.setText(p.getEmail());
    }

    private void clearForm() {
        txtid.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        tlbCursoProfesor.getSelectionModel().clearSelection();
    }

    // =============== Helpers ===============

    private void initSchemaIfNeeded() {
        try { Schema.init(); } catch (Throwable ignored) { /* evita romper la UI si ya existe */ }
    }

    private boolean existsDifferentPersonaWithId(double id, Persona selected) {
        return useCase.getListaPersonas().stream()
                .anyMatch(p -> Double.compare(p.getId(), id) == 0 &&
                        (selected == null || !Objects.equals(p.getId(), selected.getId())));
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;
        try { Double.parseDouble(s.trim()); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    private void alertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void alertInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait();
        return a.getResult() == ButtonType.YES;
    }
}

package com.myApp.ui;

import com.myApp.logicaDeNegocio.InscripcionesPersonas;
import com.myApp.logicaDB.Schema;
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
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class OpcionesInscripcionesPersonasController{

    // --- Lógica de negocio
    private final InscripcionesPersonas inscripciones = new InscripcionesPersonas();

    // --- Tabla y backing data
    @FXML private TableView<Persona> tlbCursoProfesor; // dejo tu fx:id original
    @FXML private TableColumn<Persona, Double> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, String> colEmail;

    private final ObservableList<Persona> dataPersonas = FXCollections.observableArrayList();

    // --- Formulario
    @FXML private TextField txtid;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;

    // --- Inicialización
    @FXML
    void initialize() {
        // Inicializa H2/Schema si aplica
        try { Schema.init(); } catch (Throwable ignored) {}

        // Config tabla
        tlbCursoProfesor.setItems(dataPersonas);
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colNombre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNombres()));
        colApellido.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getApellidos()));
        colEmail.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEmail()));

        // Selección -> formulario
        tlbCursoProfesor.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, p) -> {
            if (p != null) {
                txtid.setText(String.valueOf(p.getId()));
                txtNombre.setText(p.getNombres());
                txtApellido.setText(p.getApellidos());
                txtEmail.setText(p.getEmail());
            }
        });

        // Cargar datos al iniciar
        recargarDesdeBDyLN();
    }

    // --- Navegar atrás
    @FXML
    void OptionAtras(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alertError("No fue posible regresar al menú.");
        }
    }

    // --- Crear (si id no existe) o Actualizar (si id existe)
    @FXML
    void OptionCrear(ActionEvent event) {
        // Validaciones
        Double id = parseId(txtid.getText());
        if (id == null) { alertError("El ID debe ser numérico."); return; }

        String nombres   = safeTrim(txtNombre.getText());
        String apellidos = safeTrim(txtApellido.getText());
        String email     = safeTrim(txtEmail.getText());

        if (nombres.isEmpty())   { alertError("El nombre es requerido."); return; }
        if (apellidos.isEmpty()) { alertError("El apellido es requerido."); return; }
        if (!esEmailValido(email)) { alertError("Email inválido."); return; }

        // Sincroniza LN con BD
        inscripciones.cargarDatosDesdeBD();

        // ¿Existe ya ese id?
        Persona existente = buscarEnListadoInternoPorId(id);

        Persona nueva = new Persona(id, nombres, apellidos, email);

        if (existente != null) {
            // REMOVER instancia exacta para evitar duplicados (no hay equals por id)
            try { inscripciones.eliminar(existente); } catch (Throwable ignored) {}
            // Guardar (MERGE) y volver a inscribir en memoria
            inscripciones.guardarInformacion(nueva);
            try { inscripciones.inscribir(nueva); } catch (Throwable ignored) {}

            recargarDesdeBDyLN();
            alertInfo("Persona actualizada.");
        } else {
            // Guardar y registrar en memoria
            inscripciones.guardarInformacion(nueva);
            try { inscripciones.inscribir(nueva); } catch (Throwable ignored) {}

            recargarDesdeBDyLN();
            alertInfo("Persona creada.");
        }

        limpiarFormulario();
    }

    // --- Eliminar seleccionado
    @FXML
    void OptionEliminar(ActionEvent event) {
        Persona sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una persona en la tabla."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + sel.getNombres() + " " + sel.getApellidos() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        // Sincroniza y busca la MISMA instancia por id en la lista interna
        inscripciones.cargarDatosDesdeBD();
        Persona interno = buscarEnListadoInternoPorId(sel.getId());
        if (interno != null) {
            try { inscripciones.eliminar(interno); } catch (Throwable ignored) {}
        } else {
            // fallback: intentar con sel (si por casualidad es la misma referencia)
            try { inscripciones.eliminar(sel); } catch (Throwable ignored) {}
        }

        // Refrescar UI
        recargarDesdeBDyLN();
        limpiarFormulario();
        alertInfo("Persona eliminada.");
    }

    // --- Actualizar desde formulario a la persona seleccionada
    @FXML
    void OtionActuaizar(ActionEvent event) {
        Persona sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una persona en la tabla."); return; }

        Double id = parseId(txtid.getText());
        if (id == null) { alertError("El ID debe ser numérico."); return; }

        String nombres   = safeTrim(txtNombre.getText());
        String apellidos = safeTrim(txtApellido.getText());
        String email     = safeTrim(txtEmail.getText());

        if (nombres.isEmpty())   { alertError("El nombre es requerido."); return; }
        if (apellidos.isEmpty()) { alertError("El apellido es requerido."); return; }
        if (!esEmailValido(email)) { alertError("Email inválido."); return; }

        inscripciones.cargarDatosDesdeBD();

        // Evitar colisión de IDs (si cambiaste el id a uno ya existente diferente al seleccionado)
        Persona conflicto = buscarEnListadoInternoPorId(id);
        if (conflicto != null && conflicto != buscarEnListadoInternoPorId(sel.getId())) {
            alertError("Ya existe una persona con el ID " + id + ".");
            return;
        }

        // Eliminar instancia actual y guardar nueva
        Persona instanciaActual = buscarEnListadoInternoPorId(sel.getId());
        if (instanciaActual != null) {
            try { inscripciones.eliminar(instanciaActual); } catch (Throwable ignored) {}
        }

        Persona actualizada = new Persona(id, nombres, apellidos, email);
        inscripciones.guardarInformacion(actualizada);
        try { inscripciones.inscribir(actualizada); } catch (Throwable ignored) {}

        recargarDesdeBDyLN();
        limpiarFormulario();
        alertInfo("Persona actualizada.");
    }

    // ===== Helpers =====

    private void recargarDesdeBDyLN() {
        try { inscripciones.cargarDatosDesdeBD(); } catch (Throwable ignored) {}
        dataPersonas.setAll(inscripciones.getListaPersonas());  // <--- esta es la clave
        tlbCursoProfesor.refresh();
    }


    @SuppressWarnings("unchecked")
    private List<Persona> obtenerListaDesdeLN() {
        try {
            Method m = inscripciones.getClass().getMethod("getListaPersonas");
            Object r = m.invoke(inscripciones);
            if (r instanceof List<?>) {
                // filtrar por tipo por seguridad
                List<?> raw = (List<?>) r;
                List<Persona> cast = new ArrayList<>();
                for (Object o : raw) if (o instanceof Persona) cast.add((Persona) o);
                return cast;
            }
        } catch (Throwable ignored) { }
        // Si no existe el getter, devolvemos lo que hay actualmente en la tabla
        return new ArrayList<>(dataPersonas);
    }

    private Persona buscarEnListadoInternoPorId(double id) {
        for (Persona p : obtenerListaDesdeLN()) {
            if (Double.compare(p.getId(), id) == 0) return p;
        }
        return null;
    }

    private void limpiarFormulario() {
        txtid.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        tlbCursoProfesor.getSelectionModel().clearSelection();
    }

    private String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private Double parseId(String txt) {
        try { return Double.parseDouble(safeTrim(txt)); }
        catch (NumberFormatException e) { return null; }
    }

    private static final Pattern EMAIL_RX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private boolean esEmailValido(String email) { return EMAIL_RX.matcher(email).matches(); }

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
}

package com.myApp.ui;

import com.myApp.logicaDB.H2DB;
import com.myApp.logicaDB.Schema;
import com.myApp.logicaDB.dao.ImpCursoProfesorDAO;
import com.myApp.logicaDB.repository.CursoProfesorRepository;
import com.myApp.modelos.Curso;
import com.myApp.modelos.CursoProfesor;
import com.myApp.modelos.Profesor;
import com.myApp.modelos.Programa;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoProfesorController {

    // ====== Tabla ======
    @FXML private TableColumn<CursoProfesor, Integer> colAnio;
    @FXML private TableColumn<CursoProfesor, Curso>   colCurso;
    @FXML private TableColumn<CursoProfesor, Profesor> colProfesor;
    @FXML private TableColumn<CursoProfesor, Integer> colSemestre;
    @FXML private TableView<CursoProfesor> tlbCursoProfesor;

    // ====== Formulario ======
    @FXML private ComboBox<Curso> selectCurso;
    @FXML private ComboBox<Profesor> selectProfesor;
    @FXML private TextField txtAnio;
    @FXML private TextField txtSemestre;

    // Catálogos en memoria para ComboBox
    private final javafx.collections.ObservableList<Curso> catalogoCursos =
            javafx.collections.FXCollections.observableArrayList();
    private final javafx.collections.ObservableList<Profesor> catalogoProfesores =
            javafx.collections.FXCollections.observableArrayList();

    // Backing list de la tabla
    private final javafx.collections.ObservableList<CursoProfesor> dataCursoProfesor =
            javafx.collections.FXCollections.observableArrayList();

    // ====== Init ======
    @FXML
    void initialize() {
        try { Schema.init(); } catch (Throwable ignored) {}
        bindTable();
        configureRenderers();
        bindSelection();
        loadCatalogs();
        reloadTable();
    }

    // ====== Handlers (mismos nombres que tu FXML) ======

    @FXML
    void GuardarCursoProfesor(ActionEvent event) {
        Curso curso = selectCurso.getValue();
        Profesor profesor = selectProfesor.getValue();
        if (curso == null) { alertError("Selecciona un curso."); return; }
        if (profesor == null) { alertError("Selecciona un profesor."); return; }

        Integer anio = parseInt(txtAnio.getText(), "El año debe ser un entero.");
        if (anio == null) return;

        Integer semestre = parseInt(txtSemestre.getText(), "El semestre debe ser 1 o 2.");
        if (semestre == null || semestre < 1 || semestre > 2) { alertError("El semestre debe ser 1 o 2."); return; }

        CursoProfesor cp = new CursoProfesor(profesor, anio, semestre, curso);

        try {
            var repo = new CursoProfesorRepository(new ImpCursoProfesorDAO());
            boolean ok = repo.registrar(cp); // evita duplicados por PK
            if (!ok) { alertError("Ya existe esa asignación en ese período."); return; }
            reloadTable();
            clearForm();
            alertInfo("Asignación creada.");
        } catch (Exception e) {
            alertError("No fue posible guardar: " + e.getMessage());
        }
    }

    @FXML
    void MostrarListaCursos(ActionEvent event) {
        // no-op (compatibilidad con tu FXML). Útil si quieres reaccionar al cambio del ComboBox.
    }

    @FXML
    void mostrarListaProfesores(ActionEvent event) {
        // no-op (compatibilidad con tu FXML). Útil si quieres reaccionar al cambio del ComboBox.
    }

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

    // ====== UI wiring ======

    private void bindTable() {
        tlbCursoProfesor.setItems(dataCursoProfesor);
        colAnio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAnio()));
        colSemestre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSemestre()));
        colCurso.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCurso()));
        colProfesor.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getProfesor()));
    }

    private void configureRenderers() {
        // Tabla: Curso
        colCurso.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : (str(c.getNombre(), "Curso #" + c.getId())));
            }
        });
        // Tabla: Profesor
        colProfesor.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Profesor p, boolean empty) {
                super.updateItem(p, empty);
                String etiqueta = (p == null) ? "" :
                        (str(p.getNombre(), "Profesor #" + (long) p.getId()));
                setText(empty ? "" : etiqueta);
            }
        });

        // ComboBox: cursos
        selectCurso.setItems(catalogoCursos);
        selectCurso.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : str(c.getNombre(), "Curso #" + c.getId()));
            }
        });
        selectCurso.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : str(c.getNombre(), "Curso #" + c.getId()));
            }
        });

        // ComboBox: profesores
        selectProfesor.setItems(catalogoProfesores);
        selectProfesor.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Profesor p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : str(p.getNombre(), "Profesor #" + (long) p.getId()));
            }
        });
        selectProfesor.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Profesor p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : str(p.getNombre(), "Profesor #" + (long) p.getId()));
            }
        });
    }

    private void bindSelection() {
        tlbCursoProfesor.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, cp) -> {
            if (cp == null) { clearForm(); return; }
            if (cp.getCurso() != null) selectCursoById(cp.getCurso().getId());
            if (cp.getProfesor() != null) selectProfesorById((long) cp.getProfesor().getId());
            txtAnio.setText(String.valueOf(cp.getAnio()));
            txtSemestre.setText(String.valueOf(cp.getSemestre()));
        });
    }

    private void clearForm() {
        selectCurso.getSelectionModel().clearSelection();
        selectProfesor.getSelectionModel().clearSelection();
        txtAnio.clear();
        txtSemestre.clear();
        tlbCursoProfesor.getSelectionModel().clearSelection();
    }

    // ====== Catálogos y datos ======

    private void loadCatalogs() {
        cargarCursosDesdeBD();
        cargarProfesoresDesdeBD();
    }

    private void reloadTable() {
        try {
            var repo = new CursoProfesorRepository(new ImpCursoProfesorDAO());
            dataCursoProfesor.setAll(repo.listarTodo());
            tlbCursoProfesor.refresh();
        } catch (Exception e) {
            alertError("No fue posible cargar asignaciones: " + e.getMessage());
        }
    }

    private void cargarCursosDesdeBD() {
        final String SQL = """
            SELECT c.id, c.nombre, c.activo, c.programa_id, p.id, p.nombre
            FROM CURSO c
            LEFT JOIN PROGRAMA p ON p.id = c.programa_id
            ORDER BY c.nombre
        """;
        var tmp = new ArrayList<Curso>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Programa prog = new Programa(
                        rs.getDouble(5),
                        rs.getString(6),
                        0.0,
                        null,
                        null
                );
                tmp.add(new Curso(
                        rs.getInt(1),
                        rs.getString(2),
                        prog,
                        rs.getBoolean(3)
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cargando cursos: " + e.getMessage());
        }
        catalogoCursos.setAll(tmp);
    }

    private void cargarProfesoresDesdeBD() {
        final String SQL = """
            SELECT pr.persona_id, pe.nombres, pe.apellidos, pr.tipo_contrato
            FROM PROFESOR pr
            JOIN PERSONA pe ON pe.id = pr.persona_id
            ORDER BY pe.apellidos, pe.nombres
        """;
        var tmp = new ArrayList<Profesor>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Profesor p = new Profesor();
                p.setId(rs.getDouble(1)); // persona_id
                String nombreCompleto = (rs.getString(2) + " " + rs.getString(3)).trim();
                try { p.setNombres(nombreCompleto); } catch (Throwable ignored) {} // si tu modelo lo soporta
                tmp.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cargando profesores: " + e.getMessage());
        }
        catalogoProfesores.setAll(tmp);
    }

    private void selectCursoById(int id) {
        for (Curso c : catalogoCursos) {
            if (c.getId() == id) { selectCurso.getSelectionModel().select(c); return; }
        }
        selectCurso.getSelectionModel().clearSelection();
    }

    private void selectProfesorById(long personaId) {
        for (Profesor p : catalogoProfesores) {
            if ((long) p.getId() == personaId) { selectProfesor.getSelectionModel().select(p); return; }
        }
        selectProfesor.getSelectionModel().clearSelection();
    }

    // ====== Helpers ======

    private Integer parseInt(String s, String errMsg) {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (NumberFormatException e) { alertError(errMsg); return null; }
    }

    private static String str(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

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

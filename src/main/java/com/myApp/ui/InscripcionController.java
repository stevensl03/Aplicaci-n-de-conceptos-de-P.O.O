package com.myApp.ui;

import com.myApp.logicaDeNegocio.CursosInscritos;
import com.myApp.logicaDB.H2DB;
import com.myApp.logicaDB.Schema;
import com.myApp.logicaDB.dao.ImpInscripcionDAO;
import com.myApp.logicaDB.dao.InscripcionDAO;
import com.myApp.logicaDB.repository.InscripcionRepository;
import com.myApp.modelos.Curso;
import com.myApp.modelos.Estudiante;
import com.myApp.modelos.Inscripcion;
import com.myApp.modelos.Programa;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InscripcionController {

    // ===== Caso de uso (no modificable) =====
    private final CursosInscritos useCase = new CursosInscritos();

    // ===== Tabla =====
    @FXML private TableColumn<Inscripcion, Integer> colAnio;
    @FXML private TableColumn<Inscripcion, Curso>   colCurso;
    @FXML private TableColumn<Inscripcion, Estudiante> colEstudiante;
    @FXML private TableColumn<Inscripcion, Integer> colSemestre;
    @FXML private TableView<Inscripcion> tlbCursoProfesor;

    private final ObservableList<Inscripcion> dataInscripciones = FXCollections.observableArrayList();

    // ===== Formulario =====
    @FXML private ComboBox<Curso> cursopv;
    @FXML private ComboBox<Estudiante> estudianteops;
    @FXML private TextField txtAnio;
    @FXML private TextField txtSemestre;

    // Catálogos
    private final ObservableList<Curso> catalogoCursos = FXCollections.observableArrayList();
    private final ObservableList<Estudiante> catalogoEstudiantes = FXCollections.observableArrayList();

    // ===== Inicialización =====
    @FXML
    void initialize() {
        initSchemaIfNeeded();
        bindTable();
        configureRenderers();
        bindSelection();
        loadCatalogs();
        reloadTable();
    }

    // ============================================================
    // Handlers
    // ============================================================

    @FXML
    void CrearCursosInscritos(ActionEvent event) {
        Optional<Inscripcion> insOpt = buildFromForm();
        if (insOpt.isEmpty()) return;

        Inscripcion nueva = insOpt.get();

        try {
            // Persistencia (evita duplicados en BD)
            InscripcionRepository repo = new InscripcionRepository(new ImpInscripcionDAO());
            boolean created = repo.registrar(nueva);
            if (!created) {
                alertError("Ya existe una inscripción con esa combinación (estudiante/curso/año/semestre).");
                return;
            }

            // Refrescar lista en memoria + UI
            reloadTable();
            clearForm();
            alertInfo("Inscripción creada.");
        } catch (Exception e) {
            alertError("No fue posible crear: " + e.getMessage());
        }
    }

    @FXML
    void ActualizarCursosInscritos(ActionEvent event) {
        Inscripcion sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una inscripción en la tabla."); return; }

        Optional<Inscripcion> insOpt = buildFromForm();
        if (insOpt.isEmpty()) return;
        Inscripcion nueva = insOpt.get();

        // Si la clave no cambia, no hay nada que “actualizar” (todas las columnas son PK)
        if (sameKey(sel, nueva)) {
            alertInfo("No hay cambios en la clave de inscripción.");
            return;
        }

        try {
            InscripcionRepository repo = new InscripcionRepository(new ImpInscripcionDAO());

            // Verificar colisión con otra fila distinta
            var yaExiste = repo.buscarUno(
                    nueva.getEstudiante().getCodigo(),
                    nueva.getCurso().getId(),
                    nueva.getAnio(),
                    nueva.getSemestre()
            );
            if (yaExiste.isPresent()) {
                alertError("Ya existe una inscripción con esa combinación (estudiante/curso/año/semestre).");
                return;
            }

            // “Mover” la inscripción (cambiar PK): borrar antigua + insertar nueva
            repo.desinscribir(sel);
            boolean ok = repo.registrar(nueva);
            if (!ok) {
                alertError("No fue posible actualizar (PK duplicada).");
                return;
            }

            // Refrescar
            reloadTable();
            clearForm();
            alertInfo("Inscripción actualizada.");
        } catch (Exception e) {
            alertError("No fue posible actualizar: " + e.getMessage());
        }
    }

    @FXML
    void EliminarCursosInscritos(ActionEvent event) {
        Inscripcion sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una inscripción en la tabla."); return; }

        String nombreEst = sel.getEstudiante() == null ? "estudiante"
                : sel.getEstudiante().getNombres() + " " + sel.getEstudiante().getApellidos();
        String nombreCurso = sel.getCurso() == null ? "curso" : sel.getCurso().getNombre();

        if (!confirm("¿Eliminar la inscripción de " + nombreEst + " a " + nombreCurso +
                " (" + sel.getAnio() + "-" + sel.getSemestre() + ")?")) return;

        try {
            // Borrar en BD
            InscripcionRepository repo = new InscripcionRepository(new ImpInscripcionDAO());
            repo.desinscribir(sel);

            // Refrescar
            reloadTable();
            clearForm();
            alertInfo("Inscripción eliminada.");
        } catch (Exception e) {
            alertError("No fue posible eliminar: " + e.getMessage());
        }
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

    @FXML void SelectorEstudiante(ActionEvent event) { /* opcional para reacciones en UI */ }
    @FXML void selectorcurso(ActionEvent event) { /* opcional para reacciones en UI */ }

    // ============================================================
    // Orquestación de UI
    // ============================================================

    private void bindTable() {
        tlbCursoProfesor.setItems(dataInscripciones);
        colAnio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAnio()));
        colSemestre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSemestre()));
        colCurso.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCurso()));
        colEstudiante.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEstudiante()));
    }

    private void configureRenderers() {
        // Tabla
        colCurso.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getNombre() + " (id=" + c.getId() + ")");
            }
        });
        colEstudiante.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Estudiante e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? "" : e.getNombres() + " " + e.getApellidos() + " (cod=" + (long)e.getCodigo() + ")");
            }
        });

        // ComboBox Cursos
        cursopv.setItems(catalogoCursos);
        cursopv.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getNombre());
            }
        });
        cursopv.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getNombre());
            }
        });

        // ComboBox Estudiantes
        estudianteops.setItems(catalogoEstudiantes);
        estudianteops.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Estudiante e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? "" : e.getNombres() + " " + e.getApellidos());
            }
        });
        estudianteops.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Estudiante e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? "" : e.getNombres() + " " + e.getApellidos());
            }
        });
    }

    private void bindSelection() {
        tlbCursoProfesor.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, ins) -> {
            if (ins == null) { clearForm(); return; }
            if (ins.getCurso() != null) selectCursoById(ins.getCurso().getId());
            if (ins.getEstudiante() != null) selectEstudianteByCodigo((long) ins.getEstudiante().getCodigo());
            txtAnio.setText(String.valueOf(ins.getAnio()));
            txtSemestre.setText(String.valueOf(ins.getSemestre()));
        });
    }

    private void reloadTable() {
        try { useCase.cargarDatos(); } catch (Throwable ignored) {}
        dataInscripciones.setAll(useCase.getInscripcionesLista());
        tlbCursoProfesor.refresh();
    }

    private void clearForm() {
        cursopv.getSelectionModel().clearSelection();
        estudianteops.getSelectionModel().clearSelection();
        txtAnio.clear();
        txtSemestre.clear();
        tlbCursoProfesor.getSelectionModel().clearSelection();
    }

    // ============================================================
    // Validación y construcción desde el formulario
    // ============================================================

    private Optional<Inscripcion> buildFromForm() {
        Curso curso = cursopv.getValue();
        Estudiante est = estudianteops.getValue();
        if (curso == null) { alertError("Selecciona un curso."); return Optional.empty(); }
        if (est == null) { alertError("Selecciona un estudiante."); return Optional.empty(); }

        Integer anio = parseInt(txtAnio.getText(), "El año debe ser entero.");
        if (anio == null) return Optional.empty();

        Integer semestre = parseInt(txtSemestre.getText(), "El semestre debe ser 1 o 2.");
        if (semestre == null || semestre < 1 || semestre > 2) {
            alertError("El semestre debe ser 1 o 2.");
            return Optional.empty();
        }

        return Optional.of(new Inscripcion(curso, anio, semestre, est));
    }

    private boolean sameKey(Inscripcion a, Inscripcion b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return (long) a.getEstudiante().getCodigo() == (long) b.getEstudiante().getCodigo()
                && a.getCurso().getId() == b.getCurso().getId()
                && a.getAnio() == b.getAnio()
                && a.getSemestre() == b.getSemestre();
    }

    private Integer parseInt(String s, String errMsg) {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (NumberFormatException e) { alertError(errMsg); return null; }
    }

    // ============================================================
    // Carga de catálogos (adapters mínimos JDBC)
    // ============================================================

    private void loadCatalogs() {
        cargarCursosDesdeBD();
        cargarEstudiantesDesdeBD();
    }

    private void cargarCursosDesdeBD() {
        final String SQL = """
            SELECT c.id, c.nombre, c.activo, c.programa_id, p.id, p.nombre
            FROM CURSO c
            LEFT JOIN PROGRAMA p ON p.id = c.programa_id
            ORDER BY c.nombre
        """;
        List<Curso> tmp = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Programa prog = new Programa(
                        rs.getDouble(5),      // programa.id (puede ser null → 0.0)
                        rs.getString(6),      // programa.nombre
                        0.0,
                        null,
                        null
                );
                Curso c = new Curso(
                        rs.getInt(1),
                        rs.getString(2),
                        prog,                 // puede ser “vacío” si no hay programa
                        rs.getBoolean(3)
                );
                tmp.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cargando cursos: " + e.getMessage());
        }
        catalogoCursos.setAll(tmp);
    }

    private void cargarEstudiantesDesdeBD() {
        final String SQL = """
            SELECT
                p.id, p.nombres, p.apellidos, p.email,
                e.codigo, e.programa_id, e.activo, e.promedio,
                pr.id, pr.nombre
            FROM ESTUDIANTE e
            JOIN PERSONA p ON p.id = e.persona_id
            LEFT JOIN PROGRAMA pr ON pr.id = e.programa_id
            ORDER BY p.apellidos, p.nombres
        """;
        List<Estudiante> tmp = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Programa prog = new Programa(
                        rs.getDouble(9),      // pr.id
                        rs.getString(10),     // pr.nombre
                        0.0,
                        null,
                        null
                );
                Estudiante e = new Estudiante(
                        rs.getDouble(1),      // persona.id
                        rs.getString(2),      // nombres
                        rs.getString(3),      // apellidos
                        rs.getString(4),      // email
                        rs.getDouble(5),      // codigo
                        prog,
                        rs.getBoolean(7),     // activo
                        rs.getDouble(8)       // promedio
                );
                tmp.add(e);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cargando estudiantes: " + e.getMessage());
        }
        catalogoEstudiantes.setAll(tmp);
    }

    private void selectCursoById(int id) {
        for (Curso c : catalogoCursos) {
            if (c.getId() == id) { cursopv.getSelectionModel().select(c); return; }
        }
        cursopv.getSelectionModel().clearSelection();
    }

    private void selectEstudianteByCodigo(long codigo) {
        for (Estudiante e : catalogoEstudiantes) {
            if ((long) e.getCodigo() == codigo) { estudianteops.getSelectionModel().select(e); return; }
        }
        estudianteops.getSelectionModel().clearSelection();
    }

    // ============================================================
    // Infra mínima
    // ============================================================

    private void initSchemaIfNeeded() {
        try { Schema.init(); } catch (Throwable ignored) { /* evitar romper UI si ya existe */ }
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

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait();
        return a.getResult() == ButtonType.YES;
    }
}

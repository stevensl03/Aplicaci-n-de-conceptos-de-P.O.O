package com.myApp.ui;

import com.myApp.logicaDeNegocio.CursosInscritos;
import com.myApp.logicaDeNegocio.H2DB;
import com.myApp.logicaDeNegocio.Schema;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class OpcionesCursosInscriptosController {

    // ----- Lógica de negocio
    private final CursosInscritos cursosInscritos = new CursosInscritos();

    // ----- Tabla y columnas
    @FXML private TableColumn<Inscripcion, Integer> colAnio;
    @FXML private TableColumn<Inscripcion, Curso>   colCurso;
    @FXML private TableColumn<Inscripcion, Estudiante> colEstudiante;
    @FXML private TableColumn<Inscripcion, Integer> colSemestre;
    @FXML private TableView<Inscripcion> tlbCursoProfesor;

    private final ObservableList<Inscripcion> dataInscripciones = FXCollections.observableArrayList();

    // ----- Formulario / Selectores
    @FXML private ComboBox<Curso> cursopv;
    @FXML private ComboBox<Estudiante> estudianteops;
    @FXML private TextField txtAnio;
    @FXML private TextField txtSemestre;

    // Catálogos
    private final ObservableList<Curso> catalogoCursos = FXCollections.observableArrayList();
    private final ObservableList<Estudiante> catalogoEstudiantes = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        try { Schema.init(); } catch (Throwable ignored) {}

        // Tabla
        tlbCursoProfesor.setItems(dataInscripciones);
        colAnio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAnio()));
        colSemestre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSemestre()));
        colCurso.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCurso()));
        colEstudiante.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEstudiante()));

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

        // Catálogos en ComboBox
        cursopv.setItems(catalogoCursos);
        estudianteops.setItems(catalogoEstudiantes);

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

        // Selección de la tabla -> formulario (selecciona por CLAVE, no por instancia)
        tlbCursoProfesor.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, ins) -> {
            if (ins == null) return;
            if (ins.getCurso() != null) selectCursoById(ins.getCurso().getId());
            if (ins.getEstudiante() != null) selectEstudianteByCodigo((long) ins.getEstudiante().getCodigo());
            txtAnio.setText(String.valueOf(ins.getAnio()));
            txtSemestre.setText(String.valueOf(ins.getSemestre()));
        });

        // Cargar catálogos y tabla
        recargarCatalogosDesdeBD();
        recargarInscripcionesDesdeBD();
    }

    // ===== CRUD =====

    @FXML
    void CrearCursosInscritos(ActionEvent event) {
        Curso curso = cursopv.getValue();
        Estudiante est = estudianteops.getValue();
        if (curso == null) { alertError("Selecciona un curso."); return; }
        if (est == null) { alertError("Selecciona un estudiante."); return; }

        Integer anio = parseInt(txtAnio.getText(), "El año debe ser entero.");
        if (anio == null) return;
        Integer semestre = parseInt(txtSemestre.getText(), "El semestre debe ser 1 o 2.");
        if (semestre == null || semestre < 1 || semestre > 2) { alertError("El semestre debe ser 1 o 2."); return; }

        cursosInscritos.cargarDatos();

        Inscripcion nueva = new Inscripcion(curso, anio, semestre, est);
        Inscripcion existente = buscarPorClaveCompuesta(nueva);

        if (existente != null) {
            try { cursosInscritos.eliminar(existente); } catch (Throwable ignored) {}
            cursosInscritos.guardarInformacion(nueva);
            try { cursosInscritos.inscribirCurso(nueva); } catch (Throwable ignored) {}
            alertInfo("Inscripción actualizada.");
        } else {
            cursosInscritos.guardarInformacion(nueva);
            try { cursosInscritos.inscribirCurso(nueva); } catch (Throwable ignored) {}
            alertInfo("Inscripción creada.");
        }

        recargarInscripcionesDesdeBD();
        limpiarFormulario();
    }

    @FXML
    void ActualizarCursosInscritos(ActionEvent event) {
        Inscripcion sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una inscripción en la tabla."); return; }

        Curso curso = cursopv.getValue();
        Estudiante est = estudianteops.getValue();
        if (curso == null) { alertError("Selecciona un curso."); return; }
        if (est == null) { alertError("Selecciona un estudiante."); return; }

        Integer anio = parseInt(txtAnio.getText(), "El año debe ser entero.");
        if (anio == null) return;
        Integer semestre = parseInt(txtSemestre.getText(), "El semestre debe ser 1 o 2.");
        if (semestre == null || semestre < 1 || semestre > 2) { alertError("El semestre debe ser 1 o 2."); return; }

        cursosInscritos.cargarDatos();

        Inscripcion nueva = new Inscripcion(curso, anio, semestre, est);
        Inscripcion conflicto = buscarPorClaveCompuesta(nueva);
        if (conflicto != null && !esMismaInscripcion(conflicto, sel)) {
            alertError("Ya existe una inscripción con esa combinación (estudiante/curso/año/semestre).");
            return;
        }

        Inscripcion actualInterno = buscarPorClaveCompuesta(sel);
        if (actualInterno != null) {
            try { cursosInscritos.eliminar(actualInterno); } catch (Throwable ignored) {}
        } else {
            try { cursosInscritos.eliminar(sel); } catch (Throwable ignored) {}
        }

        cursosInscritos.guardarInformacion(nueva);
        try { cursosInscritos.inscribirCurso(nueva); } catch (Throwable ignored) {}

        recargarInscripcionesDesdeBD();
        limpiarFormulario();
        alertInfo("Inscripción actualizada.");
    }

    @FXML
    void EliminarCursosInscritos(ActionEvent event) {
        Inscripcion sel = tlbCursoProfesor.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Selecciona una inscripción en la tabla."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la inscripción de " +
                        (sel.getEstudiante() != null ? sel.getEstudiante().getNombres() + " " + sel.getEstudiante().getApellidos() : "estudiante") +
                        " a " + (sel.getCurso() != null ? sel.getCurso().getNombre() : "curso") +
                        " (" + sel.getAnio() + "-" + sel.getSemestre() + ")?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        cursosInscritos.cargarDatos();
        Inscripcion interno = buscarPorClaveCompuesta(sel);
        if (interno != null) {
            try { cursosInscritos.eliminar(interno); } catch (Throwable ignored) {}
        } else {
            try { cursosInscritos.eliminar(sel); } catch (Throwable ignored) {}
        }

        recargarInscripcionesDesdeBD();
        limpiarFormulario();
        alertInfo("Inscripción eliminada.");
    }

    // ===== Navegación =====
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

    @FXML void SelectorEstudiante(ActionEvent event) { /* opcional */ }
    @FXML void selectorcurso(ActionEvent event) { /* opcional */ }

    // ===== Carga de catálogos (CURSO y ESTUDIANTE) directamente desde BD =====

    private void recargarCatalogosDesdeBD() {
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
                        prog,                 // puede ser "vacío" si no hay programa
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

    // ===== Carga de la tabla de inscripciones =====

    private void recargarInscripcionesDesdeBD() {
        try { cursosInscritos.cargarDatos(); } catch (Throwable ignored) {}
        List<Inscripcion> lista = obtenerListaDesdeLN();
        dataInscripciones.setAll(lista);
        tlbCursoProfesor.refresh();
    }

    @SuppressWarnings("unchecked")
    private List<Inscripcion> obtenerListaDesdeLN() {
        // 1) getter público si existe
        try {
            Method m = cursosInscritos.getClass().getMethod("getInscripcionesLista");
            Object r = m.invoke(cursosInscritos);
            if (r instanceof List<?>) {
                List<?> raw = (List<?>) r;
                List<Inscripcion> cast = new ArrayList<>();
                for (Object o : raw) if (o instanceof Inscripcion) cast.add((Inscripcion) o);
                return cast;
            }
        } catch (Throwable ignored) {}

        // 2) acceso por reflexión al campo interno
        try {
            Field f = cursosInscritos.getClass().getDeclaredField("inscripcionesLista");
            f.setAccessible(true);
            Object r = f.get(cursosInscritos);
            if (r instanceof List<?>) {
                List<?> raw = (List<?>) r;
                List<Inscripcion> cast = new ArrayList<>();
                for (Object o : raw) if (o instanceof Inscripcion) cast.add((Inscripcion) o);
                return cast;
            }
        } catch (Throwable ignored) {}

        return new ArrayList<>();
    }

    // ===== Helpers =====

    private void selectCursoById(int id) {
        for (Curso c : catalogoCursos) {
            if (c.getId() == id) {
                cursopv.getSelectionModel().select(c);
                return;
            }
        }
        cursopv.getSelectionModel().clearSelection();
    }

    private void selectEstudianteByCodigo(long codigo) {
        for (Estudiante e : catalogoEstudiantes) {
            if ((long) e.getCodigo() == codigo) {
                estudianteops.getSelectionModel().select(e);
                return;
            }
        }
        estudianteops.getSelectionModel().clearSelection();
    }

    private Integer parseInt(String s, String errMsg) {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (NumberFormatException e) { alertError(errMsg); return null; }
    }

    private void limpiarFormulario() {
        cursopv.getSelectionModel().clearSelection();
        estudianteops.getSelectionModel().clearSelection();
        txtAnio.clear();
        txtSemestre.clear();
        tlbCursoProfesor.getSelectionModel().clearSelection();
    }

    // Clave compuesta: (estudiante_codigo, curso_id, anio, semestre)
    private boolean esMismaInscripcion(Inscripcion a, Inscripcion b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return cod(a) == cod(b) &&
                idCurso(a) == idCurso(b) &&
                a.getAnio() == b.getAnio() &&
                a.getSemestre() == b.getSemestre();
    }

    private Inscripcion buscarPorClaveCompuesta(Inscripcion ref) {
        for (Inscripcion i : obtenerListaDesdeLN()) {
            if (esMismaInscripcion(i, ref)) return i;
        }
        return null;
    }

    private long cod(Inscripcion i) {
        Estudiante e = i.getEstudiante();
        return e == null ? Long.MIN_VALUE : (long) e.getCodigo();
    }

    private int idCurso(Inscripcion i) {
        Curso c = i.getCurso();
        return c == null ? Integer.MIN_VALUE : c.getId();
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

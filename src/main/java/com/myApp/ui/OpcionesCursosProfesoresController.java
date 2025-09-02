package com.myApp.ui;

import com.myApp.logicaDeNegocio.CursosProfesores;
import com.myApp.logicaDeNegocio.Schema;
import com.myApp.modelos.*;
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

public class OpcionesCursosProfesoresController {

    private  CursosProfesores cursosProfesores = new CursosProfesores();

    // Data backing de la tabla
    private final ObservableList<CursoProfesor> dataCursoProfesor = FXCollections.observableArrayList();

    @FXML private TableColumn<CursoProfesor, Integer> colAnio;
    @FXML private TableColumn<CursoProfesor, Curso>   colCurso;
    @FXML private TableColumn<CursoProfesor, Profesor> colProfesor;
    @FXML private TableColumn<CursoProfesor, Integer> colSemestre;

    @FXML private ComboBox<Curso> selectCurso;
    @FXML private ComboBox<Profesor> selectProfesor;

    @FXML private TableView<CursoProfesor> tlbCursoProfesor;

    @FXML private TextField txtAnio;
    @FXML private TextField txtSemestre;


    @FXML
    void GuardarCursoProfesor(ActionEvent event) {
        cursosProfesores.cargarDatosDesdeBD();

        // Validaciones básicas
        Curso cursoSel = selectCurso.getValue();
        Profesor profSel = selectProfesor.getValue();

        if (cursoSel == null) {
            alertError("Selecciona un curso.");
            return;
        }
        if (profSel == null) {
            alertError("Selecciona un profesor.");
            return;
        }

        int anio;
        int semestre;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException e) {
            alertError("El año debe ser un número entero.");
            return;
        }
        try {
            semestre = Integer.parseInt(txtSemestre.getText().trim());
        } catch (NumberFormatException e) {
            alertError("El semestre debe ser un número entero (1 o 2).");
            return;
        }
        if (semestre < 1 || semestre > 2) {
            alertError("El semestre debe ser 1 o 2.");
            return;
        }

        // Crear e inscribir
        CursoProfesor cursoProfesor = new CursoProfesor();
        cursoProfesor.setCurso(cursoSel);
        cursoProfesor.setProfesor(profSel);
        cursoProfesor.setAnio(anio);
        cursoProfesor.setSemestre(semestre);
        cursosProfesores.guardarInformacion(cursoProfesor);

        // Persistir en la capa de negocio
        cursosProfesores.inscribir(cursoProfesor);

        // Reflejar en la UI
        dataCursoProfesor.add(cursoProfesor);
        tlbCursoProfesor.refresh();

        // Limpiar formulario
        selectCurso.getSelectionModel().clearSelection();
        selectProfesor.getSelectionModel().clearSelection();
        txtAnio.clear();
        txtSemestre.clear();

        alertInfo("Inscripción guardada con éxito.");
    }

    @FXML
    void MostrarListaCursos(ActionEvent event) {
        Curso cursoSeleccionado = selectCurso.getValue();
        if (cursoSeleccionado != null) {
            System.out.println("Curso seleccionado: " + cursoSeleccionado.getNombre());
        }
    }

    @FXML
    void mostrarListaProfesores(ActionEvent event) {
        cursosProfesores.cargarDatosDesdeBD();

        Profesor profesorSeleccionado = selectProfesor.getValue();
        if (profesorSeleccionado != null) {
            System.out.println("Profesor seleccionado: " + profesorSeleccionado.getNombre());
        }
    }

    @FXML
    void initialize() {
        Schema.init();

        // ----- Datos de ejemplo -----
        Persona decano = new Persona(1, "Julian", "Martinez", "decano@inillanos.edu.co");
        Facultad facultad1 = new Facultad(1, "Facultad de Ciencias Basicas e Ingenieria", decano);
        Programa programa1 = new Programa(1, "Ingenieria de Sistemas", 10, java.time.LocalDate.now(), facultad1);

        Curso curso1 = new Curso(1, "Algoritmia y Programación", programa1, true);
        Curso curso2 = new Curso(2, "Pensamiento Lógico Matemático", programa1, true);

        Profesor profesor1 = new Profesor(1, "Ana", "Ramirez", "ana.ramirez@inillanos.edu.co", "ocasional");
        Profesor profesor2 = new Profesor(2, "Juan", "Perez", "perez.rr@inillanos.edu.co", "ocasional");

        // Obtener listas de negocio y añadir ejemplos
        List<Profesor> profesores = cursosProfesores.getListaProfesors(); // usa el nombre exacto de tu método
        List<Curso> cursos = cursosProfesores.getListaCursos();
        cursosProfesores.cargarDatosDesdeBD();



        if (profesores.stream().noneMatch(p -> p.getId() == profesor1.getId())) profesores.add(profesor1);
        if (profesores.stream().noneMatch(p -> p.getId() == profesor2.getId())) profesores.add(profesor2);
        if (cursos.stream().noneMatch(c -> c.getId() == curso1.getId())) cursos.add(curso1);
        if (cursos.stream().noneMatch(c -> c.getId() == curso2.getId())) cursos.add(curso2);

        // Llenar ComboBox
        selectProfesor.setItems(FXCollections.observableArrayList(profesores));
        selectCurso.setItems(FXCollections.observableArrayList(cursos));

        // Mostrar solo el nombre en los ComboBox
        selectProfesor.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Profesor p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getNombre());
            }
        });
        selectProfesor.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Profesor p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getNombre());
            }
        });

        selectCurso.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getNombre());
            }
        });
        selectCurso.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Curso c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getNombre());
            }
        });

        // Configurar tabla
        tlbCursoProfesor.setItems(dataCursoProfesor);

        colAnio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAnio()));
        colSemestre.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSemestre()));

        // Si Curso/Profesor tienen toString() legible, esto basta:
        colCurso.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCurso()));
        colProfesor.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getProfesor()));


    }

    // Helpers de alertas
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



    @FXML
    void backScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

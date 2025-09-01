package com.myApp.ui;
import com.myApp.modelos.*;
import com.myApp.logicaDeNegocio.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class mainApp extends Application{

    public static void main(String[] args) {
        Schema.init();

        Persona decano = new Persona(1, "Julian", "Martinez", "decano@inillanos.edu.co");
        Facultad facultad1 = new Facultad(1, "Facultad de Ciencias Basicas e Ingenieria", decano);
        Programa programa1 = new Programa(1, "Ingenieria de Sistemas", 10, java.time.LocalDate.now(), facultad1);
        Estudiante estudiante2 = new Estudiante(3, "Carlos", "Gomez", "estudiante2@inillanos.edu.co", 1, programa1, true, 40);
        Estudiante estudiante1 = new Estudiante(2, "Maria", "Lopez", "estudiante@inillanos.edu.co", 1, programa1, true, 40);
        Curso curso1 = new Curso(1, "Algoritmia y Programación", programa1, true);
        Profesor profesor1 = new Profesor(4, "Ana", "Ramirez", "ana.ramirez@inillanos.edu.co", "ocasional");
        Inscripcion inscripcion1 = new Inscripcion(curso1, 2025, 1, estudiante1);

        //List<Estudiante> estudiantes = new ArrayList<>();
        //estudiantes.add(estudiante2); estudiantes.add(estudiante1);
        //List<Curso> cursos = new ArrayList<>();
        //cursos.add(curso1);

        CursosInscritos cursosInscritos = new CursosInscritos();
        cursosInscritos.cargarDatos();
        //cursosInscritos.inscribirCurso(inscripcion1);
        //cursosInscritos.guardarInformacion(inscripcion1);
        System.out.println(cursosInscritos.imprimirListado());



        launch(args);

    }

    @Override
    public void start(Stage stage) {
        VentanaPrincipal ventana = new VentanaPrincipal();
        Scene scene = new Scene(ventana.getRoot(), 400, 300);

        stage.setTitle("Mi Aplicación JavaFX");
        stage.setScene(scene);
        stage.show();
    }



}

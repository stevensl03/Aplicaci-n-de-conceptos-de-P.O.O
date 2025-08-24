package com.myapp;
import com.myapp.model.*;
import com.myapp.repository.*;


public class Main {
    static public void confirmacion(boolean x){
        System.out.println(
            "Inscripción : " + (x ? "Éxito" : "Fallo")
        );
    }

    public static void main(String[] args) {
        

        Schema.init();

        Persona decano = new Persona(1, "Julian", "Martinez", "decano@inillanos.edu.co");
        Facultad facultad1 = new Facultad(1, "Facultad de Ciencias Basicas e Ingenieria", decano);
        Programa programa1 = new Programa(1, "Ingenieria de Sistemas", 10, java.time.LocalDate.now(), facultad1);
        Estudiante estudiante2 = new Estudiante(3, "Carlos", "Gomez", "estudiante2@inillanos.edu.co", 1, programa1, true, 40);
        Estudiante estudiante1 = new Estudiante(2, "Maria", "Lopez", "estudiante@inillanos.edu.co", 1, programa1, true, 40);
        Curso curso1 = new Curso(1, "Algoritmia y Programación", programa1, true);
       // Inscripcion inscripcion1 = new Inscripcion(curso1,2025, 01, estudiante1);
       // Inscripcion inscripcion2 = new Inscripcion(curso1,2025, 01, estudiante2);

        CursosInscritos cursosInscritos = new CursosInscritos();
        cursosInscritos.cargarDatos();
        
        //boolean X1 = cursosInscritos.inscribirCurso(inscripcion1);
        //cursosInscritos.guardarInformacion(inscripcion1);
        //cursosInscritos.eliminar(inscripcion1);
        //confirmacion(X1);
        //X1 = cursosInscritos.inscribirCurso(inscripcion2);
        //confirmacion(X1);
  
       // System.out.println(cursosInscritos.imprimirPosicion("22221"));
        System.out.println(cursosInscritos.imprimirListado());
        //System.out.println("Cantidad actual de inscripciones: " + cursosInscritos.cantidadActual());

    }
    
}
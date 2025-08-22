package com.myapp.repository;
import com.myapp.service.ServicionInterface;
import com.myapp.model.Inscripcion;
import com.myapp.model.Curso;
import com.myapp.model.Persona;

import java.util.List;
import java.util.ArrayList;


public class CursosInscritos implements ServicionInterface {
    private List<Inscripcion> inscripciones;

    public CursosInscritos() {
        inscripciones = new ArrayList<>();
    }

    public void Inscribir(Curso curso, int año, int semestre, Persona estudiante)   {
        Inscripcion inscripcion = new Inscripcion(curso, año, semestre, estudiante);
        inscripciones.add(inscripcion);
    }

    

    

    @Override
    public String imprimirPosicion(String posicion) {
        return "La posición es: " + posicion;
    }

    @Override
    public int cantidadActual() {
        return inscripciones.size();
    }

    @Override
    public List<String> imprimirListado() {
        List<String> listado = new ArrayList<>();
        for (Inscripcion inscripcion : inscripciones) {
            listado.add(inscripcion.toString());
        }
        return listado;
    }
}
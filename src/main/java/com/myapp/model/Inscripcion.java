package com.myapp.model;

public class Inscripcion {
    private Curso curso;
    private int año;
    private int semestre;
    private Persona estudiante;

    public Inscripcion(Curso curso, int año, int semestre, Persona estudiante) {
        this.curso = curso;
        this.año = año;
        this.semestre = semestre;
        this.estudiante = estudiante;
    }

    @Override
    public String toString() {
        return "Inscripcion{" +
                "curso=" + curso +
                ", año=" + año +
                ", semestre=" + semestre +
                ", estudiante=" + estudiante +
                '}';
    }
}

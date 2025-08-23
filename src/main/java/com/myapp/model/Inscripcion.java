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
    
    // Getters and Setters
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public int getAño() {
        return año;
    }

    public void setAño(int año) {
        this.año = año;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public Persona getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Persona estudiante) {
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

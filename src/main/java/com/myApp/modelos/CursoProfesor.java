package com.myApp.modelos;

public class CursoProfesor {
    private Profesor profesor;
    private int anio;
    private int semestre;
    private Curso curso;

    public CursoProfesor(Profesor profesor, int anio, int semestre, Curso curso) {
        this.profesor = profesor;
        this.anio = anio;
        this.semestre = semestre;
        this.curso = curso;
    }
    public CursoProfesor() {}

    //getters y setters
    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int año) {
        this.anio = anio;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    @Override
    public String toString() {
        return "CursoProfesor{" +
                "profesor=" + profesor +
                ", año=" + anio +
                ", semestre=" + semestre +
                ", curso=" + curso +
                '}';
    }
}

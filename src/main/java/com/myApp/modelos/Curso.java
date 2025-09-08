package com.myApp.modelos;


public class Curso {
    private int id;
    private String nombre;
    private Programa programa;
    private boolean activo;
    
    public Curso(int id, String nombre, Programa programa, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.programa = programa;
        this.activo = activo;
    }
    public Curso() {}
    
    //getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Curso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", programa=" + programa +
                ", activo=" + activo +
                '}';
    }
}

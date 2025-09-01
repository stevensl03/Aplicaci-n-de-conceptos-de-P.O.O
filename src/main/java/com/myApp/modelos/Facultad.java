package com.myApp.modelos;

public class Facultad {
    private double id;
    private String nombre;
    private Persona decano;

    public Facultad(double id, String nombre, Persona decano) {
        this.id = id;
        this.nombre = nombre;
        this.decano = decano;
    }

    //getters y setters
    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Persona getDecano() {
        return decano;
    }

    public void setDecano(Persona decano) {
        this.decano = decano;
    }

    @Override
    public String toString() {
        return "Facultad{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", decano=" + decano +
                '}';
    }
}

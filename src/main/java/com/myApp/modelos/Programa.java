package com.myApp.modelos;
import java.time.LocalDate;

public class Programa {
    private double Id;
    private String nombre;
    private double duracion;
    private LocalDate registro;
    private Facultad facultad;

    public Programa(double id, String nombre, double duracion, LocalDate registro, Facultad facultad) {
        Id = id;
        this.nombre = nombre;
        this.duracion = duracion;
        this.registro = registro;
        this.facultad = facultad;
    }

    //setters y getters

    public double getId() {
        return Id;
    }

    public void setId(double id) {
        Id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion(double duracion) {
        this.duracion = duracion;
    }

    public LocalDate getRegistro() {
        return registro;
    }

    public void setRegistro(LocalDate registro) {
        this.registro = registro;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    @Override
    public String toString() {
        return "Programa{" +
                "Id=" + Id +
                ", nombre='" + nombre + '\'' +
                ", duracion=" + duracion +
                ", registro=" + registro +
                ", facultad=" + facultad +
                '}';
    }

    
}

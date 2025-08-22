package com.myapp.model;
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

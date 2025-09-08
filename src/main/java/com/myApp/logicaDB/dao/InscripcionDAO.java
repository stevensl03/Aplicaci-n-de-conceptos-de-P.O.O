package com.myApp.logicaDB.dao;

import com.myApp.modelos.Inscripcion;
import com.myApp.modelos.Persona;

import java.util.List;
import java.util.Optional;

public interface InscripcionDAO {
    void insert(Inscripcion inscripcion);
    void updates(Inscripcion inscripcion);
    void delete(Inscripcion inscripcion);
    boolean exists(Inscripcion inscripcion);

    Optional<Inscripcion> findOne(double estudianteCodigo, int cursoId, int anio, int semestre);

    List<Inscripcion> findAll();
    List<Inscripcion> findByEstudiante(double estudianteCodigo);
    List<Inscripcion> findByCursoPeriodo(int cursoId, int anio, int semestre);
}

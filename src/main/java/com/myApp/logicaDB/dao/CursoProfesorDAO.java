package com.myApp.logicaDB.dao;


import com.myApp.modelos.CursoProfesor;
import java.util.List;
import java.util.Optional;

public interface CursoProfesorDAO {
    void insert(CursoProfesor cursoProfesor);
    void delete(CursoProfesor cursoProfesor);
    boolean exists(CursoProfesor cursoProfesor);

    Optional<CursoProfesor> findOne(double profesorPersonaId, int cursoId, int anio, int semestre);

    List<CursoProfesor> findAll();
    List<CursoProfesor> findByProfesor(double profesorPersonaId);
    List<CursoProfesor> findByCursoPeriodo(int cursoId, int anio, int semestre);
}

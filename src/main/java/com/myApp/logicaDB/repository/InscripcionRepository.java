package com.myApp.logicaDB.repository;
import com.myApp.logicaDB.dao.InscripcionDAO;
import com.myApp.modelos.Inscripcion;

import java.util.List;
import java.util.Optional;

public class InscripcionRepository {
    private final InscripcionDAO inscripcionDAO;

    public InscripcionRepository(InscripcionDAO inscripcionDAO) { this.inscripcionDAO = inscripcionDAO; }

    /** Registrar evitando duplicados (PK compuesta). */
    public boolean registrar(Inscripcion inscripcion) {
        if (inscripcionDAO.exists(inscripcion)) return false;
        inscripcionDAO.insert(inscripcion);
        return true;
    }

    public void desinscribir(Inscripcion inscripcion) {
        inscripcionDAO.delete(inscripcion);
    }

    public Optional<Inscripcion> buscarUno(double estudianteCodigo, int cursoId, int anio, int semestre) {
        return inscripcionDAO.findOne(estudianteCodigo, cursoId, anio, semestre);
    }

    public List<Inscripcion> listarTodo() {
        return inscripcionDAO.findAll();
    }

    public List<Inscripcion> listarPorEstudiante(double estudianteCodigo) {
        return inscripcionDAO.findByEstudiante(estudianteCodigo);
    }

    public List<Inscripcion> listarPorCursoPeriodo(int cursoId, int anio, int semestre) {
        return inscripcionDAO.findByCursoPeriodo(cursoId, anio, semestre);
    }
}

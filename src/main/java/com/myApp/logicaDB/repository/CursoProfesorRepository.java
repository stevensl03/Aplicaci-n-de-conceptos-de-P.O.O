package com.myApp.logicaDB.repository;

import com.myApp.logicaDB.dao.CursoProfesorDAO;
import com.myApp.modelos.CursoProfesor;

import java.util.List;
import java.util.Optional;

public class CursoProfesorRepository {
    private final CursoProfesorDAO cursoProfesorDAO;

    public CursoProfesorRepository(CursoProfesorDAO cursoProfesorDAO) { this.cursoProfesorDAO = cursoProfesorDAO; }

    /** Asignar evitando duplicados en periodo (PK compuesta). */
    public boolean registrar(CursoProfesor cp) {
        if (cursoProfesorDAO.exists(cp)) return false;
        cursoProfesorDAO.insert(cp);
        return true;
    }

    public void desasignar(CursoProfesor cp) { cursoProfesorDAO.delete(cp); }

    public Optional<CursoProfesor> buscarUno(double profesorPersonaId, int cursoId, int anio, int semestre) {
        return cursoProfesorDAO.findOne(profesorPersonaId, cursoId, anio, semestre);
    }

    public List<CursoProfesor> listarTodo() {
        return cursoProfesorDAO.findAll();
    }

    public List<CursoProfesor> listarPorProfesor(double profesorPersonaId) {
        return cursoProfesorDAO.findByProfesor(profesorPersonaId);
    }

    public List<CursoProfesor> listarPorCursoPeriodo(int cursoId, int anio, int semestre) {
        return cursoProfesorDAO.findByCursoPeriodo(cursoId, anio, semestre);
    }
}

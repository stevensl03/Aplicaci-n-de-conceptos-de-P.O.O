package com.myApp.logicaDB.dao;

import com.myApp.modelos.Curso;
import com.myApp.modelos.CursoProfesor;
import com.myApp.modelos.Profesor;
import com.myApp.logicaDB.H2DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImpCursoProfesorDAO implements CursoProfesorDAO {

    @Override
    public void insert(CursoProfesor cp) {
        String sql = "INSERT INTO CURSO_PROFESOR(profesor_persona_id, curso_id, anio, semestre) VALUES(?,?,?,?)";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, cp.getProfesor().getId());
            ps.setInt(2, cp.getCurso().getId());
            ps.setInt(3, cp.getAnio());
            ps.setInt(4, cp.getSemestre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert CursoProfesor", e);
        }
    }

    @Override
    public void delete(CursoProfesor cp) {
        String sql = "DELETE FROM CURSO_PROFESOR WHERE profesor_persona_id=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, cp.getProfesor().getId()); // <-- getters del modelo
            ps.setInt(2, cp.getCurso().getId());              // <-- getters del modelo
            ps.setInt(3, cp.getAnio());
            ps.setInt(4, cp.getSemestre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete CursoProfesor", e);
        }
    }

    @Override
    public boolean exists(CursoProfesor cp) {
        String sql = "SELECT 1 FROM CURSO_PROFESOR WHERE profesor_persona_id=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, cp.getProfesor().getId()); // <-- getters del modelo
            ps.setInt(2, cp.getCurso().getId());              // <-- getters del modelo
            ps.setInt(3, cp.getAnio());
            ps.setInt(4, cp.getSemestre());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Exists CursoProfesor", e);
        }
    }

    @Override
    public Optional<CursoProfesor> findOne(double profesorPersonaId, int cursoId, int anio, int semestre) {
        String sql = "SELECT profesor_persona_id, curso_id, anio, semestre FROM CURSO_PROFESOR WHERE profesor_persona_id=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, profesorPersonaId);
            ps.setInt(2, cursoId);
            ps.setInt(3, anio);
            ps.setInt(4, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindOne CursoProfesor", e);
        }
    }

    @Override
    public List<CursoProfesor> findAll() {
        String sql = "SELECT profesor_persona_id, curso_id, anio, semestre FROM CURSO_PROFESOR";
        List<CursoProfesor> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("FindAll CursoProfesor", e);
        }
        return out;
    }

    @Override
    public List<CursoProfesor> findByProfesor(double profesorPersonaId) {
        String sql = "SELECT profesor_persona_id, curso_id, anio, semestre FROM CURSO_PROFESOR WHERE profesor_persona_id=? ORDER BY anio DESC, semestre DESC";
        List<CursoProfesor> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, profesorPersonaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindByProfesor CursoProfesor", e);
        }
        return out;
    }

    @Override
    public List<CursoProfesor> findByCursoPeriodo(int cursoId, int anio, int semestre) {
        String sql = "SELECT profesor_persona_id, curso_id, anio, semestre FROM CURSO_PROFESOR WHERE curso_id=? AND anio=? AND semestre=? ORDER BY profesor_persona_id";
        List<CursoProfesor> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, cursoId);
            ps.setInt(2, anio);
            ps.setInt(3, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindByCursoPeriodo CursoProfesor", e);
        }
        return out;
    }

    private CursoProfesor map(ResultSet rs) throws SQLException {
        double profesorPersonaId = rs.getDouble("profesor_persona_id");
        int cursoId = rs.getInt("curso_id");
        int anio = rs.getInt("anio");
        int semestre = rs.getInt("semestre");

        Profesor prof = new Profesor();
        prof.setId(profesorPersonaId); // <-- getters/setters de tu modelo

        Curso curso = new Curso();
        curso.setId(cursoId);                 // <-- getters/setters de tu modelo

        return new CursoProfesor(prof, anio, semestre, curso);
    }
}
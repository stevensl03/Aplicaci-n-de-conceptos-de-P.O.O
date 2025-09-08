package com.myApp.logicaDB.dao;

import com.myApp.modelos.Curso;
import com.myApp.modelos.Estudiante;
import com.myApp.modelos.Inscripcion;
import com.myApp.logicaDB.H2DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImpInscripcionDAO implements InscripcionDAO {

    @Override
    public void insert(Inscripcion ins) {
        String sql = "INSERT INTO INSCRIPCION(estudiante_codigo, curso_id, anio, semestre) VALUES(?,?,?,?)";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, ins.getEstudiante().getCodigo()); // <-- getters del modelo
            ps.setInt(2, ins.getCurso().getId());             // <-- getters del modelo
            ps.setInt(3, ins.getAnio());
            ps.setInt(4, ins.getSemestre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert Inscripcion", e);
        }
    }
    @Override
    public void updates(Inscripcion inscripcion) {
        // H2: MERGE hace UPSERT usando la PK compuesta
        final String sql = """
        MERGE INTO INSCRIPCION (estudiante_codigo, curso_id, anio, semestre)
        KEY (estudiante_codigo, curso_id, anio, semestre)
        VALUES (?, ?, ?, ?)
        """;
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, inscripcion.getEstudiante().getCodigo());
            ps.setInt(2,    inscripcion.getCurso().getId());
            ps.setInt(3,    inscripcion.getAnio());
            ps.setInt(4,    inscripcion.getSemestre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Upsert (MERGE) Inscripcion", e);
        }
    }



    @Override
    public void delete(Inscripcion ins) {
        String sql = "DELETE FROM INSCRIPCION WHERE estudiante_codigo=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, ins.getEstudiante().getCodigo()); // <-- getters del modelo
            ps.setInt(2, ins.getCurso().getId());             // <-- getters del modelo
            ps.setInt(3, ins.getAnio());
            ps.setInt(4, ins.getSemestre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete Inscripcion", e);
        }
    }

    @Override
    public boolean exists(Inscripcion ins) {
        String sql = "SELECT 1 FROM INSCRIPCION WHERE estudiante_codigo=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, ins.getEstudiante().getCodigo()); // <-- getters del modelo
            ps.setInt(2, ins.getCurso().getId());             // <-- getters del modelo
            ps.setInt(3, ins.getAnio());
            ps.setInt(4, ins.getSemestre());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Exists Inscripcion", e);
        }
    }

    @Override
    public Optional<Inscripcion> findOne(double estudianteCodigo, int cursoId, int anio, int semestre) {
        String sql = "SELECT estudiante_codigo, curso_id, anio, semestre FROM INSCRIPCION WHERE estudiante_codigo=? AND curso_id=? AND anio=? AND semestre=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, estudianteCodigo);
            ps.setInt(2, cursoId);
            ps.setInt(3, anio);
            ps.setInt(4, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindOne Inscripcion", e);
        }
    }

    @Override
    public List<Inscripcion> findAll() {
        String sql = "SELECT estudiante_codigo, curso_id, anio, semestre FROM INSCRIPCION";
        List<Inscripcion> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("FindAll Inscripcion", e);
        }
        return out;
    }

    @Override
    public List<Inscripcion> findByEstudiante(double estudianteCodigo) {
        String sql = "SELECT estudiante_codigo, curso_id, anio, semestre FROM INSCRIPCION WHERE estudiante_codigo=? ORDER BY anio DESC, semestre DESC";
        List<Inscripcion> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, estudianteCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindByEstudiante Inscripcion", e);
        }
        return out;
    }

    @Override
    public List<Inscripcion> findByCursoPeriodo(int cursoId, int anio, int semestre) {
        String sql = "SELECT estudiante_codigo, curso_id, anio, semestre FROM INSCRIPCION WHERE curso_id=? AND anio=? AND semestre=? ORDER BY estudiante_codigo";
        List<Inscripcion> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, cursoId);
            ps.setInt(2, anio);
            ps.setInt(3, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindByCursoPeriodo Inscripcion", e);
        }
        return out;
    }

    /** Mapea usando solo IDs; crea objetos ligeros para UI (Curso/Estudiante con solo id/codigo). */
    private Inscripcion map(ResultSet rs) throws SQLException {
        double estudianteCodigo = rs.getDouble("estudiante_codigo");
        int cursoId = rs.getInt("curso_id");
        int anio = rs.getInt("anio");
        int semestre = rs.getInt("semestre");

        Estudiante est = new Estudiante();
        est.setCodigo(estudianteCodigo);      // <-- getters/setters de tu modelo

        Curso curso = new Curso();
        curso.setId(cursoId);                 // <-- getters/setters de tu modelo

        return new Inscripcion(curso, anio, semestre, est);
    }
}
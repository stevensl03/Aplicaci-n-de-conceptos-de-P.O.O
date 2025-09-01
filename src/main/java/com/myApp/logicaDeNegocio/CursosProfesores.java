package com.myApp.logicaDeNegocio;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.myApp.modelos.CursoProfesor;


public class CursosProfesores implements Servicios{
    private List<CursoProfesor> listaCursoProfesors = new ArrayList<>();
    
    //CRUD
    //inscribir curso
    public boolean inscribir(CursoProfesor cursoProfesor) {
        Objects.requireNonNull(cursoProfesor, "cursoProfesor");
        int idx = indexOfByKey(cursoProfesor);
        if (idx != -1) return false;
        return listaCursoProfesors.add(cursoProfesor);
    }

    //base de datos h2
    public void guardarInformacion(CursoProfesor cp) {
        Objects.requireNonNull(cp, "cursoProfesor");

        final String MERGE_PERSONA = """
            MERGE INTO PERSONA (id, nombres, apellidos, email) KEY(id)
            VALUES (?, ?, ?, ?)
        """;
        final String MERGE_PROFESOR = """
            MERGE INTO PROFESOR (persona_id, tipo_contrato) KEY(persona_id)
            VALUES (?, ?)
        """;
        final String MERGE_PROGRAMA = """
            MERGE INTO PROGRAMA (id, nombre, duracion, registro, facultad_id) KEY(id)
            VALUES (?, ?, ?, ?, ?)
        """;
        final String MERGE_CURSO = """
            MERGE INTO CURSO (id, nombre, programa_id, activo) KEY(id)
            VALUES (?, ?, ?, ?)
        """;
        final String MERGE_CURSO_PROF = """
            MERGE INTO CURSO_PROFESOR (profesor_persona_id, curso_id, anio, semestre)
            KEY(profesor_persona_id, curso_id, anio, semestre)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection cn = H2DB.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement psPersona = cn.prepareStatement(MERGE_PERSONA);
                PreparedStatement psProfesor= cn.prepareStatement(MERGE_PROFESOR);
                PreparedStatement psPrograma= cn.prepareStatement(MERGE_PROGRAMA);
                PreparedStatement psCurso   = cn.prepareStatement(MERGE_CURSO);
                PreparedStatement psCP      = cn.prepareStatement(MERGE_CURSO_PROF)) {

                var prof = cp.getProfesor();   // ajusta getters si difieren
                var curso = cp.getCurso();

                // 1) PERSONA del profesor
                psPersona.setDouble(1, prof.getId());         // Persona.id
                psPersona.setString(2, prof.getNombres());
                psPersona.setString(3, prof.getApellidos());
                psPersona.setString(4, prof.getEmail());
                psPersona.executeUpdate();

                // 2) PROFESOR
                psProfesor.setDouble(1, prof.getId());        // persona_id
                psProfesor.setString(2, prof.getTipoContrato()); // ajusta si tu modelo usa otro nombre/campo
                psProfesor.executeUpdate();

                // 3) PROGRAMA del curso (si existe)
                var prog = (curso != null) ? curso.getPrograma() : null;
                if (prog != null) {
                    psPrograma.setDouble(1, prog.getId());
                    psPrograma.setString(2, prog.getNombre());
                    psPrograma.setDouble(3, prog.getDuracion()); // si no tienes, usa 0.0
                    if (prog.getRegistro() != null) {
                        psPrograma.setDate(4, java.sql.Date.valueOf(prog.getRegistro())); // LocalDate -> Date
                    } else {
                        psPrograma.setNull(4, java.sql.Types.DATE);
                    }
                    if (prog.getFacultad() != null) {
                        psPrograma.setDouble(5, prog.getFacultad().getId());
                    } else {
                        psPrograma.setNull(5, java.sql.Types.DOUBLE); // o INTEGER según schema
                    }
                    psPrograma.executeUpdate();
                }

                // 4) CURSO
                psCurso.setInt(1, curso.getId());
                psCurso.setString(2, curso.getNombre());
                if (prog != null) {
                    psCurso.setDouble(3, prog.getId());
                } else {
                    psCurso.setNull(3, java.sql.Types.DOUBLE);
                }
                psCurso.setBoolean(4, curso.isActivo());
                psCurso.executeUpdate();

                // 5) CURSO_PROFESOR
                psCP.setDouble(1, prof.getId());
                psCP.setInt(2, curso.getId());
                psCP.setInt(3, cp.getAnio());      // si tu getter es getAño(), cambia aquí
                psCP.setInt(4, cp.getSemestre());
                psCP.executeUpdate();

                cn.commit();

                // Mantén coherencia en memoria (solo si no existe ya)
                if (indexOfByKey(cp) == -1) {
                    listaCursoProfesors.add(cp);
                }

            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error guardando CursoProfesor: " + e.getMessage());
        }
    }

    public void cargarDatosDesdeBD() {
        final String SQL = """
            SELECT
                -- PROFESOR (con PERSONA)
                p.id, p.nombres, p.apellidos, p.email,
                pr.tipo_contrato,

                -- CURSO
                c.id, c.nombre, c.activo,

                -- PROGRAMA del curso (pc)
                pc.id, pc.nombre, pc.duracion, pc.registro, pc.facultad_id,

                -- CURSO_PROFESOR
                cp.anio, cp.semestre
            FROM CURSO_PROFESOR cp
            JOIN PROFESOR pr ON pr.persona_id = cp.profesor_persona_id
            JOIN PERSONA  p  ON p.id          = pr.persona_id
            JOIN CURSO    c  ON c.id          = cp.curso_id
            LEFT JOIN PROGRAMA pc ON pc.id    = c.programa_id
            ORDER BY cp.anio DESC, cp.semestre DESC, p.apellidos, p.nombres
        """;

        List<CursoProfesor> tmp = new ArrayList<>();

        try (Connection cn = H2DB.getConnection();
            PreparedStatement ps = cn.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // --- Reconstruir Profesor (ajusta al constructor real de tu clase) ---
                // Ejemplo si Profesor extiende Persona y tiene (id, nombres, apellidos, email, tipoContrato)
                var profesor = new com.myApp.modelos.Profesor(
                    rs.getDouble(1),      // persona.id
                    rs.getString(2),      // nombres
                    rs.getString(3),      // apellidos
                    rs.getString(4),      // email
                    rs.getString(5)       // tipo_contrato
                );

                // --- Reconstruir Programa del curso (si hay) ---
                com.myApp.modelos.Programa programaCurso = null;
                double progId = rs.getDouble(9); // si es NULL, getDouble devuelve 0
                boolean hayPrograma = !rs.wasNull() && progId != 0;
                if (hayPrograma) {
                    java.sql.Date sqlFecha = rs.getDate(12);
                    java.time.LocalDate registro = (sqlFecha != null) ? sqlFecha.toLocalDate() : null;

                    programaCurso = new com.myApp.modelos.Programa(
                        progId,                 // id
                        rs.getString(10),       // nombre
                        rs.getDouble(11),       // duracion
                        registro,               // registro (LocalDate)
                        null                    // facultad (cárgala si la necesitas)
                    );
                }

                // --- Reconstruir Curso (ajusta al constructor real) ---
                var curso = new com.myApp.modelos.Curso(
                    rs.getInt(6),             // id
                    rs.getString(7),          // nombre
                    programaCurso,            // programa (puede ser null)
                    rs.getBoolean(8)          // activo
                );

                // --- Reconstruir CursoProfesor (ajusta al constructor real) ---
                // --- Reconstruir CursoProfesor ---
                var cursoProf = new com.myApp.modelos.CursoProfesor(
                    profesor,
                    rs.getInt(13),   // año
                    rs.getInt(14),   // semestre
                    curso
                );


                tmp.add(cursoProf);
            }

            listaCursoProfesors.clear();
            listaCursoProfesors.addAll(tmp);

        } catch (SQLException e) {
            System.err.println("❌ Error cargando CursoProfesor desde BD: " + e.getMessage());
        }
    }


    @Override
    public String toString() {
        return "CursosProfesores [listaCursoProfesors=" + listaCursoProfesors + "]";
    }

    //Implementacion de ServicionInterface
    @Override
    public String imprimirPosicion(String db){
        StringBuilder sb = new StringBuilder();
        for (CursoProfesor cursoProfesor : listaCursoProfesors) {
            sb.append(cursoProfesor.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int cantidadActual() {
        return listaCursoProfesors.size();
    }

    @Override
    public List<String> imprimirListado() {
        List<String> listado = new ArrayList<>();
        for (CursoProfesor cursoProfesor : listaCursoProfesors) {
            listado.add(cursoProfesor.toString());
        }
        return listado;
    }

    //metodos adicionales
    //Buscar por clave
    private int indexOfByKey(CursoProfesor cursoProfesor) {
    for (int i = 0; i < listaCursoProfesors.size(); i++) {
        if (listaCursoProfesors.get(i).equals(cursoProfesor)) {
            return i;
        }
    }
    return -1;
    }


}

package com.myApp.logicaDeNegocio;

import com.myApp.modelos.Curso;
import com.myApp.modelos.Estudiante;
import com.myApp.modelos.Inscripcion;
import com.myApp.modelos.Programa;

import java.util.List;
import java.util.Objects;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;


public class CursosInscritos implements Servicios {

    private List<Inscripcion>  inscripcionesLista = new ArrayList<>();

    public List<Inscripcion> getInscripcionesLista() {
        return new ArrayList<>(inscripcionesLista);
    }

    //CRUD
    //inscribir curso
    public boolean inscribirCurso(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx != -1) return false;
        return inscripcionesLista.add(inscripcion);
    }


    //Eliminar curso
    public boolean eliminar(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx == -1) return false;
        inscripcionesLista.remove(inscripcion);
        return true;
    }

    //Actualizar curso
    public boolean actualizar(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx == -1) return false;
        inscripcionesLista.set(idx, inscripcion);
        return true;
    }

    public void guardarInformacion(Inscripcion inscripcion) {
        // MERGE explícitos (columnas + KEY) — alineados con tu Schema
        final String MERGE_PERSONA = """
            MERGE INTO PERSONA (id, nombres, apellidos, email) KEY(id)
            VALUES (?, ?, ?, ?)
        """;
        final String MERGE_FACULTAD = """
            MERGE INTO FACULTAD (id, nombre, decano_id) KEY(id)
            VALUES (?, ?, ?)
        """;
        final String MERGE_PROGRAMA = """
            MERGE INTO PROGRAMA (id, nombre, duracion, registro, facultad_id) KEY(id)
            VALUES (?, ?, ?, ?, ?)
        """;
        final String MERGE_ESTUDIANTE = """
            MERGE INTO ESTUDIANTE (codigo, persona_id, programa_id, activo, promedio) KEY(codigo)
            VALUES (?, ?, ?, ?, ?)
        """;
        final String MERGE_CURSO = """
            MERGE INTO CURSO (id, nombre, programa_id, activo) KEY(id)
            VALUES (?, ?, ?, ?)
        """;
        final String MERGE_INSCRIPCION = """
            MERGE INTO INSCRIPCION (estudiante_codigo, curso_id, anio, semestre)
            KEY(estudiante_codigo, curso_id, anio, semestre)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection cn = H2DB.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement psPersona = cn.prepareStatement(MERGE_PERSONA);
                PreparedStatement psFac     = cn.prepareStatement(MERGE_FACULTAD);
                PreparedStatement psProg    = cn.prepareStatement(MERGE_PROGRAMA);
                PreparedStatement psEst     = cn.prepareStatement(MERGE_ESTUDIANTE);
                PreparedStatement psCurso   = cn.prepareStatement(MERGE_CURSO);
                PreparedStatement psIns     = cn.prepareStatement(MERGE_INSCRIPCION)) {

                Estudiante e = inscripcion.getEstudiante();
                Curso c       = inscripcion.getCurso();
                Programa pEst = (e != null) ? e.getPrograma() : null; // programa del estudiante
                Programa pCur = (c != null) ? c.getPrograma() : null; // programa del curso

                // =======================
                // 1) FACULTAD (asegurar decano en PERSONA antes)
                // =======================
                var facEst = (pEst != null) ? pEst.getFacultad() : null;
                var facCur = (pCur != null) ? pCur.getFacultad() : null;

                // helper: upsert de FACULTAD con decano
                java.util.function.Consumer<com.myApp.modelos.Facultad> upsertFacultad = fac -> {
                    try {
                        // 1a) si hay decano, primero PERSONA del decano
                        if (fac.getDecano() != null) {
                            var dec = fac.getDecano();
                            psPersona.setDouble(1, dec.getId());
                            psPersona.setString(2, dec.getNombres());
                            psPersona.setString(3, dec.getApellidos());
                            psPersona.setString(4, dec.getEmail());
                            psPersona.executeUpdate();
                        }
                        // 1b) FACULTAD
                        psFac.setDouble(1, fac.getId());
                        psFac.setString(2, fac.getNombre());
                        if (fac.getDecano() != null) {
                            psFac.setDouble(3, fac.getDecano().getId());
                        } else {
                            psFac.setNull(3, java.sql.Types.DOUBLE); // usa INTEGER si tu columna es INT
                        }
                        psFac.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                };

                if (facEst != null) upsertFacultad.accept(facEst);
                if (facCur != null && (facEst == null || facCur.getId() != facEst.getId())) {
                    upsertFacultad.accept(facCur);
                }

                // =======================
                // 2) PROGRAMA(s)
                // =======================
                if (pEst != null) {
                    psProg.setDouble(1, pEst.getId());
                    psProg.setString(2, pEst.getNombre());
                    psProg.setDouble(3, pEst.getDuracion());
                    if (pEst.getRegistro() != null) {
                        psProg.setDate(4, java.sql.Date.valueOf(pEst.getRegistro())); // LocalDate -> Date
                    } else {
                        psProg.setNull(4, java.sql.Types.DATE);
                    }
                    if (pEst.getFacultad() != null) psProg.setDouble(5, pEst.getFacultad().getId());
                    else psProg.setNull(5, java.sql.Types.DOUBLE);
                    psProg.executeUpdate();
                }

                if (pCur != null && (pEst == null || pCur.getId() != pEst.getId())) {
                    psProg.setDouble(1, pCur.getId());
                    psProg.setString(2, pCur.getNombre());
                    psProg.setDouble(3, pCur.getDuracion());
                    if (pCur.getRegistro() != null) {
                        psProg.setDate(4, java.sql.Date.valueOf(pCur.getRegistro()));
                    } else {
                        psProg.setNull(4, java.sql.Types.DATE);
                    }
                    if (pCur.getFacultad() != null) psProg.setDouble(5, pCur.getFacultad().getId());
                    else psProg.setNull(5, java.sql.Types.DOUBLE);
                    psProg.executeUpdate();
                }

                // =======================
                // 3) PERSONA (del estudiante)
                // =======================
                psPersona.setDouble(1, e.getId());
                psPersona.setString(2, e.getNombres());
                psPersona.setString(3, e.getApellidos());
                psPersona.setString(4, e.getEmail());
                psPersona.executeUpdate();

                // =======================
                // 4) ESTUDIANTE
                // =======================
                psEst.setDouble(1, e.getCodigo());
                psEst.setDouble(2, e.getId()); // persona_id
                if (pEst != null) psEst.setDouble(3, pEst.getId());
                else psEst.setNull(3, java.sql.Types.DOUBLE);
                psEst.setBoolean(4, e.isActivo());
                psEst.setDouble(5, e.getPromedio());
                psEst.executeUpdate();

                // =======================
                // 5) CURSO
                // =======================
                psCurso.setInt(1, c.getId());
                psCurso.setString(2, c.getNombre());
                if (pCur != null) {
                    psCurso.setDouble(3, pCur.getId());
                } else if (pEst != null) {
                    // si no hay programa en el curso, usa el del estudiante (si aplica a tu modelo)
                    psCurso.setDouble(3, pEst.getId());
                } else {
                    psCurso.setNull(3, java.sql.Types.DOUBLE);
                }
                psCurso.setBoolean(4, c.isActivo());
                psCurso.executeUpdate();

                // =======================
                // 6) INSCRIPCION
                // =======================
                psIns.setDouble(1, e.getCodigo());
                psIns.setInt(2, c.getId());
                psIns.setInt(3, inscripcion.getAnio());      // usa getAnio() si tu getter no tiene ñ
                psIns.setInt(4, inscripcion.getSemestre());
                psIns.executeUpdate();

                cn.commit();
                System.out.println("✅ Inscripción guardada correctamente en BD");

            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar inscripción: " + e.getMessage());
        }
    }



    public void cargarDatos() {
        final String SQL = """
            SELECT
                -- PERSONA (del estudiante)
                p.id, p.nombres, p.apellidos, p.email,

                -- ESTUDIANTE
                e.codigo, e.programa_id, e.activo, e.promedio,

                -- CURSO
                c.id, c.nombre, c.activo, c.programa_id,

                -- PROGRAMA del ESTUDIANTE (pe)
                pe.id, pe.nombre,

                -- PROGRAMA del CURSO (pc)
                pc.id, pc.nombre,

                -- INSCRIPCION
                i.anio, i.semestre
            FROM INSCRIPCION i
            JOIN ESTUDIANTE e ON e.codigo = i.estudiante_codigo
            JOIN PERSONA   p  ON p.id     = e.persona_id
            JOIN CURSO     c  ON c.id     = i.curso_id
            JOIN PROGRAMA  pe ON pe.id    = e.programa_id
            JOIN PROGRAMA  pc ON pc.id    = c.programa_id
            ORDER BY i.anio DESC, i.semestre DESC, p.apellidos, p.nombres
        """;

        List<Inscripcion> tmp = new ArrayList<>();

        try (Connection cn = H2DB.getConnection();
            PreparedStatement ps = cn.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // ---------- PROGRAMA del estudiante ----------
                Programa progEst = new Programa(
                    rs.getDouble(13),    // pe.id
                    rs.getString(14),    // pe.nombre
                    0.0,                 // duracion (por ahora no lo traemos → default)
                    null,                // registro (por ahora null)
                    null                 // facultad (no traída en este SELECT)
                );

                // ---------- ESTUDIANTE ----------
                Estudiante est = new Estudiante(
                    rs.getDouble(1),     // p.id (Persona)
                    rs.getString(2),     // nombres
                    rs.getString(3),     // apellidos
                    rs.getString(4),     // email
                    rs.getDouble(5),     // e.codigo
                    progEst,             // programa
                    rs.getBoolean(7),    // activo
                    rs.getDouble(8)      // promedio
                );

                // ---------- PROGRAMA del curso ----------
                Programa progCurso = new Programa(
                    rs.getDouble(15),    // pc.id
                    rs.getString(16),    // pc.nombre
                    0.0,                 // duracion
                    null,                // registro
                    null                 // facultad
                );

                // ---------- CURSO ----------
                Curso cur = new Curso(
                    rs.getInt(9),        // c.id
                    rs.getString(10),    // c.nombre
                    progCurso,           // programa
                    rs.getBoolean(11)    // c.activo
                );

                // ---------- INSCRIPCION ----------
                Inscripcion ins = new Inscripcion(
                    cur,
                    rs.getInt(17),       // i.anio
                    rs.getInt(18),       // i.semestre
                    est
                );

                tmp.add(ins);
            }

            inscripcionesLista.clear();
            inscripcionesLista.addAll(tmp);
            System.out.println("📥 Inscripciones cargadas: " + inscripcionesLista.size());

        } catch (SQLException e) {
            System.err.println("❌ Error cargando inscripciones: " + e.getMessage());
        }
    }



    //Implementacion de ServicionInterface
    @Override
    public String imprimirPosicion(String db){
        StringBuilder sb = new StringBuilder();
        for (Inscripcion inscripcion : inscripcionesLista) {
            sb.append(inscripcion.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int cantidadActual() {
        return inscripcionesLista.size();
    }

    @Override
    public List<String> imprimirListado() {
        List<String> listado = new ArrayList<>();
        for (Inscripcion inscripcion : inscripcionesLista) {
            listado.add(inscripcion.toString());
        }
        return listado;
    }

    //metodos adicionales
    //Buscar por clave
    private int indexOfByKey(Inscripcion inscripcion) {
    for (int i = 0; i < inscripcionesLista.size(); i++) {
        if (inscripcionesLista.get(i).equals(inscripcion)) {
            return i;
        }
    }
    return -1;
    }

}
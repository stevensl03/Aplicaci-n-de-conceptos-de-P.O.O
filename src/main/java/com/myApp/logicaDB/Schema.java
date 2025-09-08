package com.myApp.logicaDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea el esquema en H2 de acuerdo al diagrama de clases.
 * - Herencia: Persona → Estudiante / Profesor (table-per-subclass)
 * - Clave de Inscripción: (estudiante_codigo, curso_id, anio, semestre)
 * - Clave de CursoProfesor: (profesor_persona_id, curso_id, anio, semestre)
 */
public final class Schema {

    private Schema() {}

    public static void init() {
        try (Connection cn = H2DB.getConnection();
             Statement st = cn.createStatement()) {

            // PERSONA (base de Estudiante y Profesor)
            st.execute("""
                CREATE TABLE IF NOT EXISTS PERSONA(
                  id        DOUBLE PRIMARY KEY,
                  nombres   VARCHAR(120),
                  apellidos VARCHAR(120),
                  email     VARCHAR(160)
                );
            """);

            // FACULTAD (decano es una Persona)
            st.execute("""
                CREATE TABLE IF NOT EXISTS FACULTAD(
                  id        DOUBLE PRIMARY KEY,
                  nombre    VARCHAR(160),
                  decano_id DOUBLE,
                  CONSTRAINT fk_facultad_decano
                    FOREIGN KEY (decano_id) REFERENCES PERSONA(id)
                );
            """);

            // PROGRAMA (pertenece a una Facultad)
            st.execute("""
                CREATE TABLE IF NOT EXISTS PROGRAMA(
                  id        DOUBLE PRIMARY KEY,
                  nombre    VARCHAR(160),
                  duracion  DOUBLE,
                  registro  DATE,
                  facultad_id DOUBLE,
                  CONSTRAINT fk_programa_facultad
                    FOREIGN KEY (facultad_id) REFERENCES FACULTAD(id)
                );
            """);

            // CURSO (pertenece a un Programa)
            st.execute("""
                CREATE TABLE IF NOT EXISTS CURSO(
                  id        INT PRIMARY KEY,
                  nombre    VARCHAR(160),
                  programa_id DOUBLE,
                  activo    BOOLEAN,
                  CONSTRAINT fk_curso_programa
                    FOREIGN KEY (programa_id) REFERENCES PROGRAMA(id)
                );
            """);

            // ESTUDIANTE (subclase de Persona)
            st.execute("""
                CREATE TABLE IF NOT EXISTS ESTUDIANTE(
                  codigo      DOUBLE PRIMARY KEY,
                  persona_id  DOUBLE UNIQUE,
                  programa_id DOUBLE,
                  activo      BOOLEAN,
                  promedio    DOUBLE,
                  CONSTRAINT fk_est_persona
                    FOREIGN KEY (persona_id) REFERENCES PERSONA(id),
                  CONSTRAINT fk_est_programa
                    FOREIGN KEY (programa_id) REFERENCES PROGRAMA(id)
                );
            """);

            // PROFESOR (subclase de Persona)
            st.execute("""
                CREATE TABLE IF NOT EXISTS PROFESOR(
                  persona_id   DOUBLE PRIMARY KEY,
                  tipo_contrato VARCHAR(80),
                  CONSTRAINT fk_prof_persona
                    FOREIGN KEY (persona_id) REFERENCES PERSONA(id)
                );
            """);

            // INSCRIPCION (Estudiante ↔ Curso, con año/semestre)
            st.execute("""
                CREATE TABLE IF NOT EXISTS INSCRIPCION(
                  estudiante_codigo DOUBLE NOT NULL,
                  curso_id          INT    NOT NULL,
                  anio              INT    NOT NULL,
                  semestre          INT    NOT NULL,
                  PRIMARY KEY (estudiante_codigo, curso_id, anio, semestre),
                  CONSTRAINT fk_ins_est
                    FOREIGN KEY (estudiante_codigo) REFERENCES ESTUDIANTE(codigo),
                  CONSTRAINT fk_ins_curso
                    FOREIGN KEY (curso_id) REFERENCES CURSO(id)
                );
            """);

            // CURSO_PROFESOR (asignación de profesor a curso por año/semestre)
            st.execute("""
                CREATE TABLE IF NOT EXISTS CURSO_PROFESOR(
                  profesor_persona_id DOUBLE NOT NULL,
                  curso_id            INT    NOT NULL,
                  anio                INT    NOT NULL,
                  semestre            INT    NOT NULL,
                  PRIMARY KEY (profesor_persona_id, curso_id, anio, semestre),
                  CONSTRAINT fk_cp_prof
                    FOREIGN KEY (profesor_persona_id) REFERENCES PROFESOR(persona_id),
                  CONSTRAINT fk_cp_curso
                    FOREIGN KEY (curso_id) REFERENCES CURSO(id)
                );
            """);

            // Índices útiles (opcionales)
            st.execute("CREATE INDEX IF NOT EXISTS idx_est_programa ON ESTUDIANTE(programa_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_curso_programa ON CURSO(programa_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_ins_curso ON INSCRIPCION(curso_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_ins_est ON INSCRIPCION(estudiante_codigo);");

        } catch (SQLException e) {
            throw new RuntimeException("Error creando el esquema en H2", e);
        }
    }
}

package com.myapp.repository;

import com.myapp.service.ServicionInterface;
import com.myapp.model.Inscripcion;


import java.util.List;
import java.util.Objects;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class CursosInscritos implements ServicionInterface {

    private List<Inscripcion>  inscripcionesLista = new ArrayList<>();


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

//Guardar informacion en BD
public void guardarInformacion(Inscripcion inscripcion) {
    final String MERGE_EST = "MERGE INTO ESTUDIANTE KEY(codigo) VALUES(?, ?, ?)";
    final String MERGE_CUR = "MERGE INTO CURSO KEY(id) VALUES(?, ?, ?)";
    final String MERGE_INS = """
        MERGE INTO INSCRIPCION
        KEY(estudiante_codigo, curso_id, anio, semestre)
        VALUES(?, ?, ?, ?)
    """;

    try (Connection cn = H2DB.getConnection()) {
        cn.setAutoCommit(false);

        try (PreparedStatement psE = cn.prepareStatement(MERGE_EST);
             PreparedStatement psC = cn.prepareStatement(MERGE_CUR);
             PreparedStatement psI = cn.prepareStatement(MERGE_INS)) {

            // === Guardar estudiante ===
            var e = inscripcion.getEstudiante();
            psE.setDouble(1, e.getId());
            psE.setString(2, e.getNombres());
            psE.setString(3, e.getApellidos());
            psE.executeUpdate();

            // === Guardar curso ===
            var c = inscripcion.getCurso();
            psC.setInt(1, c.getId());
            psC.setString(2, c.getNombre());
            psC.setBoolean(3, c.isActivo());
            psC.executeUpdate();

            // === Guardar inscripción ===
            psI.setDouble(1, e.getId());
            psI.setInt(2, c.getId());
            psI.setInt(3, inscripcion.getAño());
            psI.setInt(4, inscripcion.getSemestre());
            psI.executeUpdate();

            cn.commit();
            inscripcionesLista.add(inscripcion); // También en memoria
            System.out.println("✅ Inscripción guardada en BD");

        } catch (SQLException ex) {
            cn.rollback();
            throw ex;
        }
    } catch (SQLException e) {
        System.err.println("❌ Error guardando inscripción: " + e.getMessage());
    }
}

    //Cargar datos
    public void cargarDatos() {
        
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
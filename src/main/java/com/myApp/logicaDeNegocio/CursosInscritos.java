package com.myApp.logicaDeNegocio;

import com.myApp.logicaDB.H2DB;
import com.myApp.logicaDB.dao.ImpInscripcionDAO;
import com.myApp.logicaDB.dao.InscripcionDAO;
import com.myApp.logicaDB.repository.InscripcionRepository;
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

    //CRUD de Lista
    public boolean inscribirCurso(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx != -1) return false;
        return inscripcionesLista.add(inscripcion);
    }


    public boolean eliminar(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx == -1) return false;
        inscripcionesLista.remove(inscripcion);
        return true;
    }

    public boolean actualizar(Inscripcion inscripcion) {
        Objects.requireNonNull(inscripcion, "inscripcion");
        int idx = indexOfByKey(inscripcion);
        if (idx == -1) return false;
        inscripcionesLista.set(idx, inscripcion);
        return true;
    }

    //Metodos de Base de Datos
    public void guardarInformacion(Inscripcion inscripcion) {
        inscribirCurso(inscripcion);
        InscripcionDAO inscripcionDAO = new ImpInscripcionDAO();
        InscripcionRepository inscripcionRepository = new InscripcionRepository(inscripcionDAO);
        inscripcionRepository.registrar(inscripcion);
    }

    public void cargarDatos() {
        InscripcionDAO inscripcionDAO = new ImpInscripcionDAO();
        InscripcionRepository inscripcionRepository = new InscripcionRepository(inscripcionDAO);
        inscripcionesLista = inscripcionRepository.listarTodo();

    }

    //Implementacion de Servicios
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
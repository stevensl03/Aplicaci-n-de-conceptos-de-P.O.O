package com.myApp.logicaDeNegocio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.myApp.logicaDB.H2DB;
import com.myApp.logicaDB.dao.CursoProfesorDAO;
import com.myApp.logicaDB.dao.ImpCursoProfesorDAO;
import com.myApp.logicaDB.repository.CursoProfesorRepository;
import com.myApp.modelos.Curso;
import com.myApp.modelos.CursoProfesor;
import com.myApp.modelos.Profesor;


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




    //Metodos de Base de Datos
    public void guardarInformacion(CursoProfesor cursoProfesor) {
        inscribir(cursoProfesor);
        CursoProfesorDAO cursoProfesorDAO = new ImpCursoProfesorDAO();
        CursoProfesorRepository cursoProfesorRepository = new CursoProfesorRepository(cursoProfesorDAO);
        cursoProfesorRepository.registrar(cursoProfesor);
    }

    public void cargarDatosDesdeBD() {

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

package com.myApp.logicaDeNegocio;

import com.myApp.modelos.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class InscripcionesPersonas {
    private List<Persona> listaPersonas = new ArrayList<>();
    // en com.myApp.logicaDeNegocio.InscripcionesPersonas
    public List<Persona> getListaPersonas() {
        return new ArrayList<>(listaPersonas); // copia defensiva
    }


    //CRUD
    //inscribir curso
    public boolean inscribir(Persona persona) {
        Objects.requireNonNull(persona, "persona");
        int idx = indexOfByKey(persona);
        if (idx != -1) return false;
        return listaPersonas.add(persona);
    }

        //Eliminar curso
    public boolean eliminar(Persona persona) {
        Objects.requireNonNull(persona, "persona");
        int idx = indexOfByKey(persona);
        if (idx == -1) return false;
        listaPersonas.remove(persona);
        return true;
    }

    //Actualizar curso
    public boolean actualizar(Persona persona) {
        Objects.requireNonNull(persona, "persona");
        int idx = indexOfByKey(persona);
        if (idx == -1) return false;
        listaPersonas.set(idx, persona);
        return true;
    }

    //base de datos h2
    public void guardarInformacion(Persona persona) {
        Objects.requireNonNull(persona, "persona");

        final String MERGE_PERSONA = """
            MERGE INTO PERSONA (id, nombres, apellidos, email) KEY(id)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection cn = H2DB.getConnection();
            PreparedStatement ps = cn.prepareStatement(MERGE_PERSONA)) {

            ps.setDouble(1, persona.getId());
            ps.setString(2, persona.getNombres());
            ps.setString(3, persona.getApellidos());
            ps.setString(4, persona.getEmail());
            ps.executeUpdate();

            // Mantener la lista en memoria coherente
            int idx = indexOfByKey(persona);
            if (idx == -1) {
                listaPersonas.add(persona);
            } else {
                listaPersonas.set(idx, persona);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error guardando Persona: " + e.getMessage());
        }
    }

    public void cargarDatosDesdeBD() {
        final String SQL = """
            SELECT id, nombres, apellidos, email
            FROM PERSONA
            ORDER BY apellidos, nombres
        """;

        List<Persona> tmp = new ArrayList<>();

        try (Connection cn = H2DB.getConnection();
            PreparedStatement ps = cn.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Persona p = new Persona(
                    rs.getDouble(1),   // id
                    rs.getString(2),   // nombres
                    rs.getString(3),   // apellidos
                    rs.getString(4)    // email
                );
                tmp.add(p);
            }

            listaPersonas.clear();
            listaPersonas.addAll(tmp);

        } catch (SQLException e) {
            System.err.println("❌ Error cargando Personas desde BD: " + e.getMessage());
        }
    }


     //metodos adicionales
    //Buscar por clave
    private int indexOfByKey(Persona persona) {
    for (int i = 0; i < listaPersonas.size(); i++) {
        if (listaPersonas.get(i).equals(persona)) {
            return i;
        }
    }
    return -1;
    }
    
}

package com.myApp.logicaDB.dao;

import com.myApp.logicaDB.H2DB;
import com.myApp.modelos.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ImpPersonaDAO implements PersonaDAO {
    @Override
    public void insert(Persona persona) {
        String sql = "INSERT INTO PERSONA(id, nombres, apellidos, email) VALUES(?,?,?,?)";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, persona.getId());
            ps.setString(2, persona.getNombres());
            ps.setString(3, persona.getApellidos());
            ps.setString(4, persona.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert Persona", e);
        }

    }

    @Override
    public void update(Persona persona) {
        String sql = "UPDATE PERSONA SET nombres=?, apellidos=?, email=? WHERE id=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, persona.getNombres());
            ps.setString(2, persona.getApellidos());
            ps.setString(3, persona.getEmail());
            ps.setDouble(4, persona.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update Persona", e);
        }

    }

    @Override
    public void deleteById(double id) {
        String sql = "DELETE FROM PERSONA WHERE id=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete Persona", e);
        }

    }

    @Override
    public Optional<Persona> findById(double id) {
        String sql = "SELECT id, nombres, apellidos, email FROM PERSONA WHERE id=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find Persona by id", e);
        }
    }

    @Override
    public List<Persona> findAll(String filtroTexto) {
        String base = "SELECT id, nombres, apellidos, email FROM PERSONA";
        List<Persona> out = new ArrayList<>();
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = (filtroTexto == null || filtroTexto.isBlank())
                     ? cn.prepareStatement(base + " ORDER BY apellidos, nombres")
                     : cn.prepareStatement(base + " WHERE (LOWER(nombres) LIKE ? OR LOWER(apellidos) LIKE ? OR LOWER(email) LIKE ?) ORDER BY apellidos, nombres")) {

            if (filtroTexto != null && !filtroTexto.isBlank()) {
                String like = "%" + filtroTexto.toLowerCase() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FindAll Persona", e);
        }
        return out;
    }

    @Override
    public boolean existsById(double id) {
        String sql = "SELECT 1 FROM PERSONA WHERE id=?";
        try (Connection cn = H2DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exists Persona", e);
        }
    }

    private Persona map(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getDouble("ID"));
        p.setNombres(rs.getString("NOMBRES"));
        p.setApellidos(rs.getString("APELLIDOS"));
        p.setEmail(rs.getString("EMAIL"));

        System.out.println("Mapeando Persona: " + p);
        return p;
    }







}

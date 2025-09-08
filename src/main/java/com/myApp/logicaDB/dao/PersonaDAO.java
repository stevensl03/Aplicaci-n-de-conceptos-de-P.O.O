package com.myApp.logicaDB.dao;

import com.myApp.modelos.Persona;
import java.util.List;
import java.util.Optional;

public interface PersonaDAO {
    void insert(Persona persona);
    void update(Persona persona);
    void deleteById(double id);
    Optional<Persona> findById(double id);
    List<Persona> findAll(String filtroTexto);
    boolean existsById(double id);
}

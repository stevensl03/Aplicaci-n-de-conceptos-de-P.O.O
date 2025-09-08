package com.myApp.logicaDB.repository;

import com.myApp.modelos.Persona;
import com.myApp.logicaDB.dao.PersonaDAO;

import java.util.List;
import java.util.Optional;

public class PersonaRepository {
    private final PersonaDAO personaDAO;

    public PersonaRepository(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    public void upsert(Persona persona) {
        if (personaDAO.existsById(persona.getId()))
            personaDAO.update(persona);
        personaDAO.insert(persona);
    }

    public void upsert(double id, String nombres, String apellidos, String email) {
        Persona p = new Persona();
        p.setId(id);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setEmail(email);

        if (personaDAO.existsById(id)) {
            personaDAO.update(p);
        } else {
            personaDAO.insert(p);
        }
    }

    public void eliminar(double id) {
        personaDAO.deleteById(id);
    }

    public Optional<Persona> buscar(double id) {
        return personaDAO.findById(id);
    }

    public List<Persona> listar(String filtro) {
        return personaDAO.findAll(filtro);
    }

}

package com.myApp.logicaDeNegocio;

import com.myApp.logicaDB.dao.ImpPersonaDAO;
import com.myApp.logicaDB.dao.PersonaDAO;
import com.myApp.logicaDB.repository.PersonaRepository;
import com.myApp.modelos.*;


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
        inscribir(persona);
        PersonaDAO personaDAO = new ImpPersonaDAO();
        PersonaRepository personaRepository = new PersonaRepository(personaDAO);
        personaRepository.upsert(persona);

    }

    public void cargarDatosDesdeBD() {
        PersonaDAO personaDAO = new ImpPersonaDAO();
        PersonaRepository personaRepository = new PersonaRepository(personaDAO);
        listaPersonas = personaRepository.listar("");
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

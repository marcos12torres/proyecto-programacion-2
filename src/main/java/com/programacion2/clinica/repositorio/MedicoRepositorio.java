package com.programacion2.clinica.repositorio;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.programacion2.clinica.dominio.Medico;

@Repository
public class MedicoRepositorio extends Repositorio<Medico> implements IMedicoRepositorio {

    private static final String ORDEN = "apellido ascending, nombre ascending";

    public MedicoRepositorio() {
        super(Medico.class);
    }

    @Override
    public List<Medico> listarActivos() {
        return buscar("activo == true", Map.of(), ORDEN);
    }

    @Override
    public List<Medico> buscarPorEspecialidad(String especialidad) {
        return buscar("activo == true && especialidad == :especialidad",
                Map.of("especialidad", especialidad), ORDEN);
    }

    @Override
    public boolean existeDni(int dni, int idExcluido) {
        return contar("dni == :dni && id != :id", Map.of("dni", dni, "id", idExcluido)) > 0;
    }

    @Override
    public boolean existeMatricula(int matricula, int idExcluido) {
        return contar("matricula == :matricula && id != :id",
                Map.of("matricula", matricula, "id", idExcluido)) > 0;
    }

    @Override
    public long contarActivos() {
        return contar("activo == true", Map.of());
    }
}

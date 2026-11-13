package com.programacion2.clinica.repositorio;

import java.util.List;

import com.programacion2.clinica.dominio.Medico;

public interface IMedicoRepositorio extends IRepositorio<Medico> {

    List<Medico> listarActivos();

    List<Medico> buscarPorEspecialidad(String especialidad);

    boolean existeDni(int dni, int idExcluido);

    boolean existeMatricula(int matricula, int idExcluido);

    long contarActivos();
}

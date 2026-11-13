package com.programacion2.clinica.repositorio;

import java.util.List;

import com.programacion2.clinica.dominio.Paciente;

public interface IPacienteRepositorio extends IRepositorio<Paciente> {

    List<Paciente> listarActivos();

    // Busca pacientes activos por apellido, nombre o DNI.
    List<Paciente> buscarPorTexto(String texto);

    // true si otro paciente (distinto de idExcluido) ya usa ese DNI.
    boolean existeDni(int dni, int idExcluido);

    long contarActivos();
}

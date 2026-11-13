package com.programacion2.clinica.servicio;

import java.time.LocalDate;
import java.util.List;

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.Turno;

/**
 * Operaciones de negocio sobre los turnos.
 */
public interface ITurnoServicio {

    // Operación principal del negocio: asignar un turno con sus validaciones.
    void asignar(Turno turno, Integer pacienteId, Integer medicoId);

    void confirmar(int id);

    void cancelar(int id);

    List<Turno> buscar(LocalDate fecha, Integer medicoId, EstadoTurno estado);

    List<Turno> listarPorPaciente(int pacienteId);

    List<Turno> listarPorMedico(int medicoId);

    List<Turno> turnosDeHoy();

    long contarPendientes();
}

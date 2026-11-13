package com.programacion2.clinica.repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.Turno;

public interface ITurnoRepositorio extends IRepositorio<Turno> {

    // Guarda el turno relacionándolo con su paciente y su médico.
    void registrar(Turno turno, int pacienteId, int medicoId);

    // Filtro combinado: cualquiera de los parámetros puede ser null.
    List<Turno> buscar(LocalDate fecha, Integer medicoId, EstadoTurno estado);

    List<Turno> listarPorPaciente(int pacienteId);

    List<Turno> listarPorMedico(int medicoId);

    // Turnos no cancelados que ocupan ese horario.
    boolean existeTurnoMedico(int medicoId, LocalDate fecha, LocalTime hora);

    boolean existeTurnoPaciente(int pacienteId, LocalDate fecha, LocalTime hora);

    long contarPorPaciente(int pacienteId);

    long contarPorMedico(int medicoId);

    // Cancela los turnos futuros no cancelados. Devuelve cuántos canceló.
    int cancelarFuturosDePaciente(int pacienteId);

    int cancelarFuturosDeMedico(int medicoId);
}

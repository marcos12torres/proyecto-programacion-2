package com.programacion2.clinica.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.springframework.format.annotation.DateTimeFormat;

import com.programacion2.clinica.excepcion.TurnoNoDisponibleException;

/**
 * Turno médico: une a un Paciente con un Medico en una fecha y hora.
 *
 * Es el lado "muchos" de las dos relaciones uno a muchos:
 *   - muchos turnos -> un paciente (columna PACIENTE_ID)
 *   - muchos turnos -> un médico   (columna MEDICO_ID)
 */
@PersistenceCapable(detachable = "true")
public class Turno {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    // Tipos de fecha/hora reales en lugar de String (se guardan como DATE y TIME).
    // @DateTimeFormat le indica a Spring cómo convertir el texto del formulario.
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    private TipoTurno tipo;

    private String descripcion;

    private EstadoTurno estado;

    // defaultFetchGroup = "true": al leer un turno se traen también
    // su paciente y su médico (se muestran en todos los listados).
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "PACIENTE_ID")
    private Paciente paciente;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "MEDICO_ID")
    private Medico medico;

    public Turno() {
        this.estado = EstadoTurno.PENDIENTE;
    }

    public Turno(LocalDate fecha, LocalTime hora, TipoTurno tipo, String descripcion) {
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.estado = EstadoTurno.PENDIENTE;
    }

    /**
     * Regla del dominio (viene del proyecto Swing):
     * solo un turno PENDIENTE se puede confirmar.
     */
    public void confirmar() {
        if (estado != EstadoTurno.PENDIENTE) {
            throw new TurnoNoDisponibleException(
                    "Solo se pueden confirmar turnos pendientes. Estado actual: " + estado);
        }
        estado = EstadoTurno.CONFIRMADO;
    }

    /**
     * Un turno ya cancelado no se puede volver a cancelar.
     */
    public void cancelar() {
        if (estado == EstadoTurno.CANCELADO) {
            throw new TurnoNoDisponibleException("El turno ya está cancelado.");
        }
        estado = EstadoTurno.CANCELADO;
    }

    public boolean isCancelado() {
        return estado == EstadoTurno.CANCELADO;
    }

    public int getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public TipoTurno getTipo() {
        return tipo;
    }

    public void setTipo(TipoTurno tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    @Override
    public String toString() {
        return "Turno{id=" + id + ", fecha=" + fecha + ", hora=" + hora
                + ", tipo=" + tipo + ", estado=" + estado + "}";
    }
}

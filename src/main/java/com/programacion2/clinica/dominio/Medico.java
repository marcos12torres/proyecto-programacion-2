package com.programacion2.clinica.dominio;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;

/**
 * Médico de la clínica.
 *
 * Relación UNO A MUCHOS: un médico atiende muchos turnos (su agenda).
 */
@PersistenceCapable(detachable = "true")
public class Medico extends Persona {

    // La matrícula profesional no se puede repetir.
    @Unique
    private int matricula;

    private String especialidad;

    // @Order: lista ordenada por fecha y hora (sin columna de índice extra).
    @Persistent(mappedBy = "medico")
    @Order(extensions = @Extension(vendorName = "datanucleus",
            key = "list-ordering", value = "fecha ASC, hora ASC"))
    private List<Turno> turnos;

    public Medico() {
        super();
        this.turnos = new ArrayList<>();
    }

    public Medico(String nombre, String apellido, int dni, int matricula, String especialidad) {
        super(nombre, apellido, dni);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.turnos = new ArrayList<>();
    }

    // Equivale a registrarTurno() del proyecto Swing.
    public void agregarTurno(Turno turno) {
        turnos.add(turno);
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }

    @Override
    public String toString() {
        return "Medico{" + super.toString() + ", matricula=" + matricula
                + ", especialidad='" + especialidad + "'}";
    }
}

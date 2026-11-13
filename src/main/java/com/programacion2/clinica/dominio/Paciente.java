package com.programacion2.clinica.dominio;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Paciente de la clínica.
 *
 * Relación UNO A MUCHOS: un paciente tiene muchos turnos.
 * mappedBy = "paciente" indica que la clave foránea está en la tabla
 * de Turno (columna PACIENTE_ID); Turno es el "lado dueño" de la relación.
 */
@PersistenceCapable(detachable = "true")
public class Paciente extends Persona {

    private String obraSocial;

    private String telefono;

    // No se carga por defecto: el historial se consulta con TurnoRepositorio
    // (ordenado por fecha) para no traer todos los turnos en cada listado.
    // @Order: lista ordenada por fecha y hora (sin columna de índice extra).
    @Persistent(mappedBy = "paciente")
    @Order(extensions = @Extension(vendorName = "datanucleus",
            key = "list-ordering", value = "fecha ASC, hora ASC"))
    private List<Turno> turnos;

    public Paciente() {
        super();
        this.turnos = new ArrayList<>();
    }

    public Paciente(String nombre, String apellido, int dni, String obraSocial, String telefono) {
        super(nombre, apellido, dni);
        this.obraSocial = obraSocial;
        this.telefono = telefono;
        this.turnos = new ArrayList<>();
    }

    // Equivale a solicitarTurno() del proyecto Swing.
    public void agregarTurno(Turno turno) {
        turnos.add(turno);
    }

    public String getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }

    @Override
    public String toString() {
        return "Paciente{" + super.toString() + ", obraSocial='" + obraSocial + "'}";
    }
}

package com.programacion2.clinica.dominio;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

/**
 * Datos comunes de Paciente y Medico.
 *
 * COMPLETE_TABLE: cada subclase tiene su propia tabla con TODAS las
 * columnas (las de Persona + las suyas). Persona no tiene tabla.
 */
@PersistenceCapable(detachable = "true")
@Inheritance(strategy = InheritanceStrategy.COMPLETE_TABLE)
public abstract class Persona implements Comparable<Persona> {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    private String nombre;

    private String apellido;

    // El DNI no se puede repetir dentro de la misma tabla.
    @Unique
    private int dni;

    // Baja lógica: si es false la persona no aparece en los listados
    // y no se le pueden asignar turnos, pero su historial se conserva.
    private boolean activo;

    public Persona() {
        this.activo = true;
    }

    public Persona(String nombre, String apellido, int dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.activo = true;
    }

    // Nombre listo para mostrar en tablas y desplegables.
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Dos personas son iguales si tienen el mismo DNI (igual que en el proyecto Swing).
    @Override
    public int hashCode() {
        return dni;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Persona other = (Persona) obj;
        return dni == other.dni;
    }

    // Orden natural: por apellido.
    @Override
    public int compareTo(Persona o) {
        return this.apellido.compareToIgnoreCase(o.apellido);
    }

    @Override
    public String toString() {
        return "DNI: " + dni +
               " Nombre: " + nombre +
               " Apellido: " + apellido;
    }
}

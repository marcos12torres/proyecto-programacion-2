package com.programacion2.clinica.dominio;

/**
 * Reemplaza a las subclases Consulta, Estudio y Cirugia del proyecto Swing.
 * JDO guarda el enum como texto (CONSULTA, ESTUDIO, CIRUGIA).
 */
public enum TipoTurno {

    CONSULTA("Consulta"),
    ESTUDIO("Estudio"),
    CIRUGIA("Cirugía");

    private final String descripcion;

    TipoTurno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

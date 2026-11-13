package com.programacion2.clinica.excepcion;

/**
 * Se lanza cuando una operación viola una regla del negocio
 * (DNI repetido, turno superpuesto, fecha pasada, etc.).
 *
 * Los controladores la capturan y muestran getMessage() al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

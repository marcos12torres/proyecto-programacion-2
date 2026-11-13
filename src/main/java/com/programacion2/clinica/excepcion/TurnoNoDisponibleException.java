package com.programacion2.clinica.excepcion;

/**
 * Excepción del proyecto Swing, ahora como caso particular de
 * ReglaNegocioException: el turno no está disponible para la operación.
 */
public class TurnoNoDisponibleException extends ReglaNegocioException {

    public TurnoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}

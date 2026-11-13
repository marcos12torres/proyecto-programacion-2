package com.programacion2.clinica.dominio;

/**
 * Estados posibles de un turno (mismo enum del proyecto Swing).
 *
 *   PENDIENTE  --confirmar()-->  CONFIRMADO
 *   PENDIENTE / CONFIRMADO  --cancelar()-->  CANCELADO
 */
public enum EstadoTurno {
    PENDIENTE,
    CONFIRMADO,
    CANCELADO
}

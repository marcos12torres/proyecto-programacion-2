package com.programacion2.clinica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.TipoTurno;
import com.programacion2.clinica.dominio.Turno;
import com.programacion2.clinica.excepcion.TurnoNoDisponibleException;

/**
 * Pruebas de las reglas del dominio de Turno (no necesitan base de datos).
 */
class TurnoTest {

    private Turno nuevoTurno() {
        return new Turno(LocalDate.now().plusDays(1), LocalTime.of(9, 0), TipoTurno.CONSULTA, "Control");
    }

    @Test
    void unTurnoNuevoEstaPendiente() {
        assertEquals(EstadoTurno.PENDIENTE, nuevoTurno().getEstado());
    }

    @Test
    void sePuedeConfirmarUnTurnoPendiente() {
        Turno turno = nuevoTurno();
        turno.confirmar();
        assertEquals(EstadoTurno.CONFIRMADO, turno.getEstado());
    }

    @Test
    void noSePuedeConfirmarUnTurnoCancelado() {
        Turno turno = nuevoTurno();
        turno.cancelar();
        assertThrows(TurnoNoDisponibleException.class, turno::confirmar);
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        Turno turno = nuevoTurno();
        turno.cancelar();
        assertThrows(TurnoNoDisponibleException.class, turno::cancelar);
    }
}

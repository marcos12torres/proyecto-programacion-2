package com.programacion2.clinica.servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.Medico;
import com.programacion2.clinica.dominio.Paciente;
import com.programacion2.clinica.dominio.Turno;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.excepcion.TurnoNoDisponibleException;
import com.programacion2.clinica.repositorio.IMedicoRepositorio;
import com.programacion2.clinica.repositorio.IPacienteRepositorio;
import com.programacion2.clinica.repositorio.ITurnoRepositorio;

@Service
public class TurnoServicio implements ITurnoServicio {

    // Horario de atención de la clínica. Los turnos duran 30 minutos.
    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime ULTIMO_TURNO = LocalTime.of(19, 30);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private ITurnoRepositorio repositorio;

    @Autowired
    private IPacienteRepositorio pacienteRepositorio;

    @Autowired
    private IMedicoRepositorio medicoRepositorio;

    /**
     * Asigna un turno validando todas las reglas del negocio.
     * Si alguna no se cumple lanza ReglaNegocioException y no se guarda nada.
     */
    @Override
    public void asignar(Turno turno, Integer pacienteId, Integer medicoId) {

        // 1. Datos obligatorios.
        if (pacienteId == null) {
            throw new ReglaNegocioException("Debe seleccionar un paciente.");
        }
        if (medicoId == null) {
            throw new ReglaNegocioException("Debe seleccionar un médico.");
        }
        if (turno.getFecha() == null || turno.getHora() == null) {
            throw new ReglaNegocioException("La fecha y la hora del turno son obligatorias.");
        }
        if (turno.getTipo() == null) {
            throw new ReglaNegocioException("Debe seleccionar el tipo de turno.");
        }
        turno.setDescripcion(Validaciones.opcional(turno.getDescripcion(), "descripción"));

        // 2. El paciente y el médico deben existir y estar activos.
        Paciente paciente = pacienteRepositorio.consultar(pacienteId);
        if (paciente == null || !paciente.isActivo()) {
            throw new ReglaNegocioException("El paciente seleccionado no existe o fue dado de baja.");
        }
        Medico medico = medicoRepositorio.consultar(medicoId);
        if (medico == null || !medico.isActivo()) {
            throw new ReglaNegocioException("El médico seleccionado no existe o fue dado de baja.");
        }

        // 3. Fecha y horario válidos.
        validarHorario(turno.getFecha(), turno.getHora());

        // 4. Turnos superpuestos.
        if (repositorio.existeTurnoMedico(medicoId, turno.getFecha(), turno.getHora())) {
            throw new TurnoNoDisponibleException("El Dr./Dra. " + medico.getNombreCompleto()
                    + " ya tiene un turno el " + turno.getFecha().format(FORMATO_FECHA) + " a las " + turno.getHora() + ".");
        }
        if (repositorio.existeTurnoPaciente(pacienteId, turno.getFecha(), turno.getHora())) {
            throw new TurnoNoDisponibleException("El paciente " + paciente.getNombreCompleto()
                    + " ya tiene otro turno el " + turno.getFecha().format(FORMATO_FECHA) + " a las " + turno.getHora() + ".");
        }

        // 5. Todo correcto: se guarda con estado PENDIENTE.
        repositorio.registrar(turno, pacienteId, medicoId);
    }

    private void validarHorario(LocalDate fecha, LocalTime hora) {

        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se pueden asignar turnos en una fecha u hora pasada.");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ReglaNegocioException("La clínica no atiende los domingos.");
        }
        if (hora.isBefore(APERTURA) || hora.isAfter(ULTIMO_TURNO)) {
            throw new ReglaNegocioException(
                    "El horario de atención es de " + APERTURA + " a " + ULTIMO_TURNO + ".");
        }
        if (hora.getMinute() % 30 != 0 || hora.getSecond() != 0) {
            throw new ReglaNegocioException(
                    "Los turnos son cada 30 minutos (por ejemplo 09:00 o 09:30).");
        }
    }

    @Override
    public void confirmar(int id) {
        buscarPorId(id);
        // La regla "solo se confirma un turno PENDIENTE" está en Turno.confirmar().
        repositorio.actualizar(id, Turno::confirmar);
    }

    @Override
    public void cancelar(int id) {
        buscarPorId(id);
        repositorio.actualizar(id, Turno::cancelar);
    }

    private Turno buscarPorId(int id) {
        Turno turno = repositorio.consultar(id);
        if (turno == null) {
            throw new ReglaNegocioException("El turno solicitado no existe.");
        }
        return turno;
    }

    @Override
    public List<Turno> buscar(LocalDate fecha, Integer medicoId, EstadoTurno estado) {
        return repositorio.buscar(fecha, medicoId, estado);
    }

    @Override
    public List<Turno> turnosDeHoy() {
        return repositorio.buscar(LocalDate.now(), null, null);
    }

    @Override
    public long contarPendientes() {
        return repositorio.buscar(null, null, EstadoTurno.PENDIENTE).size();
    }

    @Override
    public List<Turno> listarPorPaciente(int pacienteId) {
        return repositorio.listarPorPaciente(pacienteId);
    }

    @Override
    public List<Turno> listarPorMedico(int medicoId) {
        return repositorio.listarPorMedico(medicoId);
    }
}

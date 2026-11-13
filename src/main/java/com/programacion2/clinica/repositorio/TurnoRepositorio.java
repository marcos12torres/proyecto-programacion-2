package com.programacion2.clinica.repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.Medico;
import com.programacion2.clinica.dominio.Paciente;
import com.programacion2.clinica.dominio.Turno;

@Repository
public class TurnoRepositorio extends Repositorio<Turno> implements ITurnoRepositorio {

    private static final String ORDEN = "fecha ascending, hora ascending";

    public TurnoRepositorio() {
        super(Turno.class);
    }

    @Override
    public void registrar(Turno turno, int pacienteId, int medicoId) {

        PersistenceManager pm = pmf.getPersistenceManager();
        // El turno queda DETACHED con sus datos después del commit.
        pm.setDetachAllOnCommit(true);
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

            /*
             * Buscamos paciente y médico DENTRO de la misma transacción,
             * así JDO los administra y la relación se guarda con sus
             * claves foráneas (PACIENTE_ID y MEDICO_ID).
             */
            Paciente paciente = pm.getObjectById(Paciente.class, pacienteId);
            Medico medico = pm.getObjectById(Medico.class, medicoId);

            // Mantenemos los dos lados de cada relación uno a muchos.
            turno.setPaciente(paciente);
            paciente.agregarTurno(turno);

            turno.setMedico(medico);
            medico.agregarTurno(turno);

            pm.makePersistent(turno);

            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            pm.close();
        }
    }

    @Override
    public List<Turno> buscar(LocalDate fecha, Integer medicoId, EstadoTurno estado) {

        // Armamos el filtro solo con los criterios que vienen cargados.
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> parametros = new HashMap<>();

        if (fecha != null) {
            condiciones.add("fecha == :fecha");
            parametros.put("fecha", fecha);
        }
        if (medicoId != null) {
            condiciones.add("medico.id == :medicoId");
            parametros.put("medicoId", medicoId);
        }
        if (estado != null) {
            condiciones.add("estado == :estado");
            parametros.put("estado", estado);
        }

        String filtro = condiciones.isEmpty() ? null : String.join(" && ", condiciones);

        return buscar(filtro, parametros, ORDEN);
    }

    @Override
    public List<Turno> listarPorPaciente(int pacienteId) {
        return buscar("paciente.id == :id", Map.of("id", pacienteId), ORDEN);
    }

    @Override
    public List<Turno> listarPorMedico(int medicoId) {
        return buscar("medico.id == :id", Map.of("id", medicoId), ORDEN);
    }

    @Override
    public boolean existeTurnoMedico(int medicoId, LocalDate fecha, LocalTime hora) {
        return existeTurno("medico.id", medicoId, fecha, hora);
    }

    @Override
    public boolean existeTurnoPaciente(int pacienteId, LocalDate fecha, LocalTime hora) {
        return existeTurno("paciente.id", pacienteId, fecha, hora);
    }

    private boolean existeTurno(String campo, int id, LocalDate fecha, LocalTime hora) {

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("id", id);
        parametros.put("fecha", fecha);
        parametros.put("hora", hora);
        parametros.put("cancelado", EstadoTurno.CANCELADO);

        String filtro = campo + " == :id && fecha == :fecha && hora == :hora"
                + " && estado != :cancelado";

        return contar(filtro, parametros) > 0;
    }

    @Override
    public long contarPorPaciente(int pacienteId) {
        return contar("paciente.id == :id", Map.of("id", pacienteId));
    }

    @Override
    public long contarPorMedico(int medicoId) {
        return contar("medico.id == :id", Map.of("id", medicoId));
    }

    @Override
    public int cancelarFuturosDePaciente(int pacienteId) {
        return cancelarFuturos("paciente.id", pacienteId);
    }

    @Override
    public int cancelarFuturosDeMedico(int medicoId) {
        return cancelarFuturos("medico.id", medicoId);
    }

    /*
     * Cancela, en UNA transacción, todos los turnos desde hoy en adelante
     * que todavía no estén cancelados.
     */
    private int cancelarFuturos(String campo, int id) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        Query<Turno> query = null;

        try {
            tx.begin();

            query = pm.newQuery(Turno.class);
            query.setFilter(campo + " == :id && fecha >= :hoy && estado != :cancelado");

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("id", id);
            parametros.put("hoy", LocalDate.now());
            parametros.put("cancelado", EstadoTurno.CANCELADO);
            query.setNamedParameters(parametros);

            List<Turno> turnos = query.executeList();

            // Los objetos están administrados: cancelar() genera el UPDATE al hacer commit.
            for (Turno turno : turnos) {
                turno.cancelar();
            }

            tx.commit();

            return turnos.size();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            if (query != null) {
                query.closeAll();
            }
            pm.close();
        }
    }
}

package com.programacion2.clinica.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.clinica.dominio.Medico;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.repositorio.IMedicoRepositorio;
import com.programacion2.clinica.repositorio.ITurnoRepositorio;

@Service
public class MedicoServicio implements IServicio<Medico> {

    @Autowired
    private IMedicoRepositorio repositorio;

    @Autowired
    private ITurnoRepositorio turnoRepositorio;

    @Override
    public void guardar(Medico medico) {
        validar(medico, 0);
        repositorio.guardar(medico);
    }

    @Override
    public List<Medico> consultar() {
        return repositorio.listarActivos();
    }

    // Filtro por especialidad. Sin especialidad devuelve el listado completo.
    public List<Medico> buscarPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            return consultar();
        }
        return repositorio.buscarPorEspecialidad(especialidad);
    }

    // Especialidades distintas de los médicos activos (para el filtro de la vista).
    public List<String> listarEspecialidades() {
        return consultar().stream()
                .map(Medico::getEspecialidad)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Override
    public Medico buscarPorId(int id) {
        Medico medico = repositorio.consultar(id);
        if (medico == null) {
            throw new ReglaNegocioException("El médico solicitado no existe.");
        }
        return medico;
    }

    @Override
    public void actualizar(int id, Medico datos) {

        Medico actual = buscarPorId(id);
        if (!actual.isActivo()) {
            throw new ReglaNegocioException("No se puede modificar un médico dado de baja.");
        }

        validar(datos, id);

        repositorio.actualizar(id, medico -> {
            medico.setNombre(datos.getNombre());
            medico.setApellido(datos.getApellido());
            medico.setDni(datos.getDni());
            medico.setMatricula(datos.getMatricula());
            medico.setEspecialidad(datos.getEspecialidad());
        });
    }

    @Override
    public boolean eliminar(int id) {

        buscarPorId(id);

        if (turnoRepositorio.contarPorMedico(id) == 0) {
            repositorio.eliminar(id);
            return false;
        }

        // Baja lógica: se conserva la historia clínica de sus pacientes
        // y se cancelan los turnos que tenía agendados a futuro.
        turnoRepositorio.cancelarFuturosDeMedico(id);
        repositorio.actualizar(id, medico -> medico.setActivo(false));
        return true;
    }

    public long contarActivos() {
        return repositorio.contarActivos();
    }

    private void validar(Medico medico, int idActual) {

        medico.setNombre(Validaciones.obligatorio(medico.getNombre(), "nombre"));
        medico.setApellido(Validaciones.obligatorio(medico.getApellido(), "apellido"));
        medico.setEspecialidad(Validaciones.obligatorio(medico.getEspecialidad(), "especialidad"));

        Validaciones.rango(medico.getDni(), 1_000_000, 99_999_999,
                "El DNI debe tener 7 u 8 dígitos.");
        Validaciones.rango(medico.getMatricula(), 1, 999_999,
                "La matrícula debe ser un número entre 1 y 999999.");

        if (repositorio.existeDni(medico.getDni(), idActual)) {
            throw new ReglaNegocioException(
                    "Ya existe un médico registrado con el DNI " + medico.getDni() + ".");
        }
        if (repositorio.existeMatricula(medico.getMatricula(), idActual)) {
            throw new ReglaNegocioException(
                    "Ya existe un médico con la matrícula " + medico.getMatricula() + ".");
        }
    }
}

package com.programacion2.clinica.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.clinica.dominio.Paciente;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.repositorio.IPacienteRepositorio;
import com.programacion2.clinica.repositorio.ITurnoRepositorio;

// Registra esta clase como un componente de la capa de servicios.
@Service
public class PacienteServicio implements IServicio<Paciente> {

    // Spring inyecta los repositorios en estos atributos.
    @Autowired
    private IPacienteRepositorio repositorio;

    @Autowired
    private ITurnoRepositorio turnoRepositorio;

    @Override
    public void guardar(Paciente paciente) {
        validar(paciente, 0);
        repositorio.guardar(paciente);
    }

    @Override
    public List<Paciente> consultar() {
        return repositorio.listarActivos();
    }

    // Búsqueda por apellido, nombre o DNI. Sin texto devuelve el listado completo.
    public List<Paciente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return consultar();
        }
        return repositorio.buscarPorTexto(texto.trim());
    }

    @Override
    public Paciente buscarPorId(int id) {
        Paciente paciente = repositorio.consultar(id);
        if (paciente == null) {
            throw new ReglaNegocioException("El paciente solicitado no existe.");
        }
        return paciente;
    }

    @Override
    public void actualizar(int id, Paciente datos) {

        Paciente actual = buscarPorId(id);
        if (!actual.isActivo()) {
            throw new ReglaNegocioException("No se puede modificar un paciente dado de baja.");
        }

        validar(datos, id);

        // Copiamos los datos del formulario al objeto persistente.
        repositorio.actualizar(id, paciente -> {
            paciente.setNombre(datos.getNombre());
            paciente.setApellido(datos.getApellido());
            paciente.setDni(datos.getDni());
            paciente.setObraSocial(datos.getObraSocial());
            paciente.setTelefono(datos.getTelefono());
        });
    }

    @Override
    public boolean eliminar(int id) {

        buscarPorId(id);

        // Sin turnos: se puede borrar sin dejar datos huérfanos.
        if (turnoRepositorio.contarPorPaciente(id) == 0) {
            repositorio.eliminar(id);
            return false;
        }

        // Con turnos: baja lógica para conservar el historial,
        // y se cancelan sus turnos futuros para liberar la agenda de los médicos.
        turnoRepositorio.cancelarFuturosDePaciente(id);
        repositorio.actualizar(id, paciente -> paciente.setActivo(false));
        return true;
    }

    public long contarActivos() {
        return repositorio.contarActivos();
    }

    /*
     * Reglas de negocio del paciente. Normaliza los textos (trim)
     * y lanza ReglaNegocioException si algo no es válido.
     */
    private void validar(Paciente paciente, int idActual) {

        paciente.setNombre(Validaciones.obligatorio(paciente.getNombre(), "nombre"));
        paciente.setApellido(Validaciones.obligatorio(paciente.getApellido(), "apellido"));

        Validaciones.rango(paciente.getDni(), 1_000_000, 99_999_999,
                "El DNI debe tener 7 u 8 dígitos.");

        String obraSocial = Validaciones.opcional(paciente.getObraSocial(), "obra social");
        paciente.setObraSocial(obraSocial == null ? "Particular" : obraSocial);

        String telefono = Validaciones.opcional(paciente.getTelefono(), "teléfono");
        if (telefono != null && !telefono.matches("[0-9 +()-]{6,20}")) {
            throw new ReglaNegocioException(
                    "El teléfono solo puede contener números, espacios, +, ( ) y guiones.");
        }
        paciente.setTelefono(telefono);

        if (repositorio.existeDni(paciente.getDni(), idActual)) {
            throw new ReglaNegocioException(
                    "Ya existe un paciente registrado con el DNI " + paciente.getDni() + ".");
        }
    }
}

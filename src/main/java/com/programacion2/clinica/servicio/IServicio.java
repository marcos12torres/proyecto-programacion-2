package com.programacion2.clinica.servicio;

import java.util.List;

/**
 * Operaciones ABM (alta, baja, modificación y consulta)
 * de las entidades principales: Paciente y Medico.
 */
public interface IServicio<T> {

    void guardar(T objeto);

    // Lista los registros activos.
    List<T> consultar();

    // Devuelve el registro o lanza ReglaNegocioException si no existe.
    T buscarPorId(int id);

    void actualizar(int id, T datos);

    /**
     * Elimina el registro. Si tiene turnos asociados hace una baja lógica.
     *
     * @return true si fue baja lógica, false si se borró de la base.
     */
    boolean eliminar(int id);
}

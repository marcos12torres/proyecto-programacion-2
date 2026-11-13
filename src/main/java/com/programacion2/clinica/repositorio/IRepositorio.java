package com.programacion2.clinica.repositorio;

import java.util.List;
import java.util.function.Consumer;

/**
 * Operaciones de acceso a datos comunes a todas las entidades.
 */
public interface IRepositorio<T> {

    void guardar(T objeto);

    void guardar(List<T> objetos);

    // Devuelve el objeto (desconectado de JDO) o null si no existe.
    T consultar(int id);

    List<T> listar();

    // Busca el objeto por id y le aplica los cambios dentro de una transacción.
    void actualizar(int id, Consumer<T> cambios);

    void eliminar(int id);
}

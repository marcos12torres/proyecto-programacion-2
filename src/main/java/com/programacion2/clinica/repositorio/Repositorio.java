package com.programacion2.clinica.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Repositorio genérico con DataNucleus JDO (basado en proyecto-mvc).
 *
 * Es abstracto: cada entidad tiene su repositorio concreto
 * (PacienteRepositorio, MedicoRepositorio, TurnoRepositorio) que le
 * indica la clase con la que trabaja y agrega sus consultas propias.
 *
 * Patrón que se repite en TODOS los métodos:
 *   1. pmf.getPersistenceManager()  -> abre el PersistenceManager
 *   2. tx.begin()                   -> inicia la transacción
 *   3. operación JDO
 *   4. tx.commit()                  -> confirma los cambios
 *   5. catch: tx.rollback()         -> deshace si hubo error
 *   6. finally: pm.close()          -> libera los recursos SIEMPRE
 */
public abstract class Repositorio<T> implements IRepositorio<T> {

    // Spring inyecta la fábrica definida en Configuracion.
    @Autowired
    protected PersistenceManagerFactory pmf;

    // Clase de la entidad (Paciente.class, Medico.class, Turno.class).
    protected final Class<T> clase;

    protected Repositorio(Class<T> clase) {
        this.clase = clase;
    }

    @Override
    public void guardar(T objeto) {

        PersistenceManager pm = pmf.getPersistenceManager();

        /*
         * Al hacer commit el objeto queda DETACHED con sus valores cargados,
         * así el controlador puede seguir leyéndolo (por ejemplo, para el
         * mensaje de confirmación) aunque el PersistenceManager esté cerrado.
         */
        pm.setDetachAllOnCommit(true);
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

            /*
             * El objeto creado con new está TRANSIENT.
             * makePersistent lo pasa a PERSISTENT_NEW y commit()
             * lo inserta definitivamente en la base de datos.
             */
            pm.makePersistent(objeto);

            tx.commit();

        } catch (RuntimeException e) {

            // Si algo falló, deshacemos para no dejar datos a medias.
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            // Se ejecuta siempre: liberamos el PersistenceManager.
            pm.close();
        }
    }

    @Override
    public void guardar(List<T> objetos) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            pm.makePersistentAll(objetos);
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
    public T consultar(int id) {

        PersistenceManager pm = pmf.getPersistenceManager();

        /*
         * Al hacer commit, los objetos quedan DETACHED (desconectados):
         * se pueden seguir usando en el servicio y en la vista aunque
         * el PersistenceManager ya esté cerrado.
         */
        pm.setDetachAllOnCommit(true);
        pm.getFetchPlan().setMaxFetchDepth(2);
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            T objeto = pm.getObjectById(clase, id);
            pm.retrieve(objeto);
            tx.commit();
            return objeto;

        } catch (JDOObjectNotFoundException e) {
            // No existe un registro con ese id.
            if (tx.isActive()) {
                tx.rollback();
            }
            return null;

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
    public List<T> listar() {
        return buscar(null, Map.of(), null);
    }

    @Override
    public void actualizar(int id, Consumer<T> cambios) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

            /*
             * El objeto obtenido está PERSISTENT_CLEAN (administrado por JDO).
             * Al modificarlo con sus setters pasa a PERSISTENT_DIRTY y
             * commit() ejecuta el UPDATE en la base de datos.
             */
            T objeto = pm.getObjectById(clase, id);
            cambios.accept(objeto);

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
    public void eliminar(int id) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            T objeto = pm.getObjectById(clase, id);
            pm.deletePersistent(objeto);
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

    /**
     * Consulta JDOQL reutilizable por los repositorios concretos.
     *
     * @param filtro     condición JDOQL, por ejemplo "activo == true" (null = sin filtro)
     * @param parametros valores de los parámetros con nombre (:parametro)
     * @param orden      ordenamiento, por ejemplo "apellido ascending" (null = sin orden)
     */
    protected List<T> buscar(String filtro, Map<String, Object> parametros, String orden) {

        PersistenceManager pm = pmf.getPersistenceManager();

        // Permite recuperar las relaciones directas (ej.: el paciente de un turno).
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();
        Query<T> query = null;

        try {
            tx.begin();

            query = pm.newQuery(clase);

            if (filtro != null) {
                query.setFilter(filtro);
                query.setNamedParameters(parametros);
            }
            if (orden != null) {
                query.setOrdering(orden);
            }

            List<T> resultados = query.executeList();

            // Fuerza la carga de los campos del FetchPlan.
            pm.retrieveAll(resultados, true);

            // Copias desconectadas para usarlas con el PM cerrado.
            List<T> resultadosSeparados = new ArrayList<>(pm.detachCopyAll(resultados));

            tx.commit();

            return resultadosSeparados;

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

    /**
     * Cuenta los registros que cumplen el filtro (SELECT COUNT).
     */
    protected long contar(String filtro, Map<String, Object> parametros) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        Query<T> query = null;

        try {
            tx.begin();

            query = pm.newQuery(clase);
            query.setFilter(filtro);
            query.setNamedParameters(parametros);
            query.setResult("count(this)");

            Long cantidad = query.executeResultUnique(Long.class);

            tx.commit();

            return cantidad == null ? 0 : cantidad;

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

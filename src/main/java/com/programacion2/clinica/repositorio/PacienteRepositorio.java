package com.programacion2.clinica.repositorio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.programacion2.clinica.dominio.Paciente;

@Repository
public class PacienteRepositorio extends Repositorio<Paciente> implements IPacienteRepositorio {

    private static final String ORDEN = "apellido ascending, nombre ascending";

    public PacienteRepositorio() {
        super(Paciente.class);
    }

    @Override
    public List<Paciente> listarActivos() {
        return buscar("activo == true", Map.of(), ORDEN);
    }

    @Override
    public List<Paciente> buscarPorTexto(String texto) {

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("texto", texto.toLowerCase());

        // Si el texto es numérico también se compara contra el DNI.
        int dni = texto.matches("\\d{1,9}") ? Integer.parseInt(texto) : -1;
        parametros.put("dni", dni);

        String filtro = "activo == true && ("
                + "apellido.toLowerCase().indexOf(:texto) >= 0 || "
                + "nombre.toLowerCase().indexOf(:texto) >= 0 || "
                + "dni == :dni)";

        return buscar(filtro, parametros, ORDEN);
    }

    @Override
    public boolean existeDni(int dni, int idExcluido) {
        // Incluye pacientes dados de baja: el DNI sigue siendo único en la tabla.
        return contar("dni == :dni && id != :id", Map.of("dni", dni, "id", idExcluido)) > 0;
    }

    @Override
    public long contarActivos() {
        return contar("activo == true", Map.of());
    }
}

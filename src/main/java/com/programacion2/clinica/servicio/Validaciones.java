package com.programacion2.clinica.servicio;

import com.programacion2.clinica.excepcion.ReglaNegocioException;

/**
 * Validaciones reutilizadas por los servicios.
 * Si un dato no es válido se lanza ReglaNegocioException con un mensaje claro.
 */
final class Validaciones {

    // Los String se guardan como VARCHAR(255).
    static final int LARGO_MAXIMO = 255;

    private Validaciones() {
    }

    /** Devuelve el texto sin espacios sobrantes, o error si está vacío. */
    static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        return largoMaximo(valor.trim(), campo);
    }

    /** Devuelve el texto sin espacios sobrantes, o null si está vacío. */
    static String opcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return largoMaximo(valor.trim(), campo);
    }

    static void rango(int valor, int minimo, int maximo, String mensaje) {
        if (valor < minimo || valor > maximo) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private static String largoMaximo(String valor, String campo) {
        if (valor.length() > LARGO_MAXIMO) {
            throw new ReglaNegocioException(
                    "El campo " + campo + " no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
        return valor;
    }
}

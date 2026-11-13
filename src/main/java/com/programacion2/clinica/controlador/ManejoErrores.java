package com.programacion2.clinica.controlador;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.clinica.excepcion.ReglaNegocioException;

/**
 * Atrapa las excepciones que no se manejaron en los controladores
 * (por ejemplo, abrir /pacientes/999 cuando no existe, o confirmar
 * un turno cancelado) y vuelve al inicio con un mensaje de error.
 */
@ControllerAdvice
public class ManejoErrores {

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaNegocio(ReglaNegocioException e, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }

    // Ej.: /pacientes/abc (el id no es un número).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String parametroInvalido(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "La dirección solicitada no es válida.");
        return "redirect:/";
    }
}

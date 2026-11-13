package com.programacion2.clinica.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.clinica.dominio.Paciente;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.servicio.ITurnoServicio;
import com.programacion2.clinica.servicio.PacienteServicio;

/**
 * ABM de pacientes + búsqueda + ficha con historial de turnos.
 * Todas las URLs empiezan con /pacientes.
 */
@Controller
@RequestMapping("/pacientes")
public class PacienteControlador {

    private final PacienteServicio servicio;
    private final ITurnoServicio turnoServicio;

    public PacienteControlador(PacienteServicio servicio, ITurnoServicio turnoServicio) {
        this.servicio = servicio;
        this.turnoServicio = turnoServicio;
    }

    // GET /pacientes?buscar=texto -> listado (filtrado si viene "buscar").
    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("pacientes", servicio.buscar(buscar));
        model.addAttribute("buscar", buscar);
        return "pacientes/lista";
    }

    // GET /pacientes/nuevo -> formulario vacío.
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("paciente", new Paciente());
        return formulario(model, "/pacientes", "Registrar paciente");
    }

    // POST /pacientes -> guarda el paciente enviado por el formulario.
    @PostMapping
    public String guardar(@ModelAttribute("paciente") Paciente paciente,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirect) {

        // Errores de conversión (por ejemplo, letras en el DNI).
        if (resultado.hasErrors()) {
            model.addAttribute("error", "El DNI debe ser un número sin puntos.");
            return formulario(model, "/pacientes", "Registrar paciente");
        }

        try {
            servicio.guardar(paciente);
        } catch (ReglaNegocioException e) {
            // Volvemos al formulario con los datos cargados y el mensaje de error.
            model.addAttribute("error", e.getMessage());
            return formulario(model, "/pacientes", "Registrar paciente");
        }

        // Mensaje que sobrevive al redirect (flash attribute).
        redirect.addFlashAttribute("exito",
                "Paciente " + paciente.getNombreCompleto() + " registrado correctamente.");
        return "redirect:/pacientes";
    }

    // GET /pacientes/5 -> ficha del paciente con su historial de turnos.
    @GetMapping("/{id}")
    public String ficha(@PathVariable int id, Model model) {
        model.addAttribute("paciente", servicio.buscarPorId(id));
        model.addAttribute("turnos", turnoServicio.listarPorPaciente(id));
        return "pacientes/ficha";
    }

    // GET /pacientes/5/editar -> formulario con los datos actuales.
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        model.addAttribute("paciente", servicio.buscarPorId(id));
        return formulario(model, "/pacientes/" + id, "Modificar paciente");
    }

    // POST /pacientes/5 -> guarda las modificaciones.
    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id,
                             @ModelAttribute("paciente") Paciente paciente,
                             BindingResult resultado,
                             Model model,
                             RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("error", "El DNI debe ser un número sin puntos.");
            return formulario(model, "/pacientes/" + id, "Modificar paciente");
        }

        try {
            servicio.actualizar(id, paciente);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "/pacientes/" + id, "Modificar paciente");
        }

        redirect.addFlashAttribute("exito", "Paciente modificado correctamente.");
        return "redirect:/pacientes/" + id;
    }

    // POST /pacientes/5/eliminar -> baja física o lógica según tenga turnos.
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redirect) {

        boolean bajaLogica = servicio.eliminar(id);

        redirect.addFlashAttribute("exito", bajaLogica
                ? "El paciente tenía turnos: se dio de baja (se conserva su historial "
                  + "y se cancelaron sus turnos futuros)."
                : "Paciente eliminado correctamente.");
        return "redirect:/pacientes";
    }

    // Datos comunes que necesita la vista del formulario (alta y modificación).
    private String formulario(Model model, String accion, String titulo) {
        model.addAttribute("accion", accion);
        model.addAttribute("titulo", titulo);
        return "pacientes/formulario";
    }
}

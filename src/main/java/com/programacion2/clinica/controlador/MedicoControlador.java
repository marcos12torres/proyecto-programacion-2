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

import com.programacion2.clinica.dominio.Medico;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.servicio.ITurnoServicio;
import com.programacion2.clinica.servicio.MedicoServicio;

/**
 * ABM de médicos + filtro por especialidad + ficha con su agenda.
 */
@Controller
@RequestMapping("/medicos")
public class MedicoControlador {

    private static final String ERROR_NUMEROS = "El DNI y la matrícula deben ser números sin puntos.";

    private final MedicoServicio servicio;
    private final ITurnoServicio turnoServicio;

    public MedicoControlador(MedicoServicio servicio, ITurnoServicio turnoServicio) {
        this.servicio = servicio;
        this.turnoServicio = turnoServicio;
    }

    // GET /medicos?especialidad=Cardiología
    @GetMapping
    public String listar(@RequestParam(required = false) String especialidad, Model model) {
        model.addAttribute("medicos", servicio.buscarPorEspecialidad(especialidad));
        model.addAttribute("especialidades", servicio.listarEspecialidades());
        model.addAttribute("especialidad", especialidad);
        return "medicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("medico", new Medico());
        return formulario(model, "/medicos", "Registrar médico");
    }

    @PostMapping
    public String guardar(@ModelAttribute("medico") Medico medico,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("error", ERROR_NUMEROS);
            return formulario(model, "/medicos", "Registrar médico");
        }

        try {
            servicio.guardar(medico);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "/medicos", "Registrar médico");
        }

        redirect.addFlashAttribute("exito",
                "Médico " + medico.getNombreCompleto() + " registrado correctamente.");
        return "redirect:/medicos";
    }

    // GET /medicos/3 -> ficha con la agenda de turnos del médico.
    @GetMapping("/{id}")
    public String ficha(@PathVariable int id, Model model) {
        model.addAttribute("medico", servicio.buscarPorId(id));
        model.addAttribute("turnos", turnoServicio.listarPorMedico(id));
        return "medicos/ficha";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        model.addAttribute("medico", servicio.buscarPorId(id));
        return formulario(model, "/medicos/" + id, "Modificar médico");
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable int id,
                             @ModelAttribute("medico") Medico medico,
                             BindingResult resultado,
                             Model model,
                             RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("error", ERROR_NUMEROS);
            return formulario(model, "/medicos/" + id, "Modificar médico");
        }

        try {
            servicio.actualizar(id, medico);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, "/medicos/" + id, "Modificar médico");
        }

        redirect.addFlashAttribute("exito", "Médico modificado correctamente.");
        return "redirect:/medicos/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redirect) {

        boolean bajaLogica = servicio.eliminar(id);

        redirect.addFlashAttribute("exito", bajaLogica
                ? "El médico tenía turnos: se dio de baja (se conserva el historial "
                  + "y se cancelaron sus turnos futuros)."
                : "Médico eliminado correctamente.");
        return "redirect:/medicos";
    }

    private String formulario(Model model, String accion, String titulo) {
        model.addAttribute("accion", accion);
        model.addAttribute("titulo", titulo);
        // Para sugerir especialidades ya cargadas en el formulario.
        model.addAttribute("especialidades", servicio.listarEspecialidades());
        return "medicos/formulario";
    }
}

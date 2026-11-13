package com.programacion2.clinica.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.programacion2.clinica.dominio.EstadoTurno;
import com.programacion2.clinica.dominio.TipoTurno;
import com.programacion2.clinica.dominio.Turno;
import com.programacion2.clinica.excepcion.ReglaNegocioException;
import com.programacion2.clinica.servicio.ITurnoServicio;
import com.programacion2.clinica.servicio.MedicoServicio;
import com.programacion2.clinica.servicio.PacienteServicio;

/**
 * Turnos: listado con filtros, asignación (operación de negocio),
 * confirmación y cancelación.
 */
@Controller
@RequestMapping("/turnos")
public class TurnoControlador {

    private final ITurnoServicio servicio;
    private final PacienteServicio pacienteServicio;
    private final MedicoServicio medicoServicio;

    public TurnoControlador(ITurnoServicio servicio,
                            PacienteServicio pacienteServicio,
                            MedicoServicio medicoServicio) {
        this.servicio = servicio;
        this.pacienteServicio = pacienteServicio;
        this.medicoServicio = medicoServicio;
    }

    // GET /turnos?fecha=2026-11-20&medicoId=1&estado=PENDIENTE (todos opcionales)
    @GetMapping
    public String listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer medicoId,
            @RequestParam(required = false) EstadoTurno estado,
            Model model) {

        model.addAttribute("turnos", servicio.buscar(fecha, medicoId, estado));

        // Opciones y valores elegidos para los filtros.
        model.addAttribute("medicos", medicoServicio.consultar());
        model.addAttribute("estados", EstadoTurno.values());
        model.addAttribute("fecha", fecha);
        model.addAttribute("medicoId", medicoId);
        model.addAttribute("estado", estado);
        return "turnos/lista";
    }

    // GET /turnos/nuevo?pacienteId=2 -> formulario (puede venir con paciente o médico elegido).
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Integer pacienteId,
                        @RequestParam(required = false) Integer medicoId,
                        Model model) {
        model.addAttribute("turno", new Turno());
        return formulario(model, pacienteId, medicoId);
    }

    // POST /turnos -> operación de negocio "asignar turno".
    @PostMapping
    public String asignar(@ModelAttribute("turno") Turno turno,
                          BindingResult resultado,
                          @RequestParam(required = false) Integer pacienteId,
                          @RequestParam(required = false) Integer medicoId,
                          Model model,
                          RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("error", "Revise la fecha y la hora ingresadas.");
            return formulario(model, pacienteId, medicoId);
        }

        try {
            servicio.asignar(turno, pacienteId, medicoId);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, pacienteId, medicoId);
        }

        redirect.addFlashAttribute("exito", "Turno asignado correctamente para el "
                + turno.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " a las " + turno.getHora() + ".");
        return "redirect:/turnos";
    }

    // POST /turnos/7/confirmar
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable int id,
                            @RequestParam(required = false) String volver,
                            RedirectAttributes redirect) {
        servicio.confirmar(id);
        redirect.addFlashAttribute("exito", "Turno confirmado.");
        return "redirect:" + destino(volver);
    }

    // POST /turnos/7/cancelar
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable int id,
                           @RequestParam(required = false) String volver,
                           RedirectAttributes redirect) {
        servicio.cancelar(id);
        redirect.addFlashAttribute("exito", "Turno cancelado. El horario quedó libre.");
        return "redirect:" + destino(volver);
    }

    private String formulario(Model model, Integer pacienteId, Integer medicoId) {
        // Los desplegables solo muestran pacientes y médicos activos.
        model.addAttribute("pacientes", pacienteServicio.consultar());
        model.addAttribute("medicos", medicoServicio.consultar());
        model.addAttribute("tipos", TipoTurno.values());
        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("medicoId", medicoId);
        model.addAttribute("hoy", LocalDate.now());
        return "turnos/formulario";
    }

    // Solo se permite volver a una ruta interna de la aplicación.
    private String destino(String volver) {
        if (volver != null && volver.startsWith("/") && !volver.startsWith("//")) {
            return volver;
        }
        return "/turnos";
    }
}

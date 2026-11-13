package com.programacion2.clinica.controlador;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.programacion2.clinica.servicio.ITurnoServicio;
import com.programacion2.clinica.servicio.MedicoServicio;
import com.programacion2.clinica.servicio.PacienteServicio;

// Registra esta clase como un controlador de Spring MVC.
@Controller
public class InicioControlador {

    private final PacienteServicio pacienteServicio;
    private final MedicoServicio medicoServicio;
    private final ITurnoServicio turnoServicio;

    // Spring inyecta los servicios por el único constructor, sin usar @Autowired.
    public InicioControlador(PacienteServicio pacienteServicio,
                             MedicoServicio medicoServicio,
                             ITurnoServicio turnoServicio) {
        this.pacienteServicio = pacienteServicio;
        this.medicoServicio = medicoServicio;
        this.turnoServicio = turnoServicio;
    }

    // Atiende GET / y muestra el panel principal con un resumen.
    @GetMapping("/")
    public String principal(Model model) {
        model.addAttribute("cantidadPacientes", pacienteServicio.contarActivos());
        model.addAttribute("cantidadMedicos", medicoServicio.contarActivos());
        model.addAttribute("cantidadPendientes", turnoServicio.contarPendientes());
        model.addAttribute("turnosHoy", turnoServicio.turnosDeHoy());
        model.addAttribute("hoy", LocalDate.now());
        return "principal";
    }
}

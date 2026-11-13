package com.programacion2.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada: arranca Spring Boot, escanea los subpaquetes
// (configuracion, repositorio, servicio, controlador) y levanta Tomcat.
@SpringBootApplication
public class ClinicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicaApplication.class, args);
	}

}

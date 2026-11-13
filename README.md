# 🏥 Sistema de Clínica / Consultorio — Aplicación web

Proyecto integrador grupal · IFES – Analista de Sistemas

Aplicación web para gestionar **pacientes**, **médicos** y **turnos** de una clínica.
Es la evolución del proyecto Swing del primer cuatrimestre
([proyecto-programacion-ifes-](https://github.com/marcos12torres/proyecto-programacion-ifes-)),
reescrita con **Spring Boot + MVC + Thymeleaf + Bootstrap** y persistencia en **MySQL con DataNucleus JDO**.

## 👨‍💻 Integrantes
- Romina Davila
- Valentina adad
- Marcos Torres
- Federico Matus

## 📖 Descripción del problema

Un consultorio necesita registrar a sus pacientes y médicos y organizar la agenda de turnos
sin errores: que un médico no tenga dos pacientes a la misma hora, que un paciente no tenga
dos turnos superpuestos, que no se den turnos en el pasado o fuera del horario de atención,
y que al dar de baja a alguien no se pierda la historia de sus turnos.

## 🧩 Modelo de dominio

```
Persona (abstracta: id, nombre, apellido, dni, activo)
  ├── Paciente (obraSocial, telefono)   1 ──── *  Turno
  └── Medico   (matricula, especialidad) 1 ──── *  Turno

Turno (fecha: LocalDate, hora: LocalTime, tipo: TipoTurno, descripcion, estado: EstadoTurno)
```

- **Paciente 1 → N Turno** y **Medico 1 → N Turno** (claves foráneas `PACIENTE_ID` y `MEDICO_ID` en la tabla `TURNO`).
- `TipoTurno`: CONSULTA, ESTUDIO, CIRUGIA. `EstadoTurno`: PENDIENTE, CONFIRMADO, CANCELADO.

## ✅ Funcionalidades

| Requisito | Implementación |
|---|---|
| ABM de dos entidades | Pacientes y Médicos: registrar, listar, modificar y eliminar |
| Tercera entidad | Turnos: listado general, historial por paciente y agenda por médico |
| Búsqueda / filtro | Pacientes por apellido, nombre o DNI · Médicos por especialidad · Turnos por fecha, médico y estado |
| Relaciones desde la web | Al asignar un turno se eligen paciente y médico; fichas con sus turnos |
| Operación de negocio | **Asignar turno** (con validaciones), **confirmar** y **cancelar** turnos |
| Eliminación consistente | Sin turnos → se borra. Con turnos → **baja lógica** y se cancelan sus turnos futuros |

**Validaciones al asignar un turno:** paciente y médico obligatorios y activos; fecha, hora y tipo
obligatorios; no en el pasado; no domingos; de 08:00 a 19:30 cada 30 minutos; el médico no puede
tener otro turno en ese horario; el paciente tampoco.

## 🛠️ Tecnologías

Java 17 · Maven · Spring Boot 4.1.2 (Spring MVC) · Thymeleaf · Bootstrap 5.3 (CDN) ·
DataNucleus JDO 6 · MySQL 8

## 🗂️ Estructura (capas)

```
src/main/java/com/programacion2/clinica/
├── ClinicaApplication.java   arranque de Spring Boot
├── configuracion/            bean PersistenceManagerFactory (JDO)
├── dominio/                  entidades persistentes y enums
├── excepcion/                ReglaNegocioException, TurnoNoDisponibleException
├── repositorio/              acceso a datos con JDO (transacciones)
├── servicio/                 reglas de negocio y validaciones
└── controlador/              controladores MVC (atienden al navegador)
src/main/resources/
├── META-INF/persistence.xml  conexión a la base (unidad "Clinica")
├── templates/                vistas Thymeleaf
└── static/css/estilos.css
database/crear_base.sql       script para crear la base y el usuario
```

## ▶️ Cómo ejecutarlo en otra computadora

### Requisitos
- **JDK 17** o superior
- **MySQL 8** corriendo en `localhost:3306`
- No hace falta instalar Maven: el proyecto trae el *wrapper* (`mvnw` / `mvnw.cmd`)

### 1. Preparar la base de datos (una sola vez)

Con el usuario administrador de MySQL:

```bash
mysql -u root -p < database/crear_base.sql
```

Crea la base `clinica` y el usuario genérico `programacion2` / `Programacion2`.
**Las tablas no hay que crearlas:** DataNucleus las genera solo al iniciar la aplicación.

> Si querés usar otro usuario o contraseña, cambialos en `src/main/resources/META-INF/persistence.xml`.

### 2. Ejecutar

Linux / macOS:
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

También se puede ejecutar desde VS Code / IntelliJ con la clase `ClinicaApplication`
(siempre que el IDE compile con Maven, para que corra el *enhancer* de DataNucleus).

### 3. Abrir en el navegador

http://localhost:8080

### Ejecutar las pruebas

```bash
./mvnw test
```

## ⚠️ Notas

- Se usa `spring-boot:run` (o el IDE). Ejecutar el `.jar` empaquetado con `java -jar` **no funciona**
  con DataNucleus, porque no encuentra su configuración dentro de los jars anidados de Spring Boot.
- Bootstrap se carga desde un CDN: para que se vea con estilos hace falta conexión a internet.
- La búsqueda de pacientes distingue acentos: "pér" encuentra "Pérez", "per" no.

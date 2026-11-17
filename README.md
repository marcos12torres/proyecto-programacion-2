# Sistema de Clínica / Consultorio

**Proyecto integrador grupal – IFES | Analista de Sistemas**

Este proyecto es una aplicación web pensada para administrar pacientes, médicos y turnos de un consultorio. Surge como continuación del trabajo realizado en Swing durante el primer cuatrimestre, pero ahora desarrollado con tecnologías web.

El proyecto anterior se puede consultar en el siguiente repositorio: [Proyecto de Programación IFES](https://github.com/marcos12torres/proyecto-programacion-ifes-).

Para esta nueva versión se utilizaron Spring Boot, Spring MVC, Thymeleaf y Bootstrap para la aplicación web, junto con MySQL y DataNucleus JDO para guardar la información.

## Integrantes del grupo

- Romina Davila
- Valentina adad
- Marcos Torres
- Federico Matus

## Sobre el sistema

El objetivo principal es facilitar la organización de un consultorio, permitiendo llevar un registro de pacientes y médicos y administrar los turnos desde una misma aplicación.

Al organizar los turnos pueden aparecer distintos inconvenientes, como asignar dos pacientes al mismo médico en un mismo horario o darle más de un turno al mismo paciente a la vez. Por eso, el sistema cuenta con controles para evitar estas situaciones.

También se verifica que los turnos no se asignen en fechas pasadas ni fuera del horario de atención. En caso de dar de baja a un paciente o médico, se conserva la información de sus turnos anteriores para no perder ese historial.

## Modelo de dominio

Para organizar la información se utiliza una clase abstracta llamada `Persona`, de la cual heredan `Paciente` y `Medico`. La clase `Turno` se relaciona con ambas entidades.

```text
Persona (abstracta: id, nombre, apellido, dni, activo)
  ├── Paciente (obraSocial, telefono)   1 ──── * Turno
  └── Medico (matricula, especialidad)  1 ──── * Turno

Turno (fecha: LocalDate, hora: LocalTime,
       tipo: TipoTurno, descripcion, estado: EstadoTurno)
```

Un paciente puede tener varios turnos y un médico también puede estar relacionado con varios turnos. Estas relaciones se representan mediante las claves `PACIENTE_ID` y `MEDICO_ID`, que se encuentran en la tabla `TURNO`.

Los tipos de turno que maneja el sistema son:

- `CONSULTA`
- `ESTUDIO`
- `CIRUGIA`

Y los estados posibles son:

- `PENDIENTE`
- `CONFIRMADO`
- `CANCELADO`

## Funcionalidades implementadas

Actualmente, desde la aplicación se pueden realizar las siguientes operaciones:

| Funcionalidad     | Descripción                                                                                                                             |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Pacientes         | Registrar, listar, modificar y eliminar pacientes.                                                                                      |
| Médicos           | Registrar, listar, modificar y eliminar médicos.                                                                                        |
| Turnos            | Consultar el listado general, el historial de un paciente y la agenda de un médico.                                                     |
| Búsquedas         | Buscar pacientes por apellido, nombre o DNI; médicos por especialidad y turnos por fecha, médico o estado.                              |
| Relaciones        | Al asignar un turno se selecciona un paciente y un médico. También se pueden consultar sus fichas y turnos relacionados.                |
| Gestión de turnos | Asignar, confirmar y cancelar turnos, aplicando las validaciones correspondientes.                                                      |
| Eliminaciones     | Si no existen turnos asociados, se puede eliminar el registro. Si existen, se realiza una baja lógica y se cancelan sus turnos futuros. |

### Validaciones de los turnos

Al momento de asignar un turno, el sistema controla que el paciente y el médico estén seleccionados y activos, y que la fecha, la hora y el tipo de turno sean obligatorios.

Además, se aplican las siguientes condiciones:

- No se permiten turnos en fechas pasadas.
- No se asignan turnos los domingos.
- El horario de atención es de 08:00 a 19:30.
- Los turnos se organizan en intervalos de 30 minutos.
- Un médico no puede tener dos turnos en el mismo horario.
- Un paciente tampoco puede tener dos turnos superpuestos.

## Tecnologías utilizadas

Para el desarrollo de esta versión se utilizaron:

- **Java 17:** lenguaje utilizado para desarrollar la aplicación.
- **Maven:** administración de dependencias y construcción del proyecto.
- **Spring Boot 4.1.2:** configuración y ejecución de la aplicación.
- **Spring MVC:** organización de las solicitudes y respuestas de la aplicación web.
- **Thymeleaf:** generación de las vistas HTML.
- **Bootstrap 5.3:** estilos y diseño de la interfaz, cargado mediante CDN.
- **DataNucleus JDO 6:** persistencia y acceso a los datos.
- **MySQL 8:** base de datos utilizada para almacenar la información.

## Organización del proyecto

El código está separado en distintas carpetas según la responsabilidad de cada parte. La estructura principal es la siguiente:

```text
src/main/java/com/programacion2/clinica/
├── ClinicaApplication.java    Inicio de Spring Boot
├── configuracion/             Configuración de PersistenceManagerFactory (JDO)
├── dominio/                   Entidades persistentes y enums
├── excepcion/                 Excepciones del sistema
├── repositorio/               Acceso a datos y transacciones con JDO
├── servicio/                  Reglas de negocio y validaciones
└── controlador/               Controladores MVC

src/main/resources/
├── META-INF/persistence.xml   Configuración de conexión a la base (unidad "Clinica")
├── templates/                 Vistas de Thymeleaf
└── static/css/estilos.css     Estilos propios

database/
└── crear_base.sql             Script para crear la base y el usuario
```

## Instalación y ejecución

Para ejecutar el proyecto en otra computadora es necesario contar con:

- JDK 17 o superior.
- MySQL 8 funcionando en `localhost:3306`.

No hace falta instalar Maven por separado, ya que el proyecto incluye Maven Wrapper (`mvnw` y `mvnw.cmd`).

### 1. Crear la base de datos

Primero hay que preparar la base de datos. Desde una terminal, utilizando el usuario administrador de MySQL, ejecutar:

```bash
mysql -u root -p < database/crear_base.sql
```

El script crea la base de datos `clinica` y el usuario `programacion2` con la contraseña `Programacion2`.

Las tablas no necesitan crearse manualmente, ya que DataNucleus se encarga de generarlas al iniciar la aplicación.

Si se desea utilizar otro usuario o contraseña, se pueden modificar los datos de conexión en el archivo:

`src/main/resources/META-INF/persistence.xml`

### 2. Iniciar el proyecto

Una vez preparada la base de datos, se puede iniciar la aplicación desde la terminal.

**En Linux o macOS:**

```bash
./mvnw spring-boot:run
```

**En Windows:**

```powershell
mvnw.cmd spring-boot:run
```

También se puede ejecutar desde VS Code o IntelliJ utilizando la clase `ClinicaApplication`. En ese caso, el IDE debe compilar el proyecto con Maven para que se ejecute el *enhancer* de DataNucleus.

### 3. Ingresar a la aplicación

Cuando el proyecto termine de iniciar, abrir el navegador e ingresar a:

[http://localhost:8080](http://localhost:8080)

### Ejecutar las pruebas

Para ejecutar las pruebas del proyecto se puede utilizar el siguiente comando:

```bash
./mvnw test
```

## Notas a tener en cuenta

- Para iniciar la aplicación se utiliza `spring-boot:run` o el IDE. La ejecución del `.jar` empaquetado mediante `java -jar` no funciona con la configuración actual de DataNucleus, porque no encuentra su configuración dentro de los jars anidados de Spring Boot.
- Bootstrap se carga desde un CDN. Por lo tanto, se necesita conexión a internet para que se apliquen correctamente sus estilos.
- La búsqueda de pacientes distingue los acentos. Por ejemplo, al buscar `pér` se encuentra `Pérez`, mientras que al buscar `per` no se obtiene el mismo resultado.

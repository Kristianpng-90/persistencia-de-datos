# Gestión de Estudiantes: persistencia de datos en Java con JDBC y SQLite

Aplicación de consola desarrollada para la **Práctica de Persistencia de Datos en Java** de la asignatura *Taller de Programación* del programa de Ingeniería de Software de la Universidad Manuela Beltrán.

La aplicación registra, consulta, actualiza y elimina estudiantes (CRUD). Los datos se guardan en una base de datos **SQLite** mediante **JDBC**, de modo que la información sigue disponible después de cerrar y volver a abrir el programa.

---

## Contenido

1. [Objetivo](#objetivo)
2. [Funcionalidades](#funcionalidades)
3. [Tecnologías utilizadas](#tecnologías-utilizadas)
4. [Requisitos previos](#requisitos-previos)
5. [Estructura de carpetas](#estructura-de-carpetas)
6. [Instalación](#instalación)
7. [Ejecución](#ejecución)
8. [Inicialización de la base de datos](#inicialización-de-la-base-de-datos)
9. [Cómo comprobar la persistencia](#cómo-comprobar-la-persistencia)
10. [Ejemplos de las operaciones CRUD](#ejemplos-de-las-operaciones-crud)
11. [Pruebas automatizadas](#pruebas-automatizadas)
12. [Evidencias](#evidencias)
13. [Documento técnico y video](#documento-técnico-y-video)
14. [Autor](#autor)

---

## Objetivo

Desarrollar una aplicación Java que almacene información de forma persistente en una base de datos relacional, aplicando JDBC, consultas parametrizadas con `PreparedStatement`, el patrón de diseño DAO (*Data Access Object*), validación de datos y manejo de excepciones.

## Funcionalidades

| Opción del menú | Operación | Descripción |
|---|---|---|
| 1 | Crear | Registrar un estudiante (documento, nombres, apellidos, correo, programa y semestre). |
| 2 | Leer | Listar todos los estudiantes ordenados por apellidos. |
| 3 | Leer | Buscar un estudiante por su ID. |
| 4 | Leer | Buscar un estudiante por su número de documento. |
| 5 | Actualizar | Modificar los datos de un estudiante; los campos vacíos conservan su valor. |
| 6 | Eliminar | Borrar un estudiante después de pedir confirmación (S/N). |
| 0 | — | Salir de la aplicación. |

Además:

- Valida documento (6 a 12 dígitos), nombres y apellidos, formato de correo, programa y semestre (1 a 10).
- Impide documentos o correos repetidos (restricción `UNIQUE` en la base de datos).
- Muestra mensajes claros ante errores, sin cerrar el programa.
- Usa únicamente consultas parametrizadas, lo que previene la inyección SQL.

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java (JDK) | 17 o superior | Lenguaje de programación |
| Apache Maven | 3.8 o superior | Compilación, dependencias y empaquetado |
| SQLite | incluida en el controlador | Motor de base de datos embebido (archivo local) |
| sqlite-jdbc (`org.xerial`) | 3.49.1.0 | Controlador JDBC para SQLite |
| JUnit Jupiter | 5.11.4 | Pruebas automatizadas |

## Requisitos previos

1. **JDK 17 o superior.** Compruébelo con:
   ```bash
   java -version
   ```
2. **Apache Maven 3.8 o superior.** Compruébelo con:
   ```bash
   mvn -version
   ```
   > Si usa IntelliJ IDEA, Eclipse o NetBeans, puede abrir el proyecto como *proyecto Maven*; el IDE trae Maven integrado.
3. **Git** (opcional, solo para clonar el repositorio).
4. Conexión a Internet la primera vez, para que Maven descargue las dependencias.

No es necesario instalar SQLite: el controlador `sqlite-jdbc` ya incluye el motor.

## Estructura de carpetas

```text
taller-persistencia-datos-java/
├── .gitignore                         Archivos que Git no debe publicar
├── README.md                          Este archivo
├── pom.xml                            Configuración de Maven y dependencias
├── src/
│   ├── main/
│   │   ├── java/co/edu/umb/persistencia/
│   │   │   ├── App.java                       Punto de entrada (método main)
│   │   │   ├── conexion/ConexionBD.java       Conexión JDBC e inicialización del esquema
│   │   │   ├── modelo/Estudiante.java         Entidad del dominio
│   │   │   ├── dao/EstudianteDAO.java         Interfaz del patrón DAO
│   │   │   ├── dao/EstudianteDAOImpl.java     Implementación JDBC del DAO (CRUD)
│   │   │   ├── servicio/EstudianteService.java  Reglas de negocio y validaciones
│   │   │   ├── presentacion/MenuConsola.java  Menú de consola
│   │   │   └── excepciones/                   Excepciones propias
│   │   │       ├── PersistenciaException.java
│   │   │       ├── DatoDuplicadoException.java
│   │   │       └── ValidacionException.java
│   │   └── resources/db/schema.sql            Script SQL que crea la tabla
│   └── test/java/co/edu/umb/persistencia/
│       ├── EstudianteDAOImplTest.java         Pruebas del CRUD y de la persistencia
│       └── EstudianteServiceTest.java         Pruebas de las validaciones
└── docs/
    ├── enlaces.md                             Enlaces al repositorio y al video
    ├── documento-tecnico/                     Informe en Word y PDF (APA 7)
    ├── guion-video/                           Guion del video en inglés
    └── evidencias/                            Capturas y salidas reales de ejecución
```

## Instalación

```bash
git clone https://github.com/Kristianpng-90/persistencia-de-datos.git
cd taller-persistencia-datos-java
mvn clean package
```

> Reemplace `USUARIO` por el nombre de usuario de GitHub del autor.

`mvn clean package` descarga las dependencias, compila el código, **ejecuta las 16 pruebas automatizadas** y genera el archivo ejecutable `target/gestion-estudiantes-1.0.0.jar`.

## Ejecución

Desde la carpeta raíz del proyecto, use una de estas opciones:

**Opción A: archivo JAR (recomendada)**

```bash
java -jar target/gestion-estudiantes-1.0.0.jar
```

**Opción B: con Maven**

```bash
mvn compile exec:java
```

**Opción C: desde el IDE**

Abra `src/main/java/co/edu/umb/persistencia/App.java` y ejecute el método `main`.

**Opciones adicionales**

- Cambiar la ubicación del archivo de base de datos:
  ```bash
  java -Ddb.path=otra-carpeta/mis-datos.db -jar target/gestion-estudiantes-1.0.0.jar
  ```
- Alimentar la aplicación desde un archivo de entradas (útil para demostraciones), mostrando cada dato leído:
  ```bash
  java -Dapp.eco=true -jar target/gestion-estudiantes-1.0.0.jar < docs/evidencias/entradas-de-prueba/sesion1.txt
  ```

> **Windows:** si las tildes se ven mal en la consola, ejecute `chcp 65001` antes de iniciar la aplicación.

## Inicialización de la base de datos

No hay que crear la base de datos a mano. Al iniciar, la clase `ConexionBD`:

1. Crea la carpeta `data/` si no existe.
2. Abre la conexión `jdbc:sqlite:data/estudiantes.db`; si el archivo no existe, SQLite lo crea.
3. Ejecuta el script `src/main/resources/db/schema.sql`, que usa `CREATE TABLE IF NOT EXISTS`, por lo que **no borra los datos existentes** en los siguientes inicios.

Tabla `estudiantes`:

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT |
| `documento` | TEXT | NOT NULL, UNIQUE |
| `nombres` | TEXT | NOT NULL |
| `apellidos` | TEXT | NOT NULL |
| `correo` | TEXT | NOT NULL, UNIQUE |
| `programa` | TEXT | NOT NULL |
| `semestre` | INTEGER | NOT NULL, CHECK (1 a 10) |
| `fecha_registro` | TEXT | NOT NULL, valor por defecto: fecha y hora local |

**¿Por qué el archivo `.db` no está en el repositorio?** Es un archivo binario que cambia en cada ejecución y contiene datos de prueba locales. El script `schema.sql` sí está publicado, así que cualquier persona que clone el proyecto obtiene una base de datos nueva y válida al ejecutarlo por primera vez.

## Cómo comprobar la persistencia

1. Ejecute la aplicación y registre uno o más estudiantes (opción 1).
2. Liste los estudiantes (opción 2) y salga (opción 0).
3. Compruebe que existe el archivo `data/estudiantes.db`.
4. Vuelva a ejecutar la aplicación: el encabezado del menú muestra la cantidad de estudiantes registrados y la opción 2 lista los mismos datos.
5. (Opcional) Abra `data/estudiantes.db` con [DB Browser for SQLite](https://sqlitebrowser.org/) y consulte `SELECT * FROM estudiantes;`.

La prueba automatizada `persistenciaEntreConexiones` verifica lo mismo con un archivo temporal.

## Ejemplos de las operaciones CRUD

**Crear**

```text
Seleccione una opcion: 1
--- Registrar estudiante ---
Documento: 1012345678
Nombres: Laura Valentina
Apellidos: Gómez Rojas
Correo: laura.gomez@correo.com
Programa: Ingeniería de Software
Semestre (1-10): 3
[OK] Estudiante registrado con ID 1.
```

**Leer**

```text
Seleccione una opcion: 4
Documento a buscar: 1034567890
ID   DOCUMENTO    NOMBRE COMPLETO              CORREO                         PROGRAMA                 SEM
3    1034567890   María José Castro Díaz       maria.castro@correo.com        Ingeniería Industrial      2
```

**Actualizar** (los campos vacíos conservan el valor actual)

```text
Seleccione una opcion: 5
ID del estudiante a actualizar: 1
Programa [Ingeniería de Software]: Ingeniería de Sistemas
Semestre [3]: 4
[OK] Estudiante con ID 1 actualizado.
```

**Eliminar**

```text
Seleccione una opcion: 6
ID del estudiante a eliminar: 3
Se eliminara a: María José Castro Díaz
Confirma la eliminacion? (S/N): S
[OK] Estudiante con ID 3 eliminado.
```

**Validaciones**

```text
[Duplicado] Ya existe un estudiante con ese documento.
[Validacion] El correo electronico no tiene un formato valido.
[Validacion] El semestre debe estar entre 1 y 10.
[Validacion] Debe ingresar un numero entero valido.
```

## Pruebas automatizadas

```bash
mvn test
```

Se ejecutan 16 pruebas JUnit 5:

- `EstudianteDAOImplTest` (10 pruebas): crear, leer, listar, actualizar, eliminar, documento y correo duplicados, intento de inyección SQL y persistencia al abrir una nueva conexión.
- `EstudianteServiceTest` (6 pruebas): validaciones de documento, correo, semestre, nombres vacíos e ID negativo.

Las pruebas usan un archivo SQLite temporal, por lo que no modifican `data/estudiantes.db`.

## Evidencias

Las capturas y salidas de consola de una ejecución real están en [`docs/evidencias/`](docs/evidencias/):

| Archivo | Contenido |
|---|---|
| `01-menu-y-registro.png` | Menú principal y registro del primer estudiante |
| `02-registro-de-varios-estudiantes.png` | Registro de dos estudiantes más |
| `03-listar-y-buscar.png` | Listado y búsquedas por ID y por documento |
| `04-actualizar.png` | Actualización de programa y semestre |
| `05-validaciones-y-errores.png` | Duplicados, correo inválido, semestre fuera de rango, ID no numérico e inexistente |
| `06-eliminar.png` | Eliminación confirmada y eliminación cancelada |
| `07-persistencia-tras-reinicio.png` | Los datos siguen disponibles al volver a ejecutar la aplicación |
| `08-consulta-directa-sqlite.png` | Lectura del archivo `.db` sin usar la aplicación |
| `09-pruebas-automatizadas.png` | Resultado de las 16 pruebas |

Los archivos de entrada usados para reproducir las sesiones están en `docs/evidencias/entradas-de-prueba/`.

## Documento técnico y video


- Documento técnico (PDF): [`docs/documento-tecnico/Documento_Tecnico_Persistencia_Datos_Java.pdf`](docs/documento-tecnico/Documento_Tecnico_Persistencia_Datos_Java.pdf)
- Guion del video (inglés): [`docs/guion-video/guion-video-en.md`](docs/guion-video/guion-video-en.md)
- Video explicativo en YouTube: **[PEGAR AQUÍ EL ENLACE DEL VIDEO]**

## Autor

- **Nombre:** Cristian Camilo Girata Ravelo
- **Programa:** Ingeniería de Software
- **Asignatura:** Taller de Programación
- **Institución:** Universidad Manuela Beltrán
- **Docente:** Juan Jose Osorio Tabares
- **Año:** 2026

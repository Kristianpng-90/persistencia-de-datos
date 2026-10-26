package co.edu.umb.persistencia;

import co.edu.umb.persistencia.conexion.ConexionBD;
import co.edu.umb.persistencia.dao.EstudianteDAO;
import co.edu.umb.persistencia.dao.EstudianteDAOImpl;
import co.edu.umb.persistencia.excepciones.DatoDuplicadoException;
import co.edu.umb.persistencia.modelo.Estudiante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion del DAO contra un archivo SQLite temporal.
 */
class EstudianteDAOImplTest {

    @TempDir
    Path carpetaTemporal;

    private Path archivoBD;
    private EstudianteDAO dao;

    @BeforeEach
    void preparar() {
        archivoBD = carpetaTemporal.resolve("prueba.db");
        ConexionBD conexion = new ConexionBD(archivoBD.toString());
        conexion.inicializarEsquema();
        dao = new EstudianteDAOImpl(conexion);
    }

    private Estudiante nuevo(String documento, String correo) {
        return new Estudiante(documento, "Laura", "Gomez Rojas", correo, "Ingenieria de Software", 3);
    }

    @Test
    @DisplayName("Crear: insertar asigna un id generado por la base de datos")
    void insertarAsignaId() {
        Estudiante e = dao.insertar(nuevo("1012345678", "laura@correo.com"));
        assertNotNull(e.getId());
        assertTrue(e.getId() > 0);
        assertEquals(1, dao.contar());
    }

    @Test
    @DisplayName("Leer: buscar por id y por documento devuelve el registro guardado")
    void buscarRegistro() {
        Estudiante e = dao.insertar(nuevo("1012345678", "laura@correo.com"));

        Optional<Estudiante> porId = dao.buscarPorId(e.getId());
        Optional<Estudiante> porDocumento = dao.buscarPorDocumento("1012345678");

        assertTrue(porId.isPresent());
        assertEquals("laura@correo.com", porId.get().getCorreo());
        assertTrue(porDocumento.isPresent());
        assertEquals(e.getId(), porDocumento.get().getId());
        assertNotNull(porId.get().getFechaRegistro());
    }

    @Test
    @DisplayName("Leer: buscar un id inexistente devuelve Optional vacio")
    void buscarInexistente() {
        assertTrue(dao.buscarPorId(999).isEmpty());
    }

    @Test
    @DisplayName("Leer: listar devuelve todos los registros ordenados por apellidos")
    void listarOrdenado() {
        dao.insertar(new Estudiante("1000000001", "Ana", "Zapata", "ana@correo.com", "Ingenieria", 1));
        dao.insertar(new Estudiante("1000000002", "Bruno", "Alvarez", "bruno@correo.com", "Ingenieria", 2));

        List<Estudiante> lista = dao.listarTodos();

        assertEquals(2, lista.size());
        assertEquals("Alvarez", lista.get(0).getApellidos());
    }

    @Test
    @DisplayName("Actualizar: los cambios quedan guardados")
    void actualizar() {
        Estudiante e = dao.insertar(nuevo("1012345678", "laura@correo.com"));
        e.setSemestre(4);
        e.setPrograma("Ingenieria de Sistemas");

        assertTrue(dao.actualizar(e));
        Estudiante leido = dao.buscarPorId(e.getId()).orElseThrow();
        assertEquals(4, leido.getSemestre());
        assertEquals("Ingenieria de Sistemas", leido.getPrograma());
    }

    @Test
    @DisplayName("Eliminar: el registro deja de existir")
    void eliminar() {
        Estudiante e = dao.insertar(nuevo("1012345678", "laura@correo.com"));

        assertTrue(dao.eliminar(e.getId()));
        assertTrue(dao.buscarPorId(e.getId()).isEmpty());
        assertFalse(dao.eliminar(e.getId()), "Eliminar dos veces no debe afectar filas");
    }

    @Test
    @DisplayName("Restriccion UNIQUE: documento duplicado lanza DatoDuplicadoException")
    void documentoDuplicado() {
        dao.insertar(nuevo("1012345678", "laura@correo.com"));
        DatoDuplicadoException ex = assertThrows(DatoDuplicadoException.class,
                () -> dao.insertar(nuevo("1012345678", "otro@correo.com")));
        assertTrue(ex.getMessage().contains("documento"));
    }

    @Test
    @DisplayName("Restriccion UNIQUE: correo duplicado lanza DatoDuplicadoException")
    void correoDuplicado() {
        dao.insertar(nuevo("1012345678", "laura@correo.com"));
        DatoDuplicadoException ex = assertThrows(DatoDuplicadoException.class,
                () -> dao.insertar(nuevo("1099999999", "laura@correo.com")));
        assertTrue(ex.getMessage().contains("correo"));
    }

    @Test
    @DisplayName("Inyeccion SQL: el texto malicioso se trata como dato, no como SQL")
    void inyeccionSqlNoAfectaLaTabla() {
        dao.insertar(nuevo("1012345678", "laura@correo.com"));

        Optional<Estudiante> resultado = dao.buscarPorDocumento("' OR '1'='1");

        assertTrue(resultado.isEmpty());
        assertEquals(1, dao.contar());
    }

    @Test
    @DisplayName("Persistencia: los datos siguen disponibles al abrir una nueva conexion")
    void persistenciaEntreConexiones() {
        dao.insertar(nuevo("1012345678", "laura@correo.com"));
        assertTrue(Files.exists(archivoBD));

        // Simula reiniciar la aplicacion: nuevos objetos sobre el mismo archivo.
        ConexionBD otraConexion = new ConexionBD(archivoBD.toString());
        otraConexion.inicializarEsquema(); // no debe borrar datos existentes
        EstudianteDAO otroDao = new EstudianteDAOImpl(otraConexion);

        assertEquals(1, otroDao.contar());
        assertTrue(otroDao.buscarPorDocumento("1012345678").isPresent());
    }
}

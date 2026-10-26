package co.edu.umb.persistencia;

import co.edu.umb.persistencia.conexion.ConexionBD;
import co.edu.umb.persistencia.dao.EstudianteDAOImpl;
import co.edu.umb.persistencia.excepciones.ValidacionException;
import co.edu.umb.persistencia.modelo.Estudiante;
import co.edu.umb.persistencia.servicio.EstudianteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas de validacion de la capa de servicio.
 */
class EstudianteServiceTest {

    @TempDir
    Path carpetaTemporal;

    private EstudianteService servicio;

    @BeforeEach
    void preparar() {
        ConexionBD conexion = new ConexionBD(carpetaTemporal.resolve("servicio.db").toString());
        conexion.inicializarEsquema();
        servicio = new EstudianteService(new EstudianteDAOImpl(conexion));
    }

    private Estudiante valido() {
        return new Estudiante("1012345678", "Laura", "Gomez", "Laura@Correo.com", "Ingenieria de Software", 3);
    }

    @Test
    @DisplayName("Un estudiante valido se registra y el correo se normaliza a minusculas")
    void registroValido() {
        Estudiante e = servicio.registrar(valido());
        assertNotNull(e.getId());
        assertEquals("laura@correo.com", e.getCorreo());
    }

    @Test
    @DisplayName("Documento con letras es rechazado")
    void documentoInvalido() {
        Estudiante e = valido();
        e.setDocumento("ABC123");
        assertThrows(ValidacionException.class, () -> servicio.registrar(e));
        assertEquals(0, servicio.contar());
    }

    @Test
    @DisplayName("Correo sin arroba es rechazado")
    void correoInvalido() {
        Estudiante e = valido();
        e.setCorreo("laura.correo.com");
        assertThrows(ValidacionException.class, () -> servicio.registrar(e));
    }

    @Test
    @DisplayName("Semestre fuera del rango 1-10 es rechazado")
    void semestreFueraDeRango() {
        Estudiante e = valido();
        e.setSemestre(11);
        assertThrows(ValidacionException.class, () -> servicio.registrar(e));
    }

    @Test
    @DisplayName("Nombres vacios son rechazados")
    void nombresVacios() {
        Estudiante e = valido();
        e.setNombres("   ");
        assertThrows(ValidacionException.class, () -> servicio.registrar(e));
    }

    @Test
    @DisplayName("Un id negativo es rechazado antes de consultar la base de datos")
    void idNegativo() {
        assertThrows(ValidacionException.class, () -> servicio.eliminar(-1));
    }
}

package co.edu.umb.persistencia.dao;

import co.edu.umb.persistencia.modelo.Estudiante;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos (patron DAO) para la entidad Estudiante.
 * Las capas superiores dependen de esta interfaz y no de JDBC ni de SQLite.
 */
public interface EstudianteDAO {

    /** Inserta el estudiante y devuelve el mismo objeto con el id generado. */
    Estudiante insertar(Estudiante estudiante);

    Optional<Estudiante> buscarPorId(int id);

    Optional<Estudiante> buscarPorDocumento(String documento);

    List<Estudiante> listarTodos();

    /** Devuelve true si se actualizo una fila. */
    boolean actualizar(Estudiante estudiante);

    /** Devuelve true si se elimino una fila. */
    boolean eliminar(int id);

    int contar();
}

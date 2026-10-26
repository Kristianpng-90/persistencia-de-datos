package co.edu.umb.persistencia.dao;

import co.edu.umb.persistencia.conexion.ConexionBD;
import co.edu.umb.persistencia.excepciones.DatoDuplicadoException;
import co.edu.umb.persistencia.excepciones.PersistenciaException;
import co.edu.umb.persistencia.modelo.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion JDBC del DAO de estudiantes.
 *
 * <p>Todas las consultas usan {@link PreparedStatement} con parametros "?",
 * de modo que los valores escritos por el usuario nunca se concatenan al SQL
 * (prevencion de inyeccion SQL). Los recursos se cierran con try-with-resources.</p>
 */
public class EstudianteDAOImpl implements EstudianteDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO estudiantes (documento, nombres, apellidos, correo, programa, semestre) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_COLUMNAS =
            "SELECT id, documento, nombres, apellidos, correo, programa, semestre, fecha_registro "
          + "FROM estudiantes";

    private static final String SQL_POR_ID = SQL_COLUMNAS + " WHERE id = ?";

    private static final String SQL_POR_DOCUMENTO = SQL_COLUMNAS + " WHERE documento = ?";

    private static final String SQL_LISTAR = SQL_COLUMNAS + " ORDER BY apellidos, nombres";

    private static final String SQL_ACTUALIZAR =
            "UPDATE estudiantes SET documento = ?, nombres = ?, apellidos = ?, correo = ?, "
          + "programa = ?, semestre = ? WHERE id = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM estudiantes WHERE id = ?";

    private static final String SQL_CONTAR = "SELECT COUNT(*) FROM estudiantes";

    /** Codigo de error de SQLite para violaciones de restricciones (SQLITE_CONSTRAINT). */
    private static final int SQLITE_CONSTRAINT = 19;

    private final ConexionBD conexionBD;

    public EstudianteDAOImpl(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public Estudiante insertar(Estudiante estudiante) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            asignarParametros(ps, estudiante);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }
            return estudiante;
        } catch (SQLException e) {
            throw traducir("insertar el estudiante", e);
        }
    }

    @Override
    public Optional<Estudiante> buscarPorId(int id) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_POR_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir("buscar el estudiante por id", e);
        }
    }

    @Override
    public Optional<Estudiante> buscarPorDocumento(String documento) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_POR_DOCUMENTO)) {

            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir("buscar el estudiante por documento", e);
        }
    }

    @Override
    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw traducir("listar los estudiantes", e);
        }
    }

    @Override
    public boolean actualizar(Estudiante estudiante) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            asignarParametros(ps, estudiante);
            ps.setInt(7, estudiante.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw traducir("actualizar el estudiante", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw traducir("eliminar el estudiante", e);
        }
    }

    @Override
    public int contar() {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_CONTAR);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw traducir("contar los estudiantes", e);
        }
    }

    /** Asigna los parametros 1 a 6, comunes a INSERT y UPDATE. */
    private void asignarParametros(PreparedStatement ps, Estudiante e) throws SQLException {
        ps.setString(1, e.getDocumento());
        ps.setString(2, e.getNombres());
        ps.setString(3, e.getApellidos());
        ps.setString(4, e.getCorreo());
        ps.setString(5, e.getPrograma());
        ps.setInt(6, e.getSemestre());
    }

    /** Convierte la fila actual del ResultSet en un objeto Estudiante. */
    private Estudiante mapear(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setId(rs.getInt("id"));
        e.setDocumento(rs.getString("documento"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidos(rs.getString("apellidos"));
        e.setCorreo(rs.getString("correo"));
        e.setPrograma(rs.getString("programa"));
        e.setSemestre(rs.getInt("semestre"));
        e.setFechaRegistro(rs.getString("fecha_registro"));
        return e;
    }

    /** Traduce una SQLException a una excepcion propia de la aplicacion. */
    private PersistenciaException traducir(String operacion, SQLException e) {
        String mensaje = e.getMessage() == null ? "" : e.getMessage();
        if (e.getErrorCode() == SQLITE_CONSTRAINT && mensaje.contains("UNIQUE")) {
            String campo = mensaje.contains("estudiantes.correo") ? "correo" : "documento";
            return new DatoDuplicadoException("Ya existe un estudiante con ese " + campo + ".", e);
        }
        return new PersistenciaException("Error al " + operacion + ": " + mensaje, e);
    }
}

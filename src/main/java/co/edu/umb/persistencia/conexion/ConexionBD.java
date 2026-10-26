package co.edu.umb.persistencia.conexion;

import co.edu.umb.persistencia.excepciones.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexion JDBC con la base de datos SQLite.
 *
 * <p>La URL JDBC tiene la forma {@code jdbc:sqlite:<ruta del archivo>}. Si el archivo
 * no existe, SQLite lo crea al abrir la primera conexion; despues, el metodo
 * {@link #inicializarEsquema()} ejecuta el script {@code db/schema.sql} para crear
 * la tabla si todavia no existe.</p>
 */
public class ConexionBD {

    /** Ruta por defecto del archivo de base de datos, relativa a la carpeta de ejecucion. */
    public static final String RUTA_POR_DEFECTO = "data/estudiantes.db";

    private static final String SCRIPT_ESQUEMA = "/db/schema.sql";

    private final Path rutaArchivo;
    private final String url;

    public ConexionBD(String rutaArchivo) {
        this.rutaArchivo = Paths.get(rutaArchivo).toAbsolutePath();
        this.url = "jdbc:sqlite:" + this.rutaArchivo;
    }

    /**
     * Abre una nueva conexion. Quien la solicita debe cerrarla,
     * preferiblemente con try-with-resources.
     */
    public Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new PersistenciaException("No fue posible conectar con la base de datos: " + url, e);
        }
    }

    /**
     * Crea la carpeta del archivo (si hace falta) y ejecuta el script de esquema.
     * Es seguro llamarlo en cada inicio porque el script usa CREATE TABLE IF NOT EXISTS.
     */
    public void inicializarEsquema() {
        try {
            Path carpeta = rutaArchivo.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No fue posible crear la carpeta de la base de datos.", e);
        }

        String script = leerScript();
        try (Connection conexion = obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            for (String instruccion : script.split(";")) {
                String limpia = quitarComentarios(instruccion).trim();
                if (!limpia.isEmpty()) {
                    sentencia.execute(limpia);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al crear el esquema de la base de datos.", e);
        }
    }

    public Path getRutaArchivo() {
        return rutaArchivo;
    }

    public String getUrl() {
        return url;
    }

    private String leerScript() {
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(SCRIPT_ESQUEMA)) {
            if (entrada == null) {
                throw new PersistenciaException("No se encontro el recurso " + SCRIPT_ESQUEMA);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No fue posible leer " + SCRIPT_ESQUEMA, e);
        }
    }

    private static String quitarComentarios(String sql) {
        StringBuilder resultado = new StringBuilder();
        for (String linea : sql.split("\\R")) {
            if (!linea.trim().startsWith("--")) {
                resultado.append(linea).append('\n');
            }
        }
        return resultado.toString();
    }
}

package co.edu.umb.persistencia;

import co.edu.umb.persistencia.conexion.ConexionBD;
import co.edu.umb.persistencia.dao.EstudianteDAO;
import co.edu.umb.persistencia.dao.EstudianteDAOImpl;
import co.edu.umb.persistencia.excepciones.PersistenciaException;
import co.edu.umb.persistencia.presentacion.MenuConsola;
import co.edu.umb.persistencia.servicio.EstudianteService;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>La ruta del archivo de base de datos puede cambiarse con la propiedad del sistema
 * {@code db.path}, por ejemplo: {@code java -Ddb.path=otra.db -jar ...}.
 * Por defecto se usa {@value ConexionBD#RUTA_POR_DEFECTO}.</p>
 */
public class App {

    public static void main(String[] args) {
        String ruta = System.getProperty("db.path", ConexionBD.RUTA_POR_DEFECTO);
        // Con -Dapp.eco=true se repite cada dato leido; util al alimentar la entrada desde un archivo.
        boolean eco = Boolean.getBoolean("app.eco");

        try {
            ConexionBD conexionBD = new ConexionBD(ruta);
            conexionBD.inicializarEsquema();
            System.out.println("Base de datos: " + conexionBD.getRutaArchivo());

            EstudianteDAO dao = new EstudianteDAOImpl(conexionBD);
            EstudianteService servicio = new EstudianteService(dao);

            try (Scanner entrada = new Scanner(System.in, codificacionConsola())) {
                new MenuConsola(servicio, entrada, System.out, eco).iniciar();
            }
        } catch (PersistenciaException e) {
            System.err.println("No fue posible iniciar la aplicacion: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Usa la misma codificacion de la consola para leer y escribir, de modo que
     * las tildes y la letra "ñ" se lean correctamente en Windows, Linux y macOS.
     */
    private static Charset codificacionConsola() {
        String[] propiedades = {"stdout.encoding", "sun.stdout.encoding", "native.encoding"};
        for (String propiedad : propiedades) {
            String valor = System.getProperty(propiedad);
            if (valor != null && Charset.isSupported(valor)) {
                return Charset.forName(valor);
            }
        }
        return Charset.defaultCharset();
    }
}

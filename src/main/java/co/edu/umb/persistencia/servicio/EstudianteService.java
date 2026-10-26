package co.edu.umb.persistencia.servicio;

import co.edu.umb.persistencia.dao.EstudianteDAO;
import co.edu.umb.persistencia.excepciones.ValidacionException;
import co.edu.umb.persistencia.modelo.Estudiante;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de servicio: valida las reglas de negocio antes de delegar en el DAO.
 */
public class EstudianteService {

    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("\\d{6,12}");
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_NOMBRE =
            Pattern.compile("^[\\p{L} .'-]{2,60}$");

    public static final int SEMESTRE_MINIMO = 1;
    public static final int SEMESTRE_MAXIMO = 10;

    private final EstudianteDAO dao;

    public EstudianteService(EstudianteDAO dao) {
        this.dao = dao;
    }

    public Estudiante registrar(Estudiante estudiante) {
        normalizar(estudiante);
        validar(estudiante);
        return dao.insertar(estudiante);
    }

    public List<Estudiante> listar() {
        return dao.listarTodos();
    }

    public Optional<Estudiante> buscarPorId(int id) {
        validarId(id);
        return dao.buscarPorId(id);
    }

    public Optional<Estudiante> buscarPorDocumento(String documento) {
        String doc = documento == null ? "" : documento.trim();
        if (!PATRON_DOCUMENTO.matcher(doc).matches()) {
            throw new ValidacionException("El documento debe tener entre 6 y 12 digitos.");
        }
        return dao.buscarPorDocumento(doc);
    }

    public boolean actualizar(Estudiante estudiante) {
        if (estudiante.getId() == null) {
            throw new ValidacionException("El estudiante a actualizar debe tener un id.");
        }
        validarId(estudiante.getId());
        normalizar(estudiante);
        validar(estudiante);
        return dao.actualizar(estudiante);
    }

    public boolean eliminar(int id) {
        validarId(id);
        return dao.eliminar(id);
    }

    public int contar() {
        return dao.contar();
    }

    /** Aplica todas las reglas de validacion; lanza ValidacionException en el primer error. */
    public void validar(Estudiante e) {
        if (!PATRON_DOCUMENTO.matcher(textoONulo(e.getDocumento())).matches()) {
            throw new ValidacionException("El documento debe tener entre 6 y 12 digitos.");
        }
        if (!PATRON_NOMBRE.matcher(textoONulo(e.getNombres())).matches()) {
            throw new ValidacionException("Los nombres deben tener entre 2 y 60 letras.");
        }
        if (!PATRON_NOMBRE.matcher(textoONulo(e.getApellidos())).matches()) {
            throw new ValidacionException("Los apellidos deben tener entre 2 y 60 letras.");
        }
        if (!PATRON_CORREO.matcher(textoONulo(e.getCorreo())).matches()) {
            throw new ValidacionException("El correo electronico no tiene un formato valido.");
        }
        String programa = textoONulo(e.getPrograma());
        if (programa.length() < 3 || programa.length() > 80) {
            throw new ValidacionException("El programa debe tener entre 3 y 80 caracteres.");
        }
        if (e.getSemestre() < SEMESTRE_MINIMO || e.getSemestre() > SEMESTRE_MAXIMO) {
            throw new ValidacionException("El semestre debe estar entre "
                    + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO + ".");
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El id debe ser un numero entero positivo.");
        }
    }

    /** Elimina espacios sobrantes y pasa el correo a minusculas. */
    private void normalizar(Estudiante e) {
        e.setDocumento(recortar(e.getDocumento()));
        e.setNombres(recortar(e.getNombres()));
        e.setApellidos(recortar(e.getApellidos()));
        e.setPrograma(recortar(e.getPrograma()));
        String correo = recortar(e.getCorreo());
        e.setCorreo(correo == null ? null : correo.toLowerCase());
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.trim().replaceAll("\\s+", " ");
    }

    private static String textoONulo(String valor) {
        return valor == null ? "" : valor;
    }
}

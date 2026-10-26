package co.edu.umb.persistencia.presentacion;

import co.edu.umb.persistencia.excepciones.DatoDuplicadoException;
import co.edu.umb.persistencia.excepciones.PersistenciaException;
import co.edu.umb.persistencia.excepciones.ValidacionException;
import co.edu.umb.persistencia.modelo.Estudiante;
import co.edu.umb.persistencia.servicio.EstudianteService;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Capa de presentacion: menu de consola que recibe los datos del usuario
 * y muestra los resultados de las operaciones CRUD.
 */
public class MenuConsola {

    private static final String SEPARADOR = "-".repeat(110);

    private final EstudianteService servicio;
    private final Scanner entrada;
    private final PrintStream salida;
    private final boolean eco;

    /**
     * @param eco si es true, repite en pantalla cada linea leida; util cuando la
     *            entrada proviene de un archivo y no de un usuario escribiendo.
     */
    public MenuConsola(EstudianteService servicio, Scanner entrada, PrintStream salida, boolean eco) {
        this.servicio = servicio;
        this.entrada = entrada;
        this.salida = salida;
        this.eco = eco;
    }

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            mostrarOpciones();
            String opcion = leerLinea("Seleccione una opcion: ");
            if (opcion == null) {
                break; // fin de la entrada estandar
            }
            try {
                switch (opcion.trim()) {
                    case "1" -> registrar();
                    case "2" -> listar();
                    case "3" -> buscarPorId();
                    case "4" -> buscarPorDocumento();
                    case "5" -> actualizar();
                    case "6" -> eliminar();
                    case "0" -> continuar = false;
                    default -> salida.println("[!] Opcion no valida. Intente de nuevo.");
                }
            } catch (ValidacionException e) {
                salida.println("[Validacion] " + e.getMessage());
            } catch (DatoDuplicadoException e) {
                salida.println("[Duplicado] " + e.getMessage());
            } catch (PersistenciaException e) {
                salida.println("[Error de base de datos] " + e.getMessage());
            } catch (EntradaFinalizadaException e) {
                continuar = false;
            }
        }
        salida.println("Hasta pronto. Los datos quedan guardados en la base de datos.");
    }

    private void mostrarOpciones() {
        salida.println();
        salida.println("===== GESTION DE ESTUDIANTES (" + servicio.contar() + " registrados) =====");
        salida.println("1. Registrar estudiante");
        salida.println("2. Listar estudiantes");
        salida.println("3. Buscar por ID");
        salida.println("4. Buscar por documento");
        salida.println("5. Actualizar estudiante");
        salida.println("6. Eliminar estudiante");
        salida.println("0. Salir");
    }

    private void registrar() {
        salida.println("--- Registrar estudiante ---");
        Estudiante e = new Estudiante();
        e.setDocumento(leerObligatorio("Documento: "));
        e.setNombres(leerObligatorio("Nombres: "));
        e.setApellidos(leerObligatorio("Apellidos: "));
        e.setCorreo(leerObligatorio("Correo: "));
        e.setPrograma(leerObligatorio("Programa: "));
        e.setSemestre(leerEntero("Semestre (1-10): "));
        Estudiante guardado = servicio.registrar(e);
        salida.println("[OK] Estudiante registrado con ID " + guardado.getId() + ".");
    }

    private void listar() {
        List<Estudiante> lista = servicio.listar();
        if (lista.isEmpty()) {
            salida.println("No hay estudiantes registrados.");
            return;
        }
        imprimirEncabezado();
        lista.forEach(salida::println);
        salida.println(SEPARADOR);
        salida.println("Total: " + lista.size());
    }

    private void buscarPorId() {
        int id = leerEntero("ID a buscar: ");
        mostrarResultado(servicio.buscarPorId(id), "No existe un estudiante con ID " + id + ".");
    }

    private void buscarPorDocumento() {
        String documento = leerObligatorio("Documento a buscar: ");
        mostrarResultado(servicio.buscarPorDocumento(documento),
                "No existe un estudiante con documento " + documento.trim() + ".");
    }

    private void actualizar() {
        int id = leerEntero("ID del estudiante a actualizar: ");
        Optional<Estudiante> encontrado = servicio.buscarPorId(id);
        if (encontrado.isEmpty()) {
            salida.println("No existe un estudiante con ID " + id + ".");
            return;
        }
        Estudiante e = encontrado.get();
        salida.println("Deje el campo vacio para conservar el valor actual.");
        e.setDocumento(leerConValorActual("Documento", e.getDocumento()));
        e.setNombres(leerConValorActual("Nombres", e.getNombres()));
        e.setApellidos(leerConValorActual("Apellidos", e.getApellidos()));
        e.setCorreo(leerConValorActual("Correo", e.getCorreo()));
        e.setPrograma(leerConValorActual("Programa", e.getPrograma()));
        String semestre = leerConValorActual("Semestre", String.valueOf(e.getSemestre()));
        e.setSemestre(convertirEntero(semestre));
        if (servicio.actualizar(e)) {
            salida.println("[OK] Estudiante con ID " + id + " actualizado.");
        } else {
            salida.println("No se actualizo ningun registro.");
        }
    }

    private void eliminar() {
        int id = leerEntero("ID del estudiante a eliminar: ");
        Optional<Estudiante> encontrado = servicio.buscarPorId(id);
        if (encontrado.isEmpty()) {
            salida.println("No existe un estudiante con ID " + id + ".");
            return;
        }
        salida.println("Se eliminara a: " + encontrado.get().getNombreCompleto());
        String confirmacion = leerObligatorio("Confirma la eliminacion? (S/N): ");
        if (!confirmacion.trim().equalsIgnoreCase("S")) {
            salida.println("Eliminacion cancelada.");
            return;
        }
        if (servicio.eliminar(id)) {
            salida.println("[OK] Estudiante con ID " + id + " eliminado.");
        }
    }

    private void mostrarResultado(Optional<Estudiante> resultado, String mensajeVacio) {
        if (resultado.isEmpty()) {
            salida.println(mensajeVacio);
            return;
        }
        imprimirEncabezado();
        salida.println(resultado.get());
        salida.println("Registrado el: " + resultado.get().getFechaRegistro());
    }

    private void imprimirEncabezado() {
        salida.println(SEPARADOR);
        salida.println(String.format("%-4s %-12s %-28s %-30s %-24s %3s",
                "ID", "DOCUMENTO", "NOMBRE COMPLETO", "CORREO", "PROGRAMA", "SEM"));
        salida.println(SEPARADOR);
    }

    // ----- Lectura de datos -----

    private String leerLinea(String mensaje) {
        salida.print(mensaje);
        salida.flush();
        if (!entrada.hasNextLine()) {
            return null;
        }
        String linea = entrada.nextLine();
        if (eco) {
            salida.println(linea);
        }
        return linea;
    }

    private String leerObligatorio(String mensaje) {
        String valor = leerLinea(mensaje);
        if (valor == null) {
            throw new EntradaFinalizadaException();
        }
        return valor;
    }

    private String leerConValorActual(String campo, String actual) {
        String valor = leerObligatorio(campo + " [" + actual + "]: ");
        return valor.isBlank() ? actual : valor;
    }

    private int leerEntero(String mensaje) {
        return convertirEntero(leerObligatorio(mensaje));
    }

    private int convertirEntero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("Debe ingresar un numero entero valido.");
        }
    }

    /** Señal interna para terminar cuando ya no hay mas entrada disponible. */
    private static class EntradaFinalizadaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}

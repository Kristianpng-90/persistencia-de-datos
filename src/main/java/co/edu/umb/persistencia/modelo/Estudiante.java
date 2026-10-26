package co.edu.umb.persistencia.modelo;

import java.util.Objects;

/**
 * Entidad del dominio que representa a un estudiante.
 * Cada objeto corresponde a una fila de la tabla "estudiantes".
 */
public class Estudiante {

    private Integer id;
    private String documento;
    private String nombres;
    private String apellidos;
    private String correo;
    private String programa;
    private int semestre;
    private String fechaRegistro;

    public Estudiante() {
    }

    public Estudiante(String documento, String nombres, String apellidos,
                      String correo, String programa, int semestre) {
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.programa = programa;
        this.semestre = semestre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPrograma() { return programa; }
    public void setPrograma(String programa) { this.programa = programa; }

    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estudiante otro)) return false;
        return Objects.equals(id, otro.id) && Objects.equals(documento, otro.documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documento);
    }

    @Override
    public String toString() {
        return String.format("%-4s %-12s %-28s %-30s %-24s %3d",
                id, documento, getNombreCompleto(), correo, programa, semestre);
    }
}

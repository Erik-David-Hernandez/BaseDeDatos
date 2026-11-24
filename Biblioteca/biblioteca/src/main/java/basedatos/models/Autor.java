package basedatos.models;

public class Autor {
    private int idAutor;
    private String nombreCompleto;

    public Autor(int idAutor, String nombreCompleto) {
        this.idAutor = idAutor;
        this.nombreCompleto = nombreCompleto;
    }

    public int getIdAutor() { return idAutor; }
    public String getNombreCompleto() { return nombreCompleto; }

    @Override
    public String toString() { return nombreCompleto; }
}

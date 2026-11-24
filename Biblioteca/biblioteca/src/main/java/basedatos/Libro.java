package basedatos;

public class Libro {
    private int idLibro;
    private String titulo;
    private String isbn;
    private String codigoSeccion;
    private int nEjemplares;
    private int idGenero;
    private String nombreGenero;
    private int idAutor;
    private String nombreAutor;

    public Libro(int idLibro, String titulo, String isbn, String codigoSeccion,
                 int nEjemplares, int idGenero, String nombreGenero, int idAutor, String nombreAutor) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.codigoSeccion = codigoSeccion;
        this.nEjemplares = nEjemplares;
        this.idGenero = idGenero;
        this.nombreGenero = nombreGenero;
        this.idAutor = idAutor;
        this.nombreAutor = nombreAutor;
    }

    public int getIdLibro() { return idLibro; }
    public String getTitulo() { return titulo; }
    public String getIsbn() { return isbn; }
    public String getCodigoSeccion() { return codigoSeccion; }
    public int getNEjemplares() { return nEjemplares; }
    public int getIdGenero() { return idGenero; }
    public String getNombreGenero() { return nombreGenero; }
    public int getIdAutor() { return idAutor; }
    public String getNombreAutor() { return nombreAutor; }

    public String getDisponibilidadTexto() {
        return nEjemplares > 0 ? "Disponible (" + nEjemplares + ")" : "Agotado (0)";
    }
}

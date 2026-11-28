package basedatos.models;

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
    private int idCategoria;
    private String nombreCategoria;

    public Libro(int idLibro, String titulo, String isbn, String codigoSeccion,
                 int nEjemplares, int idGenero, String nombreGenero, int idAutor, String nombreAutor) {
        this(idLibro, titulo, isbn, codigoSeccion, nEjemplares, idGenero, nombreGenero, idAutor, nombreAutor, 0, null);
    }

    public Libro(int idLibro, String titulo, String isbn, String codigoSeccion,
                 int nEjemplares, int idGenero, String nombreGenero, int idAutor, String nombreAutor,
                 int idCategoria, String nombreCategoria) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.codigoSeccion = codigoSeccion;
        this.nEjemplares = nEjemplares;
        this.idGenero = idGenero;
        this.nombreGenero = nombreGenero;
        this.idAutor = idAutor;
        this.nombreAutor = nombreAutor;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
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
    public int getIdCategoria() { return idCategoria; }
    public String getNombreCategoria() { return nombreCategoria == null ? "Sin categoría" : nombreCategoria; }

    public String getDisponibilidadTexto() {
        return nEjemplares > 0 ? "Disponible (" + nEjemplares + ")" : "Agotado (0)";
    }
}

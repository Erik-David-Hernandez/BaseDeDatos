package basedatos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Prestamo {
    private int idReserva;
    private int idUsuario;
    private String nombreUsuario;
    private String emailUsuario;
    private int idLibro;
    private String tituloLibro;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Prestamo(int idReserva, int idUsuario, String nombreUsuario, String emailUsuario,
                    int idLibro, String tituloLibro, LocalDateTime fechaInicio, LocalDateTime fechaFin, String estado) {
        this.idReserva = idReserva;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.emailUsuario = emailUsuario;
        this.idLibro = idLibro;
        this.tituloLibro = tituloLibro;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdReserva() { return idReserva; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getEmailUsuario() { return emailUsuario; }
    public int getIdLibro() { return idLibro; }
    public String getTituloLibro() { return tituloLibro; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getFechaInicioTexto() { return fechaInicio != null ? fechaInicio.format(FMT) : ""; }
    public String getFechaFinTexto() { return fechaFin != null ? fechaFin.format(FMT) : ""; }
    public String getEstado() { return estado; }
}

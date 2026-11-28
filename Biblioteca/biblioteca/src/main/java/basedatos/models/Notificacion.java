package basedatos.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacion {
    private final int idNotificacion;
    private final String asunto;
    private final String mensaje;
    private final LocalDateTime fecha;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Notificacion(int idNotificacion, String asunto, String mensaje, LocalDateTime fecha) {
        this.idNotificacion = idNotificacion;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public String getFechaTexto() { return fecha == null ? "Sin fecha" : fecha.format(FORMATO); }
}

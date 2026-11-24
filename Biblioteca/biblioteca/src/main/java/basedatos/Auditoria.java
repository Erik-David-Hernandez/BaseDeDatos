package basedatos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auditoria {
    private int idAuditoria;
    private String usuario;
    private String accion;
    private String tablaAfectada;
    private String detalle;
    private LocalDateTime fechaHora;

    public Auditoria(int idAuditoria, String usuario, String accion, String tablaAfectada, String detalle, LocalDateTime fechaHora) {
        this.idAuditoria = idAuditoria;
        this.usuario = usuario;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
    }

    public int getIdAuditoria() { return idAuditoria; }
    public String getUsuario() { return usuario; }
    public String getAccion() { return accion; }
    public String getTablaAfectada() { return tablaAfectada; }
    public String getDetalle() { return detalle; }
    public String getFechaTexto() {
        return fechaHora != null ? fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }
}

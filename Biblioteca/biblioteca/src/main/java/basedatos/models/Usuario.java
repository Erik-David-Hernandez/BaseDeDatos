package basedatos.models;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String email;
    private String rol;
    private boolean activo;

    public Usuario(int idUsuario, String nombre, String email, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public boolean isAdministrador() { return activo && "administrador".equalsIgnoreCase(rol); }
    public String getEstadoTexto() { return activo ? "Activo" : "Inactivo"; }
}

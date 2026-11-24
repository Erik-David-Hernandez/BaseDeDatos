package basedatos;

public class Session {
    private static Usuario usuarioActual;

    public static void setUsuarioActual(Usuario u) {
        usuarioActual = u;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}

package basedatos.models;

import basedatos.utils.ConexionDB;
import basedatos.utils.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Capa de acceso a datos (DAO) que cubre los 15 Requerimientos Funcionales y No Funcionales.
 */
public class BibliotecaDAO {

    // =========================================================================
    // RNF13: AUDITORÍA Y RF6/RNF9: NOTIFICACIONES POR CORREO
    // =========================================================================
    public static void registrarAuditoria(Integer idUsuario, String accion, String tabla, String detalle) {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, detalle) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUsuario != null) ps.setInt(1, idUsuario);
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, accion);
            ps.setString(3, tabla);
            ps.setString(4, detalle);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    public static void enviarNotificacionCorreo(int idUsuario, String emailDestino, String asunto, String mensaje) {
        String sql = "INSERT INTO notificacion_correo (id_usuario, asunto, mensaje, enviado) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, asunto);
            ps.setString(3, mensaje);
            ps.executeUpdate();
            System.out.println("[CORREO ENVIADO a " + emailDestino + "] " + asunto + " -> " + mensaje);
        } catch (SQLException e) {
            System.err.println("Error al registrar notificación de correo: " + e.getMessage());
        }
    }

    // =========================================================================
    // RF3 y RF4: REGISTRO E INICIO DE SESIÓN DE USUARIOS (CON HASH + SALT)
    // =========================================================================
    public static Usuario autenticar(String email, String password) throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, pswd, salt, rol, activo FROM usuario WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean activo = rs.getBoolean("activo");
                    if (!activo) {
                        throw new SQLException("Tu cuenta está desactivada. Contacta al administrador.");
                    }
                    String hashGuardado = rs.getString("pswd");
                    String saltGuardado = rs.getString("salt");
                    if (PasswordUtil.verificarPassword(password, hashGuardado, saltGuardado)) {
                        Usuario u = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("rol"),
                            activo
                        );
                        registrarAuditoria(u.getIdUsuario(), "LOGIN", "usuario", "Inicio de sesión exitoso");
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public static boolean registrarUsuario(String nombre, String email, String password) throws SQLException {
        String salt = PasswordUtil.generarSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        String sql = "INSERT INTO usuario (nombre, email, pswd, salt, rol, activo) VALUES (?, ?, ?, ?, 'lector', TRUE) RETURNING id_usuario";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, salt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nuevoId = rs.getInt(1);
                    registrarAuditoria(nuevoId, "REGISTRO_USUARIO", "usuario", "Nuevo lector registrado: " + email);
                    enviarNotificacionCorreo(nuevoId, email, "Bienvenido a la Biblioteca Virtual",
                            "Hola " + nombre + ", tu cuenta ha sido creada exitosamente.");
                    return true;
                }
            }
        }
        return false;
    }

    // =========================================================================
    // RF1 y RF2: BÚSQUEDA DE LIBROS POR TÍTULO, AUTOR Y GÉNERO + DISPONIBILIDAD
    // =========================================================================
    public static List<Libro> buscarLibros(String filtro) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT l.id_libro, l.titulo, l.isbn, l.codigo_seccion, l.n_ejemplares, " +
                     "g.id_genero, g.nombre AS genero, a.id_autor, " +
                     "a.nombres || ' ' || a.apellido_p || ' ' || COALESCE(a.apellido_m, '') AS autor " +
                     "FROM libro l " +
                     "INNER JOIN genero g ON l.id_genero = g.id_genero " +
                     "INNER JOIN autor a ON l.id_autor = a.id_autor " +
                     "WHERE LOWER(l.titulo) LIKE LOWER(?) " +
                     "OR LOWER(a.nombres || ' ' || a.apellido_p || ' ' || COALESCE(a.apellido_m, '')) LIKE LOWER(?) " +
                     "OR LOWER(g.nombre) LIKE LOWER(?) " +
                     "ORDER BY l.titulo";
        String patron = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Libro(
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getString("isbn"),
                        rs.getString("codigo_seccion"),
                        rs.getInt("n_ejemplares"),
                        rs.getInt("id_genero"),
                        rs.getString("genero"),
                        rs.getInt("id_autor"),
                        rs.getString("autor").trim()
                    ));
                }
            }
        }
        return lista;
    }

    // =========================================================================
    // RF5, RF6, RF11, RF12, RF15: RESERVAR, CANCELAR Y VER HISTORIAL
    // =========================================================================
    public static void reservarLibro(Usuario usuario, Libro libro, LocalDateTime fechaRecogida, int diasPrestamo) throws SQLException {
        if (libro.getNEjemplares() <= 0) {
            throw new SQLException("No hay ejemplares disponibles para este libro.");
        }
        LocalDateTime fechaFin = fechaRecogida.plusDays(diasPrestamo);
        String sql = "INSERT INTO prestamo (id_usuario, id_libro, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, 'reservado')";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuario.getIdUsuario());
            ps.setInt(2, libro.getIdLibro());
            ps.setTimestamp(3, Timestamp.valueOf(fechaRecogida));
            ps.setTimestamp(4, Timestamp.valueOf(fechaFin));
            ps.executeUpdate();
        }
        registrarAuditoria(usuario.getIdUsuario(), "RESERVA_LIBRO", "prestamo", "Reservó libro ID: " + libro.getIdLibro());
        enviarNotificacionCorreo(usuario.getIdUsuario(), usuario.getEmail(),
                "Reserva Confirmada: " + libro.getTitulo(),
                "Tu reserva del libro '" + libro.getTitulo() + "' ha sido confirmada. Fecha límite de devolución: " + fechaFin.toLocalDate());
    }

    public static void cancelarReserva(Usuario usuario, Prestamo prestamo) throws SQLException {
        if (!"reservado".equalsIgnoreCase(prestamo.getEstado())) {
            throw new SQLException("Solo se pueden cancelar préstamos en estado 'reservado'.");
        }
        String sql = "UPDATE prestamo SET estado = 'cancelado' WHERE id_reserva = ? AND id_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prestamo.getIdReserva());
            ps.setInt(2, usuario.getIdUsuario());
            ps.executeUpdate();
        }
        registrarAuditoria(usuario.getIdUsuario(), "CANCELAR_RESERVA", "prestamo", "Canceló reserva ID: " + prestamo.getIdReserva());
    }

    public static List<Prestamo> obtenerHistorialUsuario(int idUsuario) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.id_reserva, p.id_usuario, u.nombre, u.email, p.id_libro, l.titulo, " +
                     "p.fecha_inicio, p.fecha_fin, p.estado " +
                     "FROM prestamo p " +
                     "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
                     "INNER JOIN libro l ON p.id_libro = l.id_libro " +
                     "WHERE p.id_usuario = ? ORDER BY p.fecha_inicio DESC";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Prestamo(
                        rs.getInt("id_reserva"),
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getTimestamp("fecha_inicio").toLocalDateTime(),
                        rs.getTimestamp("fecha_fin").toLocalDateTime(),
                        rs.getString("estado")
                    ));
                }
            }
        }
        return lista;
    }

    // =========================================================================
    // RF7, RF8, RF9, RF10, RF13, RF14: FUNCIONES DE ADMINISTRADOR
    // =========================================================================
    public static List<Genero> obtenerGeneros() throws SQLException {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT id_genero, nombre FROM genero ORDER BY nombre";
        try (Connection conn = ConexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Genero(rs.getInt("id_genero"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public static void agregarGenero(String nombreGenero, int idAdmin) throws SQLException {
        String sql = "INSERT INTO genero (nombre) VALUES (?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreGenero.trim());
            ps.executeUpdate();
        }
        registrarAuditoria(idAdmin, "CREAR_GENERO", "genero", "Agregó género: " + nombreGenero);
    }

    public static List<Autor> obtenerAutores() throws SQLException {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT id_autor, nombres || ' ' || apellido_p || ' ' || COALESCE(apellido_m, '') AS completo FROM autor ORDER BY apellido_p";
        try (Connection conn = ConexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Autor(rs.getInt("id_autor"), rs.getString("completo").trim()));
            }
        }
        return lista;
    }

    public static void agregarAutor(String nombres, String apellidoP, String apellidoM, int idAdmin) throws SQLException {
        String sql = "INSERT INTO autor (nombres, apellido_p, apellido_m) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombres.trim());
            ps.setString(2, apellidoP.trim());
            ps.setString(3, (apellidoM == null || apellidoM.trim().isEmpty()) ? null : apellidoM.trim());
            ps.executeUpdate();
        }
        registrarAuditoria(idAdmin, "CREAR_AUTOR", "autor", "Agregó autor: " + nombres + " " + apellidoP);
    }

    public static void agregarLibro(String titulo, String isbn, String seccion, int ejemplares, int idGenero, int idAutor, int idAdmin) throws SQLException {
        String sql = "INSERT INTO libro (titulo, isbn, codigo_seccion, n_ejemplares, id_genero, id_autor) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titulo.trim());
            ps.setString(2, isbn.trim());
            ps.setString(3, seccion.trim());
            ps.setInt(4, ejemplares);
            ps.setInt(5, idGenero);
            ps.setInt(6, idAutor);
            ps.executeUpdate();
        }
        registrarAuditoria(idAdmin, "CREAR_LIBRO", "libro", "Agregó libro: " + titulo + " (ISBN: " + isbn + ")");
    }

    public static List<Prestamo> obtenerTodosPrestamos() throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.id_reserva, p.id_usuario, u.nombre, u.email, p.id_libro, l.titulo, " +
                     "p.fecha_inicio, p.fecha_fin, p.estado " +
                     "FROM prestamo p " +
                     "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
                     "INNER JOIN libro l ON p.id_libro = l.id_libro " +
                     "ORDER BY p.fecha_inicio DESC";
        try (Connection conn = ConexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Prestamo(
                    rs.getInt("id_reserva"),
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getInt("id_libro"),
                    rs.getString("titulo"),
                    rs.getTimestamp("fecha_inicio").toLocalDateTime(),
                    rs.getTimestamp("fecha_fin").toLocalDateTime(),
                    rs.getString("estado")
                ));
            }
        }
        return lista;
    }

    public static void actualizarLibro(Libro original, String titulo, String isbn, String seccion,
            int disponibles, int idGenero, int idAutor, int idAdmin) throws SQLException {
        if (titulo == null || titulo.trim().isEmpty() || isbn == null || isbn.trim().isEmpty()
                || seccion == null || seccion.trim().isEmpty() || disponibles < 0) {
            throw new SQLException("Completa los datos del libro y usa una cantidad disponible no negativa.");
        }
        // Compare the original fields so an open dialog cannot overwrite another
        // librarian's edit or inventory changes from new reservations/returns.
        String sql = "UPDATE libro SET titulo=?, isbn=?, codigo_seccion=?, n_ejemplares=?, id_genero=?, id_autor=? "
                + "WHERE id_libro=? AND titulo=? AND isbn=? AND codigo_seccion=? "
                + "AND n_ejemplares=? AND id_genero=? AND id_autor=?";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titulo.trim());
            ps.setString(2, isbn.trim());
            ps.setString(3, seccion.trim());
            ps.setInt(4, disponibles);
            ps.setInt(5, idGenero);
            ps.setInt(6, idAutor);
            ps.setInt(7, original.getIdLibro());
            ps.setString(8, original.getTitulo());
            ps.setString(9, original.getIsbn());
            ps.setString(10, original.getCodigoSeccion());
            ps.setInt(11, original.getNEjemplares());
            ps.setInt(12, original.getIdGenero());
            ps.setInt(13, original.getIdAutor());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("El libro cambió o fue eliminado. Cierra la ventana, recarga los datos y vuelve a editar.");
            }
        }
        registrarAuditoria(idAdmin, "EDITAR_LIBRO", "libro", "Editó libro #" + original.getIdLibro() + ": " + titulo.trim());
    }

    public static void cambiarEstadoPrestamo(Prestamo p, String nuevoEstado, int idAdmin) throws SQLException {
        String anterior = p.getEstado();
        boolean permitido = ("reservado".equals(anterior)
                && ("prestado".equals(nuevoEstado) || "cancelado".equals(nuevoEstado)))
                || (("prestado".equals(anterior) || "vencido".equals(anterior))
                && ("devuelto".equals(nuevoEstado) || "cancelado".equals(nuevoEstado)));
        if (!permitido) {
            throw new SQLException("No se puede cambiar un préstamo de '" + anterior + "' a '" + nuevoEstado + "'.");
        }
        String sql = "UPDATE prestamo SET estado = ?, fecha_devolucion_real = ? "
                + "WHERE id_reserva = ? AND estado = ? RETURNING id_libro";
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idLibro;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nuevoEstado);
                    if ("devuelto".equals(nuevoEstado)) ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    else ps.setNull(2, Types.TIMESTAMP);
                    ps.setInt(3, p.getIdReserva());
                    ps.setString(4, anterior);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo cambió o fue eliminado. Recarga los datos.");
                        idLibro = rs.getInt("id_libro");
                    }
                }
                // The installed inventory trigger releases reserved/borrowed copies,
                // but omits overdue ones. Compensate only that transition, atomically.
                if ("vencido".equals(anterior)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE libro SET n_ejemplares = n_ejemplares + 1 WHERE id_libro = ?")) {
                        ps.setInt(1, idLibro);
                        if (ps.executeUpdate() != 1) throw new SQLException("No se pudo actualizar el inventario del libro.");
                    }
                }
                conn.commit();
            } catch (SQLException error) {
                conn.rollback();
                throw error;
            }
        }
        registrarAuditoria(idAdmin, "CAMBIO_ESTADO_PRESTAMO", "prestamo",
                "Reserva #" + p.getIdReserva() + " -> " + nuevoEstado);
        enviarNotificacionCorreo(p.getIdUsuario(), p.getEmailUsuario(),
                "Actualización de Préstamo: " + p.getTituloLibro(),
                "El estado de tu préstamo (#" + p.getIdReserva() + ") ahora es: " + nuevoEstado.toUpperCase());
    }

    public static List<Usuario> obtenerTodosUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, email, rol, activo FROM usuario ORDER BY id_usuario";
        try (Connection conn = ConexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("rol"),
                    rs.getBoolean("activo")
                ));
            }
        }
        return lista;
    }

    public static void actualizarUsuarioAdmin(int idUsuario, String nuevoRol, boolean nuevoActivo, int idAdmin) throws SQLException {
        String sql = "UPDATE usuario SET rol = ?, activo = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoRol);
            ps.setBoolean(2, nuevoActivo);
            ps.setInt(3, idUsuario);
            ps.executeUpdate();
        }
        registrarAuditoria(idAdmin, "ACTUALIZAR_USUARIO", "usuario",
                "Usuario ID " + idUsuario + " -> rol=" + nuevoRol + ", activo=" + nuevoActivo);
    }

    public static List<Auditoria> obtenerAuditoria() throws SQLException {
        List<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT a.id_auditoria, COALESCE(u.nombre, 'Sistema') AS usuario, " +
                     "a.accion, a.tabla_afectada, a.detalle, a.fecha_hora " +
                     "FROM auditoria a LEFT JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     "ORDER BY a.fecha_hora DESC LIMIT 100";
        try (Connection conn = ConexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Auditoria(
                    rs.getInt("id_auditoria"),
                    rs.getString("usuario"),
                    rs.getString("accion"),
                    rs.getString("tabla_afectada"),
                    rs.getString("detalle"),
                    rs.getTimestamp("fecha_hora").toLocalDateTime()
                ));
            }
        }
        return lista;
    }
}

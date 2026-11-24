package basedatos.controllers;

import basedatos.App;
import basedatos.models.Auditoria;
import basedatos.models.Autor;
import basedatos.models.BibliotecaDAO;
import basedatos.models.Genero;
import basedatos.models.Libro;
import basedatos.models.Prestamo;
import basedatos.utils.Session;
import basedatos.models.Usuario;
import basedatos.utils.FiltroPrestamos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.sql.SQLException;

public class AdminController {

    @FXML private Label lblAdmin;

    // Tab 1: Libros, Autores y Géneros
    @FXML private TextField txtTituloLibro;
    @FXML private TextField txtIsbnLibro;
    @FXML private TextField txtSeccionLibro;
    @FXML private Spinner<Integer> spEjemplaresLibro;
    @FXML private ComboBox<Genero> cbGeneroLibro;
    @FXML private ComboBox<Autor> cbAutorLibro;

    @FXML private TextField txtNuevoGenero;
    @FXML private TextField txtAutorNombres;
    @FXML private TextField txtAutorApellidoP;
    @FXML private TextField txtAutorApellidoM;

    @FXML private TableView<Libro> tablaAdminLibros;
    @FXML private TableColumn<Libro, Integer> colLibId;
    @FXML private TableColumn<Libro, String> colLibTitulo;
    @FXML private TableColumn<Libro, String> colLibIsbn;
    @FXML private TableColumn<Libro, String> colLibAutor;
    @FXML private TableColumn<Libro, String> colLibGenero;
    @FXML private TableColumn<Libro, Integer> colLibEjemplares;

    // Tab 2: Préstamos
    @FXML private TextField txtBuscarPrestamos;
    @FXML private DatePicker dpPrestamosDesde;
    @FXML private DatePicker dpPrestamosHasta;
    @FXML private Label lblFiltroPrestamos;
    private final ObservableList<Prestamo> prestamos = FXCollections.observableArrayList();
    private final FilteredList<Prestamo> prestamosFiltrados = new FilteredList<>(prestamos);
    @FXML private TableView<Prestamo> tablaAdminPrestamos;
    @FXML private TableColumn<Prestamo, Integer> colPresId;
    @FXML private TableColumn<Prestamo, String> colPresUsuario;
    @FXML private TableColumn<Prestamo, String> colPresLibro;
    @FXML private TableColumn<Prestamo, String> colPresInicio;
    @FXML private TableColumn<Prestamo, String> colPresFin;
    @FXML private TableColumn<Prestamo, String> colPresEstado;

    // Tab 3: Usuarios
    @FXML private TableView<Usuario> tablaAdminUsuarios;
    @FXML private TableColumn<Usuario, Integer> colUsrId;
    @FXML private TableColumn<Usuario, String> colUsrNombre;
    @FXML private TableColumn<Usuario, String> colUsrEmail;
    @FXML private TableColumn<Usuario, String> colUsrRol;
    @FXML private TableColumn<Usuario, String> colUsrEstado;
    @FXML private ComboBox<String> cbRolUsuario;
    @FXML private CheckBox chkUsuarioActivo;

    // Tab 4: Auditoría
    @FXML private TableView<Auditoria> tablaAuditoria;
    @FXML private TableColumn<Auditoria, Integer> colAudId;
    @FXML private TableColumn<Auditoria, String> colAudUsuario;
    @FXML private TableColumn<Auditoria, String> colAudAccion;
    @FXML private TableColumn<Auditoria, String> colAudTabla;
    @FXML private TableColumn<Auditoria, String> colAudDetalle;
    @FXML private TableColumn<Auditoria, String> colAudFecha;

    @FXML
    public void initialize() {
        Usuario u = Session.getUsuarioActual();
        if (u != null) {
            lblAdmin.setText("Panel Administrativo - " + u.getNombre() + " [" + u.getRol().toUpperCase() + "]");
        }

        spEjemplaresLibro.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 3));
        cbRolUsuario.setItems(FXCollections.observableArrayList("lector", "bibliotecario", "administrador"));

        // Configurar columnas Libros
        colLibId.setCellValueFactory(new PropertyValueFactory<>("idLibro"));
        colLibTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colLibIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colLibAutor.setCellValueFactory(new PropertyValueFactory<>("nombreAutor"));
        colLibGenero.setCellValueFactory(new PropertyValueFactory<>("nombreGenero"));
        colLibEjemplares.setCellValueFactory(new PropertyValueFactory<>("nEjemplares"));

        // Configurar columnas Préstamos
        colPresId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colPresUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colPresLibro.setCellValueFactory(new PropertyValueFactory<>("tituloLibro"));
        colPresInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicioTexto"));
        colPresFin.setCellValueFactory(new PropertyValueFactory<>("fechaFinTexto"));
        colPresEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        SortedList<Prestamo> ordenados = new SortedList<>(prestamosFiltrados);
        ordenados.comparatorProperty().bind(tablaAdminPrestamos.comparatorProperty());
        tablaAdminPrestamos.setItems(ordenados);
        txtBuscarPrestamos.textProperty().addListener((obs, anterior, nuevo) -> filtrarPrestamos());
        dpPrestamosDesde.valueProperty().addListener((obs, anterior, nuevo) -> filtrarPrestamos());
        dpPrestamosHasta.valueProperty().addListener((obs, anterior, nuevo) -> filtrarPrestamos());

        // Configurar columnas Usuarios
        colUsrId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colUsrNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsrEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsrRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colUsrEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        tablaAdminUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                cbRolUsuario.setValue(newV.getRol());
                chkUsuarioActivo.setSelected(newV.isActivo());
            }
        });

        // Configurar columnas Auditoría
        colAudId.setCellValueFactory(new PropertyValueFactory<>("idAuditoria"));
        colAudUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colAudAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colAudTabla.setCellValueFactory(new PropertyValueFactory<>("tablaAfectada"));
        colAudDetalle.setCellValueFactory(new PropertyValueFactory<>("detalle"));
        colAudFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));

        recargarTodo();
    }

    @FXML
    private void recargarTodo() {
        try {
            cbGeneroLibro.setItems(FXCollections.observableArrayList(BibliotecaDAO.obtenerGeneros()));
            cbAutorLibro.setItems(FXCollections.observableArrayList(BibliotecaDAO.obtenerAutores()));
            tablaAdminLibros.setItems(FXCollections.observableArrayList(BibliotecaDAO.buscarLibros("")));
            prestamos.setAll(BibliotecaDAO.obtenerTodosPrestamos());
            filtrarPrestamos();
            tablaAdminUsuarios.setItems(FXCollections.observableArrayList(BibliotecaDAO.obtenerTodosUsuarios()));
            tablaAuditoria.setItems(FXCollections.observableArrayList(BibliotecaDAO.obtenerAuditoria()));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", e.getMessage());
        }
    }

    private void filtrarPrestamos() {
        prestamosFiltrados.setPredicate(p -> FiltroPrestamos.coincide(p, txtBuscarPrestamos.getText(),
                dpPrestamosDesde.getValue(), dpPrestamosHasta.getValue()));
        if (dpPrestamosDesde.getValue() != null && dpPrestamosHasta.getValue() != null
                && dpPrestamosDesde.getValue().isAfter(dpPrestamosHasta.getValue())) {
            lblFiltroPrestamos.setText("La fecha desde debe ser anterior o igual a hasta.");
        } else {
            lblFiltroPrestamos.setText(prestamosFiltrados.size() + " de " + prestamos.size() + " préstamos");
        }
    }

    @FXML
    private void limpiarFiltrosPrestamos() {
        txtBuscarPrestamos.clear();
        dpPrestamosDesde.setValue(null);
        dpPrestamosHasta.setValue(null);
    }

    @FXML
    private void editarLibro() {
        Libro libro = tablaAdminLibros.getSelectionModel().getSelectedItem();
        if (libro == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona el libro que deseas editar.");
            return;
        }
        TextField titulo = new TextField(libro.getTitulo());
        TextField isbn = new TextField(libro.getIsbn());
        TextField seccion = new TextField(libro.getCodigoSeccion());
        ComboBox<Autor> autor = new ComboBox<>(FXCollections.observableArrayList(cbAutorLibro.getItems()));
        ComboBox<Genero> genero = new ComboBox<>(FXCollections.observableArrayList(cbGeneroLibro.getItems()));
        autor.getItems().stream().filter(a -> a.getIdAutor() == libro.getIdAutor()).findFirst().ifPresent(autor::setValue);
        genero.getItems().stream().filter(g -> g.getIdGenero() == libro.getIdGenero()).findFirst().ifPresent(genero::setValue);
        Spinner<Integer> disponibles = new Spinner<>(0, Integer.MAX_VALUE, libro.getNEjemplares());
        Label error = new Label();
        error.setWrapText(true);
        error.setStyle("-fx-text-fill: #b91c1c;");
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(12));
        form.addRow(0, new Label("Título:"), titulo);
        form.addRow(1, new Label("ISBN:"), isbn);
        form.addRow(2, new Label("Sección:"), seccion);
        form.addRow(3, new Label("Autor:"), autor);
        form.addRow(4, new Label("Género:"), genero);
        form.addRow(5, new Label("Ejemplares disponibles:"), disponibles);
        form.add(error, 0, 6, 2, 1);
        titulo.setPrefWidth(300);
        autor.setMaxWidth(Double.MAX_VALUE);
        genero.setMaxWidth(Double.MAX_VALUE);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(tablaAdminLibros.getScene().getWindow());
        dialog.setTitle("Editar libro #" + libro.getIdLibro());
        dialog.setHeaderText("Modificar registro del libro");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().addAll(tablaAdminLibros.getScene().getStylesheets());
        ButtonType guardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        dialog.getDialogPane().lookupButton(guardar).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            if (titulo.getText().trim().isEmpty() || isbn.getText().trim().isEmpty()
                    || seccion.getText().trim().isEmpty() || autor.getValue() == null || genero.getValue() == null) {
                error.setText("Completa todos los datos del libro.");
                return;
            }
            try {
                BibliotecaDAO.actualizarLibro(libro, titulo.getText(), isbn.getText(), seccion.getText(),
                        disponibles.getValue(), genero.getValue().getIdGenero(), autor.getValue().getIdAutor(),
                        Session.getUsuarioActual().getIdUsuario());
                dialog.close();
                recargarTodo();
            } catch (SQLException e) {
                error.setText(e.getMessage());
            }
        });
        dialog.showAndWait();
    }

    @FXML
    private void agregarLibro() {
        String titulo = txtTituloLibro.getText().trim();
        String isbn = txtIsbnLibro.getText().trim();
        String seccion = txtSeccionLibro.getText().trim();
        Genero gen = cbGeneroLibro.getValue();
        Autor aut = cbAutorLibro.getValue();
        int ejemplares = spEjemplaresLibro.getValue();

        if (titulo.isEmpty() || isbn.isEmpty() || seccion.isEmpty() || gen == null || aut == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Completa todos los datos del libro.");
            return;
        }
        try {
            int idAdmin = Session.getUsuarioActual().getIdUsuario();
            BibliotecaDAO.agregarLibro(titulo, isbn, seccion, ejemplares, gen.getIdGenero(), aut.getIdAutor(), idAdmin);
            txtTituloLibro.clear();
            txtIsbnLibro.clear();
            txtSeccionLibro.clear();
            recargarTodo();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Libro Agregado", "El libro se registró correctamente con ID autogenerado.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar libro", e.getMessage());
        }
    }

    @FXML
    private void agregarGenero() {
        String nombre = txtNuevoGenero.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo vacío", "Ingresa el nombre del género.");
            return;
        }
        try {
            BibliotecaDAO.agregarGenero(nombre, Session.getUsuarioActual().getIdUsuario());
            txtNuevoGenero.clear();
            recargarTodo();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Género Agregado", "Género '" + nombre + "' agregado.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void agregarAutor() {
        String nombres = txtAutorNombres.getText().trim();
        String apP = txtAutorApellidoP.getText().trim();
        String apM = txtAutorApellidoM.getText().trim();
        if (nombres.isEmpty() || apP.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos requeridos", "Nombres y apellido paterno son obligatorios.");
            return;
        }
        try {
            BibliotecaDAO.agregarAutor(nombres, apP, apM, Session.getUsuarioActual().getIdUsuario());
            txtAutorNombres.clear();
            txtAutorApellidoP.clear();
            txtAutorApellidoM.clear();
            recargarTodo();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Autor Agregado", "Autor registrado correctamente.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void marcarPrestado() {
        cambiarEstadoSeleccionado("prestado");
    }

    @FXML
    private void marcarDevuelto() {
        cambiarEstadoSeleccionado("devuelto");
    }

    @FXML
    private void cancelarPrestamo() {
        Prestamo prestamo = tablaAdminPrestamos.getSelectionModel().getSelectedItem();
        if (prestamo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un préstamo de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.initOwner(tablaAdminPrestamos.getScene().getWindow());
        confirmacion.setTitle("Cancelar préstamo");
        confirmacion.setHeaderText("Cancelar préstamo #" + prestamo.getIdReserva());
        confirmacion.setContentText(prestamo.getNombreUsuario() + " — " + prestamo.getTituloLibro()
                + "\nSe devolverá un ejemplar al inventario disponible.");
        ButtonType confirmar = new ButtonType("Cancelar préstamo", ButtonBar.ButtonData.OK_DONE);
        ButtonType volver = new ButtonType("Volver", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(confirmar, volver);
        if (confirmacion.showAndWait().orElse(volver) == confirmar) cambiarEstadoSeleccionado("cancelado");
    }

    private void cambiarEstadoSeleccionado(String estado) {
        Prestamo p = tablaAdminPrestamos.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un préstamo de la tabla.");
            return;
        }
        try {
            BibliotecaDAO.cambiarEstadoPrestamo(p, estado, Session.getUsuarioActual().getIdUsuario());
            recargarTodo();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Estado Actualizado",
                    "Préstamo #" + p.getIdReserva() + " marcado como '" + estado + "' y correo enviado al lector.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void guardarCambiosUsuario() {
        Usuario u = tablaAdminUsuarios.getSelectionModel().getSelectedItem();
        if (u == null || cbRolUsuario.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un usuario de la tabla.");
            return;
        }
        try {
            BibliotecaDAO.actualizarUsuarioAdmin(u.getIdUsuario(), cbRolUsuario.getValue(),
                    chkUsuarioActivo.isSelected(), Session.getUsuarioActual().getIdUsuario());
            recargarTodo();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario Actualizado", "Permisos y estado actualizados.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void cerrarSesion() throws IOException {
        Session.cerrarSesion();
        App.setRoot("login");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

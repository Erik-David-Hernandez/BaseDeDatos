package basedatos.controllers;

import basedatos.App;
import basedatos.models.BibliotecaDAO;
import basedatos.models.Libro;
import basedatos.models.Prestamo;
import basedatos.utils.Session;
import basedatos.models.Usuario;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UsuarioController {

    @FXML private Label lblBienvenida;
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFechaRecogida;
    @FXML private Spinner<Integer> spDiasPrestamo;

    // Tabla Catálogo de Libros
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, Integer> colId;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colGenero;
    @FXML private TableColumn<Libro, String> colSeccion;
    @FXML private TableColumn<Libro, String> colDisp;

    // Tabla Historial de Préstamos/Reservas
    @FXML private TableView<Prestamo> tablaHistorial;
    @FXML private TableColumn<Prestamo, Integer> colHistId;
    @FXML private TableColumn<Prestamo, String> colHistLibro;
    @FXML private TableColumn<Prestamo, String> colHistInicio;
    @FXML private TableColumn<Prestamo, String> colHistFin;
    @FXML private TableColumn<Prestamo, String> colHistEstado;
    @FXML private Button btnCancelarReserva;

    @FXML
    public void initialize() {
        Usuario u = Session.getUsuarioActual();
        if (u != null) {
            lblBienvenida.setText("Lector: " + u.getNombre() + " (" + u.getEmail() + ")");
        }

        dpFechaRecogida.setValue(LocalDate.now());
        spDiasPrestamo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 7));

        colId.setCellValueFactory(new PropertyValueFactory<>("idLibro"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("nombreAutor"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("nombreGenero"));
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("codigoSeccion"));
        colDisp.setCellValueFactory(new PropertyValueFactory<>("disponibilidadTexto"));

        colHistId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colHistLibro.setCellValueFactory(new PropertyValueFactory<>("tituloLibro"));
        colHistInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicioTexto"));
        colHistFin.setCellValueFactory(new PropertyValueFactory<>("fechaFinTexto"));
        colHistEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        btnCancelarReserva.setDisable(true);
        tablaHistorial.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) ->
                btnCancelarReserva.setDisable(seleccionado == null || !seleccionado.puedeCancelarReserva(LocalDateTime.now())));

        cargarLibros("");
        cargarHistorial();
    }

    @FXML
    private void buscarLibros() {
        cargarLibros(txtBuscar.getText());
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarLibros("");
    }

    private void cargarLibros(String filtro) {
        try {
            tablaLibros.setItems(FXCollections.observableArrayList(BibliotecaDAO.buscarLibros(filtro)));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los libros: " + e.getMessage());
        }
    }

    private void cargarHistorial() {
        Usuario u = Session.getUsuarioActual();
        if (u == null) return;
        try {
            tablaHistorial.setItems(FXCollections.observableArrayList(BibliotecaDAO.obtenerHistorialUsuario(u.getIdUsuario())));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el historial: " + e.getMessage());
        }
    }

    @FXML
    private void reservarLibro() {
        Libro seleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        Usuario u = Session.getUsuarioActual();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un libro de la tabla para reservar.");
            return;
        }
        LocalDate fecha = dpFechaRecogida.getValue();
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Fecha inválida", "Selecciona una fecha de recogida igual o posterior a hoy.");
            return;
        }

        try {
            LocalDateTime inicio = LocalDateTime.of(fecha, LocalTime.now().withSecond(0).withNano(0));
            int dias = spDiasPrestamo.getValue();
            BibliotecaDAO.reservarLibro(u, seleccionado, inicio, dias);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Reserva Exitosa",
                    "Has reservado '" + seleccionado.getTitulo() + "'. Revisa tu correo para los detalles.");
            cargarLibros(txtBuscar.getText());
            cargarHistorial();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo reservar", e.getMessage());
        }
    }

    @FXML
    private void cancelarReserva() {
        Prestamo seleccionado = tablaHistorial.getSelectionModel().getSelectedItem();
        Usuario u = Session.getUsuarioActual();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona una reserva en tu historial para cancelar.");
            return;
        }
        try {
            BibliotecaDAO.cancelarReserva(u, seleccionado);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Reserva Cancelada", "La reserva #" + seleccionado.getIdReserva() + " fue cancelada.");
            cargarLibros(txtBuscar.getText());
            cargarHistorial();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cancelar", e.getMessage());
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

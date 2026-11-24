package basedatos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import java.io.IOException;

public class UsuarioController {

    @FXML
    private void cerrarSesion() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void reservarLibro() {
        mostrarInfo("Simulación: Libro reservado exitosamente.");
    }

    @FXML
    private void verHistorial() {
        mostrarInfo("Simulación: Abriendo ventana de historial de préstamos...");
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acción Lector");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
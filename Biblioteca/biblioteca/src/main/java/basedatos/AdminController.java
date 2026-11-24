package basedatos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import java.io.IOException;

public class AdminController {

    @FXML
    private void cerrarSesion() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void accionAdmin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acción Administrador");
        alert.setHeaderText(null);
        alert.setContentText("Simulación: Abriendo módulo de administración...");
        alert.showAndWait();
    }
}
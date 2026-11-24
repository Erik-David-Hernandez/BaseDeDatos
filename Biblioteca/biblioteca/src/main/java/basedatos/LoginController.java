package basedatos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    private void iniciarSesion() throws IOException {
        String usuario = txtUsuario.getText().trim().toLowerCase();
        
        if (usuario.equals("admin")) {
            App.setRoot("admin");
        } else if (!usuario.isEmpty()) {
            App.setRoot("usuario");
        } else {
            mostrarAlerta("Error", "Por favor, ingresa un usuario válido.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
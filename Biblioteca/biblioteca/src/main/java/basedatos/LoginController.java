package basedatos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    private void iniciarSesion() {
        String email = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, ingresa tu correo electrónico y contraseña.");
            return;
        }

        try {
            Usuario u = BibliotecaDAO.autenticar(email, password);
            if (u != null) {
                Session.setUsuarioActual(u);
                if ("administrador".equalsIgnoreCase(u.getRol()) || "bibliotecario".equalsIgnoreCase(u.getRol())) {
                    App.setRoot("admin");
                } else {
                    App.setRoot("usuario");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "Correo o contraseña incorrectos.");
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", e.getMessage());
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Interfaz", "No se pudo cargar la vista: " + e.getMessage());
        }
    }

    @FXML
    private void irARegistro() throws IOException {
        App.setRoot("registro");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package basedatos.controllers;

import basedatos.App;
import basedatos.models.BibliotecaDAO;

import javafx.fxml.FXML;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.io.IOException;
import java.sql.SQLException;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private CheckBox chkPrivacidad;
    @FXML private Button btnRegistrar;

    @FXML
    public void initialize() {
        btnRegistrar.disableProperty().bind(Bindings.createBooleanBinding(
                () -> txtNombre.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()
                        || txtPassword.getText().isEmpty() || txtConfirmarPassword.getText().isEmpty()
                        || !chkPrivacidad.isSelected(),
                txtNombre.textProperty(), txtEmail.textProperty(), txtPassword.textProperty(),
                txtConfirmarPassword.textProperty(), chkPrivacidad.selectedProperty()));
    }

    @FXML
    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText();
        String conf = txtConfirmarPassword.getText();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", "Todos los campos son obligatorios.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo inválido", "Ingresa un correo electrónico válido.");
            return;
        }
        if (pass.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!pass.equals(conf)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error", "Las contraseñas no coinciden.");
            return;
        }
        if (!chkPrivacidad.isSelected()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso de Privacidad", "Debes aceptar la política de protección de datos personales.");
            return;
        }

        try {
            if (BibliotecaDAO.registrarUsuario(nombre, email, pass)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso",
                        "Cuenta creada correctamente. Puedes consultar la confirmación en Notificaciones después de iniciar sesión.");
                App.setRoot("login");
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al registrar",
                    e.getMessage().contains("unique") ? "Este correo ya está registrado." : e.getMessage());
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void volverLogin() throws IOException {
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

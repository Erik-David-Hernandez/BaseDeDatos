package basedatos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Sistema de Gestión de Biblioteca Virtual");
        scene = new Scene(new VBox(), 960, 680);
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        checkDatabase(stage);
    }

    private void checkDatabase(Stage stage) {
        VBox loading = new VBox(16, new ProgressIndicator(),
                new Label("Comprobando y configurando PostgreSQL…"));
        loading.setAlignment(Pos.CENTER);
        scene.setRoot(loading);
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
                DatabaseSetup.ensureReady();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            try { setRoot("login"); }
            catch (IOException error) { showSetupError(stage, error); }
        });
        task.setOnFailed(event -> showSetupError(stage, task.getException()));
        Thread worker = new Thread(task, "postgresql-startup");
        worker.setDaemon(true);
        stage.setOnCloseRequest(event -> task.cancel(true));
        worker.start();
    }

    private void showSetupError(Stage stage, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Configuración de PostgreSQL");
        alert.setHeaderText("No se pudo conectar a la base de datos");
        alert.setContentText(error.getMessage()
                + "\nComprueba BIBLIOTECA_DB_USER y BIBLIOTECA_DB_PASSWORD.");
        ButtonType retry = new ButtonType("Reintentar");
        alert.getButtonTypes().setAll(retry, ButtonType.CLOSE);
        if (alert.showAndWait().orElse(ButtonType.CLOSE) == retry) checkDatabase(stage);
        else stage.close();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}

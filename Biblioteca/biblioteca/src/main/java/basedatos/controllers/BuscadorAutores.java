package basedatos.controllers;

import basedatos.models.Autor;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Author input with selectable search results; free text is never saved as an author ID. */
public final class BuscadorAutores extends VBox {
    private final TextField campo = new TextField();
    private final ContextMenu resultados = new ContextMenu();
    private List<Autor> autores = new ArrayList<>();
    private Autor seleccionado;
    private boolean actualizando;
    private int indiceResultado = -1;

    public BuscadorAutores() {
        campo.setPromptText("Buscar y seleccionar autor");
        getChildren().add(campo);
        campo.textProperty().addListener((obs, anterior, nuevo) -> {
            if (!actualizando) {
                seleccionado = null;
                mostrarResultados();
            }
        });
        campo.focusedProperty().addListener((obs, anterior, enfocado) -> {
            if (enfocado) mostrarResultados();
        });
        campo.setOnMouseClicked(event -> mostrarResultados());
        campo.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarTecla);
        // Popup windows redirect owner key events, so handle them here as well.
        resultados.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarTecla);
    }

    private void manejarTecla(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && (seleccionado == null || resultados.isShowing())) {
            List<Autor> coincidencias = coincidencias();
            if (!coincidencias.isEmpty()) {
                setAutor(coincidencias.get(Math.max(0, Math.min(indiceResultado, coincidencias.size() - 1))));
            }
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP) {
            if (!resultados.isShowing()) mostrarResultados();
            int cantidad = Math.min(20, coincidencias().size());
            if (cantidad > 0) {
                indiceResultado = event.getCode() == KeyCode.DOWN
                        ? Math.min(indiceResultado + 1, cantidad - 1) : Math.max(indiceResultado - 1, 0);
                for (int i = 0; i < resultados.getItems().size(); i++) {
                    resultados.getItems().get(i).setStyle(i == indiceResultado
                            ? "-fx-background-color: -fx-selection-bar; -fx-text-fill: -fx-selection-bar-text;" : "");
                }
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE && resultados.isShowing()) {
            resultados.hide();
            event.consume();
        }
    }

    public void setAutores(List<Autor> nuevos) {
        autores = new ArrayList<>(nuevos);
        if (seleccionado != null) setAutor(seleccionado);
        else mostrarResultados();
    }

    public List<Autor> getAutores() { return new ArrayList<>(autores); }

    public Autor getAutor() { return seleccionado; }

    public void setAutor(Autor autor) {
        seleccionado = autor == null ? null : autores.stream()
                .filter(a -> a.getIdAutor() == autor.getIdAutor()).findFirst().orElse(null);
        actualizando = true;
        try { campo.setText(seleccionado == null ? "" : seleccionado.getNombreCompleto()); }
        finally { actualizando = false; }
        resultados.hide();
    }

    private List<Autor> coincidencias() {
        String busqueda = normalizar(campo.getText());
        return autores.stream().filter(a -> normalizar(a.getNombreCompleto()).contains(busqueda))
                .collect(Collectors.toList());
    }

    private void mostrarResultados() {
        if (!campo.isFocused() || campo.getScene() == null || campo.getScene().getWindow() == null
                || !campo.getScene().getWindow().isShowing()) return;
        List<Autor> coincidencias = coincidencias();
        resultados.getItems().clear();
        indiceResultado = -1;
        coincidencias.stream().limit(20).forEach(autor -> {
            MenuItem item = new MenuItem(autor.getNombreCompleto());
            item.setOnAction(event -> setAutor(autor));
            resultados.getItems().add(item);
        });
        if (coincidencias.isEmpty() || coincidencias.size() > 20) {
            MenuItem aviso = new MenuItem(coincidencias.isEmpty() ? "Sin autores coincidentes" : "Escribe más para afinar la búsqueda");
            aviso.setDisable(true);
            resultados.getItems().add(aviso);
        }
        resultados.setMinWidth(campo.getWidth());
        if (!resultados.isShowing()) resultados.show(campo, Side.BOTTOM, 0, 0);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

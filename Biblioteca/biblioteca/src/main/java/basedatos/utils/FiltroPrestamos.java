package basedatos.utils;

import basedatos.models.Prestamo;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

public final class FiltroPrestamos {
    private FiltroPrestamos() {}

    public static boolean coincide(Prestamo prestamo, String busqueda, LocalDate desde, LocalDate hasta) {
        return coincide(prestamo, busqueda, desde, hasta, null);
    }

    public static boolean coincide(Prestamo prestamo, String busqueda, LocalDate desde, LocalDate hasta,
            Set<String> estados) {
        if (estados != null && !estados.contains(prestamo.getEstado())) return false;
        if (desde != null && hasta != null && desde.isAfter(hasta)) return false;
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        boolean coincideTexto = contiene(prestamo.getNombreUsuario(), texto)
                || contiene(prestamo.getEmailUsuario(), texto) || contiene(prestamo.getTituloLibro(), texto);
        if (!coincideTexto) return false;
        if (desde == null && hasta == null) return true;
        if (prestamo.getFechaInicio() == null) return false;
        LocalDate inicio = prestamo.getFechaInicio().toLocalDate();
        return (desde == null || !inicio.isBefore(desde)) && (hasta == null || !inicio.isAfter(hasta));
    }

    private static boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }
}

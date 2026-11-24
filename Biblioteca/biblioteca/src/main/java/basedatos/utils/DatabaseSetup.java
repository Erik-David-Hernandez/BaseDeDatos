package basedatos.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/** Startup only: DAO connection failures must never launch an installer. */
public final class DatabaseSetup {
    private DatabaseSetup() {}

    public static void ensureReady() throws IOException, SQLException, InterruptedException {
        try (Connection ignored = ConexionDB.getConnection()) {
            return;
        } catch (SQLException error) {
            String state = error.getSQLState();
            // PostgreSQL also reports a missing role as 28P01. The script checks
            // installed binaries and existing roles; it never resets their passwords.
            if (!"3D000".equals(state) && !"28000".equals(state)
                    && !"28P01".equals(state)
                    && (state == null || !state.startsWith("08"))) {
                throw error;
            }
        }

        Path script = findScript();
        int result = SetupTerminal.run(script, findBash());
        if (result != 0) {
            throw new IOException("La configuración de PostgreSQL falló (código " + result
                    + "). Revisa la ventana de configuración. En Windows comprueba "
                    + "POSTGRES_ADMIN_PASSWORD si falló la autenticación del administrador.");
        }
        try (Connection ignored = ConexionDB.getConnection()) {
            // Verify the exact credentials the rest of the app will use.
        }
    }

    private static Path findScript() throws IOException {
        String configured = System.getenv("BIBLIOTECA_SETUP_SCRIPT");
        if (configured != null && !configured.isBlank()) {
            Path script = Paths.get(configured).toAbsolutePath();
            if (Files.isRegularFile(script)) return script;
            throw new IOException("No se encontró el script: " + script);
        }
        // jpackage expands $APPDIR to the installed application payload directory.
        String appDirectory = System.getProperty("biblioteca.app.dir");
        if (appDirectory != null && !appDirectory.isBlank()) {
            Path script = Paths.get(appDirectory).resolve("database/setup-postgresql.sh");
            if (Files.isRegularFile(script)) return script;
            throw new IOException("La instalación no incluye el script: " + script);
        }
        // An executable JAR has its setup files next to it, regardless of cwd.
        try {
            Path location = Paths.get(DatabaseSetup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location)) {
                Path script = location.getParent().resolve("database/setup-postgresql.sh");
                if (Files.isRegularFile(script)) return script;
            }
        } catch (URISyntaxException error) {
            throw new IOException("No se pudo localizar el directorio de la aplicación.", error);
        }
        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path script = dir.resolve("database/setup-postgresql.sh");
            if (Files.isRegularFile(script)) return script;
        }
        throw new IOException("No se encontró database/setup-postgresql.sh. Ejecuta desde el proyecto "
                + "o configura BIBLIOTECA_SETUP_SCRIPT con su ruta absoluta.");
    }

    private static String findBash() throws IOException {
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) return "bash";
        // Avoid Windows' WSL bash: native setup must run in Git Bash.
        for (String variable : new String[]{"ProgramFiles", "ProgramFiles(x86)", "LOCALAPPDATA"}) {
            String base = System.getenv(variable);
            if (base == null) continue;
            for (String relative : new String[]{"Git/bin/bash.exe", "Programs/Git/bin/bash.exe"}) {
                Path bash = Paths.get(base, relative);
                if (Files.isRegularFile(bash)) return bash.toString();
            }
        }
        throw new IOException("Instala Git para Windows para ejecutar la configuración con Git Bash.");
    }
}

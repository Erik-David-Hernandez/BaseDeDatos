package basedatos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/** Startup only: DAO connection failures must never launch an installer. */
final class DatabaseSetup {
    private DatabaseSetup() {}

    static void ensureReady() throws IOException, SQLException, InterruptedException {
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
        ProcessBuilder builder = new ProcessBuilder(findBash(), script.toString(), "--app");
        builder.directory(script.getParent().toFile());
        builder.inheritIO();
        builder.environment().put("BIBLIOTECA_DB_USER", ConexionDB.USER);
        builder.environment().put("BIBLIOTECA_DB_PASSWORD", ConexionDB.PASSWORD);
        // Windows administrator authentication is separate from the app account.
        builder.environment().put("PGPASSWORD",
                System.getenv().getOrDefault("POSTGRES_ADMIN_PASSWORD", "admin"));
        Process setup = builder.start();
        int result;
        try {
            result = setup.waitFor();
        } catch (InterruptedException error) {
            setup.destroy();
            Thread.currentThread().interrupt();
            throw error;
        }
        if (result != 0) {
            throw new IOException("La configuración de PostgreSQL falló (código " + result
                    + "). Revisa la terminal. Para instalar, abre la aplicación desde una terminal; "
                    + "en Windows completa el instalador y configura POSTGRES_ADMIN_PASSWORD.");
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
        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path script = dir.resolve("setup-postgresql.sh");
            if (Files.isRegularFile(script)) return script;
        }
        throw new IOException("No se encontró setup-postgresql.sh. Ejecuta desde el proyecto "
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

package basedatos.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Run privileged/interactive setup in a visible terminal, reporting its actual exit code. */
final class SetupTerminal {
    private SetupTerminal() {}

    static int run(Path script, String bash) throws IOException, InterruptedException {
        Path directory = Files.createTempDirectory("biblioteca-setup-");
        Path wrapper = directory.resolve("setup.sh");
        Path result = directory.resolve("result");
        Process terminal = null;
        try {
            if (Files.getFileStore(directory).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
            }
            Files.writeString(wrapper, wrapper(script, bash, result), StandardCharsets.UTF_8);
            if (Files.getFileStore(wrapper).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(wrapper, PosixFilePermissions.fromString("rw-------"));
            }
            terminal = new ProcessBuilder(command(bash, wrapper))
                    .directory(script.getParent().toFile()).inheritIO().start();
            long deadline = System.nanoTime() + Duration.ofHours(1).toNanos();
            while (System.nanoTime() < deadline) {
                if (Files.isRegularFile(result)) {
                    String code = Files.readString(result).trim();
                    // The writer may have opened the result before writing its contents.
                    if (!code.isEmpty()) return Integer.parseInt(code);
                }
                if (!terminal.isAlive()) {
                    // Check once more after exit: the shell may have just written the result.
                    if (Files.isRegularFile(result) && !Files.readString(result).trim().isEmpty()) {
                        return Integer.parseInt(Files.readString(result).trim());
                    }
                    throw new IOException("La terminal se cerró antes de completar la configuración.");
                }
                Thread.sleep(200);
            }
            throw new IOException("La configuración excedió una hora. Revisa la terminal antes de reintentar.");
        } finally {
            // Do not terminate a package manager if the app closes during installation.
            if (terminal != null && terminal.isAlive() && !Files.isRegularFile(result)) {
                terminal.onExit().thenRun(() -> cleanup(directory));
            } else {
                cleanup(directory);
            }
        }
    }

    private static void cleanup(Path directory) {
        for (String name : Arrays.asList("result.tmp", "result", "setup.sh")) {
            try { Files.deleteIfExists(directory.resolve(name)); }
            catch (IOException ignored) { /* A running Windows terminal may still hold the file. */ }
        }
        try { Files.deleteIfExists(directory); }
        catch (IOException ignored) { /* Leave an in-use private directory for the terminal. */ }
    }

    private static String wrapper(Path script, String bash, Path result) {
        // A private file also works with terminals launched through a desktop server,
        // which may discard the Java process's environment. Secrets stay out of argv.
        return "#!/usr/bin/env bash\n"
                + "export BIBLIOTECA_DB_USER=" + quote(ConexionDB.USER) + "\n"
                + "export BIBLIOTECA_DB_PASSWORD=" + quote(ConexionDB.PASSWORD) + "\n"
                + "export PGPASSWORD=" + quote(System.getenv().getOrDefault("POSTGRES_ADMIN_PASSWORD", "admin")) + "\n"
                + "result=" + quote(result.toString()) + "\n"
                + "rm -f -- \"$0\"\n"
                + "completed=false\n"
                + "report() { printf '%s\\n' \"$1\" > \"$result.tmp\"; mv -f -- \"$result.tmp\" \"$result\"; }\n"
                + "interrupted() { if [[ $completed == false ]]; then report 130; fi; exit 130; }\n"
                + "trap interrupted HUP INT TERM\n"
                + "printf 'Configuración de PostgreSQL\\n\\n'\n"
                + quote(bash) + " " + quote(script.toString()) + " --app\n"
                + "status=$?\ncompleted=true\nreport \"$status\"\n"
                + "printf '\\nConfiguración finalizada (código %s). Pulsa Enter para cerrar.\\n' \"$status\"\n"
                + "read -r respuesta || true\nexit \"$status\"\n";
    }

    static List<String> command(String bash, Path wrapper) throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            Path mintty = Paths.get(bash).getParent().getParent().resolve("usr/bin/mintty.exe");
            if (!Files.isRegularFile(mintty)) throw new IOException("No se encontró la terminal de Git Bash: " + mintty);
            return Arrays.asList(mintty.toString(), "--nodaemon", "--title", "Configuración de PostgreSQL", bash, wrapper.toString());
        }
        if (os.contains("mac")) {
            String shell = quote(bash) + " " + quote(wrapper.toString());
            String appleScript = "tell application \"Terminal\"\nactivate\nset setupTab to do script "
                    + appleQuote(shell) + "\ndelay 1\nrepeat while busy of setupTab\ndelay 1\nend repeat\nend tell";
            return Arrays.asList("osascript", "-e", appleScript);
        }
        for (String[] terminal : new String[][]{
                {"konsole", "--nofork", "-e"},
                {"gnome-terminal", "--wait", "--"},
                {"xfce4-terminal", "--disable-server", "-x"},
                {"mate-terminal", "--disable-factory", "-x"},
                {"xterm", "-e"}, {"kitty", "--"}, {"alacritty", "-e"},
                {"x-terminal-emulator", "-e"}}) {
            if (onPath(terminal[0])) {
                List<String> args = new ArrayList<>(Arrays.asList(terminal));
                args.add(bash);
                args.add(wrapper.toString());
                return args;
            }
        }
        throw new IOException("No se encontró una terminal compatible. Instala Konsole, GNOME Terminal, "
                + "XFCE Terminal, MATE Terminal, xterm, Kitty o Alacritty.");
    }

    private static boolean onPath(String executable) {
        for (String entry : System.getenv().getOrDefault("PATH", "").split(java.io.File.pathSeparator)) {
            if (!entry.isEmpty() && Files.isExecutable(Paths.get(entry, executable))) return true;
        }
        return false;
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static String appleQuote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

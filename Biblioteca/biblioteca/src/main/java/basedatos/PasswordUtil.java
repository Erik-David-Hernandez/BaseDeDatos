package basedatos;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RNF1: Seguridad mediante Hashing (SHA-256) y Salting.
 */
public class PasswordUtil {

    public static String generarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combinado = salt + password;
            byte[] hashBytes = md.digest(combinado.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }

    public static boolean verificarPassword(String passwordIngresado, String hashGuardado, String saltGuardado) {
        String hashCalculado = hashPassword(passwordIngresado, saltGuardado);
        return hashCalculado.equalsIgnoreCase(hashGuardado);
    }
}

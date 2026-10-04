import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Passwords are never stored as plain text: we store  salt$SHA-256(salt+password)
public class PasswordUtil {

    public static String create(String password) throws Exception {
        byte[] s = new byte[16];
        new SecureRandom().nextBytes(s);
        String salt = Base64.getEncoder().encodeToString(s);
        return salt + "$" + hash(salt, password);
    }

    public static boolean verify(String password, String stored) throws Exception {
        if (stored == null || !stored.contains("$")) return false;
        String[] parts = stored.split("\\$", 2);
        byte[] a = hash(parts[0], password).getBytes(StandardCharsets.UTF_8);
        byte[] b = parts[1].getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private static String hash(String salt, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(h);
    }
}

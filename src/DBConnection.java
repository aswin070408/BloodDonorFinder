import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Reads settings from environment variables (for cloud hosting).
// If they are not set, it uses your local MySQL (for your laptop).
public class DBConnection {
    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/blood_donor_web");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "Aswin"); // local password

    static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

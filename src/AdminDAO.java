import java.sql.*;

public class AdminDAO {

    // Creates admin / admin123 the first time the app runs
    public void ensureDefaultAdmin() throws Exception {
        int count;
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM admins")) {
            rs.next();
            count = rs.getInt(1);
        }
        if (count == 0) {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO admins (username, password_hash) VALUES (?,?)")) {
                ps.setString(1, "admin");
                String pw = DBConnection.env("ADMIN_PASSWORD", "admin123");
                ps.setString(2, PasswordUtil.create(pw));
                ps.executeUpdate();
            }
            System.out.println("Default admin created (username: admin)");
        }
    }

    public boolean login(String username, String password) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM admins WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && PasswordUtil.verify(password, rs.getString(1));
            }
        }
    }
}

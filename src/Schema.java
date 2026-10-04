import java.sql.Connection;
import java.sql.Statement;

// Creates the tables automatically when the app starts, so you don't have to run schema.sql on a cloud database.
public class Schema {
    public static void ensureTables() throws Exception {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS admins ("
                + "admin_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(30) NOT NULL UNIQUE,"
                + "password_hash VARCHAR(120) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS donors ("
                + "donor_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(30) NOT NULL UNIQUE,"
                + "password_hash VARCHAR(120) NOT NULL,"
                + "name VARCHAR(50) NOT NULL,"
                + "age INT NOT NULL,"
                + "gender VARCHAR(10),"
                + "blood_group VARCHAR(3) NOT NULL,"
                + "city VARCHAR(40) NOT NULL,"
                + "phone VARCHAR(15) NOT NULL,"
                + "last_donation DATE NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }
}

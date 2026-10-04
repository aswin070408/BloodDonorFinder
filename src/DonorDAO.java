import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {

    public boolean usernameExists(String username) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM donors WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void register(Donor d, String password) throws Exception {
        String sql = "INSERT INTO donors (username, password_hash, name, age, gender, blood_group, city, phone, last_donation) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.username);
            ps.setString(2, PasswordUtil.create(password));
            ps.setString(3, d.name);
            ps.setInt(4, d.age);
            ps.setString(5, d.gender);
            ps.setString(6, d.bloodGroup);
            ps.setString(7, d.city);
            ps.setString(8, d.phone);
            ps.setDate(9, d.lastDonation);
            ps.executeUpdate();
        }
    }

    // Returns the donor if username/password are correct, otherwise null
    public Donor login(String username, String password) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM donors WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password_hash")))
                    return map(rs);
            }
        }
        return null;
    }

    public Donor findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM donors WHERE donor_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // Admin list, optional filters (blank = no filter)
    public List<Donor> list(String bloodGroup, String city) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM donors WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            sql.append(" AND blood_group=?");
            params.add(bloodGroup);
        }
        if (city != null && !city.isEmpty()) {
            sql.append(" AND LOWER(city) LIKE ?");
            params.add("%" + city.toLowerCase() + "%");
        }
        sql.append(" ORDER BY donor_id");
        List<Donor> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void recordDonationToday(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE donors SET last_donation=CURDATE() WHERE donor_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Donor map(ResultSet rs) throws SQLException {
        Donor d = new Donor();
        d.id = rs.getInt("donor_id");
        d.username = rs.getString("username");
        d.name = rs.getString("name");
        d.age = rs.getInt("age");
        d.gender = rs.getString("gender");
        d.bloodGroup = rs.getString("blood_group");
        d.city = rs.getString("city");
        d.phone = rs.getString("phone");
        d.lastDonation = rs.getDate("last_donation");
        return d;
    }
}

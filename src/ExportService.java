import java.util.List;

// Builds an Excel-readable CSV of donor details.
public class ExportService {

    public static String toCsv(List<Donor> donors) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM so Excel reads the file correctly
        sb.append("ID,Username,Name,Age,Gender,Blood Group,City,Phone,Last Donation,Eligible\r\n");
        for (Donor d : donors) {
            sb.append(d.id).append(',')
              .append(q(d.username)).append(',')
              .append(q(d.name)).append(',')
              .append(d.age).append(',')
              .append(q(d.gender)).append(',')
              .append(q(d.bloodGroup)).append(',')
              .append(q(d.city)).append(',')
              .append(q(d.phone)).append(',')
              .append(d.lastDonation == null ? "Never" : d.lastDonation.toString()).append(',')
              .append(d.isEligible() ? "Yes" : "No").append("\r\n");
        }
        return sb.toString();
    }

    private static String q(String s) {
        if (s == null) return "\"\"";
        if (s.startsWith("=") || s.startsWith("@")) s = "'" + s; // stop spreadsheet formula injection
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}

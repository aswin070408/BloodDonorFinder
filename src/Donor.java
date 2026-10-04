import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Donor {
    public int id;
    public String username, name, gender, bloodGroup, city, phone;
    public int age;
    public Date lastDonation; // null = never donated

    // -1 means never donated
    public long daysSinceDonation() {
        if (lastDonation == null) return -1;
        return ChronoUnit.DAYS.between(lastDonation.toLocalDate(), LocalDate.now());
    }

    // Eligible if never donated, or 90+ days since last donation
    public boolean isEligible() {
        long n = daysSinceDonation();
        return n < 0 || n >= 90;
    }
}

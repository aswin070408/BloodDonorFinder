import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// All HTML pages and CSS live here.
public class Views {

    static final String CSS = """
        :root{--red:#c0262d;--bg:#faf7f6;--line:#e8dedd;--muted:#6b6262}
        *{box-sizing:border-box}
        body{margin:0;font-family:Arial,sans-serif;background:var(--bg);color:#222}
        header{background:var(--red);color:#fff;display:flex;justify-content:space-between;align-items:center;padding:12px 20px;flex-wrap:wrap;gap:8px}
        .brand{font-weight:bold;font-size:1.2rem}
        nav a{color:#fff;margin-left:14px;text-decoration:none}
        .wrap{max-width:900px;margin:20px auto;padding:0 16px}
        .card{background:#fff;border:1px solid var(--line);border-radius:10px;padding:16px;margin-bottom:14px}
        label{display:block;font-size:.85rem;color:var(--muted);margin:10px 0 4px}
        input,select{width:100%;padding:10px;border:1px solid #ccc;border-radius:6px;font-size:1rem}
        .btn{display:inline-block;background:var(--red);color:#fff;border:0;padding:10px 16px;border-radius:6px;text-decoration:none;cursor:pointer;font-size:1rem;margin-top:12px}
        .err{background:#fde8e8;color:#9b1c1c;padding:10px;border-radius:6px;margin-bottom:10px}
        .good{background:#e3f4ea;color:#1f7a45;padding:10px;border-radius:6px;margin-bottom:10px}
        .filter{display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap}
        .filter div{flex:1;min-width:140px}
        .bar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px}
        .tablewrap{overflow-x:auto}
        table{width:100%;border-collapse:collapse;background:#fff}
        th,td{padding:8px 10px;border-bottom:1px solid var(--line);text-align:left;font-size:.9rem;white-space:nowrap}
        th{background:#f3e9e8}
        .tag{padding:2px 8px;border-radius:99px;font-size:.78rem}
        .yes{background:#e3f4ea;color:#1f7a45}
        .no{background:#fdf0d9;color:#9a5b00}
        .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:14px}
        .row{display:flex;gap:10px}
        .row>div{flex:1}
        .muted{color:var(--muted);font-size:.85rem}
        """;

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    static String page(String title, String body, WebApp.Session s) {
        String nav;
        if (s == null)
            nav = "<a href='/'>Home</a><a href='/admin/login'>Admin login</a><a href='/login'>Donor login</a><a href='/register'>Register</a>";
        else if ("admin".equals(s.role))
            nav = "<a href='/admin/dashboard'>Dashboard</a><a href='/logout'>Logout</a>";
        else
            nav = "<a href='/profile'>My profile</a><a href='/logout'>Logout</a>";
        return "<!DOCTYPE html><html><head><meta charset='utf-8'>"
             + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
             + "<title>" + esc(title) + "</title><style>" + CSS + "</style></head><body>"
             + "<header><div class='brand'>&#129656; Blood Donor Finder</div><nav>" + nav + "</nav></header>"
             + "<div class='wrap'>" + body + "</div></body></html>";
    }

    static String home(WebApp.Session s) {
        String body = "<h2>Welcome</h2><p class='muted'>Find and manage blood donors.</p><div class='grid'>"
            + "<div class='card'><h3>Admin</h3><p>View every registered donor and download the list as Excel.</p><a class='btn' href='/admin/login'>Admin login</a></div>"
            + "<div class='card'><h3>Donor login</h3><p>Sign in to see your details.</p><a class='btn' href='/login'>Donor login</a></div>"
            + "<div class='card'><h3>New donor?</h3><p>Create an account and register as a donor.</p><a class='btn' href='/register'>Create account</a></div>"
            + "</div>";
        return page("Home", body, s);
    }

    static String adminLogin(String error) {
        return page("Admin login", loginForm("Admin login", "/admin/login", error, null), null);
    }

    static String donorLogin(String error, String notice) {
        return page("Donor login", loginForm("Donor login", "/login", error, notice), null);
    }

    private static String loginForm(String title, String action, String error, String notice) {
        StringBuilder sb = new StringBuilder("<div class='card' style='max-width:420px;margin:auto'>");
        sb.append("<h2>").append(title).append("</h2>");
        if (error != null) sb.append("<div class='err'>").append(esc(error)).append("</div>");
        if (notice != null) sb.append("<div class='good'>").append(esc(notice)).append("</div>");
        sb.append("<form method='post' action='").append(action).append("'>")
          .append("<label>Username</label><input name='username' required>")
          .append("<label>Password</label><input name='password' type='password' required>")
          .append("<button class='btn' type='submit'>Sign in</button></form></div>");
        return sb.toString();
    }

    static String register(String error, Map<String, String> v) {
        StringBuilder sb = new StringBuilder("<div class='card' style='max-width:560px;margin:auto'><h2>Create donor account</h2>");
        if (error != null) sb.append("<div class='err'>").append(esc(error)).append("</div>");
        sb.append("<form method='post' action='/register'>");
        sb.append("<div class='row'><div><label>Username</label><input name='username' required value='").append(esc(v.get("username"))).append("'></div>");
        sb.append("<div><label>Password (min 6 characters)</label><input name='password' type='password' required></div></div>");
        sb.append("<label>Full name</label><input name='name' required value='").append(esc(v.get("name"))).append("'>");
        sb.append("<div class='row'><div><label>Age (18-65)</label><input name='age' type='number' required value='").append(esc(v.get("age"))).append("'></div>");
        sb.append("<div><label>Gender</label><select name='gender'>");
        for (String g : new String[]{"Male", "Female", "Other"})
            sb.append("<option").append(g.equals(v.get("gender")) ? " selected" : "").append(">").append(g).append("</option>");
        sb.append("</select></div></div>");
        sb.append("<div class='row'><div><label>Blood group</label><select name='bg'>");
        for (String g : WebApp.GROUPS)
            sb.append("<option").append(g.equals(v.get("bg")) ? " selected" : "").append(">").append(g).append("</option>");
        sb.append("</select></div><div><label>City</label><input name='city' required value='").append(esc(v.get("city"))).append("'></div></div>");
        sb.append("<label>Phone (10 digits)</label><input name='phone' required value='").append(esc(v.get("phone"))).append("'>");
        sb.append("<label>Last donation date (leave empty if never)</label><input name='last' type='date' value='").append(esc(v.get("last"))).append("'>");
        sb.append("<button class='btn' type='submit'>Create account</button></form></div>");
        return page("Register", sb.toString(), null);
    }

    static String dashboard(List<Donor> list, String bg, String city, WebApp.Session s) {
        StringBuilder sb = new StringBuilder("<h2>All registered donors</h2>");
        sb.append("<form class='card filter' method='get' action='/admin/dashboard'>");
        sb.append("<div><label>Blood group</label><select name='bg'><option value=''>All</option>");
        for (String g : WebApp.GROUPS)
            sb.append("<option").append(g.equals(bg) ? " selected" : "").append(">").append(g).append("</option>");
        sb.append("</select></div><div><label>City</label><input name='city' value='").append(esc(city)).append("'></div>");
        sb.append("<button class='btn' type='submit' style='margin-top:0'>Filter</button></form>");

        String exportUrl = "/admin/export?bg=" + enc(bg) + "&amp;city=" + enc(city);
        sb.append("<p class='bar'><span><b>").append(list.size()).append("</b> donor(s)</span>")
          .append("<a class='btn' style='margin-top:0' href='").append(exportUrl).append("'>Download Excel</a></p>");

        sb.append("<div class='tablewrap card' style='padding:0'><table><tr><th>ID</th><th>Name</th><th>Age</th><th>Gender</th>")
          .append("<th>Blood</th><th>City</th><th>Phone</th><th>Last donation</th><th>Status</th></tr>");
        if (list.isEmpty()) sb.append("<tr><td colspan='9'>No donors found.</td></tr>");
        for (Donor d : list) {
            sb.append("<tr><td>").append(d.id).append("</td><td>").append(esc(d.name)).append("</td><td>").append(d.age)
              .append("</td><td>").append(esc(d.gender)).append("</td><td><b>").append(esc(d.bloodGroup)).append("</b></td><td>")
              .append(esc(d.city)).append("</td><td>").append(esc(d.phone)).append("</td><td>")
              .append(d.lastDonation == null ? "Never" : d.lastDonation.toString()).append("</td><td>")
              .append(d.isEligible() ? "<span class='tag yes'>Eligible</span>" : "<span class='tag no'>Not yet</span>")
              .append("</td></tr>");
        }
        sb.append("</table></div>");
        return page("Admin dashboard", sb.toString(), s);
    }

    static String profile(Donor d, WebApp.Session s) {
        long n = d.daysSinceDonation();
        String last = d.lastDonation == null ? "Never donated" : d.lastDonation + " (" + n + " days ago)";
        StringBuilder sb = new StringBuilder("<div class='card' style='max-width:560px;margin:auto'>");
        sb.append("<h2>Hello, ").append(esc(d.name)).append("</h2>");
        sb.append(d.isEligible() ? "<span class='tag yes'>Eligible to donate</span>" : "<span class='tag no'>Not eligible yet (needs 90 days)</span>");
        sb.append("<table style='margin-top:12px'>")
          .append(row("Username", d.username)).append(row("Age", String.valueOf(d.age))).append(row("Gender", d.gender))
          .append(row("Blood group", d.bloodGroup)).append(row("City", d.city)).append(row("Phone", d.phone))
          .append(row("Last donation", last)).append("</table>");
        sb.append("<form method='post' action='/donate'><button class='btn' type='submit'>I donated today</button></form></div>");
        return page("My profile", sb.toString(), s);
    }

    private static String row(String k, String v) {
        return "<tr><th>" + k + "</th><td>" + esc(v) + "</td></tr>";
    }
}

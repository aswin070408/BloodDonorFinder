# Blood Donor Finder - Web Version (Java + MySQL)

Pure Java web server (no Tomcat). Needs JDK 17+, MySQL and the MySQL Connector/J jar.

## Setup
1. Run the database script:   mysql -u root -p < schema.sql
2. Put mysql-connector-j-x.x.x.jar in this folder (rename to mysql-connector-j.jar).
3. Set your MySQL password in src/DBConnection.java.

## Compile and run
Windows:
    javac -cp ".;mysql-connector-j.jar" -d out src/*.java
    java  -cp "out;mysql-connector-j.jar" WebApp
Linux/Mac:
    javac -cp ".:mysql-connector-j.jar" -d out src/*.java
    java  -cp "out:mysql-connector-j.jar" WebApp

Open http://localhost:8080

## Logins
- Admin:  username `admin`, password `admin123` (created automatically; change it in the admins table later)
- Donor:  click "Create account" to register, then sign in.

## Pages
/ home | /admin/login | /admin/dashboard (all donors + filters) | /admin/export (Excel .csv)
/register | /login | /profile | /donate | /logout

## Files
- schema.sql      : database (admins, donors)
- DBConnection    : JDBC connection
- PasswordUtil    : salted SHA-256 password hashing
- Donor           : model (+ 90-day eligibility)
- DonorDAO / AdminDAO : SQL for donors and admins
- ExportService   : donor list -> Excel-readable CSV
- Views           : HTML + CSS pages
- WebApp          : web server, routing, login sessions

## Deploy to the cloud (Render + hosted MySQL)
Vercel cannot run this Java app. Use Render (Docker) plus a hosted MySQL database.

1. Push this project to GitHub (set DBConnection fallback password back to your_password first).
2. Create a hosted MySQL database (e.g. Aiven, TiDB Cloud, Railway). Copy host, port, user, password, database name.
3. On Render: New > Web Service > connect your GitHub repo > Runtime: Docker.
4. Add these Environment Variables on Render:
     DB_URL        jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED
     DB_USER       your database user
     DB_PASSWORD   your database password
     ADMIN_PASSWORD  a strong admin password of your choice
5. Deploy. Tables and the admin account are created automatically on first start.
6. Open the Render link and log in as admin with your ADMIN_PASSWORD.

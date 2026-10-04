CREATE DATABASE IF NOT EXISTS blood_donor_web;
USE blood_donor_web;

CREATE TABLE IF NOT EXISTS admins (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(30) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS donors (
  donor_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(30) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  gender VARCHAR(10),
  blood_group VARCHAR(3) NOT NULL,
  city VARCHAR(40) NOT NULL,
  phone VARCHAR(15) NOT NULL,
  last_donation DATE NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- The default admin (admin / admin123) is created automatically when the app starts.

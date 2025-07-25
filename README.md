
# 🚗 Car Rental System – Android + PHP + MySQL

A full-stack car rental system built with:

- **Android (Jetpack Compose + Kotlin)**
- **PHP backend with REST-like APIs**
- **MySQL database (via XAMPP, port 3307)**
- ✅ User login, vehicle listing, rental and return flow
- 📡 Communication via HTTP (Retrofit)
- 🔐 Passwords hashed with `bcrypt` and verified with `password_verify`

---

## 📱 Android Frontend

Built using **Jetpack Compose** with **Retrofit** to handle network requests to the local PHP backend.

### Features:
- ✅ User login
- 🚙 View available vehicles
- 📝 Rent vehicles
- 🧾 Return vehicles
- 📤 Barcode support (optional for returns)

### Dependencies Used:
- Retrofit
- Gson
- Jetpack Compose UI
- Coroutine-based ViewModel support

---

## 🛠️ PHP Backend

Custom PHP endpoints acting as APIs.

### File Structure (`/api`)
```
api/
│
├── db.php                  # MySQL connection
├── login.php               # Login logic with bcrypt
├── get_vehicles.php        # Return available vehicles
├── rent_vehicle.php        # Create rental, mark vehicle rented
├── return_vehicle.php      # Update rental, return vehicle
├── pay_rental.php          # (Optional) Handle payments
├── info.php                # phpinfo() for testing
```

### Security:
- Passwords are securely hashed using `bcrypt`
- Only authenticated users can access functionality (to be expanded)

---

## 🧩 MySQL Schema

Key tables:

```sql
Users (with status, email, hashed password)
unhashedUsers (for testing only, not used in production)
Admins, unhashedAdmin
Vehicles (brand, model, rent_price, availability)
Rentals (user_id, vehicle_id, start/end dates, barcode, status)
Payments (linked to Rentals)
Documents (user uploads like license, ID)
Maintenance (vehicle maintenance logs)
```

Database: `carsdb`

Default port: `3307`

---

## 🖥️ Local Development Setup

### ✅ Backend (XAMPP):

1. Run XAMPP
2. Start Apache and MySQL
3. Make sure MySQL is on port **3307**
4. Place PHP files inside:
   ```
   C:\xampp\htdocs\car_rental_api\
   ```

### 🔗 Allow Android to connect:

- Access your PC's IP with:
  ```bash
  ipconfig
  ```
  Use the `IPv4 Address`, e.g., `192.168.1.100`

- Test `http://192.168.1.100/car_rental_api/login.php` on phone browser

- Allow `httpd.exe` through Windows Firewall if blocked

---

## 📱 Android Setup

- Use the PC's IP for your Retrofit `BASE_URL`:
  ```kotlin
  val BASE_URL = "http://192.168.1.100/car_rental_api/"
  ```

- Enable HTTP access on Android 9+:

`res/xml/network_security_config.xml`:
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.1.100</domain>
    </domain-config>
</network-security-config>
```

`AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## 🧪 Optional Tools

- 🔁 [ngrok](https://ngrok.com/) — tunnel local server for global access:
  ```bash
  ngrok http 80
  ```

---

## ⚠️ Notes

- This project is for learning and prototyping
- Avoid storing unhashed passwords in production
- Do not expose your XAMPP/MySQL instance publicly without securing it

---

## 📦 To Do / Roadmap

- [ ] Admin login panel
- [ ] Grouped API responses (e.g., `/rentals?user_id=X`)
- [ ] JWT or token-based auth
- [ ] Full payment integration
- [ ] Vehicle image uploads

---

## 👨‍💻 Author

Clarence Sabangan  
Android + PHP Developer  
📍 Philippines  

---

## 📄 License

This project is for educational use. Feel free to fork, remix, and build upon it!

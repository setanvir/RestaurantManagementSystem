# 🍴 Restaurant Management System

--

## 🚀 How to Run

1. Install **Java JDK** (make sure `javac` and `java` are available in your system PATH).
2. Save or copy `RestaurantManagementSystem.java` into a folder.
3. Open a terminal/command prompt in that folder.
4. Compile the program:

   ```bash
   javac RestaurantManagementSystem.java
   ```
5. Run the program:

   ```bash
   java RestaurantManagementSystem
   ```

---

## 📂 Features & Structure

This project is implemented in a **single Java file** but contains a full structured system:

* **Entities**

  * `Customer`, `MenuItem`, `Order`, `OrderItem` (all `Serializable`)

* **Managers (CRUD operations)**

  * `CustomerManager`
  * `MenuItemManager`
  * `OrderManager`

* **File Handling**

  * `DataStore` saves/loads snapshots to `rms_data.ser`
  * Data automatically saves on every add/update/delete
  * Data loads at startup

* **GUI (Swing-based)**

  * `MainFrame` with three panels:

    * `CustomerPanel`
    * `MenuItemPanel`
    * `OrderPanel`

---

## 💾 Persistence

* All data is **saved in `rms_data.ser`** whenever modifications occur.
* On program startup, the data is **reloaded automatically**, ensuring continuity between sessions.

---

## 🎯 Purpose

This project demonstrates:

* Java OOP principles
* File serialization & persistence
* Basic GUI development with Swing
* Single-file structured design for simplicity

---



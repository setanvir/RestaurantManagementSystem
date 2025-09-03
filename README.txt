
RestaurantManagementSystem.java - Single-file structured project

How to use:
1. Make sure Java JDK is installed (javac and java on PATH).
2. Save or copy RestaurantManagementSystem.java to a folder.
3. Open terminal/command prompt in that folder.
4. Compile:
   javac RestaurantManagementSystem.java
5. Run:
   java RestaurantManagementSystem

What it includes:
- Entities: Customer, MenuItem, Order, OrderItem (all Serializable)
- Managers: CustomerManager, MenuItemManager, OrderManager (handle CRUD)
- File Handler: DataStore (saves/loads a Snapshot to rms_data.ser)
- GUI: MainFrame with three panels (CustomerPanel, MenuItemPanel, OrderPanel)
- On every add/update/delete the system saves data to 'rms_data.ser'. Data is loaded on start.

If you want a ZIP with build scripts or a precompiled out/ folder, tell me.

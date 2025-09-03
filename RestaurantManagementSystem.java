import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/*
  RestaurantManagementSystem.java
  — Single-file Java project that follows the required structure while keeping everything in one file.
  Save as RestaurantManagementSystem.java, compile with `javac RestaurantManagementSystem.java` and run with `java RestaurantManagementSystem`.
  Data is persisted to "rms_data.ser" using Java serialization.
*/

// ===================== Entities =====================
class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String phone;

    public Customer() {}
    public Customer(int id, String name, String phone) { this.id = id; this.name = name; this.phone = phone; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override public String toString() { return name + (phone!=null && !phone.isEmpty() ? " ("+phone+")" : ""); }
}

class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double price;

    public MenuItem() {}
    public MenuItem(int id, String name, double price) { this.id = id; this.name = name; this.price = price; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override public String toString() { return name + " - " + price; }
}

class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int menuItemId;
    private String menuItemName;
    private double unitPrice;
    private int quantity;

    public OrderItem() {}
    public OrderItem(int menuItemId, String menuItemName, double unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }
    public double getLineTotal() { return unitPrice * quantity; }
}

class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int customerId;
    private String customerName;
    private LocalDateTime dateTime;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(int id, int customerId, String customerName) {
        this.id = id; this.customerId = customerId; this.customerName = customerName; this.dateTime = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LocalDateTime getDateTime() { return dateTime; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotal() { return items.stream().mapToDouble(OrderItem::getLineTotal).sum(); }
}

// ===================== Managers / Services =====================
class CustomerManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Customer> customers = new ArrayList<>();
    private int nextId = 1;

    public List<Customer> getAll() { return new ArrayList<>(customers); }
    public void setAll(List<Customer> list) { customers = new ArrayList<>(list); nextId = customers.stream().mapToInt(Customer::getId).max().orElse(0) + 1; }
    public Customer add(String name, String phone) { Customer c = new Customer(nextId++, name, phone); customers.add(c); return c; }
    public boolean update(int id, String name, String phone) {
        for (Customer c : customers) { if (c.getId() == id) { c.setName(name); c.setPhone(phone); return true; } } return false;
    }
    public boolean delete(int id) { return customers.removeIf(c -> c.getId() == id); }
    public Optional<Customer> findById(int id) { return customers.stream().filter(c->c.getId()==id).findFirst(); }
}

class MenuItemManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<MenuItem> items = new ArrayList<>();
    private int nextId = 1;

    public List<MenuItem> getAll() { return new ArrayList<>(items); }
    public void setAll(List<MenuItem> list) { items = new ArrayList<>(list); nextId = items.stream().mapToInt(MenuItem::getId).max().orElse(0) + 1; }
    public MenuItem add(String name, double price) { MenuItem m = new MenuItem(nextId++, name, price); items.add(m); return m; }
    public boolean update(int id, String name, double price) {
        for (MenuItem m : items) { if (m.getId() == id) { m.setName(name); m.setPrice(price); return true; } } return false;
    }
    public boolean delete(int id) { return items.removeIf(m -> m.getId() == id); }
    public Optional<MenuItem> findById(int id) { return items.stream().filter(m->m.getId()==id).findFirst(); }
}

class OrderManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Order> orders = new ArrayList<>();
    private int nextId = 1;

    public List<Order> getAll() { return new ArrayList<>(orders); }
    public void setAll(List<Order> list) { orders = new ArrayList<>(list); nextId = orders.stream().mapToInt(Order::getId).max().orElse(0) + 1; }
    public Order add(int customerId, String customerName, List<OrderItem> items) { Order o = new Order(nextId++, customerId, customerName); o.setItems(new ArrayList<>(items)); orders.add(o); return o; }
    public boolean update(int id, int customerId, String customerName, List<OrderItem> items) {
        for (Order o: orders) { if (o.getId()==id) { o.setCustomerId(customerId); o.setCustomerName(customerName); o.setItems(new ArrayList<>(items)); return true; } } return false;
    }
    public boolean delete(int id) { return orders.removeIf(o->o.getId()==id); }
    public Optional<Order> findById(int id) { return orders.stream().filter(o->o.getId()==id).findFirst(); }
}

// ===================== DataStore (File Handler) =====================
class DataStore {
    private static final String FILE_NAME = "rms_data.ser";

    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<Customer> customers = new ArrayList<>();
        public List<MenuItem> menuItems = new ArrayList<>();
        public List<Order> orders = new ArrayList<>();
    }

    public static Snapshot load() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return new Snapshot();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof Snapshot) return (Snapshot) obj;
            return new Snapshot();
        } catch (Exception e) {
            e.printStackTrace();
            return new Snapshot();
        }
    }

    public static void save(CustomerManager cm, MenuItemManager mm, OrderManager om) {
        Snapshot s = new Snapshot();
        s.customers = cm.getAll();
        s.menuItems = mm.getAll();
        s.orders = om.getAll();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// ===================== GUI Panels & MainFrame =====================
class CustomerPanel extends JPanel {
    private final CustomerManager cm;
    private final Runnable saver;
    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField, phoneField;

    public CustomerPanel(CustomerManager cm, Runnable saver) {
        super(new BorderLayout());
        this.cm = cm; this.saver = saver;
        init();
        refreshTable();
    }

    private void init() {
        model = new DefaultTableModel(new Object[]{"ID","Name","Phone"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> populateSelection());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        idField=new JTextField(); idField.setEditable(false);
        nameField=new JTextField(); phoneField=new JTextField();
        form.add(new JLabel("ID:")); form.add(idField);
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Phone:")); form.add(phoneField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add"), updBtn = new JButton("Update"), delBtn = new JButton("Delete");
        addBtn.addActionListener(e->onAdd()); updBtn.addActionListener(e->onUpdate()); delBtn.addActionListener(e->onDelete());
        buttons.add(addBtn); buttons.add(updBtn); buttons.add(delBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for(Customer c: cm.getAll()) model.addRow(new Object[]{c.getId(), c.getName(), c.getPhone()});
    }

    private void populateSelection() {
        int r = table.getSelectedRow();
        if (r>=0) {
            idField.setText(model.getValueAt(r,0).toString());
            nameField.setText(model.getValueAt(r,1).toString());
            phoneField.setText(model.getValueAt(r,2).toString());
        }
    }

    private void onAdd() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Name required"); return; }
        cm.add(name, phone);
        saver.run(); refreshTable(); clearForm();
    }

    private void onUpdate() {
        if (idField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select a customer"); return; }
        int id = Integer.parseInt(idField.getText());
        String name = nameField.getText().trim(); String phone = phoneField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Name required"); return; }
        if (cm.update(id, name, phone)) { saver.run(); refreshTable(); }
    }

    private void onDelete() {
        if (idField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select a customer"); return; }
        int id = Integer.parseInt(idField.getText());
        int ok = JOptionPane.showConfirmDialog(this,"Delete selected customer?","Confirm",JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) { if (cm.delete(id)) { saver.run(); refreshTable(); clearForm(); } }
    }

    private void clearForm() { idField.setText(""); nameField.setText(""); phoneField.setText(""); }
}

class MenuItemPanel extends JPanel {
    private final MenuItemManager mm;
    private final Runnable saver;
    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField, priceField;

    public MenuItemPanel(MenuItemManager mm, Runnable saver) {
        super(new BorderLayout());
        this.mm = mm; this.saver = saver;
        init(); refreshTable();
    }

    private void init() {
        model = new DefaultTableModel(new Object[]{"ID","Name","Price"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e->populateSelection());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        idField=new JTextField(); idField.setEditable(false);
        nameField=new JTextField(); priceField=new JTextField();
        form.add(new JLabel("ID:")); form.add(idField);
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Price:")); form.add(priceField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn=new JButton("Add"), updBtn=new JButton("Update"), delBtn=new JButton("Delete");
        addBtn.addActionListener(e->onAdd()); updBtn.addActionListener(e->onUpdate()); delBtn.addActionListener(e->onDelete());
        buttons.add(addBtn); buttons.add(updBtn); buttons.add(delBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (MenuItem m : mm.getAll()) model.addRow(new Object[]{m.getId(), m.getName(), m.getPrice()});
    }

    private void populateSelection() {
        int r = table.getSelectedRow();
        if (r>=0) {
            idField.setText(model.getValueAt(r,0).toString());
            nameField.setText(model.getValueAt(r,1).toString());
            priceField.setText(model.getValueAt(r,2).toString());
        }
    }

    private void onAdd() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Name required"); return; }
        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Price must be a number"); return; }
        mm.add(name, price); saver.run(); refreshTable(); clearForm();
    }

    private void onUpdate() {
        if (idField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select an item"); return; }
        int id = Integer.parseInt(idField.getText());
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Name required"); return; }
        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Price must be a number"); return; }
        if (mm.update(id, name, price)) { saver.run(); refreshTable(); }
    }

    private void onDelete() {
        if (idField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select an item"); return; }
        int id = Integer.parseInt(idField.getText());
        int ok = JOptionPane.showConfirmDialog(this,"Delete selected menu item?","Confirm",JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) { if (mm.delete(id)) { saver.run(); refreshTable(); clearForm(); } }
    }

    private void clearForm() { idField.setText(""); nameField.setText(""); priceField.setText(""); }
}

class OrderPanel extends JPanel {
    private final CustomerManager cm;
    private final MenuItemManager mm;
    private final OrderManager om;
    private final Runnable saver;

    private JTable orderTable;
    private DefaultTableModel orderModel;

    private JTable itemsTable;
    private DefaultTableModel itemsModel;

    private JComboBox<Customer> customerBox;
    private JComboBox<MenuItem> menuItemBox;
    private JSpinner qtySpinner;
    private JTextField orderIdField, totalField;

    private List<OrderItem> currentItems = new ArrayList<>();

    public OrderPanel(CustomerManager cm, MenuItemManager mm, OrderManager om, Runnable saver) {
        super(new BorderLayout());
        this.cm = cm; this.mm = mm; this.om = om; this.saver = saver;
        init();
        refreshOrderTable();
        refreshCombos();
    }

    private void init() {
        orderModel = new DefaultTableModel(new Object[]{"ID","Customer","Items","Total"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        orderTable = new JTable(orderModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.getSelectionModel().addListSelectionListener(e->populateOrderFromSelection());
        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(buildFormPanel(), BorderLayout.CENTER);
        south.add(buildButtons(), BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));

        JPanel left = new JPanel(new GridLayout(0,2,6,6));
        orderIdField = new JTextField(); orderIdField.setEditable(false);
        customerBox = new JComboBox<>();
        left.add(new JLabel("Order ID:")); left.add(orderIdField);
        left.add(new JLabel("Customer:")); left.add(customerBox);

        JPanel right = new JPanel(new BorderLayout(8,8));
        JPanel addItem = new JPanel(new GridLayout(0,2,6,6));
        menuItemBox = new JComboBox<>();
        qtySpinner = new JSpinner(new SpinnerNumberModel(1,1,999,1));
        JButton addItemBtn = new JButton("Add Item");
        addItem.add(new JLabel("Menu Item:")); addItem.add(menuItemBox);
        addItem.add(new JLabel("Qty:")); addItem.add(qtySpinner);
        addItem.add(new JLabel()); addItem.add(addItemBtn);

        itemsModel = new DefaultTableModel(new Object[]{"Item","Unit Price","Qty","Line Total"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        itemsTable = new JTable(itemsModel);
        JButton removeItemBtn = new JButton("Remove Selected Item");

        addItemBtn.addActionListener(e->onAddItem());
        removeItemBtn.addActionListener(e->onRemoveItem());

        right.add(addItem, BorderLayout.NORTH);
        right.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        right.add(removeItemBtn, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new GridLayout(1,2,6,6));
        totalField = new JTextField(); totalField.setEditable(false);
        bottom.add(new JLabel("Total:")); bottom.add(totalField);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Save Order");
        JButton updBtn = new JButton("Update Order");
        JButton delBtn = new JButton("Delete Order");
        buttons.add(addBtn); buttons.add(updBtn); buttons.add(delBtn);

        addBtn.addActionListener(e->onSaveOrder());
        updBtn.addActionListener(e->onUpdateOrder());
        delBtn.addActionListener(e->onDeleteOrder());
        return buttons;
    }

    private void refreshOrderTable() {
        orderModel.setRowCount(0);
        for (Order o : om.getAll()) orderModel.addRow(new Object[]{o.getId(), o.getCustomerName(), o.getItems().size(), o.getTotal()});
    }

    private void refreshCombos() {
        customerBox.removeAllItems();
        for (Customer c : cm.getAll()) customerBox.addItem(c);
        menuItemBox.removeAllItems();
        for (MenuItem m : mm.getAll()) menuItemBox.addItem(m);
    }

    private void onAddItem() {
        MenuItem m = (MenuItem) menuItemBox.getSelectedItem();
        if (m==null) { JOptionPane.showMessageDialog(this,"Add menu items first."); return; }
        int qty = (Integer) qtySpinner.getValue();
        OrderItem oi = new OrderItem(m.getId(), m.getName(), m.getPrice(), qty);
        currentItems.add(oi);
        renderItems();
    }

    private void onRemoveItem() {
        int r = itemsTable.getSelectedRow();
        if (r>=0 && r<currentItems.size()) { currentItems.remove(r); renderItems(); }
    }

    private void renderItems() {
        itemsModel.setRowCount(0);
        double total = 0.0;
        for (OrderItem oi : currentItems) {
            itemsModel.addRow(new Object[]{oi.getMenuItemName(), oi.getUnitPrice(), oi.getQuantity(), oi.getLineTotal()});
            total += oi.getLineTotal();
        }
        totalField.setText(String.valueOf(total));
    }

    private void onSaveOrder() {
        Customer cust = (Customer) customerBox.getSelectedItem();
        if (cust==null) { JOptionPane.showMessageDialog(this,"Please add/select a customer."); return; }
        if (currentItems.isEmpty()) { JOptionPane.showMessageDialog(this,"Add at least one item."); return; }
        Order o = om.add(cust.getId(), cust.getName(), currentItems);
        orderIdField.setText(String.valueOf(o.getId()));
        refreshOrderTable(); saver.run(); JOptionPane.showMessageDialog(this,"Order saved.");
    }

    private void onUpdateOrder() {
        if (orderIdField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select an order from the table."); return; }
        int id = Integer.parseInt(orderIdField.getText());
        Customer cust = (Customer) customerBox.getSelectedItem();
        if (cust==null) { JOptionPane.showMessageDialog(this,"Please add/select a customer."); return; }
        if (currentItems.isEmpty()) { JOptionPane.showMessageDialog(this,"Add at least one item."); return; }
        if (om.update(id, cust.getId(), cust.getName(), currentItems)) {
            refreshOrderTable(); saver.run(); JOptionPane.showMessageDialog(this,"Order updated.");
        }
    }

    private void onDeleteOrder() {
        if (orderIdField.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Select an order from the table."); return; }
        int id = Integer.parseInt(orderIdField.getText());
        int ok = JOptionPane.showConfirmDialog(this,"Delete selected order?","Confirm",JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) { if (om.delete(id)) { refreshOrderTable(); saver.run(); clearForm(); } }
    }

    private void clearForm() {
        orderIdField.setText(""); currentItems.clear(); renderItems(); if (customerBox.getItemCount()>0) customerBox.setSelectedIndex(0);
    }

    private void populateOrderFromSelection() {
        int r = orderTable.getSelectedRow();
        if (r>=0) {
            int id = Integer.parseInt(orderModel.getValueAt(r,0).toString());
            Optional<Order> opt = om.findById(id);
            if (opt.isPresent()) {
                Order o = opt.get();
                orderIdField.setText(String.valueOf(o.getId()));
                for (int i=0;i<customerBox.getItemCount();i++) {
                    Customer c = customerBox.getItemAt(i);
                    if (c.getId()==o.getCustomerId()) { customerBox.setSelectedIndex(i); break; }
                }
                currentItems = new ArrayList<>(o.getItems());
                renderItems();
            }
        }
    }
}

// MainFrame that ties panels together
class MainFrame extends JFrame {
    private final CustomerManager cm = new CustomerManager();
    private final MenuItemManager mm = new MenuItemManager();
    private final OrderManager om = new OrderManager();

    public MainFrame() {
        super("TrioBites — Restaurant Management System (Single File)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,650);
        setLocationRelativeTo(null);

        // Load data
        var snap = DataStore.load();
        cm.setAll(snap.customers);
        mm.setAll(snap.menuItems);
        om.setAll(snap.orders);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers", new CustomerPanel(cm, this::persist));
        tabs.addTab("Menu Items", new MenuItemPanel(mm, this::persist));
        tabs.addTab("Orders", new OrderPanel(cm, mm, om, this::persist));
        add(tabs, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { persist(); }
        });
    }

    private void persist() { DataStore.save(cm, mm, om); }
}

// ===================== Application Entry Point =====================
public class RestaurantManagementSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}

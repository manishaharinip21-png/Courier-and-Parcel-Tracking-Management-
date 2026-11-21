
package com.courier;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    static Properties cfg = new Properties();

    private static final Color NAVY = new Color(24, 34, 65);
    private static final Color SIDEBAR = new Color(20, 29, 55);
    private static final Color BLUE = new Color(66, 110, 230);
    private static final Color PURPLE = new Color(126, 87, 194);
    private static final Color TEAL = new Color(0, 150, 136);
    private static final Color ORANGE = new Color(245, 145, 55);
    private static final Color BG = new Color(244, 247, 252);
    private static final Color TEXT = new Color(43, 53, 75);
    private static final Color MUTED = new Color(120, 130, 150);
    private static final Color WHITE = Color.WHITE;

    private CardLayout pageLayout;
    private JPanel pages;
    private JLabel pageTitle;
    private Map<String, JButton> navigation = new LinkedHashMap<>();

    static {
        try (var in = Main.class.getResourceAsStream("/db.properties")) {
            if (in != null) cfg.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Connection db() throws SQLException {
        String url = System.getenv().getOrDefault(
            "COURIER_DB_URL", cfg.getProperty("db.url"));
        String user = System.getenv().getOrDefault(
            "COURIER_DB_USER", cfg.getProperty("db.user", "postgres"));
        String pass = System.getenv().getOrDefault(
            "COURIER_DB_PASSWORD", cfg.getProperty("db.password", ""));
        return DriverManager.getConnection(url, user, pass);
    }

    Main() {
        setTitle("Courier Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1350, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        // SIDEBAR
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(SIDEBAR);

        JPanel brand = new JPanel();
        brand.setBackground(SIDEBAR);
        brand.setBorder(new EmptyBorder(28, 20, 25, 15));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("  CARGO");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        logo.setForeground(WHITE);

        JLabel brandSub = new JLabel("  PARCEL MANAGEMENT");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandSub.setForeground(new Color(165, 180, 215));

        brand.add(logo);
        brand.add(Box.createVerticalStrut(6));
        brand.add(brandSub);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(SIDEBAR);
        menu.setBorder(new EmptyBorder(10, 12, 10, 12));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        JLabel menuLabel = new JLabel("  MAIN MENU");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        menuLabel.setForeground(new Color(135, 150, 185));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(menuLabel);
        menu.add(Box.createVerticalStrut(14));

        addNavigation(menu, "Dashboard", "dashboard");
        addNavigation(menu, "Customers", "customers");
        addNavigation(menu, "Delivery Personnel", "personnel");
        addNavigation(menu, "Parcels", "parcels");
        addNavigation(menu, "Delivery Attempts", "attempts");
        addNavigation(menu, "Track Parcel", "tracking");
        addNavigation(menu, "Analytics & FDS", "fds");

        // SCROLLABLE SIDEBAR MENU
        JScrollPane menuScroll = new JScrollPane(menu);
        menuScroll.setBorder(null);
        menuScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScroll.getViewport().setBackground(SIDEBAR);
        menuScroll.setBackground(SIDEBAR);

        sidebar.add(menuScroll, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "<html><center>COURIER SYSTEM<br>Management Portal</center></html>",
            SwingConstants.CENTER);
        footer.setForeground(new Color(145, 158, 190));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setBorder(new EmptyBorder(15, 5, 22, 5));
        sidebar.add(footer, BorderLayout.SOUTH);

        // MAIN WORKSPACE
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(BG);

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(WHITE);
        topbar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(225, 230, 240)),
            new EmptyBorder(18, 28, 18, 28)
        ));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(TEXT);

        JLabel userLabel = new JLabel("ADMINISTRATOR");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLabel.setForeground(BLUE);

        topbar.add(pageTitle, BorderLayout.WEST);
        topbar.add(userLabel, BorderLayout.EAST);
        workspace.add(topbar, BorderLayout.NORTH);

        pageLayout = new CardLayout();
        pages = new JPanel(pageLayout);
        pages.setBackground(BG);

        pages.add(dashboard(), "dashboard");

        pages.add(new DataPanel(
            "customers",
            new String[]{"customer_id","full_name","phone","email","address"},
            new String[]{"Full name","Phone","Email","Address"},
            new String[]{"full_name","phone","email","address"},
            new String[]{"text","text","optional","text"}
        ), "customers");

        pages.add(new DataPanel(
            "delivery_personnel",
            new String[]{"personnel_id","full_name","phone","vehicle_number","active"},
            new String[]{"Full name","Phone","Vehicle number","Active true/false"},
            new String[]{"full_name","phone","vehicle_number","active"},
            new String[]{"text","text","optional","boolean"}
        ), "personnel");

        pages.add(new DataPanel(
            "parcels",
            new String[]{"parcel_id","tracking_number","sender_id",
                "receiver_name","receiver_phone","delivery_address",
                "parcel_type","weight_kg","delivery_charge","booking_date",
                "expected_delivery","status","personnel_id","notes"},
            new String[]{"Tracking number (blank = auto)","Sender customer ID",
                "Receiver name","Receiver phone","Delivery address",
                "Parcel type","Weight kg","Delivery charge",
                "Booking date YYYY-MM-DD","Expected delivery YYYY-MM-DD",
                "Status","Personnel ID optional","Notes"},
            new String[]{"tracking_number","sender_id","receiver_name",
                "receiver_phone","delivery_address","parcel_type","weight_kg",
                "delivery_charge","booking_date","expected_delivery",
                "status","personnel_id","notes"},
            new String[]{"tracking","int","text","text","text","text",
                "decimal","decimal","date","optional_date","status",
                "optional_int","optional"}
        ), "parcels");

        pages.add(new DataPanel(
            "delivery_attempts",
            new String[]{"attempt_id","parcel_id","attempt_date",
                "attempt_status","remarks"},
            new String[]{"Parcel ID",
                "Date/time YYYY-MM-DD HH:MM:SS (optional)",
                "Successful/Failed","Remarks"},
            new String[]{"parcel_id","attempt_date","attempt_status","remarks"},
            new String[]{"int","optional_timestamp","attempt_status","optional"}
        ), "attempts");

        pages.add(trackingPanel(), "tracking");

        // FDS ANALYTICS PAGE
        pages.add(new FDSVisualizationPanel(), "fds");

        workspace.add(pages, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);
        root.add(workspace, BorderLayout.CENTER);

        setContentPane(root);
        showPage("dashboard", "Dashboard");
    }

    // NAVIGATION BUTTON CREATION
    private void addNavigation(JPanel menu, String title, String key) {

        JButton button = new JButton(title);

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(SIDEBAR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(14, 18, 14, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setPreferredSize(new Dimension(220, 50));

        button.addActionListener(e -> showPage(key, title));

        navigation.put(key, button);

        menu.add(button);
        menu.add(Box.createVerticalStrut(5));
    }

    // CHANGE PAGE
    private void showPage(String key, String title) {

        pageLayout.show(pages, key);
        pageTitle.setText(title);

        for (Map.Entry<String, JButton> entry : navigation.entrySet()) {

            JButton button = entry.getValue();

            if (entry.getKey().equals(key)) {
                button.setBackground(BLUE);
            } else {
                button.setBackground(SIDEBAR);
            }
        }

        pages.revalidate();
        pages.repaint();
    }

// DASHBOARD
JPanel dashboard() {
    JPanel panel = new JPanel(new BorderLayout(0, 22));
    panel.setBackground(BG);
    panel.setBorder(new EmptyBorder(28, 30, 28, 30));

    JPanel heading = new JPanel();
    heading.setOpaque(false);
    heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

    JLabel welcome = new JLabel("Welcome to your Dashboard");
    welcome.setFont(new Font("Segoe UI", Font.BOLD, 25));
    welcome.setForeground(TEXT);

    JLabel description = new JLabel(
        "Monitor your courier operations and delivery performance.");
    description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    description.setForeground(MUTED);

    heading.add(welcome);
    heading.add(Box.createVerticalStrut(7));
    heading.add(description);

    panel.add(heading, BorderLayout.NORTH);

    // EIGHT DASHBOARD CARDS - TWO ROWS
    JPanel cards = new JPanel(new GridLayout(2, 4, 18, 18));
    cards.setOpaque(false);

    String[][] stats = {
        {"CUSTOMERS",
            "SELECT COUNT(*) FROM customers"},

        {"TOTAL PARCELS",
            "SELECT COUNT(*) FROM parcels"},

        {"IN PROGRESS",
            "SELECT COUNT(*) FROM parcels WHERE status NOT IN ('Delivered','Returned')"},

        {"DELIVERED",
            "SELECT COUNT(*) FROM parcels WHERE status='Delivered'"},

        {"RETURNED PARCELS",
            "SELECT COUNT(*) FROM parcels WHERE LOWER(status)='returned'"},

        {"PENDING PARCELS",
            "SELECT COUNT(*) FROM parcels WHERE LOWER(status)='pending'"},

        {"TODAY'S BOOKINGS",
            "SELECT COUNT(*) FROM parcels WHERE booking_date::date = CURRENT_DATE"},

        {"DELIVERY ATTEMPTS",
            "SELECT COUNT(*) FROM delivery_attempts"}
    };

    Color[] accents = {
        BLUE, PURPLE, ORANGE, TEAL,
        new Color(220, 80, 80),
        new Color(230, 160, 45),
        new Color(55, 145, 190),
        new Color(120, 95, 190)
    };

    String[] symbols = {
        "C", "P", "↗", "✓",
        "R", "P", "T", "D"
    };

    for (int i = 0; i < stats.length; i++) {

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(228, 233, 243), 1, true),
            new EmptyBorder(17, 16, 17, 16)
        ));

        JLabel icon = new JLabel(symbols[i], SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(WHITE);
        icon.setOpaque(true);
        icon.setBackground(accents[i]);
        icon.setPreferredSize(new Dimension(42, 42));

        JLabel name = new JLabel(stats[i][0]);
        name.setFont(new Font("Segoe UI", Font.BOLD, 10));
        name.setForeground(MUTED);

        JLabel value = new JLabel(count(stats[i][1]));
        value.setFont(new Font("Segoe UI", Font.BOLD, 29));
        value.setForeground(TEXT);

        JLabel detail = new JLabel("Current records");
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        detail.setForeground(MUTED);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(name);
        content.add(Box.createVerticalStrut(9));
        content.add(value);
        content.add(Box.createVerticalStrut(4));
        content.add(detail);

        card.add(icon, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        cards.add(card);
    }

    JPanel center = new JPanel(new BorderLayout(0, 20));
    center.setOpaque(false);

    center.add(cards, BorderLayout.NORTH);

    JPanel info = new JPanel(new BorderLayout());
    info.setBackground(WHITE);
    info.setBorder(new CompoundBorder(
        new LineBorder(new Color(228, 233, 243), 1, true),
        new EmptyBorder(20, 24, 20, 24)
    ));

    JLabel infoTitle = new JLabel("Courier Management System");
    infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
    infoTitle.setForeground(TEXT);

    JLabel infoText = new JLabel(
        "<html>Manage customer information, parcels and delivery attempts.<br>" +
        "Use the navigation menu to access each management section " +
        "and track parcels.</html>");

    infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    infoText.setForeground(MUTED);

    JPanel infoContent = new JPanel();
    infoContent.setOpaque(false);
    infoContent.setLayout(new BoxLayout(infoContent, BoxLayout.Y_AXIS));

    infoContent.add(infoTitle);
    infoContent.add(Box.createVerticalStrut(12));
    infoContent.add(infoText);

    info.add(infoContent, BorderLayout.CENTER);

    center.add(info, BorderLayout.CENTER);

    panel.add(center, BorderLayout.CENTER);

    return panel;
}

    // TRACKING
    JPanel trackingPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 16));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        top.setBackground(WHITE);
        top.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 231, 242), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel heading = new JLabel("Tracking Number");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(TEXT);

        JTextField field = styledField(25);
        JButton button = styledButton("Track Parcel", BLUE);

        top.add(heading);
        top.add(field);
        top.add(button);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Consolas", Font.PLAIN, 14));
        output.setForeground(TEXT);
        output.setBackground(WHITE);
        output.setBorder(new EmptyBorder(20, 20, 20, 20));
        output.setLineWrap(true);
        output.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(new LineBorder(new Color(225, 231, 242), 1, true));

        JLabel instruction = new JLabel(
            "Enter a tracking number to view parcel details and delivery history.");
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instruction.setForeground(MUTED);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(instruction);
        north.add(Box.createVerticalStrut(14));
        north.add(top);

        panel.add(north, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        button.addActionListener(e -> {
            if (field.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    panel, "Please enter a tracking number.");
                return;
            }

            String sql = "SELECT p.*, COALESCE(d.full_name,'Not assigned') delivery_person " +
                "FROM parcels p LEFT JOIN delivery_personnel d " +
                "ON p.personnel_id=d.personnel_id WHERE p.tracking_number=?";

            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, field.getText().trim());

                try (ResultSet r = ps.executeQuery()) {
                    if (!r.next()) {
                        output.setText(
                            "No parcel found. Please check the tracking number.");
                        return;
                    }

                    StringBuilder b =
                        new StringBuilder("PARCEL TRACKING DETAILS\n");
                    b.append("========================================\n");

                    String[] names = {
                        "tracking_number", "receiver_name", "receiver_phone",
                        "delivery_address", "parcel_type", "weight_kg",
                        "delivery_charge", "booking_date", "expected_delivery",
                        "status", "delivery_person"
                    };

                    for (String n : names) {
                        b.append(n.replace("_", " ").toUpperCase())
                            .append(" : ").append(r.getString(n)).append("\n");
                    }

                    b.append("\nDELIVERY ATTEMPTS\n");
                    b.append("========================================\n");

                    try (PreparedStatement h = c.prepareStatement(
                        "SELECT attempt_date,attempt_status,remarks " +
                        "FROM delivery_attempts WHERE parcel_id=? ORDER BY attempt_date")) {

                        h.setInt(1, r.getInt("parcel_id"));

                        try (ResultSet a = h.executeQuery()) {
                            boolean found = false;

                            while (a.next()) {
                                found = true;
                                b.append(a.getString(1)).append(" | ")
                                    .append(a.getString(2)).append(" | ")
                                    .append(a.getString(3)).append("\n");
                            }

                            if (!found) {
                                b.append("No delivery attempts recorded.\n");
                            }
                        }
                    }

                    output.setText(b.toString());
                    output.setCaretPosition(0);
                }
            } catch (Exception ex) {
                error(panel, ex);
            }
        });

        return panel;
    }

    // DATABASE MANAGEMENT
    class DataPanel extends JPanel {
        String table;
        String[] cols, labels, fields, types;
        JTextField[] inputs;
        JTextField search = styledField(14);
        DefaultTableModel model;
        JTable grid;
        Object editId = null;

        DataPanel(String t, String[] c, String[] l,
                  String[] f, String[] ty) {

            super(new BorderLayout(10, 12));

            table = t;
            cols = c;
            labels = l;
            fields = f;
            types = ty;

            setBackground(BG);
            setBorder(new EmptyBorder(20, 22, 20, 22));

            JPanel form = new JPanel(new GridLayout(0, 4, 12, 12));
            form.setBackground(WHITE);
            form.setBorder(new CompoundBorder(
                new TitledBorder(
                    new LineBorder(new Color(220, 227, 240), 1, true),
                    "  ADD / EDIT RECORD  ",
                    TitledBorder.LEADING,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    BLUE
                ),
                new EmptyBorder(15, 15, 15, 15)
            ));

            inputs = new JTextField[f.length];

            for (int i = 0; i < f.length; i++) {
                JLabel label = new JLabel(l[i]);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(TEXT);

                form.add(label);

                inputs[i] = styledField(12);
                form.add(inputs[i]);
            }

            JButton add = styledButton("+ Add Record", TEAL);
            JButton edit = styledButton("Edit Selected", BLUE);
            JButton save = styledButton("Save Changes", PURPLE);
            JButton del = styledButton(
                "Delete Selected", new Color(205, 75, 85));
            JButton refresh = styledButton("Refresh", NAVY);
            JButton find = styledButton("Search", BLUE);
            JButton all = styledButton("Show All", MUTED);

            JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 10));
            actions.setBackground(BG);

            for (JButton b : new JButton[]{
                add, edit, save, del, refresh
            }) {
                actions.add(b);
            }

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            searchLabel.setForeground(TEXT);

            actions.add(searchLabel);
            actions.add(search);
            actions.add(find);
            actions.add(all);

            JPanel north = new JPanel(new BorderLayout(0, 10));
            north.setBackground(BG);
            north.add(form, BorderLayout.CENTER);
            north.add(actions, BorderLayout.SOUTH);

            add(north, BorderLayout.NORTH);

            String[] heads = Arrays.stream(c)
                .map(x -> x.replace("_", " ").toUpperCase())
                .toArray(String[]::new);

            model = new DefaultTableModel(heads, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            grid = new JTable(model);
            grid.setAutoCreateRowSorter(true);
            grid.setRowHeight(34);
            grid.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            grid.setForeground(TEXT);
            grid.setBackground(WHITE);
            grid.setSelectionBackground(new Color(220, 232, 255));
            grid.setSelectionForeground(TEXT);
            grid.setGridColor(new Color(232, 236, 244));
            grid.setShowVerticalLines(false);
            grid.setIntercellSpacing(new Dimension(0, 1));
            grid.setFillsViewportHeight(true);

            JTableHeader tableHeader = grid.getTableHeader();
            tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tableHeader.setForeground(WHITE);
            tableHeader.setBackground(NAVY);
            tableHeader.setOpaque(true);
            tableHeader.setPreferredSize(new Dimension(0, 40));
            tableHeader.setReorderingAllowed(false);

            JScrollPane tableScroll = new JScrollPane(grid);
            tableScroll.setBorder(
                new LineBorder(new Color(220, 226, 238), 1, true));
            tableScroll.getViewport().setBackground(WHITE);

            add(tableScroll, BorderLayout.CENTER);

            add.addActionListener(e -> insert());
            edit.addActionListener(e -> beginEdit());
            save.addActionListener(e -> saveEdit());
            del.addActionListener(e -> delete());
            refresh.addActionListener(e -> load(""));
            find.addActionListener(
                e -> load(search.getText().trim()));

            all.addActionListener(e -> {
                search.setText("");
                load("");
            });

            load("");
        }

        void load(String term) {
            model.setRowCount(0);

            String sql = "SELECT " + String.join(",", cols) +
                " FROM " + table;

            if (!term.isBlank()) {
                List<String> w = new ArrayList<>();

                for (String col : cols) {
                    w.add("CAST(" + col + " AS TEXT) ILIKE ?");
                }

                sql += " WHERE " + String.join(" OR ", w);
            }

            sql += " ORDER BY 1 DESC";

            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                if (!term.isBlank()) {
                    for (int i = 1; i <= cols.length; i++) {
                        ps.setString(i, "%" + term + "%");
                    }
                }

                try (ResultSet r = ps.executeQuery()) {
                    while (r.next()) {
                        Object[] row = new Object[cols.length];

                        for (int i = 0; i < cols.length; i++) {
                            row[i] = r.getObject(i + 1);
                        }

                        model.addRow(row);
                    }
                }
            } catch (Exception e) {
                error(this, e);
            }
        }

        void bind(PreparedStatement ps) throws Exception {
            for (int i = 0; i < inputs.length; i++) {
                String v = inputs[i].getText().trim();
                String type = types[i];

                boolean optional = type.startsWith("optional")
                    || type.equals("tracking")
                    || fields[i].equals("email")
                    || fields[i].equals("notes")
                    || fields[i].equals("vehicle_number");

                if (v.isEmpty() && optional) {
                    if (type.equals("tracking")) {
                        ps.setString(i + 1,
                            "CP" + UUID.randomUUID().toString()
                                .replace("-", "")
                                .substring(0, 12).toUpperCase());
                    } else {
                        ps.setNull(i + 1, Types.NULL);
                    }
                    continue;
                }

                if (v.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Please enter " + labels[i] + ".");
                }

                switch (type) {
                    case "int":
                        ps.setInt(i + 1, Integer.parseInt(v));
                        break;

                    case "optional_int":
                        ps.setInt(i + 1, Integer.parseInt(v));
                        break;

                    case "decimal":
                        BigDecimal n = new BigDecimal(v);

                        if (n.signum() < 0) {
                            throw new IllegalArgumentException(
                                "Value cannot be negative.");
                        }

                        ps.setBigDecimal(i + 1, n);
                        break;

                    case "date":
                        ps.setDate(i + 1,
                            java.sql.Date.valueOf(LocalDate.parse(v)));
                        break;

                    case "optional_date":
                        ps.setDate(i + 1,
                            java.sql.Date.valueOf(LocalDate.parse(v)));
                        break;

                    case "optional_timestamp":
                        ps.setTimestamp(i + 1, Timestamp.valueOf(v));
                        break;

                    case "boolean":
                        if (!v.equalsIgnoreCase("true")
                            && !v.equalsIgnoreCase("false")) {
                            throw new IllegalArgumentException(
                                "Enter true or false.");
                        }

                        ps.setBoolean(i + 1,
                            Boolean.parseBoolean(v));
                        break;

                    case "status":
                        if (!List.of(
                            "Booked", "Picked Up", "In Transit",
                            "Out for Delivery", "Delivered",
                            "Delivery Failed", "Returned"
                        ).contains(v)) {
                            throw new IllegalArgumentException(
                                "Invalid parcel status.");
                        }

                        ps.setString(i + 1, v);
                        break;

                    case "attempt_status":
                        if (!List.of("Successful", "Failed").contains(v)) {
                            throw new IllegalArgumentException(
                                "Use Successful or Failed.");
                        }

                        ps.setString(i + 1, v);
                        break;

                    case "tracking":
                        ps.setString(i + 1, v);
                        break;

                    default:
                        ps.setString(i + 1, v);
                }
            }
        }

        void insert() {
            String sql = "INSERT INTO " + table + "(" +
                String.join(",", fields) + ") VALUES(" +
                String.join(",", Collections.nCopies(fields.length, "?")) +
                ")";

            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                bind(ps);
                ps.executeUpdate();

                clear();
                load(search.getText().trim());

                JOptionPane.showMessageDialog(
                    this, "Record added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                error(this, e);
            }
        }

        void beginEdit() {
            int s = grid.getSelectedRow();

            if (s < 0) {
                JOptionPane.showMessageDialog(
                    this, "Select a row first.");
                return;
            }

            int r = grid.convertRowIndexToModel(s);
            editId = model.getValueAt(r, 0);

            for (int i = 0; i < inputs.length; i++) {
                Object v = model.getValueAt(r, i + 1);
                inputs[i].setText(v == null ? "" : v.toString());
            }

            JOptionPane.showMessageDialog(
                this, "Edit the fields, then click Save Changes.");
        }

        void saveEdit() {
            if (editId == null) {
                JOptionPane.showMessageDialog(
                    this, "Click Edit Selected first.");
                return;
            }

            String set = String.join(",",
                Arrays.stream(fields)
                    .map(f -> f + "=?")
                    .toArray(String[]::new));

            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(
                    "UPDATE " + table + " SET " + set +
                    " WHERE " + cols[0] + "=?")) {

                bind(ps);
                ps.setObject(fields.length + 1, editId);
                ps.executeUpdate();

                editId = null;
                clear();
                load(search.getText().trim());

                JOptionPane.showMessageDialog(
                    this, "Record updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                error(this, e);
            }
        }

        void delete() {
            int s = grid.getSelectedRow();

            if (s < 0) {
                JOptionPane.showMessageDialog(
                    this, "Select a row first.");
                return;
            }

            Object id = model.getValueAt(
                grid.convertRowIndexToModel(s), 0);

            if (JOptionPane.showConfirmDialog(
                this, "Delete ID " + id + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM " + table + " WHERE " + cols[0] + "=?")) {

                ps.setObject(1, id);
                ps.executeUpdate();

                load(search.getText().trim());

                JOptionPane.showMessageDialog(
                    this, "Record deleted.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                error(this, e);
            }
        }

        void clear() {
            for (JTextField f : inputs) {
                f.setText("");
            }
        }
    }

    // TEXT FIELD
    private static JTextField styledField(int columns) {
        JTextField field = new JTextField(columns);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(BLUE);
        field.setOpaque(true);

        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 218, 233), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        return field;
    }

    // BUTTON
    private static JButton styledButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setEnabled(true);

        return button;
    }

    // ERROR MESSAGE
    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(
            parent,
            e.getClass().getSimpleName() + ": " + e.getMessage(),
            "Application error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // APPLICATION START
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());

                UIManager.put("OptionPane.messageFont",
                    new Font("Segoe UI", Font.PLAIN, 13));
                UIManager.put("OptionPane.messageForeground", TEXT);

                try (Connection c = db()) {
                    // Database connection check
                }

                new Main().setVisible(true);

            } catch (Exception e) {
                error(null, e);
            }
        });
    }
    String count(String sql) {
    try (Connection c = db();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        return rs.next() ? rs.getString(1) : "0";
    } catch (Exception e) {
        e.printStackTrace();
        return "—";
    }
}
}

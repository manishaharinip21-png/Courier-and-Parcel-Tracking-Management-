
package com.courier;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class FDSVisualizationPanel extends JPanel {

    private static final Color BG = new Color(244, 247, 252);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLUE = new Color(66, 110, 230);
    private static final Color TEXT = new Color(43, 53, 75);
    private static final Color MUTED = new Color(120, 130, 150);
    private static final Color TEAL = new Color(0, 150, 136);
    private static final Color ORANGE = new Color(245, 145, 55);
    private static final Color PURPLE = new Color(126, 87, 194);

    private final ChartPanel statusChart =
            new ChartPanel("Parcel Status Distribution", "pie");

    private final ChartPanel monthlyChart =
            new ChartPanel("Monthly Parcel Bookings", "bar");

    private final ChartPanel deliveryChart =
            new ChartPanel("Successful Delivery Attempts", "line");

    private final JTextArea arrayArea = createTextArea();
    private final JTextArea mapArea = createTextArea();
    private final JTextArea listArea = createTextArea();
    private final JTextArea searchArea = createTextArea();

    public FDSVisualizationPanel() {

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("Analytics & FDS Visualization");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 23));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Visual reports and Data Structures used in the Courier Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        JButton refresh = new JButton("Refresh Data");
        refresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refresh.setForeground(Color.WHITE);
        refresh.setBackground(BLUE);
        refresh.setFocusPainted(false);
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.setBorder(new EmptyBorder(10, 16, 10, 16));
        refresh.addActionListener(e -> loadData());

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(heading);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(subtitle);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleBlock, BorderLayout.CENTER);
        header.add(refresh, BorderLayout.EAST);

        JPanel charts = new JPanel(new GridLayout(1, 3, 14, 14));
        charts.setOpaque(false);
        charts.setPreferredSize(new Dimension(1050, 340));

        charts.add(statusChart);
        charts.add(monthlyChart);
        charts.add(deliveryChart);

        JPanel structures = new JPanel(new GridLayout(2, 2, 14, 14));
        structures.setOpaque(false);
        structures.setPreferredSize(new Dimension(1050, 420));

        structures.add(conceptCard("1. ARRAY - Parcel Records", arrayArea));
        structures.add(conceptCard("2. LINKEDHASHMAP - Navigation", mapArea));
        structures.add(conceptCard("3. ARRAYLIST - Search Results", listArea));
        structures.add(conceptCard("4. SEARCHING & SORTING", searchArea));

        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        charts.setAlignmentX(Component.LEFT_ALIGNMENT);
        structures.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(charts);
        content.add(Box.createVerticalStrut(22));

        JLabel fdsHeading = new JLabel("Data Structures Demonstration");
        fdsHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fdsHeading.setForeground(TEXT);
        fdsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fdsHeading);
        content.add(Box.createVerticalStrut(12));
        content.add(structures);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    private static JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setForeground(TEXT);
        area.setBackground(WHITE);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private JPanel conceptCard(String title, JTextArea area) {

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(WHITE);

        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 231, 242), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(BLUE);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new LineBorder(new Color(235, 238, 245)));

        card.add(label, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadData() {
        loadStatusChart();
        loadMonthlyChart();
        loadDeliveryChart();
        loadFDSExamples();
    }

    // PIE CHART
    private void loadStatusChart() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql = "SELECT status, COUNT(*) FROM parcels " +
                     "GROUP BY status ORDER BY status";

        try (Connection c = Main.db();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString(1), rs.getInt(2));
            }

            statusChart.setData(data);

        } catch (Exception e) {
            statusChart.setError("Database error: " + e.getMessage());
        }
    }

    // BAR CHART
    private void loadMonthlyChart() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
                "SELECT TO_CHAR(DATE_TRUNC('month', booking_date), 'Mon YY'), " +
                "COUNT(*) FROM parcels " +
                "GROUP BY DATE_TRUNC('month', booking_date) " +
                "ORDER BY DATE_TRUNC('month', booking_date) DESC LIMIT 6";

        try (Connection c = Main.db();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString(1), rs.getInt(2));
            }

            monthlyChart.setData(reverse(data));

        } catch (Exception e) {
            monthlyChart.setError("Database error: " + e.getMessage());
        }
    }

    // LINE CHART
    private void loadDeliveryChart() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
                "SELECT TO_CHAR(DATE_TRUNC('month', attempt_date), 'Mon YY'), " +
                "COUNT(*) FILTER (WHERE attempt_status='Successful') " +
                "FROM delivery_attempts " +
                "GROUP BY DATE_TRUNC('month', attempt_date) " +
                "ORDER BY DATE_TRUNC('month', attempt_date) DESC LIMIT 6";

        try (Connection c = Main.db();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString(1), rs.getInt(2));
            }

            deliveryChart.setData(reverse(data));

        } catch (Exception e) {
            deliveryChart.setError("Database error: " + e.getMessage());
        }
    }

    // REVERSE MAP ORDER
    private Map<String, Integer> reverse(
            Map<String, Integer> original) {

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(original.entrySet());

        Collections.reverse(entries);

        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    // FDS EXAMPLES
    private void loadFDSExamples() {

        // 1. ARRAY
        try (Connection c = Main.db();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT tracking_number, status FROM parcels " +
                     "ORDER BY parcel_id DESC LIMIT 6");
             ResultSet rs = ps.executeQuery()) {

            List<String> records = new ArrayList<>();

            while (rs.next()) {
                records.add(
                        rs.getString(1) + " - " + rs.getString(2));
            }

            String[] parcelArray = records.toArray(new String[0]);

            StringBuilder b = new StringBuilder();
            b.append("String[] parcelRecords\n\n");

            for (int i = 0; i < parcelArray.length; i++) {
                b.append("Index [").append(i).append("]  ->  ")
                 .append(parcelArray[i]).append("\n");
            }

            if (parcelArray.length == 0) {
                b.append("No parcel records available.");
            }

            arrayArea.setText(b.toString());

        } catch (Exception e) {
            arrayArea.setText("Database error: " + e.getMessage());
        }

        // 2. LINKEDHASHMAP
        LinkedHashMap<String, String> navigation =
                new LinkedHashMap<>();

        navigation.put("dashboard", "Dashboard");
        navigation.put("customers", "Customers");
        navigation.put("personnel", "Delivery Personnel");
        navigation.put("parcels", "Parcels");
        navigation.put("attempts", "Delivery Attempts");
        navigation.put("tracking", "Track Parcel");
        navigation.put("fds", "Analytics & FDS");

        StringBuilder mapText = new StringBuilder();

        mapText.append("LinkedHashMap<String, String>\n\n");

        int position = 1;

        for (Map.Entry<String, String> entry : navigation.entrySet()) {
            mapText.append(position++).append(". ")
                   .append(entry.getKey()).append("  ->  ")
                   .append(entry.getValue()).append("\n");
        }

        mapText.append("\nInsertion order is maintained.");

        mapArea.setText(mapText.toString());

        // 3. ARRAYLIST
        try (Connection c = Main.db();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT tracking_number FROM parcels " +
                     "WHERE status='In Transit' " +
                     "ORDER BY parcel_id DESC LIMIT 10");
             ResultSet rs = ps.executeQuery()) {

            ArrayList<String> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rs.getString(1));
            }

            StringBuilder b = new StringBuilder();

            b.append("ArrayList<String> searchResults\n\n");

            for (int i = 0; i < results.size(); i++) {
                b.append("[").append(i).append("]  ")
                 .append(results.get(i)).append("\n");
            }

            b.append("\nTotal results: ").append(results.size());

            listArea.setText(b.toString());

            // 4. SEARCHING AND SORTING
            ArrayList<String> sorted = new ArrayList<>(results);
            Collections.sort(sorted);

            StringBuilder s = new StringBuilder();

            s.append("SEARCHING\n");
            s.append("------------------------------\n");
            s.append("Condition: status = In Transit\n");
            s.append("Matching records: ")
             .append(results.size()).append("\n\n");

            s.append("SORTING\n");
            s.append("------------------------------\n");
            s.append("Tracking numbers in alphabetical order:\n\n");

            for (String value : sorted) {
                s.append("- ").append(value).append("\n");
            }

            searchArea.setText(s.toString());

        } catch (Exception e) {
            listArea.setText("Database error: " + e.getMessage());
            searchArea.setText("Database error: " + e.getMessage());
        }
    }

    // CHART PANEL
    static class ChartPanel extends JPanel {

        private final String title;
        private final String type;

        private Map<String, Integer> data = new LinkedHashMap<>();
        private String errorMessage;

        private final Color[] colors = {
                new Color(66, 110, 230),
                new Color(0, 150, 136),
                new Color(245, 145, 55),
                new Color(126, 87, 194),
                new Color(220, 85, 105),
                new Color(60, 160, 190),
                new Color(150, 165, 185)
        };

        ChartPanel(String title, String type) {

            this.title = title;
            this.type = type;

            setBackground(WHITE);

            setBorder(new CompoundBorder(
                    new LineBorder(new Color(225, 231, 242), 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));

            setPreferredSize(new Dimension(350, 340));
            setMinimumSize(new Dimension(280, 300));
        }

        void setData(Map<String, Integer> values) {
            data = new LinkedHashMap<>(values);
            errorMessage = null;
            revalidate();
            repaint();
        }

        void setError(String message) {
            errorMessage = message;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(title, 14, 24);

            if (errorMessage != null) {
                g.setColor(new Color(190, 65, 65));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                drawWrappedText(g, errorMessage, 15, 60,
                        getWidth() - 30, 16);

                g.dispose();
                return;
            }

            if (data.isEmpty()) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("No data available yet.", 16, 70);
                g.dispose();
                return;
            }

            if (type.equals("pie")) {
                drawPie(g);
            } else if (type.equals("bar")) {
                drawBar(g);
            } else {
                drawLine(g);
            }

            g.dispose();
        }

        private void drawWrappedText(
                Graphics2D g, String message,
                int x, int y, int width, int lineHeight) {

            FontMetrics fm = g.getFontMetrics();
            StringBuilder line = new StringBuilder();

            for (String word : message.split(" ")) {

                String test = line.length() == 0
                        ? word : line + " " + word;

                if (fm.stringWidth(test) > width && line.length() > 0) {
                    g.drawString(line.toString(), x, y);
                    y += lineHeight;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }

            if (line.length() > 0) {
                g.drawString(line.toString(), x, y);
            }
        }

        // PIE CHART
        private void drawPie(Graphics2D g) {

            int total = data.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            if (total <= 0) {
                g.setColor(MUTED);
                g.drawString("No parcel records yet.", 20, 65);
                return;
            }

            int diameter = Math.min(155, getWidth() - 45);
            int x = (getWidth() - diameter) / 2;
            int y = 42;

            int start = 0;
            int index = 0;

            for (int value : data.values()) {

                int angle = (int) Math.round(
                        value * 360.0 / total);

                g.setColor(colors[index % colors.length]);
                g.fillArc(x, y, diameter, diameter, start, angle);

                start += angle;
                index++;
            }

            int legendY = y + diameter + 22;
            index = 0;

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                int lx = 15 + (index % 2) * (getWidth() / 2);
                int ly = legendY + (index / 2) * 19;

                g.setColor(colors[index % colors.length]);
                g.fillRoundRect(lx, ly - 9, 10, 10, 3, 3);

                g.setColor(TEXT);

                String label = entry.getKey() + ": " + entry.getValue();

                g.drawString(label, lx + 14, ly);

                index++;
            }
        }

        // BAR CHART
        private void drawBar(Graphics2D g) {

            int left = 38;
            int right = getWidth() - 16;
            int top = 52;
            int bottom = getHeight() - 48;

            int chartHeight = bottom - top;

            int max = Math.max(1,
                    Collections.max(data.values()));

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            for (int i = 0; i <= 4; i++) {

                int y = bottom - (chartHeight * i / 4);

                g.setColor(new Color(232, 236, 244));
                g.drawLine(left, y, right, y);

                g.setColor(MUTED);
                g.drawString(
                        String.valueOf(max * i / 4), 5, y + 4);
            }

            int count = data.size();
            int slot = Math.max(1, (right - left) / count);
            int barWidth = Math.max(12, slot / 2);

            int index = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                int height = (int) (
                        entry.getValue() * chartHeight / (double) max);

                int x = left + index * slot + (slot - barWidth) / 2;
                int y = bottom - height;

                g.setColor(colors[index % colors.length]);
                g.fillRoundRect(x, y, barWidth, height, 7, 7);

                g.setColor(TEXT);
                g.drawString(
                        String.valueOf(entry.getValue()),
                        x, Math.max(top + 10, y - 5));

                g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g.drawString(entry.getKey(), x - 5, bottom + 17);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                index++;
            }
        }

        // LINE CHART
        private void drawLine(Graphics2D g) {

            int left = 38;
            int right = getWidth() - 18;
            int top = 52;
            int bottom = getHeight() - 48;

            int chartHeight = bottom - top;

            int max = Math.max(1,
                    Collections.max(data.values()));

            for (int i = 0; i <= 4; i++) {

                int y = bottom - (chartHeight * i / 4);

                g.setColor(new Color(232, 236, 244));
                g.drawLine(left, y, right, y);

                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                g.drawString(
                        String.valueOf(max * i / 4), 5, y + 4);
            }

            int n = data.size();
            int index = 0;

            int previousX = -1;
            int previousY = -1;

            g.setColor(BLUE);
            g.setStroke(new BasicStroke(2.5f));

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                int x = n == 1
                        ? (left + right) / 2
                        : left + index * (right - left) / (n - 1);

                int y = bottom - (int) (
                        entry.getValue() * chartHeight / (double) max);

                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, x, y);
                }

                g.fillOval(x - 5, y - 5, 10, 10);

                g.setColor(TEXT);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                g.drawString(
                        String.valueOf(entry.getValue()), x - 8, y - 10);

                g.drawString(entry.getKey(), x - 16, bottom + 17);

                g.setColor(BLUE);

                previousX = x;
                previousY = y;
                index++;
            }
        }
    }
}

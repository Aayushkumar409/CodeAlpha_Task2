import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class StockTradingPlatformGUI extends JFrame {

    static class Stock {
        String symbol;
        double price;

        Stock(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
        }
    }

    static class Holding {
        String symbol;
        int quantity;
        double price;

        Holding(String symbol, int quantity, double price) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
        }
    }

    Map<String, Stock> market = new HashMap<>();
    Map<String, Holding> portfolio = new HashMap<>();
    double balance = 10000.0;

    DefaultTableModel marketTable, portfolioTable;
    JLabel balanceLabel;

    public StockTradingPlatformGUI() {
        setTitle("📊 Stock Trading Platform");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Market Table
        marketTable = new DefaultTableModel(new Object[]{"Stock", "Price"}, 0);
        JTable marketTableView = new JTable(marketTable);
        add(new JScrollPane(marketTableView), BorderLayout.WEST);

        // Portfolio Table
        portfolioTable = new DefaultTableModel(new Object[]{"Stock", "Quantity", "Current Price"}, 0);
        JTable portfolioView = new JTable(portfolioTable);
        add(new JScrollPane(portfolioView), BorderLayout.CENTER);

        // Control Panel
        JPanel control = new JPanel(new GridLayout(3, 1));

        balanceLabel = new JLabel("💰 Balance: ₹" + balance);
        control.add(balanceLabel);

        JPanel buySellPanel = new JPanel();
        JTextField symbolField = new JTextField(5);
        JTextField qtyField = new JTextField(5);
        JButton buyBtn = new JButton("Buy");
        JButton sellBtn = new JButton("Sell");
        buySellPanel.add(new JLabel("Symbol:"));
        buySellPanel.add(symbolField);
        buySellPanel.add(new JLabel("Qty:"));
        buySellPanel.add(qtyField);
        buySellPanel.add(buyBtn);
        buySellPanel.add(sellBtn);
        control.add(buySellPanel);

        JButton refreshBtn = new JButton("Refresh Market");
        control.add(refreshBtn);

        add(control, BorderLayout.SOUTH);

        loadMarket();
        loadPortfolio();
        updateTables();

        // Buy button
        buyBtn.addActionListener(e -> {
            String symbol = symbolField.getText().toUpperCase();
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid quantity.");
                return;
            }

            if (!market.containsKey(symbol)) {
                JOptionPane.showMessageDialog(this, "Stock not found.");
                return;
            }

            double cost = market.get(symbol).price * qty;
            if (balance < cost) {
                JOptionPane.showMessageDialog(this, "Not enough balance.");
                return;
            }

            balance -= cost;
            portfolio.putIfAbsent(symbol, new Holding(symbol, 0, market.get(symbol).price));
            portfolio.get(symbol).quantity += qty;
            updateTables();
        });

        // Sell button
        sellBtn.addActionListener(e -> {
            String symbol = symbolField.getText().toUpperCase();
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid quantity.");
                return;
            }

            if (!portfolio.containsKey(symbol) || portfolio.get(symbol).quantity < qty) {
                JOptionPane.showMessageDialog(this, "Not enough stock to sell.");
                return;
            }

            double revenue = market.get(symbol).price * qty;
            balance += revenue;
            portfolio.get(symbol).quantity -= qty;
            if (portfolio.get(symbol).quantity == 0) portfolio.remove(symbol);
            updateTables();
        });

        // Refresh Market
        refreshBtn.addActionListener(e -> {
            simulatePriceChange();
            updateTables();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                savePortfolio();
            }
        });

        setVisible(true);
    }

    void loadMarket() {
        market.put("TCS", new Stock("TCS", 3200));
        market.put("INFY", new Stock("INFY", 1400));
        market.put("RELIANCE", new Stock("RELIANCE", 2500));
        market.put("WIPRO", new Stock("WIPRO", 430));
    }

    void simulatePriceChange() {
        Random r = new Random();
        for (Stock s : market.values()) {
            double change = (r.nextDouble() - 0.5) * 0.1; // -5% to +5%
            s.price *= (1 + change);
        }
    }

    void updateTables() {
        // Update market table
        marketTable.setRowCount(0);
        for (Stock s : market.values()) {
            marketTable.addRow(new Object[]{s.symbol, String.format("₹%.2f", s.price)});
        }

        // Update portfolio table
        portfolioTable.setRowCount(0);
        for (Holding h : portfolio.values()) {
            double currentPrice = market.get(h.symbol).price;
            portfolioTable.addRow(new Object[]{h.symbol, h.quantity, String.format("₹%.2f", currentPrice)});
        }

        balanceLabel.setText("💰 Balance: ₹" + String.format("%.2f", balance));
    }

    void savePortfolio() {
        try (PrintWriter out = new PrintWriter("portfolio.txt")) {
            out.println(balance);
            for (Holding h : portfolio.values()) {
                out.println(h.symbol + "," + h.quantity);
            }
        } catch (Exception e) {
            System.out.println("Error saving portfolio.");
        }
    }

    void loadPortfolio() {
        File file = new File("portfolio.txt");
        if (!file.exists()) return;

        try (Scanner sc = new Scanner(file)) {
            balance = Double.parseDouble(sc.nextLine());
            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(",");
                String sym = data[0];
                int qty = Integer.parseInt(data[1]);
                if (market.containsKey(sym)) {
                    portfolio.put(sym, new Holding(sym, qty, market.get(sym).price));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading portfolio.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StockTradingPlatformGUI::new);
    }
}
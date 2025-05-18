package com.editor;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;

public class DatabasePanel extends JPanel {
  private Timer fadeInTimer;
  private Timer fadeOutTimer;

  private JComboBox<DatabaseManager.DatabaseType> databaseSelector;
  private JComboBox<String> tableSelector;
  private JTable table;
  private DefaultTableModel model;
  private Connection connection;

  private JPanel loadingOverlay;
  private JLabel loadingLabel;
  private JProgressBar spinnerBar;
  public enum FontStyle {
    REGULAR, BOLD, ITALIC, SEMIBOLD
  }

  private static Font jetBrainsMonoRegular;
  private static Font jetBrainsMonoBold;
  private static Font jetBrainsMonoItalic;
  private static Font jetBrainsMonoSemiBold;

  static {
    try {
      jetBrainsMonoRegular = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/JetBrainsMono-Regular.ttf"));
      jetBrainsMonoBold = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/JetBrainsMono-Bold.ttf"));
      jetBrainsMonoItalic = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/JetBrainsMono-Italic.ttf"));
      jetBrainsMonoSemiBold = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/JetBrainsMono-SemiBold.ttf"));
    } catch (Exception e) {
      System.err.println("Failed to load JetBrains Mono fonts: " + e.getMessage());
      jetBrainsMonoRegular = new Font("Serif", Font.PLAIN, 12);
      jetBrainsMonoBold = jetBrainsMonoRegular;
      jetBrainsMonoItalic = jetBrainsMonoRegular;
      jetBrainsMonoSemiBold = jetBrainsMonoRegular;
    }
  }

  public static Font getJetBrainsFont( FontStyle style, float size) {
    switch (style) {
      case BOLD:
        return jetBrainsMonoBold.deriveFont(size);
      case ITALIC:
        return jetBrainsMonoItalic.deriveFont(size);
      case SEMIBOLD:
        return jetBrainsMonoSemiBold.deriveFont(size);
      case REGULAR:
      default:
        return jetBrainsMonoRegular.deriveFont(size);
    }
  }



  public DatabasePanel() throws IOException, FontFormatException {
    setLayout(new BorderLayout());
    setBackground(new Color(40, 40, 40));

    JLayeredPane layeredPane = new JLayeredPane();
    add(layeredPane, BorderLayout.CENTER);

    applyDarkTheme();

    JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
    contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    contentPanel.setOpaque(false);

    JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    connectionPanel.setBackground(new Color(40, 40, 40));
    connectionPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    databaseSelector = new JComboBox<>(DatabaseManager.DatabaseType.values());
    JButton connectBtn = new JButton("Connect");

    connectionPanel.add(new JLabel("Select Database:"));
    connectionPanel.add(databaseSelector);
    connectionPanel.setFont(getJetBrainsFont(FontStyle.BOLD, 15));
    connectionPanel.add(connectBtn);
    contentPanel.add(connectionPanel, BorderLayout.NORTH);

    JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
    tablePanel.setBackground(new Color(40, 40, 40));
    tablePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBackground(new Color(40, 40, 40));
    topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    tableSelector = new JComboBox<>();
    JButton loadBtn = new JButton("Load Table");
    JButton addRowBtn = new JButton("Add Row");
    JButton deleteRowBtn = new JButton("Delete Row");

    topPanel.add(tableSelector, BorderLayout.CENTER);

    JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    buttonGroup.setBackground(new Color(40, 40, 40));
    buttonGroup.add(addRowBtn);
    buttonGroup.add(deleteRowBtn);
    buttonGroup.add(loadBtn);
    topPanel.add(buttonGroup, BorderLayout.EAST);

    tablePanel.add(topPanel, BorderLayout.NORTH);

    model = new DefaultTableModel();
    table = new JTable(model);
    table.setShowGrid(true);
    table.setGridColor(new Color(77, 77, 77));
    table.setFont(getJetBrainsFont(FontStyle.REGULAR, 15));
    table.setForeground(Color.WHITE);
    table.setBackground(new Color(60, 63, 65));
    table.setSelectionBackground(new Color(96, 99, 102));
    table.setSelectionForeground(Color.WHITE);
    table.setRowHeight(22);
    table.setIntercellSpacing(new Dimension(6, 6));
    table.setFillsViewportHeight(true);

    JTableHeader header = table.getTableHeader();
    header.setBackground(new Color(50, 50, 50));
    header.setForeground(Color.WHITE);
    header.setFont(getJetBrainsFont(FontStyle.BOLD, 15));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(77, 77, 77)));
    tablePanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.add(tablePanel, BorderLayout.CENTER);

    JButton saveBtn = new JButton("Save Changes");
    JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    savePanel.setBackground(new Color(40, 40, 40));
    savePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    savePanel.add(saveBtn);
    contentPanel.add(savePanel, BorderLayout.SOUTH);

    layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);

    loadingOverlay = new FadingPanel();
    loadingOverlay.setLayout(new GridBagLayout());
    loadingOverlay.setBackground(new Color(0, 0, 0, 240));
    ((FadingPanel) loadingOverlay).setAlpha(0f);
    loadingOverlay.setVisible(false);

    // Block mouse input to everything behind the overlay
    loadingOverlay.addMouseListener(new MouseAdapter() {});
    loadingOverlay.addMouseMotionListener(new MouseMotionAdapter() {});
    loadingOverlay.setFocusable(true);
    loadingOverlay.requestFocusInWindow();

    loadingLabel = new JLabel("Loading...");
    loadingLabel.setForeground(Color.WHITE);
    loadingLabel.setFont(getJetBrainsFont(FontStyle.BOLD, 40));
    spinnerBar = new JProgressBar();
    spinnerBar.setIndeterminate(true);
    spinnerBar.setPreferredSize(new Dimension(250, 50));
    spinnerBar.setForeground(new Color(166, 0, 0));
    spinnerBar.setBorderPainted(false);
    spinnerBar.setOpaque(false);

    JPanel loadingBox = new JPanel();
    loadingBox.setLayout(new BoxLayout(loadingBox, BoxLayout.Y_AXIS));
    loadingBox.setOpaque(false);

    loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    spinnerBar.setAlignmentX(Component.CENTER_ALIGNMENT);

    loadingBox.add(loadingLabel);
    loadingBox.add(Box.createVerticalStrut(10));
    loadingBox.add(spinnerBar);

    loadingOverlay.add(loadingBox);
    layeredPane.add(loadingOverlay, JLayeredPane.PALETTE_LAYER);

    addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent evt) {
        Dimension size = getSize();

        layeredPane.setBounds(0, 0, size.width, size.height);
        loadingOverlay.setBounds(0, 0, size.width, size.height);
        contentPanel.setBounds(0, 0, size.width, size.height);

        // Force layout update and repaint
        revalidate();
        repaint();

        // Optional: Refresh scroll pane or any dynamic sub-component
        if (scrollPane != null) {
          scrollPane.revalidate();
          scrollPane.repaint();
        }
      }
    });



    connectBtn.addActionListener(e -> runAsync(() -> connectToDatabase(), "Connecting..."));
    loadBtn.addActionListener(e -> runAsync(() -> loadSelectedTable(), "Loading Table..."));
    saveBtn.addActionListener(e -> runAsync(() -> saveChanges(), "Saving Changes..."));
    addRowBtn.addActionListener(e -> addRowToTable());
    deleteRowBtn.addActionListener(e -> deleteSelectedRow());
  }
  private void fadeInOverlay() {
    if (fadeOutTimer != null && fadeOutTimer.isRunning()) {
      fadeOutTimer.stop();
    }
    if (fadeInTimer != null && fadeInTimer.isRunning()) {
      return; // Already fading in
    }

    loadingOverlay.setVisible(true);
    fadeInTimer = new Timer(15, null);
    fadeInTimer.addActionListener(e -> {
      float alpha = ((FadingPanel) loadingOverlay).getAlpha();
      if (alpha < 1f) {
        ((FadingPanel) loadingOverlay).setAlpha(Math.min(1f, alpha + 0.05f));
      } else {
        ((FadingPanel) loadingOverlay).setAlpha(1f);
        fadeInTimer.stop();
      }
    });
    fadeInTimer.start();
  }

  private void fadeOutOverlay() {
    if (fadeInTimer != null && fadeInTimer.isRunning()) {
      fadeInTimer.stop();
    }
    if (fadeOutTimer != null && fadeOutTimer.isRunning()) {
      return; // Already fading out
    }

    fadeOutTimer = new Timer(15, null);
    fadeOutTimer.addActionListener(e -> {
      float alpha = ((FadingPanel) loadingOverlay).getAlpha();
      if (alpha > 0f) {
        ((FadingPanel) loadingOverlay).setAlpha(Math.max(0f, alpha - 0.05f));
      } else {
        ((FadingPanel) loadingOverlay).setAlpha(0f);
        loadingOverlay.setVisible(false);
        fadeOutTimer.stop();
      }
    });
    fadeOutTimer.start();
  }

  private void applyDarkTheme() {
    UIManager.put("Panel.background", new Color(40, 40, 40));
    UIManager.put("Table.background", new Color(60, 63, 65));
    UIManager.put("Table.foreground", Color.WHITE);
    UIManager.put("Table.gridColor", new Color(77, 77, 77));
    UIManager.put("Table.selectionBackground", new Color(96, 99, 102));
    UIManager.put("Table.selectionForeground", Color.WHITE);
    UIManager.put("TableHeader.background", new Color(50, 50, 50));
    UIManager.put("TableHeader.foreground", Color.WHITE);
    UIManager.put("TableHeader.font", getJetBrainsFont(FontStyle.BOLD, 13));
    UIManager.put("Button.background", new Color(55, 55, 55));
    UIManager.put("Button.foreground", Color.WHITE);
    UIManager.put("Button.font", getJetBrainsFont(FontStyle.REGULAR, 13));
    UIManager.put("ComboBox.background", new Color(60, 60, 60));
    UIManager.put("ComboBox.foreground", Color.WHITE);
    UIManager.put("ComboBox.font", getJetBrainsFont(FontStyle.BOLD, 13));
  }

  private void addRowToTable() {
    if (model.getColumnCount() == 0) {
      JOptionPane.showMessageDialog(this, "Please load a table first.");
      return;
    }
    Vector<Object> newRow = new Vector<>();
    for (int i = 0; i < model.getColumnCount(); i++) {
      newRow.add("None");
    }
    model.addRow(newRow);
  }

  private void deleteSelectedRow() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Please select a row to delete.");
      return;
    }
    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected row?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      model.removeRow(selectedRow);
    }
  }

  private void connectToDatabase() {
    DatabaseManager.DatabaseType selected = (DatabaseManager.DatabaseType) databaseSelector.getSelectedItem();
    if (selected == null) return;
    try {
      connection = DatabaseManager.getConnection(selected);
      tableSelector.removeAllItems();

      String catalog = selected.getDbName();
      ResultSet rs = connection.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"});

      boolean first = true;
      while (rs.next()) {
        String tableName = rs.getString("TABLE_NAME");
        tableSelector.addItem(tableName);
        if (first) {
          tableSelector.setSelectedItem(tableName);
          first = false;
        }
      }

      if (tableSelector.getItemCount() > 0) {
        loadSelectedTable();
        SwingUtilities.invokeLater(() -> {
          showStatusOverlay("Connected successfully!", new Color(0x00FF66)); // Green
          Timer timer = new Timer(0, e -> hideLoadingOverlay());
          timer.setRepeats(false);
          timer.start();
        });
      }


    } catch (Exception ex) {
      ex.printStackTrace();

      SwingUtilities.invokeLater(() -> {
        showStatusOverlay("Failed to connect.", new Color(0xFF0000)); // Green
        Timer timer = new Timer(0, e -> hideLoadingOverlay());
        timer.setRepeats(false);
        timer.start();
      });
    }
  }

  private void showStatusOverlay(String message, Color textColor) {
    loadingLabel.setText(message);
    loadingLabel.setFont(getJetBrainsFont(FontStyle.BOLD, 40));
    loadingLabel.setForeground(textColor);
    spinnerBar.setVisible(false); // Hide spinner for success/info messages
    loadingOverlay.setBackground(new Color(0, 0, 0, 240)); // Black with opacity
    loadingOverlay.setBounds(0, 0, getWidth(), getHeight());
    fadeInOverlay();
  }

  private void loadSelectedTable() {
    if (connection == null) return;
    String tableName = (String) tableSelector.getSelectedItem();
    if (tableName == null) return;
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

      ResultSetMetaData meta = rs.getMetaData();
      int colCount = meta.getColumnCount();

      Vector<String> cols = new Vector<>();
      for (int i = 1; i <= colCount; i++) {
        cols.add(meta.getColumnName(i));
      }

      Vector<Vector<Object>> data = new Vector<>();
      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        for (int i = 1; i <= colCount; i++) {
          row.add(rs.getObject(i));
        }
        data.add(row);
      }

      model.setDataVector(data, cols);
      SwingUtilities.invokeLater(() -> {
        showStatusOverlay("Table Loaded!", new Color(0x00FF66)); // Green
        Timer timer = new Timer(0, e -> hideLoadingOverlay());
        timer.setRepeats(false);
        timer.start();
      });

    } catch (Exception ex) {
      ex.printStackTrace();
      SwingUtilities.invokeLater(() -> {
        showStatusOverlay("Failed to load table.", new Color(0xFF0000)); // Green

        Timer timer = new Timer(0, e -> hideLoadingOverlay());
        timer.setRepeats(false);
        timer.start();
      });
    }
  }

  private void saveChanges() {
    if (connection == null) return;
    String tableName = (String) tableSelector.getSelectedItem();
    if (tableName == null) return;

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("DELETE FROM " + tableName);

      int cols = model.getColumnCount();
      for (int row = 0; row < model.getRowCount(); row++) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES(");
        for (int col = 0; col < cols; col++) {
          Object value = model.getValueAt(row, col);
          if (value == null) {
            sql.append("NULL");
          } else if (value instanceof Boolean) {
            sql.append((Boolean) value ? 1 : 0);
          } else if (value instanceof String &&
                     (((String) value).equalsIgnoreCase("true") ||
                      ((String) value).equalsIgnoreCase("false"))) {
            sql.append(((String) value).equalsIgnoreCase("true") ? 1 : 0);
          } else if (value instanceof Number) {
            sql.append(value);
          } else {
            sql.append("'").append(value.toString().replace("'", "''")).append("'");
          }
          if (col < cols - 1) sql.append(", ");
        }
        sql.append(")");
        stmt.executeUpdate(sql.toString());
      }

      // Now show success message (after task finishes)
      SwingUtilities.invokeLater(() -> {
        showStatusOverlay("Changes Saved!", new Color(0x00FF66)); // Green
        Timer timer = new Timer(0, e -> hideLoadingOverlay());
        timer.setRepeats(false);
        timer.start();
      });

    } catch (Exception ex) {
      ex.printStackTrace();
      SwingUtilities.invokeLater(() -> {
        showStatusOverlay("Failed to save changes.", new Color(0xFF0000)); // Green
        Timer timer = new Timer(0, e -> hideLoadingOverlay());
        timer.setRepeats(false);
        timer.start();
      });
    }
  }




  private void showLoadingOverlay(String message) {
    loadingLabel.setText(message);
    loadingLabel.setForeground(new Color(0xFFFFFF));

    boolean showSpinner = message.toLowerCase().contains("loading") ||
                          message.toLowerCase().contains("connecting") ||
                          message.toLowerCase().contains("saving");
    if (spinnerBar != null) {
      spinnerBar.setVisible(showSpinner);
    }
    loadingOverlay.setBounds(0, 0, getWidth(), getHeight());
    fadeInOverlay();
  }

  private void hideLoadingOverlay() {
    fadeOutOverlay();
  }

  private void runAsync(Runnable task, String loadingMessage, boolean autoHide) {
    new Thread(() -> {
      SwingUtilities.invokeLater(() -> showLoadingOverlay(loadingMessage));
      try {
        task.run();
      } finally {
        if (autoHide) {
          SwingUtilities.invokeLater(this::hideLoadingOverlay);
        }
      }
    }).start();
  }

  // convenience version with autoHide = true
  private void runAsync(Runnable task, String loadingMessage) {
    runAsync(task, loadingMessage, true);
  }

}
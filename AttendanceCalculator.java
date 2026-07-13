import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AttendanceCalculator extends JFrame {
    private static final String APP_VERSION = "2.4.0";
    private static final Color COLOR_HEADER = new Color(41, 128, 185);
    private static final Color COLOR_SAFE = new Color(39, 174, 96);
    private static final Color COLOR_WARNING = new Color(241, 196, 15);
    private static final Color COLOR_DANGER = new Color(192, 57, 43);
    private static final Color COLOR_TABLE_HEADER = new Color(236, 240, 241);
    private static final Color COLOR_SELECTION = new Color(189, 195, 199);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MENU = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private JTextField subjectField;
    private JTextField totalClassesField;
    private JTextField attendedClassesField;
    private JTextField requiredPercentageField;
    private DefaultTableModel tableModel;
    private JTable subjectTable;
    private JLabel overallAttendanceLabel;
    private JLabel rowCountLabel;
    private JLabel lastModifiedLabel;
    private JPanel bottomPanel;
    private JProgressBar attendanceBar;
    private JComboBox<String> categoryCombo;
    private JCheckBoxMenuItem autoSaveMenuItem;
    private JCheckBoxMenuItem minimizeToTrayMenuItem;
    private JPanel statusBar;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JLabel statsLabel;
    private boolean autoSave = true;
    private boolean darkMode = false;
    private boolean useMonochromeBar = false;
    private String databaseFile = "attendance_data.csv";
    private Deque<Object[][]> undoStack = new ArrayDeque<>();
    private java.util.Map<String, java.util.List<Double>> attendanceHistory = new java.util.HashMap<>();

    public AttendanceCalculator() {
        setTitle("AC Pro v" + APP_VERSION + " | " + java.time.LocalDate.now());
        setSize(900, 600);
        setMinimumSize(new Dimension(720, 460));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (tableModel.getRowCount() > 0) {
                    int confirm = JOptionPane.showConfirmDialog(null, "Do you want to exit? Unsaved changes may be lost.", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (autoSave) saveDataQuiet();
                        dispose();
                        System.exit(0);
                    }
                } else {
                    dispose();
                    System.exit(0);
                }
            }

            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                if (tableModel.getRowCount() > 0) {
                    setTitle("AC Pro v" + APP_VERSION + " | " + java.time.LocalDate.now() + " | Modified");
                } else {
                    setTitle("AC Pro v" + APP_VERSION + " | " + java.time.LocalDate.now());
                }
            }
        });

        addWindowStateListener(e -> {
            if ((e.getOldState() & java.awt.Frame.ICONIFIED) == 0 && (e.getNewState() & java.awt.Frame.ICONIFIED) != 0) {
                if (minimizeToTrayMenuItem.isSelected() && java.awt.SystemTray.isSupported()) {
                    setVisible(false);
                    JOptionPane.showMessageDialog(null, "App minimized to system tray.\nRight-click tray icon to restore or exit.", "System Tray", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        int hour = java.time.LocalTime.now().getHour();
        String greeting = hour < 11 ? "Good Morning" : hour < 16 ? "Good Afternoon" : "Good Evening";
        JLabel titleLabel = new JLabel(greeting + " - Attendance Calculator Pro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setToolTipText("Shortcuts: Ctrl+S Save | Ctrl+L Load | Ctrl+E Export | Ctrl+P Print | F1 Help | Ctrl+Z Undo");
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(FONT_MENU);
        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.setFont(FONT_MENU);
        saveMenu.addActionListener(e -> saveData());
        JMenuItem saveAsMenu = new JMenuItem("Save As CSV");
        saveAsMenu.setFont(FONT_MENU);
        saveAsMenu.addActionListener(e -> exportCSV());
        JMenuItem loadMenu = new JMenuItem("Load");
        loadMenu.setFont(FONT_MENU);
        loadMenu.addActionListener(e -> loadData());
        JMenuItem exportMenu = new JMenuItem("Export CSV");
        exportMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportMenu.addActionListener(e -> exportCSV());
        JMenuItem importMenu = new JMenuItem("Import CSV");
        importMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        importMenu.addActionListener(e -> importCSV());
        JMenuItem exportHtmlMenu = new JMenuItem("Export HTML");
        exportHtmlMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportHtmlMenu.addActionListener(e -> exportHTML());
        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exitMenu.addActionListener(e -> {
            if (autoSave && tableModel.getRowCount() > 0) saveDataQuiet();
            dispose();
            System.exit(0);
        });
        JMenuItem newMenu = new JMenuItem("New Subject");
        newMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newMenu.addActionListener(e -> {
            subjectField.setText("");
            totalClassesField.setText("");
            attendedClassesField.setText("");
            requiredPercentageField.setText("75");
            subjectField.requestFocus();
        });
        fileMenu.add(newMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveMenu);
        fileMenu.add(saveAsMenu);
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(exportMenu);
        fileMenu.add(importMenu);
        fileMenu.add(exportHtmlMenu);
        JMenuItem exportJsonMenu = new JMenuItem("Export JSON");
        exportJsonMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportJsonMenu.addActionListener(e -> exportJSON());
        fileMenu.add(exportJsonMenu);
        JMenuItem summaryReportMenu = new JMenuItem("Summary Report");
        summaryReportMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryReportMenu.addActionListener(e -> exportSummaryReport());
        fileMenu.add(summaryReportMenu);
        JMenuItem restoreBackupMenu = new JMenuItem("Restore from Backup");
        restoreBackupMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        restoreBackupMenu.addActionListener(e -> restoreFromBackup());
        fileMenu.add(restoreBackupMenu);
        fileMenu.addSeparator();
        JMenuItem resetMenu = new JMenuItem("Reset Fields");
        resetMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resetMenu.addActionListener(e -> {
            subjectField.setText("");
            totalClassesField.setText("");
            attendedClassesField.setText("");
            requiredPercentageField.setText("75");
            subjectField.requestFocus();
        });
        fileMenu.add(resetMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        autoSaveMenuItem = new JCheckBoxMenuItem("Auto-save", autoSave);
        autoSaveMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        autoSaveMenuItem.addActionListener(e -> {
            autoSave = autoSaveMenuItem.isSelected();
            for (java.awt.Component c : statusBar.getComponents()) {
                if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Auto-Save:")) {
                    ((JLabel) c).setText("Auto-Save: " + (autoSave ? "ON" : "OFF"));
                    break;
                }
            }
        });
        viewMenu.add(autoSaveMenuItem);

        JCheckBoxMenuItem darkModeMenuItem = new JCheckBoxMenuItem("Dark Mode", false);
        darkModeMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        darkModeMenuItem.addActionListener(e -> {
            darkMode = darkModeMenuItem.isSelected();
            applyDarkMode(darkMode);
        });
        viewMenu.add(darkModeMenuItem);

        JCheckBoxMenuItem monoBarItem = new JCheckBoxMenuItem("Monochrome Progress Bar", false);
        monoBarItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monoBarItem.addActionListener(e -> {
            useMonochromeBar = monoBarItem.isSelected();
            updateOverallAttendance();
        });
        viewMenu.add(monoBarItem);

        minimizeToTrayMenuItem = new JCheckBoxMenuItem("Minimize to System Tray", false);
        minimizeToTrayMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        viewMenu.add(minimizeToTrayMenuItem);

        JMenuItem weeklyTracker = new JMenuItem("Weekly Summary");
        weeklyTracker.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        weeklyTracker.addActionListener(e -> showWeeklySummary());
        viewMenu.add(weeklyTracker);

        JMenuItem themeColorItem = new JMenuItem("Change Theme Color");
        themeColorItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        themeColorItem.setToolTipText("Customize header and status bar color");
        themeColorItem.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose Theme Color", new Color(52, 152, 219));
            if (chosen != null) {
                headerPanel.setBackground(chosen);
                statusBar.setBackground(chosen);
            }
        });
        viewMenu.add(themeColorItem);

        JMenuItem goalSetter = new JMenuItem("Set Attendance Goal");
        goalSetter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        goalSetter.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter your target attendance percentage:", "90");
            if (input != null) {
                try {
                    double goal = Double.parseDouble(input.trim());
                    if (goal >= 0 && goal <= 100) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== Attendance Goal Tracker ===\n");
                        sb.append("App: v" + APP_VERSION + "\n");
                        sb.append("Target: ").append(String.format("%.0f%%", goal)).append("\n\n");
                        int count = 0, onTrack = 0;
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            String subject = (String) tableModel.getValueAt(i, 0);
                            double pct = Double.parseDouble(((String) tableModel.getValueAt(i, 3)).replace("%", ""));
                            count++;
                            if (pct >= goal) onTrack++;
                            sb.append(String.format("%s: %.1f%% - %s%n", subject, pct, pct >= goal ? "ON TRACK" : "BEHIND"));
                        }
                        sb.append(String.format("\n%d/%d subjects meeting goal", onTrack, count));
                        JOptionPane.showMessageDialog(this, sb.toString(), "Attendance Goal", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid percentage.", "Goal Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        viewMenu.add(goalSetter);

        viewMenu.addSeparator();
        JMenuItem sortByName = new JMenuItem("Sort by Name");
        sortByName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortByName.addActionListener(e -> subjectTable.getRowSorter().setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING))));
        viewMenu.add(sortByName);

        JMenuItem sortByPct = new JMenuItem("Sort by Attendance %");
        sortByPct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortByPct.addActionListener(e -> subjectTable.getRowSorter().setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(3, javax.swing.SortOrder.DESCENDING))));
        viewMenu.add(sortByPct);

        JMenuItem sortByStatus = new JMenuItem("Sort by Status");
        sortByStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortByStatus.addActionListener(e -> subjectTable.getRowSorter().setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(5, javax.swing.SortOrder.ASCENDING))));
        viewMenu.add(sortByStatus);

        JMenuItem sortByCategory = new JMenuItem("Sort by Category");
        sortByCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortByCategory.addActionListener(e -> subjectTable.getRowSorter().setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(7, javax.swing.SortOrder.ASCENDING))));
        viewMenu.add(sortByCategory);

        JCheckBoxMenuItem alwaysOnTopItem = new JCheckBoxMenuItem("Always on Top", false);
        alwaysOnTopItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        alwaysOnTopItem.addActionListener(e -> setAlwaysOnTop(alwaysOnTopItem.isSelected()));
        viewMenu.add(alwaysOnTopItem);

        viewMenu.addSeparator();
        JMenu columnMenu = new JMenu("Show/Hide Columns");
        columnMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (int ci = 0; ci < subjectTable.getColumnCount(); ci++) {
            final int colIndex = ci;
            String colName = subjectTable.getColumnModel().getColumn(ci).getHeaderValue().toString();
            JCheckBoxMenuItem colItem = new JCheckBoxMenuItem(colName, true);
            colItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            colItem.addActionListener(ev -> {
                if (colItem.isSelected()) {
                    subjectTable.getColumnModel().getColumn(colIndex).setMinWidth(15);
                    subjectTable.getColumnModel().getColumn(colIndex).setMaxWidth(500);
                    subjectTable.getColumnModel().getColumn(colIndex).setPreferredWidth(100);
                } else {
                    subjectTable.getColumnModel().getColumn(colIndex).setMinWidth(0);
                    subjectTable.getColumnModel().getColumn(colIndex).setMaxWidth(0);
                    subjectTable.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
                }
            });
            columnMenu.add(colItem);
        }
        viewMenu.add(columnMenu);
        viewMenu.addSeparator();
        JMenuItem statsItem = new JMenuItem("Show Statistics Chart");
        statsItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsItem.addActionListener(e -> showStatisticsChart());
        viewMenu.add(statsItem);

        viewMenu.addSeparator();
        JMenuItem refreshItem = new JMenuItem("Refresh Table");
        refreshItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshItem.addActionListener(e -> updateOverallAttendance());
        viewMenu.add(refreshItem);

        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "prevField");
        rootPane.getActionMap().put("prevField", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focused == totalClassesField) subjectField.requestFocus();
                else if (focused == attendedClassesField) totalClassesField.requestFocus();
                else if (focused == requiredPercentageField) attendedClassesField.requestFocus();
            }
        });
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"), "nextField");
        rootPane.getActionMap().put("nextField", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focused == subjectField) totalClassesField.requestFocus();
                else if (focused == totalClassesField) attendedClassesField.requestFocus();
                else if (focused == attendedClassesField) requiredPercentageField.requestFocus();
                else if (focused == requiredPercentageField) calculateAndAdd();
            }
        });


        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JMenuItem aboutMenu = new JMenuItem("About");
        aboutMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aboutMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "AC Pro v" + APP_VERSION + "\nMade by LOQ\n\nJava Swing Application\nBuilt for Students\n\nCreated: 2024 | Updated: " + java.time.Year.now(), "About", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem helpContentMenu = new JMenuItem("Help");
        helpContentMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        helpContentMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "Keyboard Shortcuts:\n"
                + "Ctrl+S - Save | Ctrl+L - Load | Ctrl+E - Export | Ctrl+A - Select All\n"
                + "Ctrl+Z - Undo | Ctrl+D - Duplicate | Ctrl+R - Reset | Ctrl+P - Print\n"
                + "Ctrl+N - New Subject | Delete - Remove Row | F1 - Help\n"
                + "Enter - Calculate | Click table headers to sort | Use Search field to filter\n\n"
                + "How to use:\n"
                + "1. Enter subject name, total classes, attended classes, and required percentage.\n"
                + "2. Click Calculate (or press Enter) to add to the table.\n"
                + "3. Status column shows if you're safe or need more classes.\n"
                + "4. Use File menu to save/load or export/import data.\n"
                + "5. Toggle Dark Mode from View menu.\n"
                + "6. Use Search field to filter subjects in the table.", "Help", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(helpContentMenu);
        helpMenu.add(aboutMenu);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Keyboard shortcuts
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("control S"), "save");
        inputMap.put(KeyStroke.getKeyStroke("control shift S"), "saveAs");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveData();
            }
        });
        actionMap.put("saveAs", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportCSV();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control L"), "load");
        actionMap.put("load", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadData();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control A"), "selectAll");
        actionMap.put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectTable.selectAll();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control E"), "export");
        inputMap.put(KeyStroke.getKeyStroke("control shift E"), "exportSummary");
        inputMap.put(KeyStroke.getKeyStroke("control H"), "exportHtml");
        actionMap.put("export", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportCSV();
            }
        });
        actionMap.put("exportSummary", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportSummaryReport();
            }
        });
        actionMap.put("exportHtml", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportHTML();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control R"), "resetFields");
        actionMap.put("resetFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectField.setText("");
                totalClassesField.setText("");
                attendedClassesField.setText("");
                requiredPercentageField.setText("75");
                subjectField.requestFocus();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteRow");
        inputMap.put(KeyStroke.getKeyStroke("control UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("control DOWN"), "moveDown");
        actionMap.put("deleteRow", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                    tableModel.removeRow(modelRow);
                    updateOverallAttendance();
                }
            }
        });
        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow <= 0) return;
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                saveUndoState();
                Object[] rowData = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) rowData[j] = tableModel.getValueAt(modelRow, j);
                tableModel.removeRow(modelRow);
                tableModel.insertRow(modelRow - 1, rowData);
                subjectTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                updateOverallAttendance();
            }
        });
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow < 0 || selectedRow >= subjectTable.getRowCount() - 1) return;
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                saveUndoState();
                Object[] rowData = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) rowData[j] = tableModel.getValueAt(modelRow, j);
                tableModel.removeRow(modelRow);
                tableModel.insertRow(modelRow + 1, rowData);
                subjectTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
                updateOverallAttendance();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
        inputMap.put(KeyStroke.getKeyStroke("control shift UP"), "moveTop");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                undoLastAction();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control shift DOWN"), "moveBottom");
        actionMap.put("moveTop", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                moveRowToTopOrBottom(true);
            }
        });
        actionMap.put("moveBottom", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                moveRowToTopOrBottom(false);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control D"), "duplicateRow");
        actionMap.put("duplicateRow", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                    Object[] rowData = new Object[tableModel.getColumnCount()];
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        rowData[j] = tableModel.getValueAt(modelRow, j);
                    }
                    rowData[0] = rowData[0] + " (Copy)";
                    saveUndoState();
                    tableModel.addRow(rowData);
                    updateOverallAttendance();
                }
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("F1"), "showHelp");
        actionMap.put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Keyboard Shortcuts:\n"
                        + "Ctrl+S - Save | Ctrl+L - Load | Ctrl+E - Export | Ctrl+A - Select All\n"
                        + "Ctrl+Z - Undo | Ctrl+D - Duplicate | Ctrl+R - Reset | Ctrl+P - Print\n"
                        + "Ctrl+N - New Subject | Delete - Remove Row | F1 - Help\n"
                        + "Enter - Calculate | Click table headers to sort | Use Search field to filter\n\n"
                        + "How to use:\n"
                        + "1. Enter subject name, total classes, attended classes, and required percentage.\n"
                        + "2. Click Calculate (or press Enter) to add to the table.\n"
                        + "3. Status column shows if you're safe or need more classes.\n"
                        + "4. Use File menu to save/load or export/import data.\n"
                        + "5. Toggle Dark Mode from View menu.\n"
                        + "6. Use Search field to filter subjects in the table.", "Help - Attendance Calculator Pro v" + APP_VERSION, JOptionPane.INFORMATION_MESSAGE);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control N"), "newSubject");
        actionMap.put("newSubject", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectField.setText("");
                totalClassesField.setText("");
                attendedClassesField.setText("");
                requiredPercentageField.setText("75");
                subjectField.requestFocus();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control P"), "printTable");
        inputMap.put(KeyStroke.getKeyStroke("F5"), "refreshTable");
        inputMap.put(KeyStroke.getKeyStroke("F2"), "renameSubject");
        actionMap.put("printTable", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printAttendanceTable();
            }
        });
        actionMap.put("refreshTable", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateOverallAttendance();
            }
        });
        actionMap.put("renameSubject", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow == -1) return;
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                String currentName = (String) tableModel.getValueAt(modelRow, 0);
                String newName = JOptionPane.showInputDialog(AttendanceCalculator.this, "Rename subject:", currentName);
                if (newName != null && !newName.trim().isEmpty()) {
                    tableModel.setValueAt(newName.trim(), modelRow, 0);
                }
            }
        });

        // Top Panel for Inputs
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "New Subject", 0, 0, new Font("Segoe UI", Font.BOLD, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        JLabel subLabel = new JLabel("Subject Name:");
        subLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(subLabel, gbc);
        subjectField = new JTextField(15);
        subjectField.setFont(fieldFont);
        subjectField.setToolTipText("Subject name (max 40 chars)");
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(subjectField, gbc);

        JLabel totLabel = new JLabel("Total Classes:");
        totLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 0; inputPanel.add(totLabel, gbc);
        totalClassesField = new JTextField(10);
        totalClassesField.setFont(fieldFont);
        totalClassesField.setToolTipText("Total number of classes held (must be >= attended)");
        gbc.gridx = 3; gbc.gridy = 0; inputPanel.add(totalClassesField, gbc);

        JLabel attLabel = new JLabel("Attended Classes:");
        attLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(attLabel, gbc);
        attendedClassesField = new JTextField(15);
        attendedClassesField.setFont(fieldFont);
        attendedClassesField.setToolTipText("Number of classes you attended");
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(attendedClassesField, gbc);

        JLabel reqLabel = new JLabel("Required %:");
        reqLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(reqLabel, gbc);
        requiredPercentageField = new JTextField("75", 10);
        requiredPercentageField.setFont(fieldFont);
        requiredPercentageField.setToolTipText("Minimum attendance percentage required (default: 75%)");
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(requiredPercentageField, gbc);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(catLabel, gbc);
        String[] categories = {"Theory", "Lab", "Core", "Elective", "Other"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(fieldFont);
        categoryCombo.setToolTipText("Select subject category for grouping (Alt+1-5)");
        categoryCombo.setSelectedIndex(0);
        for (int k = 1; k <= 5; k++) {
            final int index = k - 1;
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt " + k), "cat" + k);
            rootPane.getActionMap().put("cat" + k, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (index < categoryCombo.getItemCount()) {
                        categoryCombo.setSelectedIndex(index);
                    }
                }
            });
        }
        gbc.gridx = 1; gbc.gridy = 2; inputPanel.add(categoryCombo, gbc);

        // Enter key triggers calculate
        java.awt.event.KeyListener enterKeyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    calculateAndAdd();
                }
            }
        };
        subjectField.addKeyListener(enterKeyListener);
        totalClassesField.addKeyListener(enterKeyListener);
        attendedClassesField.addKeyListener(enterKeyListener);
        requiredPercentageField.addKeyListener(enterKeyListener);

        JButton calculateButton = new JButton("Add Subject");
        calculateButton.setBackground(new Color(39, 174, 96));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        calculateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calculateButton.setToolTipText("Calculate attendance and add subject to table (Enter key also works in any input field)");
        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2; 
        gbc.insets = new Insets(15, 5, 5, 5);
        inputPanel.add(calculateButton, gbc);

        // Center Panel for Table
        String[] columns = {"Subject", "Total", "Attended", "Current %", "Required %", "Status / Needed", "Trend", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2;
            }
        };
        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(32);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subjectTable.setRowSorter(new javax.swing.table.TableRowSorter<>(tableModel));
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectTable.getTableHeader().setBackground(COLOR_TABLE_HEADER);
        subjectTable.getTableHeader().setToolTipText("<html><b>Subject</b> - Name<br><b>Total</b> - Classes held<br><b>Attended</b> - Classes attended<br><b>Current %</b> - Current %<br><b>Required %</b> - Required %<br><b>Status</b> - Safe/Alert<br><b>Trend</b> - UP/DOWN<br><b>Category</b> - Type</html>");

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    try {
                        int total = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
                        int attended = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
                        if (total > 0 && attended >= 0 && attended <= total) {
                            double pct = ((double) attended / total) * 100;
                            tableModel.setValueAt(String.format("%.2f%%", pct), row, 3);
                            double req = Double.parseDouble(tableModel.getValueAt(row, 4).toString().replace("%", ""));
                            String status;
                            if (pct >= req) {
                                int canMiss = (int) Math.floor(((double) attended * 100 / req) - total);
                                status = canMiss > 0 ? "Good! Can skip " + canMiss + " classes." : "On track.";
                            } else {
                                double r = req / 100.0;
                                int needed = (int) Math.ceil((r * total - attended) / (1 - r));
                                status = "Warning! Need " + needed + " more.";
                            }
                            tableModel.setValueAt(status, row, 5);
                            updateOverallAttendance();
                        }
                    } catch (Exception ex) {
                        System.err.println("Table model update error at row " + row + ": " + ex.getMessage());
                    }
                }
            }
        });
        subjectTable.setSelectionBackground(COLOR_SELECTION);
        subjectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        subjectTable.setShowGrid(true);
        subjectTable.setIntercellSpacing(new Dimension(1, 1));
        subjectTable.setToolTipText("Double-click cell to edit (Subject, Total, Attended)");
        subjectTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = subjectTable.rowAtPoint(e.getPoint());
                int col = subjectTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    subjectTable.setToolTipText("<html><b>" + tableModel.getColumnName(col) + ":</b> " + tableModel.getValueAt(subjectTable.convertRowIndexToModel(row), col) + "</html>");
                }
            }
        });

        // Custom renderer for row colors based on attendance percentage
        subjectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String currentPctStr = (String) table.getModel().getValueAt(row, 3);
                    try {
                        double pct = Double.parseDouble(currentPctStr.replace("%", ""));
                        if (pct >= 90) {
                            c.setBackground(new Color(200, 240, 200)); // Dark green - Excellent
                        } else if (pct >= 75) {
                            c.setBackground(new Color(230, 255, 230)); // Light green - Good
                        } else if (pct >= 60) {
                            c.setBackground(new Color(255, 255, 200)); // Light yellow - Warning
                        } else if (pct >= 50) {
                            c.setBackground(new Color(255, 220, 180)); // Light orange - Danger
                        } else {
                            c.setBackground(new Color(255, 200, 200)); // Light red - Critical
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Subjects List", 0, 0, new Font("Segoe UI", Font.BOLD, 12)));

        JPopupMenu tableContextMenu = new JPopupMenu();
        JMenuItem ctxRename = new JMenuItem("Rename Subject");
        ctxRename.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxRename.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a row to rename.", "Rename", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
            String currentName = (String) tableModel.getValueAt(modelRow, 0);
            String newName = JOptionPane.showInputDialog(this, "Enter new subject name:", currentName);
            if (newName != null && !newName.trim().isEmpty()) {
                tableModel.setValueAt(newName.trim(), modelRow, 0);
            }
        });
        JMenuItem ctxDelete = new JMenuItem("Delete Selected Row");
        ctxDelete.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxDelete.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                tableModel.removeRow(modelRow);
                updateOverallAttendance();
            }
        });
        JMenuItem ctxClearAll = new JMenuItem("Clear All Rows");
        ctxClearAll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxClearAll.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all rows? This cannot be undone.", "Clear All Rows", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                saveUndoState();
                tableModel.setRowCount(0);
                updateOverallAttendance();
            }
        });
        JMenuItem ctxExport = new JMenuItem("Export CSV");
        ctxExport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxExport.addActionListener(e -> exportCSV());
        JMenuItem ctxCopy = new JMenuItem("Copy Table Data");
        ctxCopy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxCopy.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                sb.append(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) sb.append("\t");
            }
            sb.append("\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    sb.append(tableModel.getValueAt(i, j));
                    if (j < tableModel.getColumnCount() - 1) sb.append("\t");
                }
                sb.append("\n");
            }
            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(sb.toString());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            JOptionPane.showMessageDialog(null, "Table data copied to clipboard!", "Copy", JOptionPane.INFORMATION_MESSAGE);
        });
        tableContextMenu.add(ctxRename);
        tableContextMenu.add(ctxDelete);
        tableContextMenu.add(ctxClearAll);
        JMenuItem ctxSelectAll = new JMenuItem("Select All Rows");
        ctxSelectAll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxSelectAll.addActionListener(e -> subjectTable.selectAll());
        tableContextMenu.add(ctxSelectAll);
        JMenuItem ctxDeselectAll = new JMenuItem("Deselect All");
        ctxDeselectAll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxDeselectAll.addActionListener(e -> subjectTable.clearSelection());
        tableContextMenu.add(ctxDeselectAll);
        JMenuItem ctxMoveTop = new JMenuItem("Move to Top");
        ctxMoveTop.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxMoveTop.addActionListener(e -> moveRowToTopOrBottom(true));
        tableContextMenu.add(ctxMoveTop);
        JMenuItem ctxMoveBottom = new JMenuItem("Move to Bottom");
        ctxMoveBottom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxMoveBottom.addActionListener(e -> moveRowToTopOrBottom(false));
        tableContextMenu.add(ctxMoveBottom);
        JMenuItem ctxCopyCell = new JMenuItem("Copy Cell Value");
        ctxCopyCell.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxCopyCell.addActionListener(e -> {
            int row = subjectTable.getSelectedRow();
            int col = subjectTable.getSelectedColumn();
            if (row >= 0 && col >= 0) {
                Object val = tableModel.getValueAt(subjectTable.convertRowIndexToModel(row), col);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(val.toString()), null);
            }
        });
        tableContextMenu.add(ctxCopyCell);
        tableContextMenu.addSeparator();
        tableContextMenu.add(ctxExport);
        tableContextMenu.add(ctxCopy);
        JMenuItem ctxPrint = new JMenuItem("Print Table");
        ctxPrint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxPrint.addActionListener(e -> printAttendanceTable());
        tableContextMenu.add(ctxPrint);
        subjectTable.setComponentPopupMenu(tableContextMenu);

        // Filter/search panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        filterPanel.add(new JLabel("Filter:"));
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Type to filter subjects in the table");
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Category:"));
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All", "Theory", "Lab", "Core", "Elective", "Other"});
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Safe", "At Risk"});
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.setToolTipText("Filter by subject category");
        filterPanel.add(categoryFilter);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setToolTipText("Filter by attendance status");
        filterPanel.add(statusFilter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                applyFilters(searchField.getText(), categoryFilter.getSelectedIndex(), statusFilter.getSelectedIndex());
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    searchField.setText("");
                    categoryFilter.setSelectedIndex(0);
                    statusFilter.setSelectedIndex(0);
                    subjectField.requestFocus();
                }
            }
        });
        categoryFilter.addActionListener(e -> applyFilters(searchField.getText(), categoryFilter.getSelectedIndex(), statusFilter.getSelectedIndex()));
        statusFilter.addActionListener(e -> applyFilters(searchField.getText(), categoryFilter.getSelectedIndex(), statusFilter.getSelectedIndex()));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(filterPanel, BorderLayout.NORTH);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for actions and summary
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(new Color(200, 50, 50));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setToolTipText("Delete selected row(s) from table (Delete key, supports multi-select)");
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setToolTipText("Clear all subjects from the table");

        JButton predictButton = new JButton("Forecast");
        predictButton.setBackground(new Color(142, 68, 173));
        predictButton.setForeground(Color.WHITE);
        predictButton.setFocusPainted(false);
        predictButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        predictButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        predictButton.setToolTipText("Predict attendance after N more classes");

        moveUpButton = new JButton("Move Up");
        moveUpButton.setBackground(new Color(52, 152, 219));
        moveUpButton.setForeground(Color.WHITE);
        moveUpButton.setFocusPainted(false);
        moveUpButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        moveUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moveUpButton.setToolTipText("Move selected row up");

        moveDownButton = new JButton("Move Down");
        moveDownButton.setBackground(new Color(52, 152, 219));
        moveDownButton.setForeground(Color.WHITE);
        moveDownButton.setFocusPainted(false);
        moveDownButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        moveDownButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moveDownButton.setToolTipText("Move selected row down");

        actionPanel.add(deleteButton);
        actionPanel.add(clearButton);
        actionPanel.add(predictButton);
        actionPanel.add(moveUpButton);
        actionPanel.add(moveDownButton);
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectAllBtn.addActionListener(e -> subjectTable.selectAll());
        actionPanel.add(selectAllBtn);
        JButton deselectAllBtn = new JButton("Deselect All");
        deselectAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deselectAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deselectAllBtn.addActionListener(e -> subjectTable.clearSelection());
        actionPanel.add(deselectAllBtn);

        JButton customPctButton = new JButton("Set Custom %");
        customPctButton.setBackground(new Color(211, 84, 0));
        customPctButton.setForeground(Color.WHITE);
        customPctButton.setFocusPainted(false);
        customPctButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        customPctButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customPctButton.setToolTipText("Set custom required percentage for selected subject (e.g., 80, 85, 90)");
        actionPanel.add(customPctButton);

        overallAttendanceLabel = new JLabel("Overall Attendance: 0.00% (0 / 0)");
        overallAttendanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overallAttendanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        attendanceBar = new JProgressBar(0, 100);
        attendanceBar.setValue(0);
        attendanceBar.setStringPainted(true);
        attendanceBar.setString("Empty");
        attendanceBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        attendanceBar.setPreferredSize(new Dimension(180, 22));
        attendanceBar.setForeground(new Color(39, 174, 96));

        statsLabel = new JLabel("Rows: 0 | Max: 0% | Min: 0% | Avg: 0%");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel summaryPanel = new JPanel(new BorderLayout(5, 5));
        JPanel topSummary = new JPanel(new BorderLayout());
        topSummary.add(overallAttendanceLabel, BorderLayout.NORTH);
        topSummary.add(attendanceBar, BorderLayout.SOUTH);
        summaryPanel.add(topSummary, BorderLayout.NORTH);
        summaryPanel.add(statsLabel, BorderLayout.SOUTH);

        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(summaryPanel, BorderLayout.EAST);

        // Main Layout wrapper
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContent.add(inputPanel, BorderLayout.NORTH);
        mainContent.add(tableWrapper, BorderLayout.CENTER);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        // Status bar
        statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        statusBar.setBackground(new Color(52, 152, 219));
        JLabel versionLabel = new JLabel("Ver. " + APP_VERSION);
        versionLabel.setForeground(Color.WHITE);
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBar.add(versionLabel);
        rowCountLabel = new JLabel("Rows: 0");
        rowCountLabel.setForeground(Color.WHITE);
        rowCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(rowCountLabel);
        lastModifiedLabel = new JLabel("Last Modified: Never");
        lastModifiedLabel.setForeground(Color.WHITE);
        lastModifiedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(lastModifiedLabel);
        JLabel backupLabel = new JLabel("Backup: N/A");
        backupLabel.setForeground(Color.WHITE);
        backupLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backupLabel.setToolTipText("Last backup file timestamp");
        statusBar.add(backupLabel);
        JLabel statusTimeLabel = new JLabel();
        statusTimeLabel.setForeground(Color.WHITE);
        statusTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(statusTimeLabel);
        JLabel undoSizeLabel = new JLabel("Undo Stack: 0");
        undoSizeLabel.setForeground(Color.WHITE);
        undoSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        undoSizeLabel.setToolTipText("Number of undo steps available");
        statusBar.add(undoSizeLabel);
        JLabel autoSaveStatus = new JLabel("AutoSave: ON");
        autoSaveStatus.setForeground(Color.WHITE);
        autoSaveStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        autoSaveStatus.setToolTipText("Auto-save status");
        statusBar.add(autoSaveStatus);
        add(statusBar, BorderLayout.SOUTH);

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> {
            statusTimeLabel.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + " | " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        clockTimer.start();
        statusTimeLabel.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + " | " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Action Listeners
        calculateButton.addActionListener(e -> calculateAndAdd());
        
        deleteButton.addActionListener(e -> {
            int[] selectedRows = subjectTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one row to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + selectedRows.length + " row(s)?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saveUndoState();
                int[] modelRows = new int[selectedRows.length];
                for (int i = 0; i < selectedRows.length; i++) {
                    modelRows[i] = subjectTable.convertRowIndexToModel(selectedRows[i]);
                }
                java.util.Arrays.sort(modelRows);
                for (int i = modelRows.length - 1; i >= 0; i--) {
                    tableModel.removeRow(modelRows[i]);
                }
                updateOverallAttendance();
            }
        });

        clearButton.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all data?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.setRowCount(0);
                updateOverallAttendance();
            }
        });

        predictButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a subject row to predict.", "Predict", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter number of future classes to predict:", "10");
            if (input == null) return;
            try {
                int futureClasses = Integer.parseInt(input.trim());
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                int total = (int) tableModel.getValueAt(modelRow, 1);
                int attended = (int) tableModel.getValueAt(modelRow, 2);
                String subject = (String) tableModel.getValueAt(modelRow, 0);
                double predictedPct = ((double) (attended + futureClasses) / (total + futureClasses)) * 100;
                JOptionPane.showMessageDialog(this, String.format(
                    "Prediction for %s after %d more classes (attending all):\n\nCurrent: %d/%d (%.2f%%)\nPredicted: %d/%d (%.2f%%)",
                    subject, futureClasses, attended, total, ((double)attended/total)*100,
                    attended + futureClasses, total + futureClasses, predictedPct),
                    "Attendance Prediction", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Predict Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        customPctButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a subject row.", "Custom %", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter custom required percentage:", "75");
            if (input == null) return;
            try {
                double customPct = Double.parseDouble(input.trim());
                if (customPct < 0 || customPct > 100) {
                    JOptionPane.showMessageDialog(this, "Percentage must be between 0 and 100.", "Custom % Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                int total = (int) tableModel.getValueAt(modelRow, 1);
                int attended = (int) tableModel.getValueAt(modelRow, 2);
                double currentPct = ((double) attended / total) * 100;
                String status;
                if (currentPct >= customPct) {
                    int canMiss = (int) Math.floor(((double) attended * 100 / customPct) - total);
                    status = canMiss > 0 ? "Safe! You can miss " + canMiss + " classes." : "On track.";
                } else {
                    double r = customPct / 100.0;
                    double needed = (r * total - attended) / (1 - r);
                    int neededClasses = (int) Math.ceil(needed);
                    status = "Warning! Need " + neededClasses + " more classes.";
                }
                tableModel.setValueAt(String.format("%.0f%%", customPct), modelRow, 4);
                tableModel.setValueAt(status, modelRow, 5);
                updateOverallAttendance();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Custom % Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        moveUpButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow <= 0) return;
            int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
            saveUndoState();
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                rowData[j] = tableModel.getValueAt(modelRow, j);
            }
            tableModel.removeRow(modelRow);
            tableModel.insertRow(modelRow - 1, rowData);
            subjectTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            updateOverallAttendance();
        });
        moveDownButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow < 0 || selectedRow >= subjectTable.getRowCount() - 1) return;
            int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
            saveUndoState();
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                rowData[j] = tableModel.getValueAt(modelRow, j);
            }
            tableModel.removeRow(modelRow);
            tableModel.insertRow(modelRow + 1, rowData);
            subjectTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
            updateOverallAttendance();
        });
    }

    private void moveRowToTopOrBottom(boolean toTop) {
        int selectedRow = subjectTable.getSelectedRow();
        if (selectedRow < 0) return;
        int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
        saveUndoState();
        Object[] rowData = new Object[tableModel.getColumnCount()];
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            rowData[j] = tableModel.getValueAt(modelRow, j);
        }
        tableModel.removeRow(modelRow);
        int targetRow = toTop ? 0 : tableModel.getRowCount();
        tableModel.insertRow(targetRow, rowData);
        int newViewRow = subjectTable.convertRowIndexToView(targetRow);
        subjectTable.setRowSelectionInterval(newViewRow, newViewRow);
        updateOverallAttendance();
    }

    private void applyDarkMode(boolean dark) {
        Color bg = dark ? new Color(43, 43, 43) : UIManager.getColor("Panel.background");
        Color fg = dark ? Color.WHITE : Color.BLACK;
        Color tableBg = dark ? new Color(55, 55, 55) : Color.WHITE;
        Color tableFg = dark ? new Color(200, 200, 200) : Color.BLACK;
        Color headerBg = dark ? new Color(60, 60, 60) : new Color(236, 240, 241);

        getContentPane().setBackground(bg);
        for (Component c : getContentPane().getComponents()) {
            c.setBackground(bg);
            c.setForeground(fg);
            if (c instanceof JPanel) {
                for (Component inner : ((JPanel) c).getComponents()) {
                    inner.setBackground(bg);
                    inner.setForeground(fg);
                }
            }
        }
        rowCountLabel.setText("Subjects: " + tableModel.getRowCount());
        subjectTable.setBackground(tableBg);
        subjectTable.setForeground(tableFg);
        subjectTable.getTableHeader().setBackground(headerBg);
        subjectTable.getTableHeader().setForeground(fg);
        subjectTable.setGridColor(dark ? new Color(70, 70, 70) : new Color(200, 200, 200));
        overallAttendanceLabel.setForeground(fg);
        for (Component c : statusBar.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(dark ? Color.BLACK : Color.WHITE);
            }
        }
        repaint();
    }

    private void calculateAndAdd() {
        try {
            String subject = subjectField.getText().trim();
            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a subject name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (subject.length() > 40) {
                int confirm = JOptionPane.showConfirmDialog(this, "Subject name is very long (" + subject.length() + " chars). Truncate to 40 characters?", "Long Name", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    subject = subject.substring(0, 40);
                }
            }

            boolean duplicate = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (((String) tableModel.getValueAt(i, 0)).equalsIgnoreCase(subject)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                int confirm = JOptionPane.showConfirmDialog(this, "Subject '" + subject + "' already exists. Add anyway?", "Duplicate Subject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            int total = Integer.parseInt(totalClassesField.getText().trim());
            int attended = Integer.parseInt(attendedClassesField.getText().trim());
            double required = Double.parseDouble(requiredPercentageField.getText().trim());

            if (total <= 0) {
                JOptionPane.showMessageDialog(this, "Total classes must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (attended < 0) {
                JOptionPane.showMessageDialog(this, "Attended classes cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (required < 0 || required > 100) {
                JOptionPane.showMessageDialog(this, "Required percentage must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (attended > total) {
                JOptionPane.showMessageDialog(this, "Attended classes cannot be greater than total classes.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double currentPercentage = ((double) attended / total) * 100;
            String currentPercentageStr = String.format("%.2f%%", currentPercentage);
            String status = "";

            if (currentPercentage >= required) {
                int canMiss = (int) Math.floor(((double) attended * 100 / required) - total);
                if (canMiss > 0) {
                    status = "Safe! You can miss " + canMiss + " upcoming classes.";
                } else {
                    status = "On track. Cannot miss the next class.";
                }
            } else {
                double r = required / 100.0;
                double needed = (r * total - attended) / (1 - r);
                int neededClasses = (int) Math.ceil(needed);
                status = "Alert! Need to attend " + neededClasses + " more classes.";
            }

            String trend;
            java.util.List<Double> history = attendanceHistory.getOrDefault(subject, new ArrayList<>());
            if (history.size() >= 2) {
                double lastPct = history.get(history.size() - 1);
                if (currentPercentage > lastPct) trend = "UP";
                else if (currentPercentage < lastPct) trend = "DOWN";
                else trend = "STABLE";
            } else {
                trend = "NEW";
            }
            history.add(currentPercentage);
            attendanceHistory.put(subject, history);

            Object[] row = {
                    subject,
                    total,
                    attended,
                    currentPercentageStr,
                    String.format("%.0f%%", required),
                    status,
                    trend,
                    categoryCombo.getSelectedItem()
            };
            saveUndoState();
            tableModel.addRow(row);
            updateOverallAttendance();

            subjectField.setText("");
            totalClassesField.setText("");
            attendedClassesField.setText("");
            subjectField.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for classes/days and percentage.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusBarColor() {
        int totalClassesAll = 0;
        int totalAttendedAll = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalClassesAll += (int) tableModel.getValueAt(i, 1);
            totalAttendedAll += (int) tableModel.getValueAt(i, 2);
        }
        if (totalClassesAll > 0) {
            double overallPercent = ((double) totalAttendedAll / totalClassesAll) * 100;
            if (overallPercent >= 75) {
                statusBar.setBackground(new Color(39, 174, 96));
            } else if (overallPercent >= 60) {
                statusBar.setBackground(new Color(241, 196, 15));
            } else {
                statusBar.setBackground(new Color(192, 57, 43));
            }
        } else {
        statusBar.setBackground(COLOR_HEADER);
        }
    }

    private void updateOverallAttendanceTooltip() {
        int totalClassesAll = 0, totalAttendedAll = 0, above75 = 0, totalSubjects = tableModel.getRowCount();
        for (int i = 0; i < totalSubjects; i++) {
            totalClassesAll += (int) tableModel.getValueAt(i, 1);
            totalAttendedAll += (int) tableModel.getValueAt(i, 2);
            double pct = ((double) (int) tableModel.getValueAt(i, 2) / (int) tableModel.getValueAt(i, 1)) * 100;
            if (pct >= 75) above75++;
        }
        double overallPct = totalClassesAll > 0 ? ((double) totalAttendedAll / totalClassesAll) * 100 : 0;
        overallAttendanceLabel.setToolTipText(String.format("<html>Subjects: %d<br>Safe (>=75%%): %d<br>At Risk: %d<br>Overall: %.2f%%</html>",
                totalSubjects, above75, totalSubjects - above75, overallPct));
    }

    private void updateOverallAttendance() {
        int totalClassesAll = 0;
        int totalAttendedAll = 0;
        
        double highestPct = 0, lowestPct = 100, totalPct = 0;
        int rowCount = tableModel.getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            try {
                totalClassesAll += (int) tableModel.getValueAt(i, 1);
                totalAttendedAll += (int) tableModel.getValueAt(i, 2);
                double pct = ((double) (int) tableModel.getValueAt(i, 2) / (int) tableModel.getValueAt(i, 1)) * 100;
                if (pct > highestPct) highestPct = pct;
                if (pct < lowestPct) lowestPct = pct;
                totalPct += pct;
            } catch (Exception ex) {
                System.err.println("Error calculating attendance for row " + i + ": " + ex.getMessage());
            }
        }

        if (totalClassesAll == 0) {
            overallAttendanceLabel.setText("No subjects. Add entries to view summary.");
            overallAttendanceLabel.setForeground(Color.BLACK);
            if (statsLabel != null) statsLabel.setText("Subjects: 0 | Highest: 0% | Lowest: 0% | Avg: 0%");
            attendanceBar.setValue(0);
            attendanceBar.setString("Empty");
        } else {
            double overallPercent = ((double) totalAttendedAll / totalClassesAll) * 100;
            overallAttendanceLabel.setText(String.format("Overall Attendance: %.2f%% (%d / %d)", overallPercent, totalAttendedAll, totalClassesAll));
            attendanceBar.setValue((int) overallPercent);
            String statusText = overallPercent >= 75 ? "SAFE" : overallPercent >= 60 ? "WARNING" : "CRITICAL";
            attendanceBar.setString(String.format("%.1f%% - %s", overallPercent, statusText));
            double avgPct = totalPct / rowCount;
            if (statsLabel != null) {
                statsLabel.setText(String.format("Subjects: %d | Highest: %.2f%% | Lowest: %.2f%% | Avg: %.2f%%", rowCount, highestPct, lowestPct, avgPct));
            }
            
            double required = 75.0;
            try {
                required = Double.parseDouble(requiredPercentageField.getText().trim());
            } catch (NumberFormatException ex) {
                required = 75.0;
            }

            if (overallPercent < required) {
                overallAttendanceLabel.setForeground(new Color(192, 57, 43));
                attendanceBar.setForeground(useMonochromeBar ? new Color(52, 152, 219) : new Color(192, 57, 43));
            } else {
                overallAttendanceLabel.setForeground(new Color(39, 174, 96));
                attendanceBar.setForeground(useMonochromeBar ? new Color(52, 152, 219) : new Color(39, 174, 96));
            }
        }
        rowCountLabel.setText("Subjects: " + tableModel.getRowCount());
        updateStatusBarColor();
        updateOverallAttendanceTooltip();
    }

    private void printAttendanceTable() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to print.", "Print", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            subjectTable.print(JTable.PrintMode.FIT_WIDTH);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStatisticsChart() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stats data.", "Statistics", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFrame chartFrame = new JFrame("Statistics Chart - v" + APP_VERSION);
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(this);

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int rows = tableModel.getRowCount();
                int barWidth = Math.max(30, (getWidth() - 80) / rows);
                int maxHeight = getHeight() - 80;
                for (int i = 0; i < rows; i++) {
                    String name = (String) tableModel.getValueAt(i, 0);
                    if (name.length() > 8) name = name.substring(0, 8) + "..";
                    double pct = Double.parseDouble(((String) tableModel.getValueAt(i, 3)).replace("%", ""));
                    int barHeight = (int) (pct / 100.0 * maxHeight);
                    int x = 50 + i * barWidth;
                    int y = getHeight() - 40 - barHeight;
                    if (pct >= 75) g2d.setColor(new Color(39, 174, 96));
                    else if (pct >= 60) g2d.setColor(new Color(241, 196, 15));
                    else g2d.setColor(new Color(192, 57, 43));
                    g2d.fillRoundRect(x, y, barWidth - 5, barHeight, 4, 4);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    FontMetrics fm = g2d.getFontMetrics();
                    int nameWidth = fm.stringWidth(name);
                    g2d.drawString(name, x + (barWidth - 5 - nameWidth) / 2, getHeight() - 25);
                    g2d.drawString(String.format("%.0f%%", pct), x + (barWidth - 5 - fm.stringWidth(String.format("%.0f%%", pct))) / 2, y - 5);
                }
                g2d.setColor(Color.GRAY);
                g2d.drawLine(45, getHeight() - 40, getWidth() - 10, getHeight() - 40);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartFrame.add(chartPanel, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(Color.WHITE);
        JLabel greenLabel = new JLabel("Safe (>=75%)");
        greenLabel.setForeground(new Color(39, 174, 96));
        greenLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel yellowLabel = new JLabel("Warning (60-74%)");
        yellowLabel.setForeground(new Color(241, 196, 15));
        yellowLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel redLabel = new JLabel("Critical (<60%)");
        redLabel.setForeground(new Color(192, 57, 43));
        redLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        legendPanel.add(greenLabel);
        legendPanel.add(yellowLabel);
        legendPanel.add(redLabel);
        chartFrame.add(legendPanel, BorderLayout.SOUTH);

        chartFrame.setVisible(true);
    }

    private void showWeeklySummary() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No weekly data.", "Weekly Summary", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Weekly Attendance Summary ===\n");
        sb.append("App: Attendance Calculator Pro v" + APP_VERSION + "\n");
        sb.append("Date: ").append(java.time.LocalDate.now()).append("\n\n");
        double totalPct = 0;
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String subject = (String) tableModel.getValueAt(i, 0);
            int total = (int) tableModel.getValueAt(i, 1);
            int attended = (int) tableModel.getValueAt(i, 2);
            double pct = ((double) attended / total) * 100;
            totalPct += pct;
            count++;
            String status = pct >= 75 ? "GOOD" : "NEEDS IMPROVEMENT";
            sb.append(String.format("%s: %d/%d (%.1f%%) - %s%n", subject, attended, total, pct, status));
        }
        sb.append(String.format("\nOverall Average: %.2f%%", totalPct / count));
        JOptionPane.showMessageDialog(this, sb.toString(), "Weekly Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateUndoLabel() {
        for (java.awt.Component c : statusBar.getComponents()) {
            if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Undo")) {
                ((JLabel) c).setText("Undo Stack: " + undoStack.size());
                break;
            }
        }
    }

    private void saveUndoState() {
        int rowCount = tableModel.getRowCount();
        int colCount = tableModel.getColumnCount();
        Object[][] state = new Object[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                state[i][j] = tableModel.getValueAt(i, j);
            }
        }
        undoStack.offerLast(state);
        if (undoStack.size() > 20) undoStack.pollFirst();
        updateUndoLabel();
    }

    private void undoLastAction() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No undo steps.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[][] prevState = undoStack.pollLast();
        tableModel.setRowCount(0);
        for (Object[] row : prevState) {
            tableModel.addRow(row);
        }
        updateOverallAttendance();
        updateUndoLabel();
    }

    private void saveData() {
        saveDataQuiet();
        lastModifiedLabel.setText("Modified: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        setTitle("AC Pro v" + APP_VERSION + " | " + java.time.LocalDate.now() + " | Saved");
        JOptionPane.showMessageDialog(this, "Saved!", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateBackupLabel() {
        for (java.awt.Component c : statusBar.getComponents()) {
            if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Backup:")) {
                File bak = new File(databaseFile + ".bak");
                ((JLabel) c).setText("Bak: " + (bak.exists() ? java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "None"));
                break;
            }
        }
    }

    private void saveDataQuiet() {
        try {
            File mainFile = new File(databaseFile);
            if (mainFile.exists()) {
                File backupFile = new File(databaseFile + ".bak");
                java.nio.file.Files.copy(mainFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            System.err.println("Warning: Could not create backup file: " + ex.getMessage());
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(databaseFile))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String subject = (String) tableModel.getValueAt(i, 0);
                int total = (int) tableModel.getValueAt(i, 1);
                int attended = (int) tableModel.getValueAt(i, 2);
                String currentPct = (String) tableModel.getValueAt(i, 3);
                String requiredPct = (String) tableModel.getValueAt(i, 4);
                String status = (String) tableModel.getValueAt(i, 5);
                pw.printf("%s,%d,%d,%s,%s,%s%n", escapeCsv(subject), total, attended, currentPct, requiredPct, escapeCsv(status));
            }
            updateBackupLabel();
        } catch (IOException ex) {
            if ( SwingUtilities.getWindowAncestor(this) != null ) {
                JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCsv(String s) {
        if (s.contains(",")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void loadData() {
        File file = new File(databaseFile);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No saved data.", "Load", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tableModel.getRowCount() > 0) {
            int saveFirst = JOptionPane.showConfirmDialog(this, "You have unsaved data. Save before loading?", "Unsaved Data", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (saveFirst == JOptionPane.CANCEL_OPTION) return;
            if (saveFirst == JOptionPane.YES_OPTION) saveDataQuiet();
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Loading will replace current data. Continue?", "Confirm Load", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() >= 6) {
                    tableModel.addRow(fields.toArray());
                }
            }
            updateOverallAttendance();
            JOptionPane.showMessageDialog(this, "Loaded!", "Load", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                if (inQuotes && sb.length() > 0 && sb.charAt(sb.length() - 1) == '"') {
                    sb.append('"');
                    inQuotes = false;
                } else if (!inQuotes) {
                    inQuotes = true;
                } else {
                    sb.append(c);
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export CSV");
        fileChooser.setSelectedFile(new File("exported_data.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Subject,Total,Attended,Current %,Required %,Status/Needed,Trend,Category");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String subject = (String) tableModel.getValueAt(i, 0);
                    int total = (int) tableModel.getValueAt(i, 1);
                    int attended = (int) tableModel.getValueAt(i, 2);
                    String currentPct = (String) tableModel.getValueAt(i, 3);
                    String requiredPct = (String) tableModel.getValueAt(i, 4);
                    String status = (String) tableModel.getValueAt(i, 5);
                    String trend = tableModel.getColumnCount() > 6 ? (String) tableModel.getValueAt(i, 6) : "";
                    String category = tableModel.getColumnCount() > 7 ? (String) tableModel.getValueAt(i, 7) : "";
                    pw.printf("%s,%d,%d,%s,%s,%s,%s,%s%n", escapeCsv(subject), total, attended, currentPct, requiredPct, escapeCsv(status), trend, category);
                }
                JOptionPane.showMessageDialog(this, "Exported to: " + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import CSV");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int imported = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() >= 6) {
                        while (fields.size() < 8) {
                            fields.add("");
                        }
                        tableModel.addRow(fields.toArray());
                        imported++;
                    }
                }
                updateOverallAttendance();
                JOptionPane.showMessageDialog(this, "Imported " + imported + " subjects successfully!", "Import", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error importing data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportSummaryReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Summary Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Summary Report");
        fileChooser.setSelectedFile(new File("summary_report.txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            int totalAll = 0, attendedAll = 0;
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("============================================");
                pw.println("   ATTENDANCE SUMMARY REPORT");
                pw.println("   Generated: " + java.time.LocalDateTime.now());
                pw.println("   App Version: " + APP_VERSION);
                pw.println("============================================");
                pw.println();
                pw.printf("%-25s %5s %5s %8s %8s%n", "Subject", "Total", "Att.", "Pct", "Status");
                pw.println("------------------------------------------------");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String subject = (String) tableModel.getValueAt(i, 0);
                    int total = (int) tableModel.getValueAt(i, 1);
                    int attended = (int) tableModel.getValueAt(i, 2);
                    String currentPct = (String) tableModel.getValueAt(i, 3);
                    String status = (String) tableModel.getValueAt(i, 5);
                    totalAll += total;
                    attendedAll += attended;
                    String shortStatus = status.startsWith("Safe") ? "SAFE" : status.startsWith("Alert") ? "ALERT" : "ON TRACK";
                    pw.printf("%-25s %5d %5d %8s %8s%n", subject.length() > 23 ? subject.substring(0, 23) + ".." : subject, total, attended, currentPct, shortStatus);
                }
                pw.println("------------------------------------------------");
                double overallPct = totalAll > 0 ? ((double) attendedAll / totalAll) * 100 : 0;
                pw.printf("%-25s %5d %5d %8.2f%%%n", "TOTAL", totalAll, attendedAll, overallPct);
                pw.println("============================================");
                JOptionPane.showMessageDialog(this, "Summary exported to: " + file.getAbsolutePath(), "Summary Report", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restoreFromBackup() {
        File bakFile = new File(databaseFile + ".bak");
        if (!bakFile.exists()) {
            JOptionPane.showMessageDialog(this, "No backup file found.", "Restore Backup", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Restore data from backup? Current data will be lost.", "Restore Backup", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            tableModel.setRowCount(0);
            try (BufferedReader br = new BufferedReader(new FileReader(bakFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() >= 6) {
                        tableModel.addRow(fields.toArray());
                    }
                }
            }
            updateOverallAttendance();
            JOptionPane.showMessageDialog(this, "Backup restored successfully!", "Restore Backup", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error restoring backup: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters(String query, int categoryIndex, int statusIndex) {
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) subjectTable.getRowSorter();
        String categoryFilter = categoryIndex == 0 ? null : (String) categoryCombo.getItemAt(categoryIndex - 1);
        if (query.isEmpty() && categoryFilter == null && statusIndex == 0) {
            sorter.setRowFilter(null);
        } else {
            String finalQuery = query.toLowerCase();
            sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    boolean matchesSearch = finalQuery.isEmpty() || entry.getStringValue(0).toLowerCase().contains(finalQuery);
                    boolean matchesCategory = categoryFilter == null || (entry.getValueCount() > 7 && categoryFilter.equals(entry.getStringValue(7)));
                    boolean matchesStatus = true;
                    if (statusIndex == 1) matchesStatus = entry.getStringValue(5).startsWith("Safe") || entry.getStringValue(5).startsWith("On track");
                    else if (statusIndex == 2) matchesStatus = entry.getStringValue(5).startsWith("Alert");
                    return matchesSearch && matchesCategory && matchesStatus;
                }
            });
        }
    }

    private void exportJSON() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export JSON", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export JSON");
        fileChooser.setSelectedFile(new File("data_export.json"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("[");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("  {\"subject\":\"%s\",\"total\":%d,\"attended\":%d,\"percentage\":\"%s\",\"required\":\"%s\",\"status\":\"%s\"}%s%n",
                            escapeJson((String) tableModel.getValueAt(i, 0)),
                            (int) tableModel.getValueAt(i, 1),
                            (int) tableModel.getValueAt(i, 2),
                            escapeJson((String) tableModel.getValueAt(i, 3)),
                            escapeJson((String) tableModel.getValueAt(i, 4)),
                            escapeJson((String) tableModel.getValueAt(i, 5)),
                            i < tableModel.getRowCount() - 1 ? "," : "");
                }
                pw.println("]");
                JOptionPane.showMessageDialog(this, "Exported to: " + file.getAbsolutePath(), "Export JSON", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void exportHTML() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export HTML", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export HTML");
        fileChooser.setSelectedFile(new File("report.html"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Attendance Report</title>");
                pw.println("<style>body{font-family:Segoe UI,sans-serif;margin:20px}");
                pw.println("h1{color:#3498db}table{border-collapse:collapse;width:100%;margin-top:20px}");
                pw.println("th{background:#3498db;color:#fff;padding:10px;text-align:left}");
                pw.println("td{padding:8px;border-bottom:1px solid #ddd}");
                pw.println(".safe{color:#27ae60}.alert{color:#e74c3c}.warn{color:#f39c12}");
                pw.println("</style></head><body>");
                pw.println("<h1>Attendance Report</h1>");
                pw.println("<p>Generated: " + java.time.LocalDateTime.now() + "</p>");
                pw.println("<table><tr><th>Subject</th><th>Total</th><th>Attended</th><th>Current %</th><th>Required %</th><th>Status</th></tr>");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String subject = (String) tableModel.getValueAt(i, 0);
                    int total = (int) tableModel.getValueAt(i, 1);
                    int attended = (int) tableModel.getValueAt(i, 2);
                    String currentPct = (String) tableModel.getValueAt(i, 3);
                    String requiredPct = (String) tableModel.getValueAt(i, 4);
                    String status = (String) tableModel.getValueAt(i, 5);
                    String cls = status.startsWith("Safe") || status.startsWith("On track") ? "safe" : "alert";
                    pw.printf("<tr><td>%s</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td class='%s'>%s</td></tr>%n",
                            escapeHtml(subject), total, attended, currentPct, requiredPct, cls, escapeHtml(status));
                }
                pw.println("</table></body></html>");
                JOptionPane.showMessageDialog(this, "Exported to: " + file.getAbsolutePath(), "Export HTML", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static void main(String[] args) {
        try {
            // Use modern system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AttendanceCalculator app = new AttendanceCalculator();
            java.awt.Image icon = createAppIcon();
            if (icon != null) app.setIconImage(icon);
            app.setVisible(true);
            app.toFront();
            app.requestFocus();
            app.setState(java.awt.Frame.NORMAL);
        });
    }

    private static java.awt.Image createAppIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(0, 0, 32, 32, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString("A", 9, 24);
        g2d.dispose();
        return img;
    }
}




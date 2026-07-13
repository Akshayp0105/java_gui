# Attendance Calculator Pro

A Java Swing-based attendance tracking application for students.

## Features

- Calculate and track attendance for multiple subjects
- Version tracking with v2.4.0
- See if you're safe or need more classes to meet requirements
- Color-coded rows (green = safe, red = alert)
- Green for 90%+, light green for 75%+, yellow for warning, orange for danger, red below 50%
- Overall attendance summary with min/max/average stats
- Progress bar visual indicator with monochrome toggle
- Search and filter subjects in the table
- Category filter dropdown (Core, Elective, Lab, Theory, Other)
- Real-time filtering as you type
- Table column sorting by clicking headers
- Sort by name, attendance percentage, or status
- Dark mode toggle
- Customizable theme colors
- Live date/time status bar
- Auto-updating clock display
- Undo stack with size indicator
- Backup file management with restore capability
- Color-coded status bar based on attendance health
- Time-based greeting in header
- Dynamic table cell tooltips with full details
- Column show/hide toggle in View menu
- Export to CSV, HTML, or Summary Report
- Move rows up/down and to top/bottom
- Rename subjects (F2)
- Batch delete multiple rows
- Keyboard shortcuts for category selection (Alt+1-5)
- Subject name character limit validation
- Unsaved data warning before load
- Overwrite confirmation on exports

## Keyboard Shortcuts

Press F1 at any time to view all available shortcuts.

| Shortcut | Action |
|----------|--------|
| Ctrl+S | Save data |
| Ctrl+Shift+S | Export as CSV (Save As) |
| Ctrl+L | Load data |
| Ctrl+Shift+L | Load data with unsaved warning |
| Ctrl+E | Export as CSV |
| Ctrl+H | Export as HTML |
| Ctrl+A | Select all rows |
| Ctrl+Z | Undo last action |
| Ctrl+D | Duplicate selected row |
| Ctrl+Shift+Up | Move row to top |
| Ctrl+Shift+Down | Move row to bottom |
| Ctrl+R | Reset input fields |
| Ctrl+N | New subject (clear fields) |
| Ctrl+P | Print table |
| F1 | Show help dialog |
| F2 | Rename selected subject |
| F5 | Refresh table |
| Alt+1-5 | Select category (Core/Elective/Lab/Theory/Other) |
| Enter | Calculate & Add subject |
| Delete | Remove selected row(s) |
| Escape | Clear search field |
| Up/Down | Navigate between input fields |

## File Operations

All file operations include error handling and user feedback.

- **Save**: Save current data to database file (auto-creates backup with timestamp)
- **Load**: Load previously saved data (with unsaved data warning)
- **Restore from Backup**: Restore data from backup file
- **Export as CSV**: Export table data to CSV file (with overwrite confirmation)
- **Export as HTML**: Export table data to styled HTML report
- **Export Summary Report**: Export a text summary report
- **Import from CSV**: Import subjects from a CSV file (supports quoted fields)

## How to Use

1. Enter subject name, total classes, attended classes, and required percentage (all fields validated)
2. Click **Calculate & Add** or press **Enter** to add to the table (duplicate detection included)
3. Status column shows if you're safe or need more classes (with class count)
4. Use File menu to save, load, export, or import data (auto-save available)
5. Toggle **Dark Mode** from the View menu (full UI theme)
6. Use the **Search** field to filter subjects in the table (case-insensitive)
7. Use **Category** dropdown to filter by subject type
8. Right-click on table rows for context menu actions (rename, move, delete, export)
9. Use **Move Up/Down** buttons or **Move to Top/Bottom** from context menu to reorder rows

## System Requirements

- Java JDK 8 or higher
- Windows/macOS/Linux compatible
- No external dependencies required

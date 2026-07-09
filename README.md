# Attendance Calculator 

A Java Swing-based attendance tracking application for students.

## Features

- Calculate and track attendance for multiple subjects
- Version tracking with v2.2.0
- See if you're safe or need more classes to meet requirements
- Color-coded rows (green = safe, red = alert)
- Green for 90%+, light green for 75%+, yellow for warning, orange for danger, red below 50%
- Overall attendance summary with min/max/average stats
- Progress bar visual indicator
- Search and filter subjects in the table
- Real-time filtering as you type
- Table column sorting by clicking headers
- Sort by name, attendance percentage, or status
- Dark mode toggle
- Customizable theme colors
- Live date/time status bar
- Auto-updating clock display

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+S | Save data |
| Ctrl+Shift+S | Save As |
| Ctrl+L | Load data |
| Ctrl+F | Find/Search |
| Ctrl+E | Export as CSV |
| Ctrl+I | Import from CSV |
| Ctrl+A | Select all rows |
| Ctrl+Z | Undo last action |
| Ctrl+D | Duplicate selected row |
| Ctrl+R | Reset input fields |
| Ctrl+N | New subject (clear fields) |
| Ctrl+P | Print table |
| Enter | Calculate & Add subject |
| Delete | Remove selected row |
| F1 | Show help dialog |

## File Operations

- **Save**: Save current data to database file
- **Load**: Load previously saved data
- **Export as CSV**: Export table data to CSV file
- **Import from CSV**: Import subjects from a CSV file

## How to Use

1. Enter subject name, total classes, attended classes, and required percentage
2. Click **Calculate & Add** or press **Enter** to add to the table
3. Status column shows if you're safe or need more classes
4. Use File menu to save, load, export, or import data
5. Toggle **Dark Mode** from the View menu
6. Use the **Search** field to filter subjects in the table

## Requirements

- Java JDK 8 or higher

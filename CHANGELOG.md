# Changelog

All notable changes to Attendance Calculator Pro will be documented in this file.

## [2.4.1] - 2026-07-14

### Added
- Subject name length constants (MAX_SUBJECT_NAME_LENGTH, MIN_SUBJECT_NAME_LENGTH)
- Status text constants (STATUS_SAFE, STATUS_ALERT, STATUS_ON_TRACK, STATUS_CANNOT_MISS)
- Trend text constants (TREND_UP, TREND_DOWN, TREND_STABLE, TREND_NEW)
- Chart threshold constants (CHART_THRESHOLD_SAFE, CHART_THRESHOLD_WARNING)
- Category arrays as constants (CATEGORIES, FILTER_CATEGORIES, STATUS_FILTERS)
- Max undo history constant (MAX_UNDO_HISTORY)
- Database file name and backup suffix constants
- Default goal percentage constant
- File extension constants (EXT_CSV, EXT_JSON, EXT_HTML)
- Column names constant
- CSV export header constant
- Confirmation message constants

### Changed
- Refactored code to use named constants instead of magic numbers
- Improved code maintainability and readability

## [2.4.0] - 2026-07-13

### Added
- Dark mode theme support
- Custom theme color picker
- System tray integration
- Keyboard shortcuts for all major operations
- Undo/redo functionality (up to 20 states)
- CSV, HTML, JSON export formats
- Summary report generation
- Row filtering by name, category, and status
- Context menu on table (right-click)
- Auto-save with backup file management
- Live clock in status bar
- Time-based greeting messages
- Statistics charts

### Changed
- Improved input validation
- Enhanced error messages
- Better color-coded status indicators

## [2.2.0] - 2026-06-21

### Added
- Initial release with core attendance tracking
- Multiple subject support
- Percentage calculation
- Save/load functionality
- CSV export

## [2.0.0] - 2026-06-01

### Added
- Project inception

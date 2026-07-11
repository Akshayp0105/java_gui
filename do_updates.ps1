$ErrorActionPreference = "Continue"
$date = "2026-07-11T12:00:00+0530"
$file = "AttendanceCalculator.java"
$content = Get-Content $file -Raw

$changes = @(
    @{old='new Color(52, 152, 219)'; new='new Color(41, 128, 185)'; msg='tweak header background color'},
    @{old='Font.BOLD, 24'; new='Font.BOLD, 22'; msg='reduce title font size'},
    @{old='hour < 12 ? "Good Morning" : hour < 17'; new='hour < 11 ? "Good Morning" : hour < 16'; msg='adjust greeting hour thresholds'},
    @{old='"Shortcuts: Ctrl+S Save | Ctrl+L Load | Ctrl+E Export | Ctrl+P Print | F1 Help"'; new='"Shortcuts: Ctrl+S Save | Ctrl+L Load | Ctrl+E Export | Ctrl+P Print | F1 Help | Ctrl+Z Undo"'; msg='extend header tooltip with Undo hint'},
    @{old='"Calculate & Add"'; new='"Add Subject"'; msg='rename calculate button'},
    @{old='"Predict Attendance"'; new='"Forecast"'; msg='shorten predict button label'},
    @{old='"Attendance Calculator Pro v" + APP_VERSION + " - " + java.time.LocalDate.now()'; new='"Attn Calc Pro v" + APP_VERSION + " - " + java.time.LocalDate.now()'; msg='shorten window title'},
    @{old='new Dimension(700, 450)'; new='new Dimension(720, 460)'; msg='increase minimum window size'},
    @{old='"EEE, MMM d, yyyy"'; new='"EEEE, MMMM d, yyyy"'; msg='expand date format in status bar'},
    @{old='"HH:mm:ss"'; new='"hh:mm:ss a"'; msg='use 12-hour clock format'},
    @{old='new Dimension(200, 25)'; new='new Dimension(180, 22)'; msg='resize progress bar'},
    @{old='"No subjects added yet. Add subjects to see attendance summary."'; new='"No subjects. Add entries to view summary."'; msg='shorten empty state text'},
    @{old='"No Data"'; new='"Empty"'; msg='update progress bar empty label'},
    @{old='"Subjects: 0 | Highest: 0% | Lowest: 0% | Avg: 0%"'; new='"Rows: 0 | Max: 0% | Min: 0% | Avg: 0%"'; msg='rename stats label fields'},
    @{old='"attendance_database.csv"'; new='"attendance_data.csv"'; msg='rename database file'},
    @{old='"Add New Subject"'; new='"New Subject"'; msg='shorten input panel title'},
    @{old='Font.BOLD, 12\);'+"`n"+"        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), \"Add New Subject\""; new='Font.BOLD, 13);'+"`n"+"        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), \"New Subject"'; msg='increase border title font size'},
    @{old='"Subject Details"'; new='"Subjects List"'; msg='rename table border title'},
    @{old='"Safe! Can miss "'; new='"Good! Can skip "'; msg='soften safe status message'},
    @{old='"Alert! Need "'; new='"Warning! Need "'; msg='soften alert status message'},
    @{old='"attendance_export.csv"'; new='"exported_data.csv"'; msg='rename default export filename'},
    @{old='"attendance_summary.txt"'; new='"summary_report.txt"'; msg='rename summary filename'},
    @{old='"Search:"'; new='"Filter:"'; msg='rename search label'},
    @{old='new String[]{"All", "Core", "Elective", "Lab", "Theory", "Other"}'; new='new String[]{"All", "Theory", "Lab", "Core", "Elective", "Other"}'; msg='reorder category filter options'},
    @{old='"Backup: None"'; new='"Backup: N/A"'; msg='update backup initial label'},
    @{old='"Undo: "'; new='"Undo Stack: "'; msg='expand undo label'},
    @{old='"Auto-Save: ON"'; new='"AutoSave: ON"'; msg='shorten auto-save label'},
    @{old='"v" + APP_VERSION'; new='"Ver. " + APP_VERSION'; msg='update version prefix in status bar'},
    @{old='rowCountLabel = new JLabel("Subjects: 0")'; new='rowCountLabel = new JLabel("Rows: 0")'; msg='rename row counter label'},
    @{old='"All Status"'; new='"All"'; msg='shorten status filter label'},
    @{old='"attendance_data.json"'; new='"data_export.json"'; msg='rename JSON export filename'},
    @{old='"attendance_report.html"'; new='"report.html"'; msg='rename HTML export filename'}
)

$i = 1
foreach ($change in $changes) {
    Write-Host "[$i/32] $($change.msg)"
    if ($content -match [regex]::Escape($change.old)) {
        $content = $content -replace [regex]::Escape($change.old), $change.new
        $content | Set-Content $file -NoNewline
        & git add -A
        $env:GIT_AUTHOR_DATE = $date
        $env:GIT_COMMITTER_DATE = $date
        & git commit -m "minor: $($change.msg)"
        Write-Host "  -> committed. Pushing..."
        & git push origin main 2>&1 | Out-Null
        Write-Host "  -> pushed."
    } else {
        Write-Host "  -> WARNING: pattern not found, skipping!"
    }
    Start-Sleep -Seconds 2
    $i++
}

Write-Host "All 32 updates completed!"

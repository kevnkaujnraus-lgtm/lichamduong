package org.lichamduong.app

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

// ============================================================
// THỨ
// ============================================================

private val THU = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

private val THU_FULL = listOf(
    "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm",
    "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
)

// ============================================================
// Ô LỊCH (tương đương class Cell trong main.py)
// ============================================================

private class DayCell(context: android.content.Context) : LinearLayout(context) {

    val solarText = TextView(context)
    val lunarText = TextView(context)

    var date: LocalDate? = null
    var baseBg: Int = Color.WHITE
    var normalColor: Int = Color.parseColor("#111111")

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true

        solarText.gravity = Gravity.CENTER
        solarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        solarText.setTypeface(null, Typeface.BOLD)

        lunarText.gravity = Gravity.CENTER
        lunarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)

        addView(solarText)
        addView(lunarText)
    }

    // Tương đương Cell.set_selected() trong main.py
    fun setSelectedState(selected: Boolean) {
        if (selected) {
            setBackgroundColor(Color.parseColor("#3389F2"))
            solarText.setTextColor(Color.WHITE)
            lunarText.setTextColor(Color.WHITE)
        } else {
            setBackgroundColor(baseBg)
            solarText.setTextColor(normalColor)
            lunarText.setTextColor(normalColor)
        }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var monthTitle: TextView
    private lateinit var bigDay: TextView
    private lateinit var weekdayLabel: TextView

    private lateinit var lunarMonthLabel: TextView
    private lateinit var lunarDayLabel: TextView
    private lateinit var tietLabel: TextView

    private lateinit var yearCcLabel: TextView
    private lateinit var monthCcLabel: TextView
    private lateinit var dayCcLabel: TextView
    private lateinit var hourCcLabel: TextView

    private lateinit var goodHoursLabel: TextView

    private lateinit var monthBtn: Button
    private lateinit var yearBtn: Button

    private lateinit var grid: GridLayout

    private var today: LocalDate = LocalDate.now()
    private var selected: LocalDate = today
    private var year: Int = today.year
    private var month: Int = today.monthValue

    // Lưu các ô đang hiển thị theo ngày để tô sáng khi chọn
    // (tương đương self.cell_by_date trong main.py)
    private val cellByDate = mutableMapOf<LocalDate, DayCell>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = buildUi()
        setContentView(root)

        refresh()
    }

    // ========================================================
    // TIỆN ÍCH
    // ========================================================

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    private fun sp(view: TextView, size: Float) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    private fun label(text: String, size: Float, bold: Boolean, heightDp: Int): TextView {
        return TextView(this).apply {
            this.text = text
            sp(this, size)
            if (bold) setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(heightDp)
            )
        }
    }

    // ========================================================
    // GIAO DIỆN (tương đương Calendar.make_ui trong main.py)
    // ========================================================

    private fun buildUi(): View {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(2), dp(2), dp(2), 0)
        }

        // ---------------- THÁNG / NĂM ----------------
        monthTitle = label("", 18f, false, 40)
        root.addView(monthTitle)

        // ---------------- NGÀY DƯƠNG (to) ----------------
        bigDay = label("", 52f, true, 78)
        root.addView(bigDay)

        // ---------------- THỨ ----------------
        weekdayLabel = label("", 20f, true, 36)
        root.addView(weekdayLabel)

        // ---------------- THÔNG TIN ÂM LỊCH ----------------
        val info = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(110)
            )
        }

        // Bên trái: tháng âm / ngày âm / tiết khí
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        lunarMonthLabel = label("", 16f, false, 28)
        lunarDayLabel = label("", 32f, false, 44)
        tietLabel = label("", 14f, false, 32)

        left.addView(lunarMonthLabel)
        left.addView(lunarDayLabel)
        left.addView(tietLabel)

        // Bên phải: năm/tháng/ngày Can Chi + giờ
        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        yearCcLabel = label("", 14f, false, 26)
        monthCcLabel = label("", 14f, false, 26)
        dayCcLabel = label("", 14f, false, 26)
        hourCcLabel = label("", 14f, false, 26)

        right.addView(yearCcLabel)
        right.addView(monthCcLabel)
        right.addView(dayCcLabel)
        right.addView(hourCcLabel)

        info.addView(left)
        info.addView(right)
        root.addView(info)

        // ---------------- GIỜ HOÀNG ĐẠO ----------------
        goodHoursLabel = label("", 12f, false, 34).apply {
            setLineSpacing(0f, 1f)
        }
        root.addView(goodHoursLabel)

        // ---------------- THANH ĐIỀU HƯỚNG ----------------
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(60)
            )
        }

        fun navButton(text: String): Button = Button(this).apply {
            this.text = text
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#B8B8B8"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        val prevBtn = navButton("<")
        monthBtn = navButton("")
        yearBtn = navButton("")
        val nextBtn = navButton(">")

        prevBtn.setOnClickListener { changeMonth(-1) }
        nextBtn.setOnClickListener { changeMonth(1) }

        monthBtn.setOnClickListener { showMonthPicker() }
        yearBtn.setOnClickListener { showYearPicker() }

        // Bấm giữ nút Tháng hoặc Năm để nhảy nhanh về hôm nay
        monthBtn.setOnLongClickListener { goToday(); true }
        yearBtn.setOnLongClickListener { goToday(); true }

        nav.addView(prevBtn)
        nav.addView(monthBtn)
        nav.addView(yearBtn)
        nav.addView(nextBtn)
        root.addView(nav)

        // ---------------- LƯỚI LỊCH ----------------
        grid = GridLayout(this).apply {
            columnCount = 7
            rowCount = 7
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(grid)

        return root
    }

    // ========================================================
    // REFRESH LỊCH (tương đương Calendar.refresh trong main.py)
    // ========================================================

    private fun refresh() {

        grid.removeAllViews()
        cellByDate.clear()

        monthTitle.text = "Tháng $month năm $year"
        monthBtn.text = "T$month"
        yearBtn.text = year.toString()

        // ---------------- TÊN THỨ ----------------
        for (i in THU.indices) {
            val color = when (i) {
                6 -> Color.parseColor("#CC0D0D")
                5 -> Color.parseColor("#0D1ABF")
                else -> Color.BLACK
            }
            val headerLabel = TextView(this).apply {
                text = THU[i]
                setTextColor(color)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                sp(this, 14f)
            }
            addGridCell(headerLabel, row = 0, col = i)
        }

        // ---------------- NGÀY ĐẦU THÁNG ----------------
        val firstOfMonth = LocalDate.of(year, month, 1)
        // Python: Monday=0 ... Sunday=6 (giống DayOfWeek.value - 1)
        val first = firstOfMonth.dayOfWeek.value - 1

        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()

        // ---------------- THÁNG TRƯỚC ----------------
        var prevY = year
        var prevM = month - 1
        if (prevM == 0) {
            prevM = 12
            prevY -= 1
        }
        val prevDays = YearMonth.of(prevY, prevM).lengthOfMonth()

        var row = 1
        var col = 0

        for (index in 0 until first) {
            val day = prevDays - first + index + 1
            val dt = LocalDate.of(prevY, prevM, day)
            val lunar = LunarCalendar.solarToLunar(day, prevM, prevY)

            val cell = createCell(
                solarDay = day,
                lunarDay = lunar.first,
                lunarMonth = lunar.second,
                muted = true,
                sunday = false,
                saturday = false,
                today = false
            )
            cell.date = dt
            cellByDate[dt] = cell
            cell.setSelectedState(dt == selected)

            addGridCell(cell, row, col)
            col++
            if (col == 7) { col = 0; row++ }
        }

        // ---------------- NGÀY TRONG THÁNG HIỆN TẠI ----------------
        for (day in 1..daysInMonth) {
            val dt = LocalDate.of(year, month, day)
            val lunar = LunarCalendar.solarToLunar(dt.dayOfMonth, dt.monthValue, dt.year)

            val isSunday = dt.dayOfWeek == DayOfWeek.SUNDAY
            val isSaturday = dt.dayOfWeek == DayOfWeek.SATURDAY

            val cell = createCell(
                solarDay = day,
                lunarDay = lunar.first,
                lunarMonth = lunar.second,
                muted = false,
                sunday = isSunday,
                saturday = isSaturday,
                today = dt == today
            )
            cell.date = dt
            cellByDate[dt] = cell
            cell.setSelectedState(dt == selected)

            cell.setOnClickListener { select(dt) }

            addGridCell(cell, row, col)
            col++
            if (col == 7) { col = 0; row++ }
        }

        // ---------------- NGÀY THÁNG SAU ----------------
        var nextY = year
        var nextM = month + 1
        if (nextM == 13) { nextM = 1; nextY += 1 }

        var nextDay = 1
        var total = first + daysInMonth

        while (total < 42) {
            val dt = LocalDate.of(nextY, nextM, nextDay)
            val lunar = LunarCalendar.solarToLunar(nextDay, nextM, nextY)

            val cell = createCell(
                solarDay = nextDay,
                lunarDay = lunar.first,
                lunarMonth = lunar.second,
                muted = true,
                sunday = false,
                saturday = false,
                today = false
            )
            cell.date = dt
            cellByDate[dt] = cell
            cell.setSelectedState(dt == selected)

            addGridCell(cell, row, col)
            col++
            if (col == 7) { col = 0; row++ }

            nextDay++
            total++
        }

        // ---------------- ĐẢM BẢO NGÀY ĐANG CHỌN HỢP LỆ ----------------
        if (selected.year != year || selected.monthValue != month) {
            val maxDay = daysInMonth
            val day = minOf(selected.dayOfMonth, maxDay)
            selected = LocalDate.of(year, month, day)
        }

        select(selected)
    }

    // Thêm view vào GridLayout với vị trí hàng/cột, chia đều 7 cột x 7 hàng
    private fun addGridCell(view: View, row: Int, col: Int) {
        val params = GridLayout.LayoutParams(
            GridLayout.spec(row, GridLayout.FILL, 1f),
            GridLayout.spec(col, GridLayout.FILL, 1f)
        )
        params.width = 0
        params.height = 0
        view.layoutParams = params
        grid.addView(view)
    }

    // Tương đương hàm tạo Cell(...) trong main.py
    private fun createCell(
        solarDay: Int,
        lunarDay: Long,
        lunarMonth: Long,
        muted: Boolean,
        sunday: Boolean,
        saturday: Boolean,
        today: Boolean
    ): DayCell {

        val cell = DayCell(this)

        val color = when {
            muted -> Color.parseColor("#BDBDBD")
            sunday -> Color.parseColor("#D62828")
            saturday -> Color.parseColor("#1825C9")
            else -> Color.parseColor("#111111")
        }

        cell.normalColor = color
        cell.baseBg = if (today) Color.parseColor("#E0F0FD") else Color.WHITE

        cell.solarText.text = solarDay.toString()
        cell.lunarText.text = "$lunarDay/$lunarMonth"

        cell.setSelectedState(false)

        return cell
    }

    // ========================================================
    // CHỌN NGÀY (tương đương Calendar.select trong main.py)
    // ========================================================

    private fun select(dt: LocalDate) {

        val oldSelected = selected
        selected = dt

        // ---------------- TÔ SÁNG Ô ĐANG CHỌN TRÊN LƯỚI ----------------
        cellByDate[oldSelected]?.setSelectedState(false)
        cellByDate[dt]?.setSelectedState(true)

        val (lunarDay, lunarMonth, lunarYear) = LunarCalendar.solarToLunar(
            dt.dayOfMonth,
            dt.monthValue,
            dt.year
        )

        // ---------------- NGÀY DƯƠNG ----------------
        bigDay.text = dt.dayOfMonth.toString()

        // ---------------- THỨ ----------------
        weekdayLabel.text = THU_FULL[dt.dayOfWeek.value - 1]

        // ---------------- NGÀY ÂM ----------------
        lunarMonthLabel.text = "Tháng $lunarMonth"
        lunarDayLabel.text = lunarDay.toString()

        // ---------------- TIẾT KHÍ ----------------
        tietLabel.text = try {
            "Tiết ${LunarCalendar.tietKhi(dt)}"
        } catch (e: Exception) {
            "Tiết khí: --"
        }

        // ---------------- CAN CHI NĂM ----------------
        yearCcLabel.text = try {
            "Năm ${LunarCalendar.canChiYear(lunarYear)}"
        } catch (e: Exception) {
            "Năm --"
        }

        // ---------------- CAN CHI THÁNG ----------------
        monthCcLabel.text = try {
            "Tháng ${LunarCalendar.canChiMonth(lunarYear, lunarMonth)}"
        } catch (e: Exception) {
            "Tháng --"
        }

        // ---------------- CAN CHI NGÀY ----------------
        dayCcLabel.text = try {
            "Ngày ${LunarCalendar.canChiDay(dt)}"
        } catch (e: Exception) {
            "Ngày --"
        }

        // ---------------- GIỜ CAN CHI ----------------
        hourCcLabel.text = "Giờ Canh Tý"

        // ---------------- GIỜ HOÀNG ĐẠO ----------------
        goodHoursLabel.text = "Giờ hoàng đạo: Sửu (1-3), Thìn (7-9), Ngọ (11-13), " +
            "Mùi (13-15), Tuất (19-21), Hợi (21-23)"
    }

    // ========================================================
    // CHỌN NHANH THÁNG / NĂM
    // ========================================================

    private fun showMonthPicker() {

        val picker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 12
            value = month
            displayedValues = (1..12).map { "Tháng $it" }.toTypedArray()
            wrapSelectorWheel = true
        }

        val container = FrameLayout(this).apply {
            setPadding(dp(24), dp(8), dp(24), dp(8))
            addView(
                picker,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn tháng")
            .setView(container)
            .setPositiveButton("Chọn") { _, _ ->
                month = picker.value

                val maxDay = YearMonth.of(year, month).lengthOfMonth()
                val day = minOf(selected.dayOfMonth, maxDay)
                selected = LocalDate.of(year, month, day)

                refresh()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showYearPicker() {

        val picker = NumberPicker(this).apply {
            minValue = 1900
            maxValue = 2100
            value = year
            wrapSelectorWheel = false
        }

        val container = FrameLayout(this).apply {
            setPadding(dp(24), dp(8), dp(24), dp(8))
            addView(
                picker,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn năm")
            .setView(container)
            .setPositiveButton("Chọn") { _, _ ->
                year = picker.value

                val maxDay = YearMonth.of(year, month).lengthOfMonth()
                val day = minOf(selected.dayOfMonth, maxDay)
                selected = LocalDate.of(year, month, day)

                refresh()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // ========================================================
    // CHUYỂN THÁNG (tương đương Calendar.change_month trong main.py)
    // ========================================================

    private fun changeMonth(step: Int) {

        var newMonth = month + step
        var newYear = year

        if (newMonth < 1) {
            newMonth = 12
            newYear -= 1
        } else if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }

        month = newMonth
        year = newYear

        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        val day = minOf(selected.dayOfMonth, maxDay)
        selected = LocalDate.of(year, month, day)

        refresh()
    }

    // ========================================================
    // VỀ HÔM NAY (tương đương Calendar.go_today trong main.py)
    // ========================================================

    private fun goToday() {
        today = LocalDate.now()
        selected = today
        month = today.monthValue
        year = today.year
        refresh()
    }
}

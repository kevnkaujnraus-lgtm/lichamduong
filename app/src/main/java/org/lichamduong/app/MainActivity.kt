package org.lichamduong.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

private const val MIN_YEAR = 1901
private const val MAX_YEAR = 2099

private val THU = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
private val THU_FULL = listOf(
    "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
)

// ============================================================
// Ô NGÀY TRONG LƯỚI
// ============================================================

private class DayCell(context: Context) : LinearLayout(context) {

    private val solarText = TextView(context).apply { style(Ui.M, bold = true) }
    private val lunarText = TextView(context).apply { style(Ui.S) }

    private var baseBg = Color.WHITE
    private var solarColor = Ui.TEXT
    private var lunarColor = Ui.SUB

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        addView(solarText)
        addView(lunarText)
    }

    fun setup(solar: String, lunar: String, solarColor: Int, lunarColor: Int, isToday: Boolean) {
        solarText.text = solar
        lunarText.text = lunar
        this.solarColor = solarColor
        this.lunarColor = lunarColor
        baseBg = if (isToday) Ui.TODAY_BG else Color.WHITE
        setPicked(false)
    }

    fun setPicked(picked: Boolean) {
        if (picked) {
            setBackgroundColor(Ui.SELECT)
            solarText.setTextColor(Color.WHITE)
            lunarText.setTextColor(Color.WHITE)
        } else {
            setBackgroundColor(baseBg)
            solarText.setTextColor(solarColor)
            lunarText.setTextColor(lunarColor)
        }
    }
}

// ============================================================
// MÀN HÌNH CHÍNH
// ============================================================

class MainActivity : AppCompatActivity() {

    private lateinit var bigDay: TextView
    private lateinit var dateLabel: TextView
    private lateinit var lunarMonthLabel: TextView
    private lateinit var lunarDayLabel: TextView
    private lateinit var termLabel: TextView
    private lateinit var yearCc: TextView
    private lateinit var monthCc: TextView
    private lateinit var dayCc: TextView
    private lateinit var hourCc: TextView
    private lateinit var eventLabel: TextView
    private lateinit var goodHoursLabel: TextView
    private lateinit var navTitle: TextView
    private lateinit var grid: GridLayout

    private var today: LocalDate = LocalDate.now()
    private var selected: LocalDate = today
    private var year: Int = today.year
    private var month: Int = today.monthValue

    private val cells = mutableMapOf<LocalDate, DayCell>()

    // Vuốt ngang để chuyển tháng
    private val swipe by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (abs(dx) > dp(80) && abs(dx) > abs(dy) * 1.5f && abs(velocityX) > 800f) {
                    changeMonth(if (dx < 0) 1 else -1)
                    return true
                }
                return false
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
    }

    override fun onResume() {
        super.onResume()
        today = LocalDate.now()   // qua nửa đêm thì "hôm nay" tự cập nhật
        refresh()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        swipe.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // ========================================================
    // TIỆN ÍCH
    // ========================================================

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()

    private fun tv(size: Float, bold: Boolean = false, color: Int = Ui.TEXT): TextView =
        TextView(this).apply { style(size, bold, color) }

    private fun lp(height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

    private fun column(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    private fun View.withRipple() {
        val out = TypedValue()
        if (this@MainActivity.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true)) {
            foreground = ContextCompat.getDrawable(this@MainActivity, out.resourceId)
        }
    }

    // ========================================================
    // DỰNG GIAO DIỆN
    // ========================================================

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(4), dp(8), dp(4), 0)
        }

        // Ngày dương + thứ/ngày/tháng/năm
        bigDay = tv(Ui.XL, bold = true)
        dateLabel = tv(Ui.M, bold = true)
        root.addView(bigDay, lp())
        root.addView(dateLabel, lp())

        // Âm lịch (trái) | Can Chi (phải)
        val info = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(4))
        }
        val left = column()
        val right = column()

        lunarMonthLabel = tv(Ui.M)
        lunarDayLabel = tv(Ui.L, bold = true)
        termLabel = tv(Ui.S, color = Ui.SUB)
        left.addView(lunarMonthLabel)
        left.addView(lunarDayLabel)
        left.addView(termLabel)

        yearCc = tv(Ui.M)
        monthCc = tv(Ui.M)
        dayCc = tv(Ui.M)
        hourCc = tv(Ui.M)
        listOf(yearCc, monthCc, dayCc, hourCc).forEach { right.addView(it) }

        info.addView(left)
        info.addView(right)
        root.addView(info, lp())

        // Ngày lễ + giờ hoàng đạo
        eventLabel = tv(Ui.S, bold = true, color = Ui.SUN).apply { minHeight = dp(22) }
        goodHoursLabel = tv(Ui.S, color = Ui.SUB).apply { setPadding(dp(8), 0, dp(8), dp(8)) }
        root.addView(eventLabel, lp())
        root.addView(goodHoursLabel, lp())

        // Thanh điều hướng:  <   Tháng 11 · 2026   Hôm nay   >
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun navButton(label: String, textSize: Float, weight: Float, onClick: () -> Unit): TextView =
            tv(textSize, bold = true).apply {
                text = label
                setBackgroundColor(Ui.BAR)
                isClickable = true
                withRipple()
                setOnClickListener { onClick() }
                layoutParams = LinearLayout.LayoutParams(0, dp(52), weight).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
            }
        navTitle = navButton("", Ui.M, 3f) { showPicker() }
        nav.addView(navButton("<", Ui.L, 1f) { changeMonth(-1) })
        nav.addView(navTitle)
        nav.addView(navButton("Hôm nay", Ui.M, 2f) { goToday() })
        nav.addView(navButton(">", Ui.L, 1f) { changeMonth(1) })
        root.addView(nav, lp())

        // Lưới lịch: 1 hàng tên thứ + 6 hàng ngày
        grid = GridLayout(this).apply {
            columnCount = 7
            rowCount = 7
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        root.addView(grid)

        return root
    }

    // ========================================================
    // VẼ LẠI LỊCH THÁNG
    // ========================================================

    private fun refresh() {
        grid.removeAllViews()
        cells.clear()

        navTitle.text = "Tháng $month · $year"

        // Hàng tên thứ
        for (i in THU.indices) {
            val color = when (i) { 6 -> Ui.SUN; 5 -> Ui.SAT; else -> Ui.TEXT }
            addGridCell(tv(Ui.M, bold = true, color = color).apply { text = THU[i] }, 0, i)
        }

        // 42 ô ngày, bắt đầu từ thứ Hai của tuần chứa ngày 1
        val first = LocalDate.of(year, month, 1)
        val start = first.minusDays((first.dayOfWeek.value - 1).toLong())

        for (i in 0 until 42) {
            val d = start.plusDays(i.toLong())
            val inMonth = d.monthValue == month && d.year == year
            val lunar = LunarCalendar.solarToLunar(d)
            val off = Holidays.of(d, lunar).any { it.off }

            val solarColor = when {
                !inMonth -> Ui.MUTED
                off || d.dayOfWeek == DayOfWeek.SUNDAY -> Ui.SUN
                d.dayOfWeek == DayOfWeek.SATURDAY -> Ui.SAT
                else -> Ui.TEXT
            }
            val lunarColor = if (inMonth) Ui.SUB else Ui.MUTED

            val cell = DayCell(this)
            cell.setup(
                solar = d.dayOfMonth.toString(),
                lunar = lunarCellText(lunar),
                solarColor = solarColor,
                lunarColor = lunarColor,
                isToday = d == today
            )
            cell.withRipple()
            cell.setOnClickListener { pick(d) }
            cell.setPicked(d == selected)

            cells[d] = cell
            addGridCell(cell, 1 + i / 7, i % 7)
        }

        showInfo(selected)
    }

    // Mùng 1 hiện "1/9" (thêm "n" nếu tháng nhuận); các ngày khác chỉ hiện số ngày âm
    private fun lunarCellText(l: LunarDate): String =
        if (l.day == 1) "1/${l.month}${if (l.leap) "n" else ""}" else l.day.toString()

    private fun addGridCell(view: View, row: Int, col: Int) {
        view.layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(row, GridLayout.FILL, 1f),
            GridLayout.spec(col, GridLayout.FILL, 1f)
        ).apply {
            width = 0
            height = 0
        }
        grid.addView(view)
    }

    // ========================================================
    // THÔNG TIN NGÀY ĐANG CHỌN
    // ========================================================

    private fun showInfo(d: LocalDate) {
        val l = LunarCalendar.solarToLunar(d)

        bigDay.text = d.dayOfMonth.toString()
        dateLabel.text =
            "${THU_FULL[d.dayOfWeek.value - 1]}, ${d.dayOfMonth}/${d.monthValue}/${d.year}"

        lunarMonthLabel.text = "Tháng ${l.month}${if (l.leap) " nhuận" else ""} âm lịch"
        lunarDayLabel.text = l.day.toString()

        val term = LunarCalendar.tietKhi(d)
        termLabel.text =
            if (LunarCalendar.tietKhiBatDauHomNay(d)) "Hôm nay vào tiết $term" else "Tiết $term"

        yearCc.text = "Năm ${LunarCalendar.canChiYear(l.year)}"
        monthCc.text = "Tháng ${LunarCalendar.canChiMonth(l.year, l.month)}"
        dayCc.text = "Ngày ${LunarCalendar.canChiDay(d)}"
        // Hôm nay: giờ hiện tại. Ngày khác: giờ Tý (giờ đầu tiên của ngày)
        hourCc.text = if (d == LocalDate.now())
            "Giờ ${LunarCalendar.canChiHourNow(d, LocalTime.now())}"
        else
            "Giờ ${LunarCalendar.canChiHour(d, 0)}"

        val events = Holidays.of(d, l)
        eventLabel.text = events.joinToString(" · ") { it.name }
        eventLabel.setTextColor(if (events.any { it.off }) Ui.SUN else Ui.SUB)

        goodHoursLabel.text = "Giờ hoàng đạo: ${LunarCalendar.goodHours(d)}"
    }

    // ========================================================
    // ĐIỀU HƯỚNG
    // ========================================================

    private fun pick(d: LocalDate) {
        if (d.year != year || d.monthValue != month) {   // bấm ngày của tháng kề bên
            goTo(d)
            return
        }
        cells[selected]?.setPicked(false)
        selected = d
        cells[d]?.setPicked(true)
        showInfo(d)
    }

    private fun goTo(d: LocalDate) {
        if (d.year < MIN_YEAR || d.year > MAX_YEAR) return
        selected = d
        year = d.year
        month = d.monthValue
        refresh()
    }

    private fun changeMonth(step: Int) = goTo(selected.plusMonths(step.toLong()))

    private fun goToday() {
        today = LocalDate.now()
        goTo(today)
    }

    // Một hộp thoại chọn cả tháng lẫn năm
    private fun showPicker() {
        val monthPicker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 12
            displayedValues = Array(12) { "Tháng ${it + 1}" }
            value = month
            wrapSelectorWheel = true
        }
        val yearPicker = NumberPicker(this).apply {
            minValue = MIN_YEAR
            maxValue = MAX_YEAR
            value = year
            wrapSelectorWheel = false
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(8), dp(24), dp(8))
            addView(monthPicker)
            addView(yearPicker)
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn tháng / năm")
            .setView(box)
            .setPositiveButton("Chọn") { _, _ ->
                val first = LocalDate.of(yearPicker.value, monthPicker.value, 1)
                goTo(first.withDayOfMonth(minOf(selected.dayOfMonth, first.lengthOfMonth())))
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

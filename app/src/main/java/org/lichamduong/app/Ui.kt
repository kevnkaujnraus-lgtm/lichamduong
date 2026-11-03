package org.lichamduong.app

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/**
 * Nơi DUY NHẤT quy định font, cỡ chữ và màu của toàn bộ ứng dụng.
 * Muốn đổi font: bỏ file .ttf vào res/font rồi thay 2 dòng regular/bold bên dưới, ví dụ
 *   val regular = ResourcesCompat.getFont(context, R.font.ten_font)
 */
object Ui {

    // ---- Font: một họ chữ duy nhất, chỉ khác độ đậm ----
    val regular: Typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    val bold: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    // ---- Thang cỡ chữ: chỉ 4 mức (sp) ----
    const val XL = 52f   // ngày dương lớn
    const val L = 28f    // ngày âm, nút điều hướng
    const val M = 16f    // chữ chính
    const val S = 12f    // chữ phụ

    // ---- Bảng màu ----
    val TEXT: Int = Color.parseColor("#111111")
    val SUB: Int = Color.parseColor("#6B6B6B")
    val MUTED: Int = Color.parseColor("#BDBDBD")
    val SUN: Int = Color.parseColor("#D62828")
    val SAT: Int = Color.parseColor("#1825C9")
    val SELECT: Int = Color.parseColor("#3389F2")
    val TODAY_BG: Int = Color.parseColor("#E0F0FD")
    val BAR: Int = Color.parseColor("#EDEDED")
}

/** Áp dụng kiểu chữ thống nhất cho mọi TextView. */
fun TextView.style(size: Float, bold: Boolean = false, color: Int = Ui.TEXT) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    typeface = if (bold) Ui.bold else Ui.regular
    setTextColor(color)
    gravity = Gravity.CENTER
}

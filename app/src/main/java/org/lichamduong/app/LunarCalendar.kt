package org.lichamduong.app

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin

/**
 * Chuyển đổi dương lịch <-> âm lịch, tính Can Chi và tiết khí.
 * Được chuyển đổi (port) trực tiếp từ file lunar.py (thuật toán quen thuộc
 * của Hồ Ngọc Đức), giữ nguyên công thức để cho ra kết quả giống hệt bản Python.
 */
object LunarCalendar {

    private val CAN = listOf(
        "Giáp", "Ất", "Bính", "Đinh", "Mậu",
        "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
    )

    private val CHI = listOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
        "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    private val TIET = listOf(
        "Xuân phân", "Thanh minh", "Cốc vũ", "Lập hạ", "Tiểu mãn", "Mang chủng",
        "Hạ chí", "Tiểu thử", "Đại thử", "Lập thu", "Xử thử", "Bạch lộ",
        "Thu phân", "Hàn lộ", "Sương giáng", "Lập đông", "Tiểu tuyết", "Đại tuyết",
        "Đông chí", "Tiểu hàn", "Đại hàn", "Lập xuân", "Vũ thủy", "Kinh trập"
    )

    // Tương đương hàm INT() trong bản Python: cắt phần thập phân về phía 0
    private fun INT(x: Double): Long = x.toLong()

    // Số ngày Julius (Julian Day Number) từ ngày/tháng/năm dương lịch
    fun jdFromDate(dd: Int, mm: Int, yy: Int): Long {
        val a = INT((14.0 - mm) / 12.0)
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        return if (yy < 1582 || (yy == 1582 && mm < 10) || (yy == 1582 && mm == 10 && dd < 15)) {
            dd + INT((153.0 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - 32083
        } else {
            dd + INT((153.0 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) -
                INT(y / 100.0) + INT(y / 400.0) - 32045
        }
    }

    private fun newMoon(k: Double): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180

        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)

        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3

        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * m * dr)
        c1 -= 0.4068 * sin(mpr * dr) - 0.0161 * sin(2 * mpr * dr)
        c1 += 0.0104 * sin(2 * f * dr) - 0.0051 * sin((m + mpr) * dr)
        c1 -= 0.0074 * sin((m - mpr) * dr) + 0.0004 * sin((2 * f + m) * dr)
        c1 -= 0.0004 * sin((2 * f - m) * dr) - 0.0006 * sin((2 * f + mpr) * dr)
        c1 += 0.0010 * sin((2 * f - mpr) * dr) + 0.0005 * sin((2 * mpr + m) * dr)

        val dt = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }

        return jd1 + c1 - dt
    }

    private fun getNewMoonDay(k: Double, tz: Double = 7.0): Long =
        INT(newMoon(k) + 0.5 + tz / 24.0)

    // Python % luôn trả kết quả cùng dấu với số chia (mẫu số dương -> luôn >= 0).
    // Kotlin rem (%) giữ dấu số bị chia nên phải bù lại cho khớp.
    private fun pyMod(x: Double, m: Double): Double {
        val r = x % m
        return if (r < 0) r + m else r
    }

    private fun sunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = PI / 180

        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2

        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(2 * dr * m) + 0.000290 * sin(3 * dr * m)

        return pyMod((l0 + dl) * dr, 2 * PI)
    }

    private fun getSunLongitude(dayNumber: Long, tz: Double = 7.0): Long =
        INT(sunLongitude(dayNumber - 0.5 - tz / 24.0) / PI * 6.0)

    private fun getLunarMonth11(yy: Int, tz: Double = 7.0): Long {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = INT(off / 29.530588853)
        var nm = getNewMoonDay(k.toDouble(), tz)
        if (getSunLongitude(nm, tz) >= 9) {
            nm = getNewMoonDay((k - 1).toDouble(), tz)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Long, tz: Double = 7.0): Long {
        val k = INT(0.5 + (a11 - 2415021.076998695) / 29.530588853)
        var last = 0L
        var i = 1L
        var arc = getSunLongitude(getNewMoonDay((k + i).toDouble(), tz), tz)
        while (arc != last && i < 14) {
            last = arc
            i += 1
            arc = getSunLongitude(getNewMoonDay((k + i).toDouble(), tz), tz)
        }
        return i - 1
    }

    /**
     * Đổi ngày dương lịch sang âm lịch.
     * QUAN TRỌNG: tham số truyền vào đúng thứ tự (ngày, tháng, năm) - giống
     * hệt hàm solar_to_lunar(dd, mm, yy) trong lunar.py. Đây chính là chỗ
     * bản main.py cũ từng gọi sai thứ tự gây ra lỗi hiển thị kiểu "-8/65".
     *
     * @return Triple(ngày âm, tháng âm, năm âm)
     */
    fun solarToLunar(dd: Int, mm: Int, yy: Int, tz: Double = 7.0): Triple<Long, Long, Long> {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = INT((dayNumber - 2415021.076998695) / 29.530588853)

        var monthStart = getNewMoonDay((k + 1).toDouble(), tz)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k.toDouble(), tz)
        }

        var a11 = getLunarMonth11(yy, tz)
        var b11 = a11
        var lunarYear: Long

        if (a11 >= monthStart) {
            lunarYear = yy.toLong()
            a11 = getLunarMonth11(yy - 1, tz)
        } else {
            lunarYear = yy + 1L
            b11 = getLunarMonth11(yy + 1, tz)
        }

        val lunarDay = dayNumber - monthStart + 1
        val diff = INT((monthStart - a11) / 29.0)
        var lunarMonth = diff + 11

        if (b11 - a11 > 365) {
            val leapDiff = getLeapMonthOffset(a11, tz)
            if (diff >= leapDiff) {
                lunarMonth = diff + 10
            }
        }

        if (lunarMonth > 12) lunarMonth -= 12
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1

        return Triple(lunarDay, lunarMonth, lunarYear)
    }

    fun canChiYear(year: Long): String {
        val canIdx = (((year + 6) % 10 + 10) % 10).toInt()
        val chiIdx = (((year + 8) % 12 + 12) % 12).toInt()
        return "${CAN[canIdx]} ${CHI[chiIdx]}"
    }

    fun canChiMonth(lunarYear: Long, lunarMonth: Long): String {
        val canYear = (((lunarYear + 6) % 10 + 10) % 10).toInt()
        val firstCanMap = mapOf(
            0 to 2, 5 to 2,
            1 to 4, 6 to 4,
            2 to 6, 7 to 6,
            3 to 8, 8 to 8,
            4 to 0, 9 to 0
        )
        val firstCan = firstCanMap[canYear] ?: 0
        val canIdx = (((firstCan + lunarMonth - 1) % 10 + 10) % 10).toInt()
        val chiIdx = (((lunarMonth + 1) % 12 + 12) % 12).toInt()
        return "${CAN[canIdx]} ${CHI[chiIdx]}"
    }

    fun canChiDay(date: LocalDate): String {
        val jd = jdFromDate(date.dayOfMonth, date.monthValue, date.year)
        val canIdx = (((jd + 9) % 10 + 10) % 10).toInt()
        val chiIdx = (((jd + 1) % 12 + 12) % 12).toInt()
        return "${CAN[canIdx]} ${CHI[chiIdx]}"
    }

    fun tietKhi(date: LocalDate): String {
        val jd = jdFromDate(date.dayOfMonth, date.monthValue, date.year)
        val deg = sunLongitude(jd - 0.5 - 7.0 / 24.0) * 180.0 / PI
        val idx = (pyMod(deg + 7.5, 360.0) / 15.0).toInt()
        return TIET[idx.coerceIn(0, TIET.size - 1)]
    }

    // Khung giờ (theo giờ) tương ứng với từng Chi, theo đúng thứ tự CHI ở trên
    private val CHI_HOUR_RANGE = listOf(
        "23-1", "1-3", "3-5", "5-7", "7-9", "9-11",
        "11-13", "13-15", "15-17", "17-19", "19-21", "21-23"
    )

    // Bảng tra giờ hoàng đạo theo nhóm ngày (6 nhóm, mỗi nhóm gồm 2 Chi
    // đối xung cách nhau 6 vị trí: Tý-Ngọ, Sửu-Mùi, Dần-Thân, Mão-Dậu,
    // Thìn-Tuất, Tỵ-Hợi). Giá trị là chỉ số (0=Tý...11=Hợi) của các giờ
    // hoàng đạo trong ngày thuộc nhóm đó.
    private val GOOD_HOURS_TABLE = mapOf(
        0 to listOf(0, 1, 3, 6, 8, 9),   // Ngày Tý, Ngọ
        1 to listOf(2, 3, 5, 8, 10, 11), // Ngày Sửu, Mùi
        2 to listOf(0, 1, 4, 5, 7, 10),  // Ngày Dần, Thân
        3 to listOf(0, 2, 3, 6, 7, 9),   // Ngày Mão, Dậu
        4 to listOf(2, 4, 5, 8, 9, 11),  // Ngày Thìn, Tuất
        5 to listOf(1, 4, 6, 7, 10, 11)  // Ngày Tỵ, Hợi
    )

    /**
     * Trả về danh sách giờ hoàng đạo trong ngày, dạng chuỗi đã format sẵn,
     * ví dụ: "Sửu (1-3), Thìn (7-9), Tỵ (9-11), Mùi (13-15), Tuất (19-21)".
     * Kết quả phụ thuộc vào Chi của ngày dương lịch truyền vào nên sẽ
     * thay đổi theo từng ngày.
     */
    fun goodHours(date: LocalDate): String {
        val jd = jdFromDate(date.dayOfMonth, date.monthValue, date.year)
        val chiIdx = (((jd + 1) % 12 + 12) % 12).toInt()
        val group = chiIdx % 6
        val hours = GOOD_HOURS_TABLE[group] ?: emptyList()

        return hours.joinToString(", ") { idx ->
            "${CHI[idx]} (${CHI_HOUR_RANGE[idx]})"
        }
    }
}

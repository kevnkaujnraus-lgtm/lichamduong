package org.lichamduong.app

import java.time.LocalDate

data class Holiday(val name: String, val off: Boolean)

/** Ngày lễ / ngày kỷ niệm: khóa là (ngày to tháng). */
object Holidays {

    private val SOLAR = mapOf(
        (1 to 1) to Holiday("Tết Dương lịch", true),
        (3 to 2) to Holiday("Thành lập Đảng CSVN", false),
        (14 to 2) to Holiday("Valentine", false),
        (8 to 3) to Holiday("Quốc tế Phụ nữ", false),
        (26 to 3) to Holiday("Thành lập Đoàn TNCS HCM", false),
        (30 to 4) to Holiday("Giải phóng miền Nam", true),
        (1 to 5) to Holiday("Quốc tế Lao động", true),
        (7 to 5) to Holiday("Chiến thắng Điện Biên Phủ", false),
        (19 to 5) to Holiday("Sinh nhật Chủ tịch Hồ Chí Minh", false),
        (1 to 6) to Holiday("Quốc tế Thiếu nhi", false),
        (28 to 6) to Holiday("Ngày Gia đình Việt Nam", false),
        (27 to 7) to Holiday("Thương binh Liệt sĩ", false),
        (19 to 8) to Holiday("Cách mạng Tháng Tám", false),
        (2 to 9) to Holiday("Quốc khánh", true),
        (10 to 10) to Holiday("Giải phóng Thủ đô", false),
        (20 to 10) to Holiday("Phụ nữ Việt Nam", false),
        (20 to 11) to Holiday("Nhà giáo Việt Nam", false),
        (22 to 12) to Holiday("Quân đội Nhân dân Việt Nam", false),
        (24 to 12) to Holiday("Đêm Giáng sinh", false),
        (25 to 12) to Holiday("Giáng sinh", false)
    )

    private val LUNAR = mapOf(
        (1 to 1) to Holiday("Tết Nguyên Đán", true),
        (2 to 1) to Holiday("Mùng 2 Tết", true),
        (3 to 1) to Holiday("Mùng 3 Tết", true),
        (15 to 1) to Holiday("Tết Nguyên Tiêu", false),
        (3 to 3) to Holiday("Tết Hàn Thực", false),
        (10 to 3) to Holiday("Giỗ Tổ Hùng Vương", true),
        (15 to 4) to Holiday("Lễ Phật Đản", false),
        (5 to 5) to Holiday("Tết Đoan Ngọ", false),
        (15 to 7) to Holiday("Lễ Vu Lan", false),
        (15 to 8) to Holiday("Tết Trung Thu", false),
        (9 to 9) to Holiday("Tết Trùng Cửu", false),
        (23 to 12) to Holiday("Ông Táo chầu trời", false)
    )

    fun of(date: LocalDate, lunar: LunarDate): List<Holiday> {
        val out = mutableListOf<Holiday>()
        SOLAR[date.dayOfMonth to date.monthValue]?.let { out.add(it) }
        if (!lunar.leap) {
            LUNAR[lunar.day to lunar.month]?.let { out.add(it) }
            // Giao thừa: ngày cuối cùng của tháng Chạp (29 hoặc 30)
            if (lunar.month == 12 && lunar.day >= 29 &&
                LunarCalendar.solarToLunar(date.plusDays(1)).day == 1
            ) {
                out.add(Holiday("Giao thừa", true))
            }
        }
        return out
    }
}

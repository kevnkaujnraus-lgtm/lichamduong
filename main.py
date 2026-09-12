from datetime import date
from calendar import monthrange

from kivy.app import App
from kivy.metrics import dp
from kivy.core.window import Window
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.button import Button
from kivy.graphics import Color, Line

from lunar import (
    solar_to_lunar,
    can_chi_day,
    can_chi_month,
    can_chi_year,
    tiet_khi,
)


# ============================================================
# CỬA SỔ
# ============================================================

Window.clearcolor = (1, 1, 1, 1)


# ============================================================
# THỨ
# ============================================================

THU = [
    "T2",
    "T3",
    "T4",
    "T5",
    "T6",
    "T7",
    "CN",
]

THU_FULL = [
    "Thứ Hai",
    "Thứ Ba",
    "Thứ Tư",
    "Thứ Năm",
    "Thứ Sáu",
    "Thứ Bảy",
    "Chủ Nhật",
]


# ============================================================
# Ô LỊCH
# ============================================================

class Cell(Button):

    def __init__(
        self,
        solar_day=None,
        lunar_day=None,
        lunar_month=None,
        muted=False,
        sunday=False,
        saturday=False,
        today=False,
        **kwargs
    ):
        super().__init__(**kwargs)

        self.background_normal = ""
        self.background_down = ""
        self.border = (0, 0, 0, 0)

        self.background_color = (1, 1, 1, 1)
        self.color = (0, 0, 0, 1)

        self.markup = True
        self.halign = "center"
        self.valign = "middle"

        self.solar_day = solar_day

        # Ô trống
        if solar_day is None:
            self.text = ""
            return

        # ----------------------------------------------------
        # MÀU NGÀY
        # ----------------------------------------------------

        if muted:
            main_color = "bdbdbd"
            sub_color = "c7c7c7"

        elif sunday:
            main_color = "d62828"
            sub_color = "c62828"

        elif saturday:
            main_color = "1825c9"
            sub_color = "1825c9"

        else:
            main_color = "111111"
            sub_color = "111111"

        # ----------------------------------------------------
        # HIỂN THỊ
        # ----------------------------------------------------

        self.text = (
            f"[size=31][color={main_color}]{solar_day}[/color][/size]\n"
            f"[size=17][color={sub_color}]"
            f"{lunar_day}/{lunar_month}"
            f"[/color][/size]"
        )

        # ----------------------------------------------------
        # ĐÁNH DẤU HÔM NAY
        # ----------------------------------------------------

        if today:
            self.background_color = (
                0.88,
                0.94,
                0.99,
                1
            )

        self.bind(
            pos=self._draw_border,
            size=self._draw_border
        )

    def _draw_border(self, *args):

        self.canvas.after.clear()

        with self.canvas.after:

            Color(
                0.84,
                0.84,
                0.84,
                1
            )

            Line(
                rectangle=(
                    self.x,
                    self.y,
                    self.width,
                    self.height
                ),
                width=0.7
            )


# ============================================================
# LỊCH
# ============================================================

class Calendar(BoxLayout):

    def __init__(self, **kwargs):

        super().__init__(
            orientation="vertical",
            **kwargs
        )

        self.today = date.today()

        self.selected = self.today

        self.year = self.today.year
        self.month = self.today.month

        self.make_ui()

        self.refresh()

    # ========================================================
    # LABEL
    # ========================================================

    def label(
        self,
        text="",
        size=16,
        bold=False,
        height=None
    ):

        widget = Label(
            text=text,
            font_size=dp(size),
            bold=bold,
            color=(0, 0, 0, 1),
            halign="center",
            valign="middle",
            size_hint_y=None,
            height=dp(
                height if height else 35
            )
        )

        widget.bind(
            size=lambda obj, value:
            setattr(obj, "text_size", value)
        )

        return widget

    # ========================================================
    # GIAO DIỆN
    # ========================================================

    def make_ui(self):

        self.padding = [
            dp(2),
            dp(2),
            dp(2),
            dp(0)
        ]

        self.spacing = dp(2)

        # ----------------------------------------------------
        # THÁNG / NĂM
        # ----------------------------------------------------

        self.month_title = self.label(
            "",
            22,
            False,
            43
        )

        self.add_widget(
            self.month_title
        )

        # ----------------------------------------------------
        # NGÀY DƯƠNG
        # ----------------------------------------------------

        self.big_day = self.label(
            "",
            66,
            True,
            82
        )

        self.add_widget(
            self.big_day
        )

        # ----------------------------------------------------
        # THỨ
        # ----------------------------------------------------

        self.weekday = self.label(
            "",
            25,
            True,
            39
        )

        self.add_widget(
            self.weekday
        )

        # ----------------------------------------------------
        # THÔNG TIN ÂM LỊCH
        # ----------------------------------------------------

        info = BoxLayout(
            size_hint_y=None,
            height=dp(116),
            spacing=dp(3)
        )

        # ----------------------------------------------------
        # BÊN TRÁI
        # ----------------------------------------------------

        left = BoxLayout(
            orientation="vertical"
        )

        self.lunar_month = self.label(
            "",
            22,
            False,
            30
        )

        self.lunar_day = self.label(
            "",
            40,
            False,
            47
        )

        self.tiet = self.label(
            "",
            20,
            False,
            34
        )

        left.add_widget(
            self.lunar_month
        )

        left.add_widget(
            self.lunar_day
        )

        left.add_widget(
            self.tiet
        )

        # ----------------------------------------------------
        # BÊN PHẢI
        # ----------------------------------------------------

        right = BoxLayout(
            orientation="vertical"
        )

        self.year_cc = self.label(
            "",
            20,
            False,
            28
        )

        self.month_cc = self.label(
            "",
            20,
            False,
            28
        )

        self.day_cc = self.label(
            "",
            20,
            False,
            28
        )

        self.hour_cc = self.label(
            "",
            20,
            False,
            28
        )

        right.add_widget(
            self.year_cc
        )

        right.add_widget(
            self.month_cc
        )

        right.add_widget(
            self.day_cc
        )

        right.add_widget(
            self.hour_cc
        )

        info.add_widget(left)
        info.add_widget(right)

        self.add_widget(info)

        # ----------------------------------------------------
        # GIỜ HOÀNG ĐẠO
        # ----------------------------------------------------

        self.good_hours = self.label(
            "",
            16,
            False,
            37
        )

        self.add_widget(
            self.good_hours
        )

        # ----------------------------------------------------
        # THANH ĐIỀU HƯỚNG
        # ----------------------------------------------------

        nav = BoxLayout(
            size_hint_y=None,
            height=dp(73),
            spacing=dp(4)
        )

        self.prev = Button(
            text="<",
            font_size=dp(42),
            background_normal="",
            background_color=(
                0.72,
                0.72,
                0.72,
                1
            ),
            color=(0, 0, 0, 1)
        )

        self.month_btn = Button(
            text="",
            font_size=dp(42),
            background_normal="",
            background_color=(
                0.72,
                0.72,
                0.72,
                1
            ),
            color=(0, 0, 0, 1)
        )

        self.year_btn = Button(
            text="",
            font_size=dp(42),
            background_normal="",
            background_color=(
                0.72,
                0.72,
                0.72,
                1
            ),
            color=(0, 0, 0, 1)
        )

        self.next = Button(
            text=">",
            font_size=dp(42),
            background_normal="",
            background_color=(
                0.72,
                0.72,
                0.72,
                1
            ),
            color=(0, 0, 0, 1)
        )

        self.prev.bind(
            on_release=lambda *_:
            self.change_month(-1)
        )

        self.next.bind(
            on_release=lambda *_:
            self.change_month(1)
        )

        self.month_btn.bind(
            on_release=lambda *_:
            self.go_today()
        )

        self.year_btn.bind(
            on_release=lambda *_:
            self.go_today()
        )

        nav.add_widget(self.prev)
        nav.add_widget(self.month_btn)
        nav.add_widget(self.year_btn)
        nav.add_widget(self.next)

        self.add_widget(nav)

        # ----------------------------------------------------
        # LƯỚI LỊCH
        # ----------------------------------------------------

        self.grid = GridLayout(
            cols=7,
            rows=7,
            spacing=0,
            size_hint_y=1
        )

        self.add_widget(
            self.grid
        )

    # ========================================================
    # REFRESH LỊCH
    # ========================================================

    def refresh(self):

        self.grid.clear_widgets()

        self.month_title.text = (
            f"Tháng {self.month} năm {self.year}"
        )

        self.month_btn.text = (
            f"T{self.month}"
        )

        self.year_btn.text = (
            str(self.year)
        )

        # ----------------------------------------------------
        # TÊN THỨ
        # ----------------------------------------------------

        for i, weekday_name in enumerate(THU):

            if i == 6:

                color = (
                    0.80,
                    0.05,
                    0.05,
                    1
                )

            elif i == 5:

                color = (
                    0.05,
                    0.10,
                    0.75,
                    1
                )

            else:

                color = (
                    0,
                    0,
                    0,
                    1
                )

            self.grid.add_widget(
                Label(
                    text=weekday_name,
                    font_size=dp(25),
                    bold=True,
                    color=color
                )
            )

        # ----------------------------------------------------
        # NGÀY ĐẦU THÁNG
        # ----------------------------------------------------

        first = date(
            self.year,
            self.month,
            1
        ).weekday()

        # ----------------------------------------------------
        # SỐ NGÀY TRONG THÁNG
        # ----------------------------------------------------

        days = monthrange(
            self.year,
            self.month
        )[1]

        # ----------------------------------------------------
        # THÁNG TRƯỚC
        # ----------------------------------------------------

        prev_y = self.year
        prev_m = self.month - 1

        if prev_m == 0:
            prev_m = 12
            prev_y -= 1

        prev_days = monthrange(
            prev_y,
            prev_m
        )[1]

        # ----------------------------------------------------
        # CÁC NGÀY CUỐI THÁNG TRƯỚC
        # ----------------------------------------------------

        for index in range(first):

            day = (
                prev_days
                - first
                + index
                + 1
            )

            lunar = solar_to_lunar(
                prev_y,
                prev_m,
                day
            )

            self.grid.add_widget(
                Cell(
                    day,
                    lunar[0],
                    lunar[1],
                    muted=True
                )
            )

        # ----------------------------------------------------
        # NGÀY TRONG THÁNG HIỆN TẠI
        # ----------------------------------------------------

        for day in range(1, days + 1):

            dt = date(
                self.year,
                self.month,
                day
            )

            lunar = solar_to_lunar(
                dt.year,
                dt.month,
                dt.day
            )

            cell = Cell(
                day,
                lunar[0],
                lunar[1],
                sunday=(
                    dt.weekday() == 6
                ),
                saturday=(
                    dt.weekday() == 5
                ),
                today=(
                    dt == self.today
                )
            )

            cell.bind(
                on_release=lambda obj,
                selected_date=dt:
                self.select(selected_date)
            )

            self.grid.add_widget(cell)

        # ----------------------------------------------------
        # NGÀY THÁNG SAU
        # ----------------------------------------------------

        total = first + days

        next_day = 1

        while total < 42:

            next_m = self.month + 1
            next_y = self.year

            if next_m == 13:
                next_m = 1
                next_y += 1

            lunar = solar_to_lunar(
                next_y,
                next_m,
                next_day
            )

            self.grid.add_widget(
                Cell(
                    next_day,
                    lunar[0],
                    lunar[1],
                    muted=True
                )
            )

            next_day += 1
            total += 1

        # ----------------------------------------------------
        # ĐẢM BẢO NGÀY ĐANG CHỌN HỢP LỆ
        # ----------------------------------------------------

        if (
            self.selected.year != self.year
            or self.selected.month != self.month
        ):

            max_day = days

            day = min(
                self.selected.day,
                max_day
            )

            self.selected = date(
                self.year,
                self.month,
                day
            )

        self.select(
            self.selected
        )

    # ========================================================
    # CHỌN NGÀY
    # ========================================================

    def select(self, dt):

        if not isinstance(dt, date):
            return

        self.selected = dt

        lunar = solar_to_lunar(
            dt.year,
            dt.month,
            dt.day
        )

        # ----------------------------------------------------
        # KIỂM TRA DỮ LIỆU ÂM LỊCH
        # ----------------------------------------------------

        if not lunar or len(lunar) < 3:

            lunar_day = "--"
            lunar_month = "--"
            lunar_year = dt.year

        else:

            lunar_day = lunar[0]
            lunar_month = lunar[1]
            lunar_year = lunar[2]

        # ----------------------------------------------------
        # NGÀY DƯƠNG
        # ----------------------------------------------------

        self.big_day.text = str(
            dt.day
        )

        # ----------------------------------------------------
        # THỨ
        # ----------------------------------------------------

        self.weekday.text = (
            THU_FULL[dt.weekday()]
        )

        # ----------------------------------------------------
        # NGÀY ÂM
        # ----------------------------------------------------

        self.lunar_month.text = (
            f"Tháng {lunar_month}"
        )

        self.lunar_day.text = (
            str(lunar_day)
        )

        # ----------------------------------------------------
        # TIẾT KHÍ
        # ----------------------------------------------------

        try:

            self.tiet.text = (
                f"Tiết {tiet_khi(dt)}"
            )

        except Exception:

            self.tiet.text = (
                "Tiết khí: --"
            )

        # ----------------------------------------------------
        # CAN CHI NĂM
        # ----------------------------------------------------

        try:

            self.year_cc.text = (
                f"Năm {can_chi_year(lunar_year)}"
            )

        except Exception:

            self.year_cc.text = (
                "Năm --"
            )

        # ----------------------------------------------------
        # CAN CHI THÁNG
        # ----------------------------------------------------

        try:

            self.month_cc.text = (
                f"Tháng "
                f"{can_chi_month(lunar_year, lunar_month)}"
            )

        except Exception:

            self.month_cc.text = (
                "Tháng --"
            )

        # ----------------------------------------------------
        # CAN CHI NGÀY
        # ----------------------------------------------------

        try:

            self.day_cc.text = (
                f"Ngày {can_chi_day(dt)}"
            )

        except Exception:

            self.day_cc.text = (
                "Ngày --"
            )

        # ----------------------------------------------------
        # GIỜ CAN CHI
        # ----------------------------------------------------

        self.hour_cc.text = (
            "Giờ Canh Tý"
        )

        # ----------------------------------------------------
        # GIỜ HOÀNG ĐẠO
        # ----------------------------------------------------

        self.good_hours.text = (
            "Giờ hoàng đạo: "
            "Sửu (1-3), "
            "Thìn (7-9), "
            "Ngọ (11-13), "
            "Mùi (13-15), "
            "Tuất (19-21), "
            "Hợi (21-23)"
        )

    # ========================================================
    # CHUYỂN THÁNG
    # ========================================================

    def change_month(self, step):

        new_month = self.month + step
        new_year = self.year

        if new_month < 1:

            new_month = 12
            new_year -= 1

        elif new_month > 12:

            new_month = 1
            new_year += 1

        self.month = new_month
        self.year = new_year

        max_day = monthrange(
            self.year,
            self.month
        )[1]

        day = min(
            self.selected.day,
            max_day
        )

        self.selected = date(
            self.year,
            self.month,
            day
        )

        self.refresh()

    # ========================================================
    # VỀ HÔM NAY
    # ========================================================

    def go_today(self):

        self.today = date.today()

        self.selected = self.today

        self.month = self.today.month
        self.year = self.today.year

        self.refresh()


# ============================================================
# APP
# ============================================================

class LichAmDuongApp(App):

    title = "Lịch Âm Dương"

    def build(self):

        return Calendar()


# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":

    LichAmDuongApp().run()
from datetime import date
import math

PI = math.pi
CAN = ["Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý"]
CHI = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"]

def INT(x): return int(x)

def jd_from_date(dd, mm, yy):
    a = INT((14-mm)/12)
    y = yy + 4800 - a
    m = mm + 12*a - 3
    if yy < 1582 or (yy == 1582 and mm < 10) or (yy == 1582 and mm == 10 and dd < 15):
        return dd + INT((153*m+2)/5) + 365*y + INT(y/4) - 32083
    return dd + INT((153*m+2)/5) + 365*y + INT(y/4) - INT(y/100) + INT(y/400) - 32045

def new_moon(k):
    T=k/1236.85; T2=T*T; T3=T2*T
    dr=PI/180
    Jd1=2415020.75933+29.53058868*k+0.0001178*T2-0.000000155*T3
    Jd1+=0.00033*math.sin((166.56+132.87*T-0.009173*T2)*dr)
    M=359.2242+29.10535608*k-0.0000333*T2-0.00000347*T3
    Mpr=306.0253+385.81691806*k+0.0107306*T2+0.00001236*T3
    F=21.2964+390.67050646*k-0.0016528*T2-0.00000239*T3
    C1=(0.1734-0.000393*T)*math.sin(M*dr)+0.0021*math.sin(2*M*dr)
    C1-=0.4068*math.sin(Mpr*dr)-0.0161*math.sin(2*Mpr*dr)
    C1+=0.0104*math.sin(2*F*dr)-0.0051*math.sin((M+Mpr)*dr)
    C1-=0.0074*math.sin((M-Mpr)*dr)+0.0004*math.sin((2*F+M)*dr)
    C1-=0.0004*math.sin((2*F-M)*dr)-0.0006*math.sin((2*F+Mpr)*dr)
    C1+=0.0010*math.sin((2*F-Mpr)*dr)+0.0005*math.sin((2*Mpr+M)*dr)
    if T < -11:
        dt=0.001+0.000839*T+0.0002261*T2-0.00000845*T3-0.000000081*T*T3
    else:
        dt=-0.000278+0.000265*T+0.000262*T2
    return Jd1+C1-dt

def get_new_moon_day(k,tz=7):
    return INT(new_moon(k)+0.5+tz/24)

def sun_longitude(jdn):
    T=(jdn-2451545.0)/36525; T2=T*T; dr=PI/180
    M=357.52910+35999.05030*T-0.0001559*T2-0.00000048*T*T2
    L0=280.46645+36000.76983*T+0.0003032*T2
    DL=(1.914600-0.004817*T-0.000014*T2)*math.sin(dr*M)
    DL+=(0.019993-0.000101*T)*math.sin(2*dr*M)+0.000290*math.sin(3*dr*M)
    return (L0+DL)*dr % (2*PI)

def get_sun_longitude(day_number,tz=7):
    return INT(sun_longitude(day_number-0.5-tz/24)/PI*6)

def get_lunar_month_11(yy,tz=7):
    off=jd_from_date(31,12,yy)-2415021
    k=INT(off/29.530588853)
    nm=get_new_moon_day(k,tz)
    if get_sun_longitude(nm,tz)>=9: nm=get_new_moon_day(k-1,tz)
    return nm

def get_leap_month_offset(a11,tz=7):
    k=INT(0.5+(a11-2415021.076998695)/29.530588853)
    last=0; i=1
    arc=get_sun_longitude(get_new_moon_day(k+i,tz),tz)
    while arc != last and i < 14:
        last=arc; i+=1
        arc=get_sun_longitude(get_new_moon_day(k+i,tz),tz)
    return i-1

def solar_to_lunar(dd,mm,yy,tz=7):
    day_number=jd_from_date(dd,mm,yy)
    k=INT((day_number-2415021.076998695)/29.530588853)
    month_start=get_new_moon_day(k+1,tz)
    if month_start>day_number: month_start=get_new_moon_day(k,tz)
    a11=get_lunar_month_11(yy,tz); b11=a11
    if a11>=month_start:
        lunar_year=yy; a11=get_lunar_month_11(yy-1,tz)
    else:
        lunar_year=yy+1; b11=get_lunar_month_11(yy+1,tz)
    lunar_day=day_number-month_start+1
    diff=INT((month_start-a11)/29)
    lunar_month=diff+11; leap=0
    if b11-a11>365:
        leap_diff=get_leap_month_offset(a11,tz)
        if diff>=leap_diff:
            lunar_month=diff+10
            if diff==leap_diff: leap=1
    if lunar_month>12: lunar_month-=12
    if lunar_month>=11 and diff<4: lunar_year-=1
    return lunar_day,lunar_month,lunar_year

def can_chi_year(year):
    return CAN[(year+6)%10]+" "+CHI[(year+8)%12]

def can_chi_month(lunar_year,lunar_month):
    # Can tháng: Dần là Bính nếu năm Giáp/Kỷ, Can tiếp tục tuần tự.
    can_year=(lunar_year+6)%10
    first_can={0:2,5:2,1:4,6:4,2:6,7:6,3:8,8:8,4:0,9:0}[can_year]
    return CAN[(first_can+lunar_month-1)%10]+" "+CHI[(lunar_month+1)%12]

def can_chi_day(d):
    jd=jd_from_date(d.day,d.month,d.year)
    return CAN[(jd+9)%10]+" "+CHI[(jd+1)%12]

TIET = ["Xuân phân","Thanh minh","Cốc vũ","Lập hạ","Tiểu mãn","Mang chủng",
        "Hạ chí","Tiểu thử","Đại thử","Lập thu","Xử thử","Bạch lộ",
        "Thu phân","Hàn lộ","Sương giáng","Lập đông","Tiểu tuyết","Đại tuyết",
        "Đông chí","Tiểu hàn","Đại hàn","Lập xuân","Vũ thủy","Kinh trập"]

def tiet_khi(d):
    deg=sun_longitude(jd_from_date(d.day,d.month,d.year)-0.5-7/24)*180/PI
    return TIET[int(((deg+7.5)%360)/15)]

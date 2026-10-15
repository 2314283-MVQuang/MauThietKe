package com.trasua.pattern;

import com.trasua.entity.EmploymentType;
import com.trasua.pattern.decorator.salary.PhuCapCuoiTuanDecorator;
import com.trasua.pattern.decorator.salary.SalaryDecorator;
import com.trasua.pattern.decorator.salary.ThuongChuyenCanDecorator;
import com.trasua.pattern.strategy.salary.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử tính lương: STRATEGY (theo giờ / lương cứng) + DECORATOR (phụ cấp, thưởng). */
class SalaryPatternTest {

    private static final BigDecimal RATE = BigDecimal.valueOf(25_000);
    /** 14/10/2026 là Thứ 4. */
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 14);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 10, 17);

    private static ShiftInfo shift(LocalDate day, int fromHour, int hours, BigDecimal rate) {
        LocalDateTime in = day.atTime(fromHour, 0);
        return new ShiftInfo(in, in.plusHours(hours), rate);
    }

    private static void assertMoney(long expected, BigDecimal actual) {
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual), "mong đợi " + expected + " nhưng là " + actual);
    }

    @Test
    void banThoiGian_3Ca4Gio_300Nghin() {
        List<ShiftInfo> shifts = List.of(shift(WEDNESDAY, 8, 4, RATE), shift(WEDNESDAY.plusDays(1), 8, 4, RATE),
                shift(WEDNESDAY.minusDays(1), 8, 4, RATE));
        SalaryBreakdown result = new PartTimeHourlyStrategy().calculate(new SalaryInput(shifts, RATE, null));
        assertMoney(300_000, result.getTotal());
        assertEquals(720, new SalaryInput(shifts, RATE, null).totalMinutes());
    }

    @Test
    void banThoiGian_dungDonGiaCuaTungCa() {
        // Ca 1 lương cũ 25.000, ca 2 sau khi tăng lương 30.000 -> 4x25.000 + 4x30.000
        List<ShiftInfo> shifts = List.of(shift(WEDNESDAY, 8, 4, RATE), shift(WEDNESDAY.plusDays(1), 8, 4, BigDecimal.valueOf(30_000)));
        assertMoney(220_000, new PartTimeHourlyStrategy().calculate(new SalaryInput(shifts, BigDecimal.valueOf(30_000), null)).getTotal());
    }

    @Test
    void banThoiGian_tinhTheoPhut() {
        // 4 giờ 15 phút x 25.000 = 106.250
        ShiftInfo s = new ShiftInfo(WEDNESDAY.atTime(8, 0), WEDNESDAY.atTime(12, 15), RATE);
        assertMoney(106_250, new PartTimeHourlyStrategy().calculate(new SalaryInput(List.of(s), RATE, null)).getTotal());
        assertEquals("4,25", PayrollFormat.hours(s.minutes()));
    }

    @Test
    void toanThoiGian_luongCungVaLamThem() {
        BigDecimal rate = BigDecimal.valueOf(32_000);
        List<ShiftInfo> shifts = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            shifts.add(shift(WEDNESDAY.minusDays(i), 7, 8, rate));          // 208 giờ chuẩn
        }
        SalaryInput normal = new SalaryInput(shifts, rate, BigDecimal.valueOf(6_500_000));
        assertMoney(6_500_000, new FullTimeMonthlyStrategy().calculate(normal).getTotal());

        shifts.add(shift(WEDNESDAY.minusDays(27), 7, 2, rate));             // +2 giờ làm thêm
        SalaryBreakdown overtime = new FullTimeMonthlyStrategy().calculate(new SalaryInput(shifts, rate, BigDecimal.valueOf(6_500_000)));
        assertMoney(6_500_000 + 2 * 32_000 * 3 / 2, overtime.getTotal());   // 2h x 32.000 x 1,5 = 96.000
        assertEquals(2, overtime.getLines().size());
    }

    @Test
    void context_doiChienLuocLucChay() {
        SalaryInput input = new SalaryInput(List.of(shift(WEDNESDAY, 8, 4, RATE)), RATE, BigDecimal.valueOf(5_000_000));
        PayrollCalculator calculator = new PayrollCalculator(new PartTimeHourlyStrategy());
        assertMoney(100_000, calculator.calculate(input).getTotal());
        calculator.setStrategy(new FullTimeMonthlyStrategy());                // cùng dữ liệu, đổi cách tính
        assertMoney(5_000_000, calculator.calculate(input).getTotal());
        assertThrows(IllegalArgumentException.class, () -> calculator.setStrategy(null));
    }

    @Test
    void decorator_phuCapCuoiTuan20PhanTram() {
        List<ShiftInfo> shifts = List.of(shift(WEDNESDAY, 8, 4, RATE), shift(SATURDAY, 8, 5, RATE));
        SalaryStrategy strategy = new PhuCapCuoiTuanDecorator(new PartTimeHourlyStrategy());
        SalaryBreakdown result = strategy.calculate(new SalaryInput(shifts, RATE, null));
        // 9 giờ x 25.000 = 225.000 + 20% của 5 giờ T7 (125.000) = 25.000
        assertMoney(250_000, result.getTotal());
        assertEquals(2, result.getLines().size());
        assertTrue(strategy.getName().contains("phụ cấp cuối tuần"));
    }

    @Test
    void decorator_khongDuDieuKienThiKhongCong() {
        SalaryStrategy strategy = new ThuongChuyenCanDecorator(new PhuCapCuoiTuanDecorator(new PartTimeHourlyStrategy()));
        SalaryBreakdown result = strategy.calculate(new SalaryInput(List.of(shift(WEDNESDAY, 8, 4, RATE)), RATE, null));
        assertMoney(100_000, result.getTotal());
        assertEquals(1, result.getLines().size());
    }

    @Test
    void decorator_thuongChuyenCanKhiDu20Ca() {
        List<ShiftInfo> shifts = new ArrayList<>();
        LocalDate monday = LocalDate.of(2026, 10, 5);
        for (int week = 0; week < 4; week++) {
            for (int d = 0; d < 5; d++) {                                    // 20 ca Thứ 2 - Thứ 6
                shifts.add(shift(monday.plusWeeks(week).plusDays(d), 17, 4, RATE));
            }
        }
        SalaryStrategy strategy = new ThuongChuyenCanDecorator(new PartTimeHourlyStrategy());
        assertMoney(20 * 100_000 + 200_000, strategy.calculate(new SalaryInput(shifts, RATE, null)).getTotal());
    }

    @Test
    void factory_chonChienLuocTheoLoaiNhanVien() {
        SalaryStrategy part = SalaryStrategyFactory.forType(EmploymentType.PART_TIME);
        SalaryStrategy full = SalaryStrategyFactory.forType(EmploymentType.FULL_TIME);
        assertInstanceOf(ThuongChuyenCanDecorator.class, part);
        SalaryStrategy inner = ((SalaryDecorator) part).getWrapped();
        assertInstanceOf(PhuCapCuoiTuanDecorator.class, inner);
        assertInstanceOf(PartTimeHourlyStrategy.class, ((SalaryDecorator) inner).getWrapped());
        assertInstanceOf(FullTimeMonthlyStrategy.class, ((SalaryDecorator) full).getWrapped());
    }

    @Test
    void caKhongHopLe() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShiftInfo(WEDNESDAY.atTime(12, 0), WEDNESDAY.atTime(8, 0), RATE));
    }
}

package com.trasua.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Bộ lọc khoảng thời gian cho thống kê: theo NGÀY / TUẦN (T2-CN) / THÁNG / TẤT CẢ.
 * Đọc từ tham số URL ?bs=day|week|month|all&bsDate=yyyy-MM-dd ; không cho chọn thời gian chưa tới.
 */
public final class PeriodFilter {

    public enum Mode { DAY, WEEK, MONTH, ALL }

    private final Mode mode;
    private final LocalDate anchor;       // ngày đại diện của khoảng đang xem
    private final LocalDate today = LocalDate.now();

    private PeriodFilter(Mode mode, LocalDate anchor) {
        this.mode = mode;
        this.anchor = anchor.isAfter(today) ? today : anchor;
    }

    public static PeriodFilter parse(String modeParam, String dateParam) {
        Mode mode = switch (modeParam == null ? "" : modeParam) {
            case "day" -> Mode.DAY;
            case "week" -> Mode.WEEK;
            case "all" -> Mode.ALL;
            default -> Mode.MONTH;
        };
        LocalDate anchor = LocalDate.now();
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                anchor = dateParam.length() == 7 ? YearMonth.parse(dateParam).atDay(1) : LocalDate.parse(dateParam);
            } catch (DateTimeParseException ignored) {
                // sai định dạng -> hôm nay
            }
        }
        return new PeriodFilter(mode, anchor);
    }

    public LocalDate getStartDate() {
        return switch (mode) {
            case DAY -> anchor;
            case WEEK -> anchor.with(DayOfWeek.MONDAY);
            case MONTH -> anchor.withDayOfMonth(1);
            case ALL -> null;
        };
    }

    /** Ngày kết thúc (bao gồm). */
    public LocalDate getEndDate() {
        return switch (mode) {
            case DAY -> anchor;
            case WEEK -> anchor.with(DayOfWeek.SUNDAY);
            case MONTH -> YearMonth.from(anchor).atEndOfMonth();
            case ALL -> null;
        };
    }

    public LocalDateTime getFrom() {
        return mode == Mode.ALL ? null : getStartDate().atStartOfDay();
    }

    public LocalDateTime getTo() {
        return mode == Mode.ALL ? null : getEndDate().plusDays(1).atStartOfDay();
    }

    public String getMode() {
        return mode.name().toLowerCase();
    }

    /** Giá trị cho ô chọn: yyyy-MM-dd (ngày, tuần) hoặc yyyy-MM (tháng). */
    public String getInputValue() {
        return mode == Mode.MONTH ? YearMonth.from(anchor).toString() : anchor.toString();
    }

    public String getMaxDate() { return today.toString(); }
    public String getMaxMonth() { return YearMonth.from(today).toString(); }

    /** Nhãn hiển thị: "Hôm nay 15/10", "Tuần 12/10 – 18/10", "Tháng 10/2026", "Toàn thời gian". */
    public String getLabel() {
        return switch (mode) {
            case DAY -> (anchor.equals(today) ? "Hôm nay " : anchor.equals(today.minusDays(1)) ? "Hôm qua " : "Ngày ")
                    + fmt(anchor) + "/" + anchor.getYear();
            case WEEK -> "Tuần " + fmt(getStartDate()) + " – " + fmt(getEndDate())
                    + (getStartDate().equals(today.with(DayOfWeek.MONDAY)) ? " (tuần này)" : "");
            case MONTH -> "Tháng " + String.format("%02d/%d", anchor.getMonthValue(), anchor.getYear())
                    + (YearMonth.from(anchor).equals(YearMonth.from(today)) ? " (tháng này)" : "");
            case ALL -> "Toàn thời gian";
        };
    }

    /** Query string cho khoảng trước / sau (null nếu không có). */
    public String getPrevQuery() {
        return switch (mode) {
            case DAY -> q(anchor.minusDays(1).toString());
            case WEEK -> q(anchor.minusWeeks(1).toString());
            case MONTH -> q(YearMonth.from(anchor).minusMonths(1).toString());
            case ALL -> null;
        };
    }

    public String getNextQuery() {
        LocalDate next = switch (mode) {
            case DAY -> anchor.plusDays(1);
            case WEEK -> anchor.plusWeeks(1).with(DayOfWeek.MONDAY);
            case MONTH -> YearMonth.from(anchor).plusMonths(1).atDay(1);
            case ALL -> null;
        };
        if (next == null || next.isAfter(today)) {
            return null;   // khoảng sau chưa tới
        }
        return q(mode == Mode.MONTH ? YearMonth.from(next).toString() : next.toString());
    }

    private String q(String date) {
        return "bs=" + getMode() + "&bsDate=" + date;
    }

    private static String fmt(LocalDate d) {
        return String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue());
    }
}

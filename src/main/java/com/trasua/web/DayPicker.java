package com.trasua.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thanh chọn ngày: Hôm kia / Hôm qua / Hôm nay + 7 ngày của tuần đang xem.
 * Ngày chưa tới bị "bôi đen" và không bấm được.
 */
public final class DayPicker {

    private static final String[] DOW = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    /** Một ô ngày trong tuần. */
    public static class Day {
        private final LocalDate date;
        private final long count;
        private final boolean selected;
        private final boolean today;
        private final boolean future;
        private final String query;

        Day(LocalDate date, long count, boolean selected, boolean today, boolean future, String query) {
            this.date = date;
            this.count = count;
            this.selected = selected;
            this.today = today;
            this.future = future;
            this.query = query;
        }

        public LocalDate getDate() { return date; }
        public String getDow() { return DOW[date.getDayOfWeek().getValue() - 1]; }
        public String getDayMonth() { return String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue()); }
        public long getCount() { return count; }
        public boolean isSelected() { return selected; }
        public boolean isToday() { return today; }
        public boolean isFuture() { return future; }
        public String getQuery() { return query; }
    }

    /** Nút nhanh (Hôm nay, Hôm qua, Hôm kia). */
    public static class Quick {
        private final String label;
        private final String query;
        private final boolean active;

        Quick(String label, String query, boolean active) {
            this.label = label;
            this.query = query;
            this.active = active;
        }

        public String getLabel() { return label; }
        public String getQuery() { return query; }
        public boolean isActive() { return active; }
    }

    private final LocalDate selected;
    private final LocalDate today;
    private final List<Day> days = new ArrayList<>();
    private final List<Quick> quicks = new ArrayList<>();
    private final String prevWeekQuery;
    private final String nextWeekQuery;

    /**
     * @param selected   ngày đang xem
     * @param counts     số đơn theo ngày (của tuần đang xem)
     * @param extraQuery tham số giữ lại khi đổi ngày, VD "&status=HOAN_THANH" (hoặc "")
     */
    public DayPicker(LocalDate selected, Map<LocalDate, Long> counts, String extraQuery) {
        this.today = LocalDate.now();
        this.selected = selected;
        String extra = extraQuery == null ? "" : extraQuery;
        LocalDate monday = weekStart(selected);
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            days.add(new Day(d, counts.getOrDefault(d, 0L), d.equals(selected), d.equals(today), d.isAfter(today),
                    "date=" + d + extra));
        }
        quicks.add(new Quick("Hôm kia", "date=" + today.minusDays(2) + extra, selected.equals(today.minusDays(2))));
        quicks.add(new Quick("Hôm qua", "date=" + today.minusDays(1) + extra, selected.equals(today.minusDays(1))));
        quicks.add(new Quick("Hôm nay", "date=" + today + extra, selected.equals(today)));
        prevWeekQuery = "date=" + monday.minusDays(7) + extra;
        LocalDate nextMonday = monday.plusDays(7);
        // Tuần sau toàn ngày chưa tới -> không cho chuyển
        nextWeekQuery = nextMonday.isAfter(today) ? null : "date=" + (nextMonday.plusDays(6).isAfter(today) ? today : nextMonday) + extra;
    }

    /** Đọc ?date=yyyy-MM-dd; sai định dạng/bỏ trống -> hôm nay; ngày tương lai -> hôm nay. */
    public static LocalDate parse(String value) {
        LocalDate today = LocalDate.now();
        if (value == null || value.isBlank()) {
            return today;
        }
        try {
            LocalDate d = LocalDate.parse(value.trim());
            return d.isAfter(today) ? today : d;
        } catch (DateTimeParseException e) {
            return today;
        }
    }

    public static LocalDate weekStart(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    }

    public LocalDate getSelected() { return selected; }
    public String getSelectedIso() { return selected.toString(); }
    public String getTodayIso() { return today.toString(); }
    public boolean isSelectedToday() { return selected.equals(today); }
    public List<Day> getDays() { return days; }
    public List<Quick> getQuicks() { return quicks; }
    public String getPrevWeekQuery() { return prevWeekQuery; }
    public String getNextWeekQuery() { return nextWeekQuery; }

    /** "Thứ Tư, 14/10/2026". */
    public String getSelectedLabel() {
        String[] names = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
        return names[selected.getDayOfWeek().getValue() - 1] + ", "
                + String.format("%02d/%02d/%d", selected.getDayOfMonth(), selected.getMonthValue(), selected.getYear());
    }

    /** Nhãn tương đối: Hôm nay / Hôm qua / Hôm kia / null. */
    public String getRelativeLabel() {
        if (selected.equals(today)) return "Hôm nay";
        if (selected.equals(today.minusDays(1))) return "Hôm qua";
        if (selected.equals(today.minusDays(2))) return "Hôm kia";
        return null;
    }
}

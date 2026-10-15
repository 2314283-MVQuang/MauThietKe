package com.trasua.service;

import com.trasua.entity.WorkShift;
import com.trasua.pattern.strategy.salary.PayrollFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Dữ liệu vẽ biểu đồ thanh "số giờ làm" (theo ngày / theo nhân viên). */
public final class HoursChart {

    /** Một thanh: nhãn, số giờ, % độ dài so với thanh lớn nhất. */
    public static class Bar {
        private final String label;
        private final long minutes;
        private final int percent;

        Bar(String label, long minutes, int percent) {
            this.label = label;
            this.minutes = minutes;
            this.percent = percent;
        }

        public String getLabel() { return label; }
        public long getMinutes() { return minutes; }
        public String getHoursText() { return PayrollFormat.hours(minutes); }
        public int getPercent() { return percent; }
    }

    private HoursChart() {
    }

    /** Tổng giờ theo từng ngày (gộp mọi ca bắt đầu trong ngày đó). */
    public static List<Bar> byDay(List<WorkShift> shifts) {
        Map<java.time.LocalDate, Long> minutes = new TreeMap<>();
        Map<java.time.LocalDate, String> labels = new TreeMap<>();
        for (WorkShift s : shifts) {
            java.time.LocalDate day = s.getCheckIn().toLocalDate();
            minutes.merge(day, s.getMinutes(), Long::sum);
            labels.putIfAbsent(day, s.getDayLabel());
        }
        return bars(labels, minutes);
    }

    /** Tổng giờ theo nhân viên trong tháng. */
    public static List<Bar> byStaff(List<PayrollSummary> summaries) {
        Map<String, Long> minutes = new java.util.LinkedHashMap<>();
        Map<String, String> labels = new java.util.LinkedHashMap<>();
        for (PayrollSummary s : summaries) {
            String key = s.getStaff().getUsername();
            minutes.put(key, s.getTotalMinutes());
            labels.put(key, s.getStaff().getFullName());
        }
        return bars(labels, minutes);
    }

    private static <K> List<Bar> bars(Map<K, String> labels, Map<K, Long> minutes) {
        long max = minutes.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<Bar> result = new ArrayList<>();
        minutes.forEach((key, m) -> result.add(new Bar(labels.get(key), m, max == 0 ? 0 : (int) Math.round(m * 100.0 / max))));
        return result;
    }
}

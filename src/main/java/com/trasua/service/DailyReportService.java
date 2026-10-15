package com.trasua.service;

import com.trasua.entity.DailyClosing;
import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.PaymentMethod;
import com.trasua.entity.PaymentStatus;
import com.trasua.entity.User;
import com.trasua.repository.DailyClosingRepository;
import com.trasua.repository.OrderRepository;
import com.trasua.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tổng kết hoá đơn theo ngày + chốt két:
 * quản lý so tiền mặt đếm được với tiền mặt theo hoá đơn để phát hiện thiếu tiền / quỵt bill.
 */
@Service
public class DailyReportService {

    private final OrderRepository orderRepository;
    private final DailyClosingRepository closingRepository;
    private final UserRepository userRepository;

    public DailyReportService(OrderRepository orderRepository, DailyClosingRepository closingRepository,
                              UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.closingRepository = closingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DailyReport report(LocalDate date) {
        List<Order> orders = orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        Map<OrderStatus, Long> statusCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) {
            statusCounts.put(s, orders.stream().filter(o -> o.getStatus() == s).count());
        }
        List<Order> completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.HOAN_THANH).toList();
        BigDecimal revenue = sum(completed);
        BigDecimal discount = completed.stream().map(Order::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tiền đã thu thực tế = đơn ĐÃ THANH TOÁN và KHÔNG bị huỷ
        List<Order> collected = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.DA_THANH_TOAN && o.getStatus() != OrderStatus.DA_HUY)
                .toList();
        List<DailyReport.PaymentRow> payments = new ArrayList<>();
        for (PaymentMethod m : PaymentMethod.values()) {
            List<Order> byMethod = collected.stream().filter(o -> o.getPaymentMethod() == m).toList();
            payments.add(new DailyReport.PaymentRow(m.getLabel(), byMethod.size(), sum(byMethod)));
        }
        BigDecimal expectedCash = sum(collected.stream().filter(o -> o.getPaymentMethod() == PaymentMethod.TIEN_MAT).toList());
        BigDecimal expectedTransfer = sum(collected.stream().filter(o -> o.getPaymentMethod() != PaymentMethod.TIEN_MAT).toList());

        // Theo người thu tiền
        Map<String, DailyReport.CashierRow> cashiers = new LinkedHashMap<>();
        for (Order o : collected) {
            DailyReport.CashierRow row = cashierRow(cashiers, o.getCashier());
            if (o.getPaymentMethod() == PaymentMethod.TIEN_MAT) {
                row.addCash(o.getTotal());
            } else {
                row.addTransfer(o.getTotal());
            }
        }
        List<Order> cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.DA_HUY).toList();
        for (Order o : cancelled) {
            // Chỉ tính đơn do NHÂN VIÊN / QUẢN LÝ huỷ (khách tự huỷ đơn chờ xác nhận thì bỏ qua)
            boolean byStaff = o.getCancelledBy() != null && userRepository.findByUsername(o.getCancelledBy())
                    .map(u -> u.getRole() != com.trasua.entity.Role.CUSTOMER).orElse(false);
            if (byStaff) {
                cashierRow(cashiers, o.getCancelledBy()).addCancel(o.getTotal());
            }
        }
        List<DailyReport.CashierRow> cashierRows = new ArrayList<>(cashiers.values());
        cashierRows.sort(Comparator.comparing(DailyReport.CashierRow::getTotal).reversed());

        List<Order> paidThenCancelled = cancelled.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.DA_THANH_TOAN).toList();
        List<Order> unpaid = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DA_HUY && o.getPaymentStatus() == PaymentStatus.CHUA_THANH_TOAN)
                .toList();
        List<Order> unfinished = orders.stream().filter(o -> !o.getStatus().isFinished()).toList();

        return new DailyReport(date, orders, statusCounts, revenue, discount, payments, expectedCash, expectedTransfer,
                cashierRows, cancelled, paidThenCancelled, unpaid, unfinished,
                closingRepository.findByBusinessDate(date).orElse(null));
    }

    /** Quản lý nhập số tiền mặt đếm được -> lưu biên bản chốt két (chốt lại sẽ ghi đè). */
    @Transactional
    public DailyClosing close(LocalDate date, BigDecimal countedCash, String note, String adminUsername) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể chốt két cho ngày chưa tới");
        }
        if (countedCash == null || countedCash.signum() < 0) {
            throw new IllegalArgumentException("Vui lòng nhập số tiền mặt đếm được (≥ 0)");
        }
        DailyReport report = report(date);
        DailyClosing closing = closingRepository.findByBusinessDate(date).orElseGet(DailyClosing::new);
        closing.setBusinessDate(date);
        closing.setExpectedCash(report.getExpectedCash());
        closing.setExpectedTransfer(report.getExpectedTransfer());
        closing.setCountedCash(countedCash);
        closing.setNote(note == null || note.isBlank() ? null : note.trim());
        closing.setClosedBy(adminUsername);
        closing.setClosedAt(LocalDateTime.now());
        return closingRepository.save(closing);
    }

    @Transactional
    public void reopen(LocalDate date) {
        closingRepository.findByBusinessDate(date).ifPresent(closingRepository::delete);
    }

    /** Lịch sử chốt két 30 ngày gần nhất. */
    public List<DailyClosing> recentClosings() {
        LocalDate today = LocalDate.now();
        return closingRepository.findByBusinessDateBetweenOrderByBusinessDateDesc(today.minusDays(30), today);
    }

    private DailyReport.CashierRow cashierRow(Map<String, DailyReport.CashierRow> map, String username) {
        String key = username == null ? "(khách tự đặt online)" : username;
        return map.computeIfAbsent(key, k -> new DailyReport.CashierRow(k,
                username == null ? "Chưa rõ người thu" :
                        userRepository.findByUsername(username).map(User::getFullName).orElse(username)));
    }

    private static BigDecimal sum(List<Order> orders) {
        return orders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
